package com.rr.erp.service;

import com.rr.erp.entity.CashTransaction;
import com.rr.erp.entity.EmployeeCashSummary;
import com.rr.erp.entity.SalaryAdvance;
import com.rr.erp.entity.SalaryPayment;
import com.rr.erp.repository.CashAccountRepository;
import com.rr.erp.repository.SalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Salary advances and salary payments made from employee (petty) cash accounts.
 * Every advance / salary payment posts a matching EXPENSE through {@link CashTransactionService},
 * so the cash account balance and these records move together.
 */
@Service
@RequiredArgsConstructor
public class SalaryService {

    private static final String ADVANCE_CATEGORY_CODE = "SALARY_ADVANCE";
    private static final String SALARY_CATEGORY_CODE = "SALARY";

    private static final Set<String> ADVANCE_STATUSES = Set.of(
            SalaryAdvance.STATUS_OPEN,
            SalaryAdvance.STATUS_PARTIAL,
            SalaryAdvance.STATUS_CLOSED,
            SalaryAdvance.STATUS_VOID
    );

    private static final Set<String> PAYMENT_STATUSES = Set.of(
            SalaryPayment.STATUS_PAID,
            SalaryPayment.STATUS_VOID
    );

    private static final DateTimeFormatter CODE_PERIOD = DateTimeFormatter.ofPattern("yyyyMM");

    private final SalaryRepository repository;
    private final CashAccountRepository cashAccountRepository;
    private final CashTransactionService cashTransactionService;


    // =========================================================
    // ADVANCES - READ
    // =========================================================

    public List<SalaryAdvance> getAllAdvances(
            String employeeCode,
            UUID cashAccountId,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {

        String normalizedStatus = normalize(status);
        if (normalizedStatus != null && !ADVANCE_STATUSES.contains(normalizedStatus)) {
            throw badRequest("Invalid status: " + normalizedStatus + ". Allowed values: OPEN, PARTIAL, CLOSED, VOID");
        }

        requireValidRange(fromDate, toDate);

        return repository.getAllAdvances(blankToNull(employeeCode), cashAccountId, normalizedStatus, fromDate, toDate);
    }

    public SalaryAdvance getAdvanceById(UUID advanceId) {

        return repository.getAdvanceById(advanceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Salary advance not found with id: " + advanceId
                ));
    }


    // =========================================================
    // ADVANCES - CREATE
    // =========================================================

    @Transactional
    public SalaryAdvance createAdvance(SalaryAdvance advance) {

        String employeeCode = advance.getEmployeeCode().trim();
        String createdBy = advance.getCreatedBy().trim();
        requireEmployee(employeeCode, "Employee");
        requireEmployee(createdBy, "Created by employee");

        BigDecimal amount = advance.getAmount().setScale(2, RoundingMode.HALF_UP);
        LocalDate advanceDate = advance.getAdvanceDate() == null ? LocalDate.now() : advance.getAdvanceDate();

        repository.lockAdvanceCodeGeneration();
        String advanceCode = nextCode("ADV", repository.getMaxAdvanceSequence(codePrefix("ADV")));

        // Pays out of the cash account: checks the account is active and has enough balance
        CashTransaction expense = postExpense(
                advance.getCashAccountId(),
                amount,
                categoryId(ADVANCE_CATEGORY_CODE),
                "Salary advance " + advanceCode + " to " + employeeCode,
                advanceCode,
                advanceDate,
                createdBy,
                employeeCode,
                CashTransaction.SALARY_ADVANCE
        );

        advance.setAdvanceCode(advanceCode);
        advance.setEmployeeCode(employeeCode);
        advance.setCreatedBy(createdBy);
        advance.setAmount(amount);
        advance.setAdvanceDate(advanceDate);
        advance.setRemarks(blankToNull(advance.getRemarks()));
        advance.setCashTransactionId(expense.getTransactionId());

        return getAdvanceById(repository.createAdvance(advance));
    }


    // =========================================================
    // ADVANCES - VOID
    // =========================================================

    @Transactional
    public SalaryAdvance voidAdvance(UUID advanceId) {

        SalaryAdvance existing = getAdvanceById(advanceId);

        if (SalaryAdvance.STATUS_VOID.equals(existing.getStatus())) {
            throw conflict("Salary advance is already VOID");
        }
        if (existing.getRecoveredAmount().signum() > 0) {
            throw conflict("Cannot void: " + existing.getRecoveredAmount()
                    + " of this advance has already been recovered from salary. Void that salary payment first.");
        }

        if (repository.voidAdvance(advanceId) == 0) {
            throw conflict("Salary advance status changed, please reload and try again");
        }

        voidCashTransaction(existing.getCashTransactionId());

        return getAdvanceById(advanceId);
    }


