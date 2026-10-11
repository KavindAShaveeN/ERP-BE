package com.rr.erp.service;

import com.rr.erp.entity.CashAccount;
import com.rr.erp.repository.CashAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashAccountService {

    private static final Set<String> ACCOUNT_TYPES = Set.of(
            CashAccount.TYPE_MAIN_CASH,
            CashAccount.TYPE_EMPLOYEE
    );

    private final CashAccountRepository repository;


    // =========================================================
    // GET ALL
    // =========================================================

    public List<CashAccount> getAllCashAccounts(
            String accountType,
            String employeeCode,
            Boolean isActive) {

        String type = normalizeType(accountType);

        if (type != null && !ACCOUNT_TYPES.contains(type)) {
            throw invalidType(type);
        }

        return repository.getAllCashAccounts(
                type,
                blankToNull(employeeCode),
                isActive
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public CashAccount getCashAccountById(UUID cashAccountId) {

        return repository.getCashAccountById(cashAccountId)
                .orElseThrow(() -> notFound(cashAccountId));
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public CashAccount createCashAccount(CashAccount account) {

        validateAndNormalize(account);

        if (account.getIsActive() == null) {
            account.setIsActive(true);
        }

        if (account.getAccountBalance() == null) {
            account.setAccountBalance(BigDecimal.ZERO);
        }

        UUID id = repository.createCashAccount(account);

        return getCashAccountById(id);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public CashAccount updateCashAccount(
            UUID cashAccountId,
            CashAccount account) {

        CashAccount existing = getCashAccountById(cashAccountId);

        validateAndNormalize(account);

        if (account.getIsActive() == null) {
            account.setIsActive(existing.getIsActive());
        }

        if (account.getAccountBalance() == null) {
            account.setAccountBalance(existing.getAccountBalance());
        }

        repository.updateCashAccount(cashAccountId, account);

        return getCashAccountById(cashAccountId);
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE
    // =========================================================

    @Transactional
    public CashAccount updateActiveStatus(
            UUID cashAccountId,
            boolean isActive) {

        int updatedRows =
                repository.updateActiveStatus(cashAccountId, isActive);

        if (updatedRows == 0) {
            throw notFound(cashAccountId);
        }

        return getCashAccountById(cashAccountId);
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateAndNormalize(CashAccount account) {

        String type = normalizeType(account.getAccountType());

        if (type == null || !ACCOUNT_TYPES.contains(type)) {
            throw invalidType(type);
        }

        account.setAccountType(type);
        account.setAccountName(account.getAccountName().trim());

        if (CashAccount.TYPE_MAIN_CASH.equals(type)) {
            // Main cash accounts are not owned by an employee
            account.setEmployeeCode(null);
            return;
        }

        String employeeCode = blankToNull(account.getEmployeeCode());

        if (employeeCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee code is required for EMPLOYEE cash accounts"
            );
        }

        if (!repository.employeeExists(employeeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee not found with employee code: " + employeeCode
            );
        }

        account.setEmployeeCode(employeeCode);
    }

    private static String normalizeType(String accountType) {

        String value = blankToNull(accountType);

        return value == null ? null : value.toUpperCase();
    }

    private static String blankToNull(String value) {

        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ResponseStatusException invalidType(String type) {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid account type: " + type
                        + ". Allowed values: MAIN_CASH, EMPLOYEE"
        );
    }

    private static ResponseStatusException notFound(UUID cashAccountId) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cash account not found with id: " + cashAccountId
        );
    }
}
