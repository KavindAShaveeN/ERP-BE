package com.rr.erp.service;

import com.rr.erp.entity.CashAccount;
import com.rr.erp.entity.CashAccountBalance;
import com.rr.erp.entity.CashTransaction;
import com.rr.erp.repository.CashAccountRepository;
import com.rr.erp.repository.CashTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CashTransactionService {

    private static final Set<String> TRANSACTION_TYPES = Set.of(
            CashTransaction.TYPE_TRANSFER,
            CashTransaction.TYPE_EXPENSE,
            CashTransaction.TYPE_RECEIPT
    );

    private static final Set<String> STATUSES = Set.of(
            CashTransaction.STATUS_DRAFT,
            CashTransaction.STATUS_POSTED,
            CashTransaction.STATUS_VOID
    );

    private static final Map<String, String> CODE_PREFIXES = Map.of(
            CashTransaction.TYPE_TRANSFER, "TRF",
            CashTransaction.TYPE_EXPENSE, "EXP",
            CashTransaction.TYPE_RECEIPT, "RCP"
    );

    private static final DateTimeFormatter CODE_PERIOD = DateTimeFormatter.ofPattern("yyyyMM");

    private static final Pattern CODE_SEQUENCE_PATTERN = Pattern.compile("^.+-(\\d+)$");

    private final CashTransactionRepository repository;
    private final CashAccountRepository cashAccountRepository;


    // =========================================================
    // GET ALL
    // =========================================================

    public List<CashTransaction> getAllTransactions(
            String transactionType,
            String status,
            UUID accountId,
            UUID categoryId,
            String createdBy,
            LocalDate fromDate,
            LocalDate toDate) {

        String type = normalize(transactionType);
        if (type != null && !TRANSACTION_TYPES.contains(type)) {
            throw invalidType(type);
        }

        String normalizedStatus = normalize(status);
        if (normalizedStatus != null && !STATUSES.contains(normalizedStatus)) {
            throw invalidStatus(normalizedStatus);
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw badRequest("fromDate must be on or before toDate");
        }

        return repository.getAllTransactions(
                type,
                normalizedStatus,
                accountId,
                categoryId,
                blankToNull(createdBy),
                fromDate == null ? null : fromDate.atStartOfDay(),
                // toDate is inclusive: include the whole day
                toDate == null ? null : toDate.plusDays(1).atStartOfDay()
        );
    }


    // =========================================================
    // GET BY ID / CODE
    // =========================================================

    public CashTransaction getTransactionById(UUID transactionId) {

        return repository.getTransactionById(transactionId)
                .orElseThrow(() -> notFound(transactionId));
    }

    public CashTransaction getTransactionByCode(String transactionCode) {

        String code = transactionCode.trim().toUpperCase();

        return repository.getTransactionByCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cash transaction not found with code: " + code
                ));
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public CashTransaction createTransaction(CashTransaction transaction) {

        validateAndNormalize(transaction);

        String status = normalize(transaction.getStatus());
        if (status == null) {
            status = CashTransaction.STATUS_POSTED;
        }
        if (!CashTransaction.STATUS_DRAFT.equals(status)
                && !CashTransaction.STATUS_POSTED.equals(status)) {
            throw badRequest("A new cash transaction can only be created as DRAFT or POSTED");
        }
        transaction.setStatus(status);

        String createdBy = transaction.getCreatedBy().trim();
        if (!repository.employeeExists(createdBy)) {
            throw badRequest("Employee not found with employee code: " + createdBy);
        }
        transaction.setCreatedBy(createdBy);

        if (CashTransaction.STATUS_POSTED.equals(status)) {
            lockAndCheckFunds(transaction);
        }

        repository.lockTransactionCodeGeneration();
        assignTransactionCode(transaction);

        UUID id = repository.createTransaction(transaction);

        if (CashTransaction.STATUS_POSTED.equals(status)) {
            applyToBalances(transaction, false);
        }

        return getTransactionById(id);
    }


    // =========================================================
    // UPDATE (DRAFT only)
    // =========================================================

    @Transactional
    public CashTransaction updateTransaction(
            UUID transactionId,
            CashTransaction transaction) {

        CashTransaction existing = getTransactionById(transactionId);
        requireStatus(existing, CashTransaction.STATUS_DRAFT, "Only DRAFT transactions can be edited");

        validateAndNormalize(transaction);

        int updatedRows = repository.updateDraftTransaction(transactionId, transaction);
        if (updatedRows == 0) {
            // Status changed between the read and the update
            throw conflict("Only DRAFT transactions can be edited");
        }

        return getTransactionById(transactionId);
    }


    // =========================================================
    // POST (DRAFT -> POSTED)
    // =========================================================

    @Transactional
    public CashTransaction postTransaction(UUID transactionId) {

        CashTransaction existing = getTransactionById(transactionId);
        requireStatus(existing, CashTransaction.STATUS_DRAFT, "Only DRAFT transactions can be posted");

        // Accounts or category may have been deactivated since the draft was saved
        validateReferences(existing);
        lockAndCheckFunds(existing);

        changeStatus(
                transactionId,
                CashTransaction.STATUS_DRAFT,
                CashTransaction.STATUS_POSTED,
                "Only DRAFT transactions can be posted"
        );

        applyToBalances(existing, false);

        return getTransactionById(transactionId);
    }


    // =========================================================
    // VOID (DRAFT / POSTED -> VOID)
    // =========================================================

    @Transactional
    public CashTransaction voidTransaction(UUID transactionId) {

        CashTransaction existing = getTransactionById(transactionId);
        String currentStatus = existing.getStatus();

        if (CashTransaction.STATUS_VOID.equals(currentStatus)) {
            throw conflict("Cash transaction is already VOID");
        }

        if (CashTransaction.STATUS_POSTED.equals(currentStatus)
                && existing.getToAccountId() != null) {
            // Reversing money that came into an account must not leave it negative
            lockAccounts(existing.getFromAccountId(), existing.getToAccountId());

            BigDecimal balance = getBalanceAmount(existing.getToAccountId());
            if (balance.compareTo(existing.getAmount()) < 0) {
                throw conflict("Cannot void: account " + existing.getToAccountName()
                        + " has a balance of " + balance
                        + ", which is less than the transaction amount " + existing.getAmount());
            }
        }

        changeStatus(
                transactionId,
                currentStatus,
                CashTransaction.STATUS_VOID,
                "Cash transaction status changed, please reload and try again"
        );

        if (CashTransaction.STATUS_POSTED.equals(currentStatus)) {
            applyToBalances(existing, true);
        }

        return getTransactionById(transactionId);
    }


    // =========================================================
    // ACCOUNT BALANCE
    // =========================================================

    public CashAccountBalance getAccountBalance(UUID cashAccountId) {

        CashAccount account = cashAccountRepository.getCashAccountById(cashAccountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cash account not found with id: " + cashAccountId
                ));

        BigDecimal totalIn = repository.getTotalIn(cashAccountId);
        BigDecimal totalOut = repository.getTotalOut(cashAccountId);

        CashAccountBalance balance = new CashAccountBalance();
        balance.setCashAccountId(cashAccountId);
        balance.setAccountName(account.getAccountName());
        balance.setTotalIn(totalIn);
        balance.setTotalOut(totalOut);
        balance.setBalance(balanceOf(account));

        return balance;
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateAndNormalize(CashTransaction transaction) {

        String type = normalize(transaction.getTransactionType());
        if (type == null || !TRANSACTION_TYPES.contains(type)) {
            throw invalidType(type);
        }
        transaction.setTransactionType(type);

        switch (type) {
            case CashTransaction.TYPE_TRANSFER -> {
                if (transaction.getFromAccountId() == null || transaction.getToAccountId() == null) {
                    throw badRequest("Both from account and to account are required for TRANSFER");
                }
                if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {
                    throw badRequest("From account and to account must be different for TRANSFER");
                }
                validatePurpose(transaction);
                if (!CashTransaction.PURPOSE_INTERNAL.equals(transaction.getPurpose())) {
                    throw badRequest("A transfer between accounts must have the type INTERNAL; "
                            + "money paid to someone else is recorded without a to account");
                }
            }
            case CashTransaction.TYPE_EXPENSE -> {
                if (transaction.getFromAccountId() == null) {
                    throw badRequest("From account is required for EXPENSE");
                }
                // Money paid to someone outside the accounts (salary, purchasing, subcontractor, ...) has no
                // destination account: it is an expense from the paying account, filed under the category
                // named after its purpose.
                if (normalize(transaction.getPurpose()) != null) {
                    validatePurpose(transaction);
                    if (CashTransaction.PURPOSE_INTERNAL.equals(transaction.getPurpose())) {
                        throw badRequest("INTERNAL is for transfers between accounts; use TRANSFER");
                    }
                    if (transaction.getCategoryId() == null) {
                        String categoryCode = transaction.getPurpose();
                        transaction.setCategoryId(repository.getCategoryIdByCode(categoryCode)
                                .orElseThrow(() -> badRequest("Expense category " + categoryCode
                                        + " is missing. Run cash_transfer_purpose_v1.sql to create it.")));
                    }
                }
                if (transaction.getCategoryId() == null) {
                    throw badRequest("Expense category is required for EXPENSE");
                }
                transaction.setToAccountId(null);
            }
            case CashTransaction.TYPE_RECEIPT -> {
                if (transaction.getToAccountId() == null) {
                    throw badRequest("To account is required for RECEIPT");
                }
                transaction.setFromAccountId(null);
            }
            default -> throw invalidType(type);
        }

        // Purpose, employee and salary kind only apply to transfers and salary paid to an employee
        boolean isPurposeExpense = CashTransaction.TYPE_EXPENSE.equals(type) && transaction.getPurpose() != null;
        if (!CashTransaction.TYPE_TRANSFER.equals(type) && !isPurposeExpense) {
            transaction.setPurpose(null);
            transaction.setEmployeeCode(null);
            transaction.setSalaryKind(null);
        }

        // Expense categories only apply to expenses
        if (!CashTransaction.TYPE_EXPENSE.equals(type)) {
            transaction.setCategoryId(null);
        }

        transaction.setDescription(blankToNull(transaction.getDescription()));
        transaction.setReferenceNumber(blankToNull(transaction.getReferenceNumber()));

        validateReferences(transaction);
    }

    private static final Set<String> PURPOSES = Set.of(
            "INTERNAL", "PURCHASING", "SALARY", "SUBCONTRACTOR", "TRANSPORT", "UTILITIES", "OTHER"
    );

    /** A transfer must say why: salary needs an employee and ADVANCE / FULL_SALARY, purchasing needs a remark. */
    private void validatePurpose(CashTransaction transaction) {

        String purpose = normalize(transaction.getPurpose());
        if (purpose == null || !PURPOSES.contains(purpose)) {
            throw badRequest("Transfer purpose is required. Allowed values: INTERNAL, PURCHASING, SALARY, SUBCONTRACTOR, TRANSPORT, UTILITIES, OTHER");
        }
        transaction.setPurpose(purpose);

        if (CashTransaction.PURPOSE_SALARY.equals(purpose)) {
            String employeeCode = blankToNull(transaction.getEmployeeCode());
            if (employeeCode == null) {
                throw badRequest("Employee is required for a SALARY transfer");
            }
            if (!repository.employeeExists(employeeCode)) {
                throw badRequest("Employee not found with employee code: " + employeeCode);
            }
            String kind = normalize(transaction.getSalaryKind());
            if (!CashTransaction.SALARY_ADVANCE.equals(kind) && !CashTransaction.SALARY_FULL.equals(kind)) {
                throw badRequest("Salary kind must be ADVANCE or FULL_SALARY");
            }
            transaction.setEmployeeCode(employeeCode);
            transaction.setSalaryKind(kind);
        } else {
            transaction.setEmployeeCode(null);
            transaction.setSalaryKind(null);
        }

        if (CashTransaction.PURPOSE_PURCHASING.equals(purpose) && blankToNull(transaction.getDescription()) == null) {
            throw badRequest("A remark explaining the purchase is required for a PURCHASING transfer");
        }
    }

    private void validateReferences(CashTransaction transaction) {

        validateAccount(transaction.getFromAccountId(), "From");
        validateAccount(transaction.getToAccountId(), "To");

        if (transaction.getCategoryId() != null) {
            Boolean active = repository.getCategoryActive(transaction.getCategoryId())
                    .orElseThrow(() -> badRequest(
                            "Expense category not found with id: " + transaction.getCategoryId()));
            if (!active) {
                throw badRequest("Expense category is inactive: " + transaction.getCategoryId());
            }
        }
    }

    private void validateAccount(UUID accountId, String label) {

        if (accountId == null) {
            return;
        }

        Boolean active = cashAccountRepository.getCashAccountById(accountId)
                .orElseThrow(() -> badRequest(label + " cash account not found with id: " + accountId))
                .getIsActive();

        if (!Boolean.TRUE.equals(active)) {
            throw badRequest(label + " cash account is inactive: " + accountId);
        }
    }

    /** Lock the affected accounts and make sure the paying account can cover the amount. */
    private void lockAndCheckFunds(CashTransaction transaction) {

        lockAccounts(transaction.getFromAccountId(), transaction.getToAccountId());

        UUID fromAccountId = transaction.getFromAccountId();
        if (fromAccountId == null) {
            return;
        }

        // The main cash account is the company's own money: it can pay out any amount
        boolean isMainCash = cashAccountRepository.getCashAccountById(fromAccountId)
                .map(account -> CashAccount.TYPE_MAIN_CASH.equals(account.getAccountType()))
                .orElse(false);
        if (isMainCash) {
            return;
        }

        BigDecimal balance = getBalanceAmount(fromAccountId);
        if (balance.compareTo(transaction.getAmount()) < 0) {
            throw conflict("Insufficient balance in from account. Available: " + balance
                    + ", required: " + transaction.getAmount());
        }
    }

    /** Lock in a fixed order so two transfers between the same accounts cannot deadlock. */
    private void lockAccounts(UUID first, UUID second) {

        Stream.of(first, second)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(accountId -> repository.lockAccount(accountId)
                        .orElseThrow(() -> badRequest("Cash account not found with id: " + accountId)));
    }

    /** Current balance as stored on cash_account (callers lock the row first). */
    private BigDecimal getBalanceAmount(UUID cashAccountId) {

        return cashAccountRepository.getCashAccountById(cashAccountId)
                .map(CashTransactionService::balanceOf)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal balanceOf(CashAccount account) {

        return account.getAccountBalance() == null
                ? BigDecimal.ZERO
                : account.getAccountBalance();
    }

    /** Move the amount out of the from account and into the to account (or back, when reversing). */
    private void applyToBalances(CashTransaction transaction, boolean reverse) {

        BigDecimal amount = reverse
                ? transaction.getAmount().negate()
                : transaction.getAmount();

        if (transaction.getFromAccountId() != null) {
            cashAccountRepository.adjustBalance(transaction.getFromAccountId(), amount.negate());
        }
        if (transaction.getToAccountId() != null) {
            cashAccountRepository.adjustBalance(transaction.getToAccountId(), amount);
        }
    }

    private void changeStatus(
            UUID transactionId,
            String fromStatus,
            String toStatus,
            String conflictMessage) {

        int updatedRows = repository.updateStatus(transactionId, fromStatus, toStatus);

        if (updatedRows == 0) {
            throw conflict(conflictMessage);
        }
    }

    private static void requireStatus(
            CashTransaction transaction,
            String expectedStatus,
            String message) {

        if (!expectedStatus.equals(transaction.getStatus())) {
            throw conflict(message + ". Current status: " + transaction.getStatus());
        }
    }


    // =========================================================
    // TRANSACTION CODE
    // =========================================================

    /** Use the supplied code if unique, otherwise generate e.g. EXP-202610-0001. */
    private void assignTransactionCode(CashTransaction transaction) {

        String code = blankToNull(transaction.getTransactionCode());

        if (code != null) {
            code = code.toUpperCase();
            if (repository.transactionCodeExists(code)) {
                throw conflict("Transaction code already exists: " + code);
            }
            transaction.setTransactionCode(code);
            return;
        }

        String prefix = CODE_PREFIXES.get(transaction.getTransactionType())
                + "-" + LocalDateTime.now().format(CODE_PERIOD) + "-";

        int maxSequence = 0;
        for (String existing : repository.getTransactionCodesWithPrefix(prefix)) {
            Matcher matcher = CODE_SEQUENCE_PATTERN.matcher(existing);
            if (matcher.matches()) {
                maxSequence = Math.max(maxSequence, Integer.parseInt(matcher.group(1)));
            }
        }

        transaction.setTransactionCode(String.format("%s%04d", prefix, maxSequence + 1));
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private static String normalize(String value) {

        String trimmed = blankToNull(value);

        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private static String blankToNull(String value) {

        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ResponseStatusException invalidType(String type) {

        return badRequest("Invalid transaction type: " + type
                + ". Allowed values: TRANSFER, EXPENSE, RECEIPT");
    }

    private static ResponseStatusException invalidStatus(String status) {

        return badRequest("Invalid status: " + status
                + ". Allowed values: DRAFT, POSTED, VOID");
    }

    private static ResponseStatusException badRequest(String message) {

        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException conflict(String message) {

        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static ResponseStatusException notFound(UUID transactionId) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cash transaction not found with id: " + transactionId
        );
    }
}