    // =========================================================
    // PAYMENTS - READ
    // =========================================================

    public List<SalaryPayment> getAllPayments(
            String employeeCode,
            UUID cashAccountId,
            String salaryPeriod,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {

        String normalizedStatus = normalize(status);
        if (normalizedStatus != null && !PAYMENT_STATUSES.contains(normalizedStatus)) {
            throw badRequest("Invalid status: " + normalizedStatus + ". Allowed values: PAID, VOID");
        }

        requireValidRange(fromDate, toDate);

        return repository.getAllPayments(
                blankToNull(employeeCode),
                cashAccountId,
                blankToNull(salaryPeriod),
                normalizedStatus,
                fromDate,
                toDate
        );
    }

    public SalaryPayment getPaymentById(UUID salaryPaymentId) {

        return repository.getPaymentById(salaryPaymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Salary payment not found with id: " + salaryPaymentId
                ));
    }


    // =========================================================
    // PAYMENTS - CREATE
    // =========================================================

    @Transactional
    public SalaryPayment createPayment(SalaryPayment payment) {

        String employeeCode = payment.getEmployeeCode().trim();
        String createdBy = payment.getCreatedBy().trim();
        requireEmployee(employeeCode, "Employee");
        requireEmployee(createdBy, "Created by employee");

        BigDecimal gross = payment.getGrossAmount().setScale(2, RoundingMode.HALF_UP);
        LocalDate paymentDate = payment.getPaymentDate() == null ? LocalDate.now() : payment.getPaymentDate();

        repository.lockPaymentCodeGeneration();

        if (repository.paidPaymentExists(employeeCode, payment.getSalaryPeriod())) {
            throw conflict("Salary for " + payment.getSalaryPeriod() + " has already been paid to employee "
                    + employeeCode + ". Void that payment first to pay again.");
        }

        // Advances still to recover, oldest first (rows locked for the rest of the transaction)
        List<SalaryAdvance> advances = repository.lockRecoverableAdvances(employeeCode);
        BigDecimal outstanding = advances.stream()
                .map(SalaryAdvance::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deduction;
        if (payment.getAdvanceDeducted() == null) {
            deduction = outstanding.min(gross);
        } else {
            deduction = payment.getAdvanceDeducted().setScale(2, RoundingMode.HALF_UP);
            if (deduction.compareTo(gross) > 0) {
                throw badRequest("Advance deduction " + deduction + " cannot exceed the gross salary " + gross);
            }
            if (deduction.compareTo(outstanding) > 0) {
                throw badRequest("Advance deduction " + deduction + " exceeds the outstanding advance "
                        + outstanding + " of employee " + employeeCode);
            }
        }

        BigDecimal net = gross.subtract(deduction);
        String paymentCode = nextCode("SAL", repository.getMaxPaymentSequence(codePrefix("SAL")));

        UUID cashAccountId = null;
        UUID cashTransactionId = null;
        if (net.signum() > 0) {
            if (payment.getCashAccountId() == null) {
                throw badRequest("Cash account is required to pay the net salary of " + net);
            }
            CashTransaction expense = postExpense(
                    payment.getCashAccountId(),
                    net,
                    categoryId(SALARY_CATEGORY_CODE),
                    "Salary " + payment.getSalaryPeriod() + " to " + employeeCode + " (" + paymentCode + ")",
                    paymentCode,
                    paymentDate,
                    createdBy,
                    employeeCode,
                    CashTransaction.SALARY_FULL
            );
            cashAccountId = payment.getCashAccountId();
            cashTransactionId = expense.getTransactionId();
        }

        payment.setPaymentCode(paymentCode);
        payment.setEmployeeCode(employeeCode);
        payment.setCreatedBy(createdBy);
        payment.setGrossAmount(gross);
        payment.setAdvanceDeducted(deduction);
        payment.setNetAmount(net);
        payment.setCashAccountId(cashAccountId);
        payment.setCashTransactionId(cashTransactionId);
        payment.setPaymentDate(paymentDate);
        payment.setRemarks(blankToNull(payment.getRemarks()));

        UUID paymentId = repository.createPayment(payment);

        // Recover from the oldest advance first
        BigDecimal remaining = deduction;
        for (SalaryAdvance advance : advances) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal recovered = advance.getOutstandingAmount().min(remaining);
            repository.createRecovery(paymentId, advance.getAdvanceId(), recovered);
            repository.adjustAdvanceRecovery(advance.getAdvanceId(), recovered);
            remaining = remaining.subtract(recovered);
        }

        return getPaymentById(paymentId);
    }


