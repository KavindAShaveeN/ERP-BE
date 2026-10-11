package com.rr.erp.repository;

import com.rr.erp.entity.CashAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CashAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_SQL = """
            SELECT
                ca.cash_account_id,
                ca.employee_code,
                e.full_name AS employee_name,
                ca.account_name,
                ca.account_type,
                ca.is_active,
                ca.created_at
            FROM cash_account ca
            LEFT JOIN employee e
                ON ca.employee_code = e.employee_code
            """;

    private static final RowMapper<CashAccount> ROW_MAPPER = (rs, rowNum) -> {

        CashAccount account = new CashAccount();

        account.setCashAccountId(
                rs.getObject("cash_account_id", UUID.class)
        );
        account.setEmployeeCode(
                rs.getString("employee_code")
        );
        account.setEmployeeName(
                rs.getString("employee_name")
        );
        account.setAccountName(
                rs.getString("account_name")
        );
        account.setAccountType(
                rs.getString("account_type")
        );
        account.setIsActive(
                rs.getBoolean("is_active")
        );
        account.setCreatedAt(
                rs.getObject("created_at", LocalDateTime.class)
        );

        return account;
    };


    // =========================================================
    // GET ALL (optional filters)
    // =========================================================

    public List<CashAccount> getAllCashAccounts(
            String accountType,
            String employeeCode,
            Boolean isActive) {

        StringBuilder sql = new StringBuilder(SELECT_SQL)
                .append(" WHERE 1 = 1");

        List<Object> params = new ArrayList<>();

        if (accountType != null) {
            sql.append(" AND ca.account_type = ?");
            params.add(accountType);
        }

        if (employeeCode != null) {
            sql.append(" AND ca.employee_code = ?");
            params.add(employeeCode);
        }

        if (isActive != null) {
            sql.append(" AND ca.is_active = ?");
            params.add(isActive);
        }

        sql.append(" ORDER BY ca.account_type, ca.account_name");

        return jdbcTemplate.query(
                sql.toString(),
                ROW_MAPPER,
                params.toArray()
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<CashAccount> getCashAccountById(UUID cashAccountId) {

        String sql = SELECT_SQL + " WHERE ca.cash_account_id = ?";

        return jdbcTemplate.query(
                sql,
                ROW_MAPPER,
                cashAccountId
        ).stream().findFirst();
    }


    // =========================================================
    // CREATE
    // =========================================================

    public UUID createCashAccount(CashAccount account) {

        String sql = """
                INSERT INTO cash_account (
                    employee_code,
                    account_name,
                    account_type,
                    is_active
                )
                VALUES (?, ?, ?, ?)
                RETURNING cash_account_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                account.getEmployeeCode(),
                account.getAccountName(),
                account.getAccountType(),
                account.getIsActive()
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public int updateCashAccount(
            UUID cashAccountId,
            CashAccount account) {

        String sql = """
                UPDATE cash_account
                SET
                    employee_code = ?,
                    account_name = ?,
                    account_type = ?,
                    is_active = ?
                WHERE cash_account_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                account.getEmployeeCode(),
                account.getAccountName(),
                account.getAccountType(),
                account.getIsActive(),
                cashAccountId
        );
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE
    // =========================================================

    public int updateActiveStatus(
            UUID cashAccountId,
            boolean isActive) {

        String sql = """
                UPDATE cash_account
                SET is_active = ?
                WHERE cash_account_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                isActive,
                cashAccountId
        );
    }


    // =========================================================
    // EMPLOYEE EXISTS
    // =========================================================

    public boolean employeeExists(String employeeCode) {

        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM employee WHERE employee_code = ?)",
                Boolean.class,
                employeeCode
        );

        return Boolean.TRUE.equals(exists);
    }
}