    /** Pays many employees in one database transaction; any failure rolls every payment back. */
    @Transactional
    public List<SalaryPayment> createBulkPayments(List<SalaryPayment> payments) {

        long distinctEmployees = payments.stream()
                .map(payment -> payment.getEmployeeCode().trim() + "|" + payment.getSalaryPeriod())
                .distinct()
                .count();
        if (distinctEmployees != payments.size()) {
            throw badRequest("An employee appears more than once for the same salary month");
        }

        List<SalaryPayment> created = new java.util.ArrayList<>();
        for (SalaryPayment payment : payments) {
            try {
                created.add(createPayment(payment));
            } catch (ResponseStatusException e) {
                // Say which employee failed so the user can fix that row
                throw new ResponseStatusException(e.getStatusCode(), payment.getEmployeeCode() + ": " + e.getReason());
            }
        }
        return created;
    }


    // =========================================================
    // PAYMENTS - VOID
    // =========================================================

    @Transactional
    public SalaryPayment voidPayment(UUID salaryPaymentId) {

        SalaryPayment existing = getPaymentById(salaryPaymentId);

        if (SalaryPayment.STATUS_VOID.equals(existing.getStatus())) {
            throw conflict("Salary payment is already VOID");
        }

        if (repository.voidPayment(salaryPaymentId) == 0) {
            throw conflict("Salary payment status changed, please reload and try again");
        }

        // Put the recovered amounts back on the advances
        for (SalaryRepository.Recovery recovery : repository.getRecoveries(salaryPaymentId)) {
            repository.adjustAdvanceRecovery(recovery.advanceId(), recovery.amount().negate());
        }
        repository.deleteRecoveries(salaryPaymentId);

        voidCashTransaction(existing.getCashTransactionId());

        return getPaymentById(salaryPaymentId);
    }


    // =========================================================
    // SUMMARY
    // =========================================================

    public List<EmployeeCashSummary> getSummary(String employeeCode, LocalDate fromDate, LocalDate toDate) {

        requireValidRange(fromDate, toDate);

        return repository.getSummary(blankToNull(employeeCode), fromDate, toDate);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    /** Post an EXPENSE from the cash account (balance and active checks happen there). */
    private CashTransaction postExpense(
            UUID cashAccountId,
            BigDecimal amount,
            UUID categoryId,
            String description,
            String referenceNumber,
            LocalDate date,
            String createdBy,
            String employeeCode,
            String salaryKind) {

        CashTransaction expense = new CashTransaction();
        // Tagged as salary so the cash statement and reports see it like any other salary payment
        expense.setPurpose(CashTransaction.PURPOSE_SALARY);
        expense.setEmployeeCode(employeeCode);
        expense.setSalaryKind(salaryKind);
        expense.setTransactionType(CashTransaction.TYPE_EXPENSE);
        expense.setFromAccountId(cashAccountId);
        expense.setAmount(amount);
        expense.setCategoryId(categoryId);
        expense.setDescription(description);
        expense.setReferenceNumber(referenceNumber);
        expense.setTransactionDate(date.atTime(LocalDateTime.now().toLocalTime()));
        expense.setStatus(CashTransaction.STATUS_POSTED);
        expense.setCreatedBy(createdBy);

        return cashTransactionService.createTransaction(expense);
    }

    /** Void the backing cash expense so the money returns to the account (skipped when already voided there). */
    private void voidCashTransaction(UUID transactionId) {

        if (transactionId == null) {
            return;
        }

        CashTransaction transaction = cashTransactionService.getTransactionById(transactionId);
        if (!CashTransaction.STATUS_VOID.equals(transaction.getStatus())) {
            cashTransactionService.voidTransaction(transactionId);
        }
    }

    private UUID categoryId(String categoryCode) {

        return repository.getCategoryIdByCode(categoryCode)
                .orElseThrow(() -> conflict("Expense category " + categoryCode
                        + " is missing. Run salary_advance_payment_v1.sql to create it."));
    }

    private void requireEmployee(String employeeCode, String label) {

        if (!cashAccountRepository.employeeExists(employeeCode)) {
            throw badRequest(label + " not found with employee code: " + employeeCode);
        }
    }

    private static void requireValidRange(LocalDate fromDate, LocalDate toDate) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw badRequest("fromDate must be on or before toDate");
        }
    }

    private static String codePrefix(String type) {

        return type + "-" + LocalDateTime.now().format(CODE_PERIOD) + "-";
    }

    private static String nextCode(String type, int maxSequence) {

        return String.format("%s%04d", codePrefix(type), maxSequence + 1);
    }

    private static String normalize(String value) {

        String trimmed = blankToNull(value);

        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private static String blankToNull(String value) {

        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ResponseStatusException badRequest(String message) {

        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException conflict(String message) {

        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
