package com.rr.erp.repository;

import com.rr.erp.entity.CashTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CashTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_SQL = """
            SELECT
                ct.transaction_id,
                ct.transaction_code,
                ct.transaction_type,
                ct.from_account_id,
                fa.account_name AS from_account_name,
                ct.to_account_id,
                ta.account_name AS to_account_name,
                ct.amount,
                ct.category_id,
                cec.category_code,
                cec.category_name,
                ct.description,
                ct.reference_number,
                ct.transaction_date,
                ct.status,
                ct.created_by,
                e.full_name AS created_by_name,
                ct.created_at
            FROM cash_transaction ct
            LEFT JOIN cash_account fa
                ON ct.from_account_id = fa.cash_account_id
            LEFT JOIN cash_account ta
                ON ct.to_account_id = ta.cash_account_id
            LEFT JOIN cash_expense_category cec
                ON ct.category_id = cec.category_id
            LEFT JOIN employee e
                ON ct.created_by = e.employee_code
            """;

    private static final RowMapper<CashTransaction> ROW_MAPPER = (rs, rowNum) -> {

        CashTransaction transaction = new CashTransaction();

        transaction.setTransactionId(
                rs.getObject("transaction_id", UUID.class)
        );
        transaction.setTransactionCode(
                rs.getString("transaction_code")
        );
        transaction.setTransactionType(
                rs.getString("transaction_type")
        );
        transaction.setFromAccountId(
                rs.getObject("from_account_id", UUID.class)
        );
        transaction.setFromAccountName(
                rs.getString("from_account_name")
        );
        transaction.setToAccountId(
                rs.getObject("to_account_id", UUID.class)
        );
        transaction.setToAccountName(
                rs.getString("to_account_name")
        );
        transaction.setAmount(
                rs.getBigDecimal("amount")
        );
        transaction.setCategoryId(
                rs.getObject("category_id", UUID.class)
        );
        transaction.setCategoryCode(
                rs.getString("category_code")
        );
        transaction.setCategoryName(
                rs.getString("category_name")
        );
        transaction.setDescription(
                rs.getString("description")
        );
        transaction.setReferenceNumber(
                rs.getString("reference_number")
        );
        transaction.setTransactionDate(
                rs.getObject("transaction_date", LocalDateTime.class)
        );
        transaction.setStatus(
                rs.getString("status")
        );
        transaction.setCreatedBy(
                rs.getString("created_by")
        );
        transaction.setCreatedByName(
                rs.getString("created_by_name")
        );
        transaction.setCreatedAt(
                rs.getObject("created_at", LocalDateTime.class)
        );

        return transaction;
    };


    // =========================================================
    // GET ALL (optional filters)
    // =========================================================

    public List<CashTransaction> getAllTransactions(
            String transactionType,
            String status,
            UUID accountId,
            UUID categoryId,
            String createdBy,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        StringBuilder sql = new StringBuilder(SELECT_SQL)
                .append(" WHERE 1 = 1");

        List<Object> params = new ArrayList<>();

        if (transactionType != null) {
            sql.append(" AND ct.transaction_type = ?");
            params.add(transactionType);
        }

        if (status != null) {
            sql.append(" AND ct.status = ?");
            params.add(status);
        }

        if (accountId != null) {
            sql.append(" AND (ct.from_account_id = ? OR ct.to_account_id = ?)");
            params.add(accountId);
            params.add(accountId);
        }

        if (categoryId != null) {
            sql.append(" AND ct.category_id = ?");
            params.add(categoryId);
        }

        if (createdBy != null) {
            sql.append(" AND ct.created_by = ?");
            params.add(createdBy);
        }

        if (fromDate != null) {
            sql.append(" AND ct.transaction_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append(" AND ct.transaction_date < ?");
            params.add(toDate);
        }

        sql.append(" ORDER BY ct.transaction_date DESC, ct.created_at DESC");

        return jdbcTemplate.query(
                sql.toString(),
                ROW_MAPPER,
                params.toArray()
        );
    }


    // =========================================================
    // GET BY ID / CODE
    // =========================================================

    public Optional<CashTransaction> getTransactionById(UUID transactionId) {

        String sql = SELECT_SQL + " WHERE ct.transaction_id = ?";

        return jdbcTemplate.query(
                sql,
                ROW_MAPPER,
                transactionId
        ).stream().findFirst();
    }

    public Optional<CashTransaction> getTransactionByCode(String transactionCode) {

        String sql = SELECT_SQL + " WHERE ct.transaction_code = ?";

        return jdbcTemplate.query(
                sql,
                ROW_MAPPER,
                transactionCode
        ).stream().findFirst();
    }


    // =========================================================
    // CREATE
    // =========================================================

    public UUID createTransaction(CashTransaction transaction) {

        String sql = """
                INSERT INTO cash_transaction (
                    transaction_code,
                    transaction_type,
                    from_account_id,
                    to_account_id,
                    amount,
                    category_id,
                    description,
                    reference_number,
                    transaction_date,
                    status,
                    created_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, COALESCE(?, NOW()), ?, ?)
                RETURNING transaction_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                transaction.getTransactionCode(),
                transaction.getTransactionType(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getCategoryId(),
                transaction.getDescription(),
                transaction.getReferenceNumber(),
                transaction.getTransactionDate(),
                transaction.getStatus(),
                transaction.getCreatedBy()
        );
    }


    // =========================================================
    // UPDATE (DRAFT only)
    // =========================================================

    public int updateDraftTransaction(
            UUID transactionId,
            CashTransaction transaction) {

        String sql = """
                UPDATE cash_transaction
                SET
                    transaction_type = ?,
                    from_account_id = ?,
                    to_account_id = ?,
                    amount = ?,
                    category_id = ?,
                    description = ?,
                    reference_number = ?,
                    transaction_date = COALESCE(?, transaction_date)
                WHERE transaction_id = ?
                  AND status = 'DRAFT'
                """;

        return jdbcTemplate.update(
                sql,
                transaction.getTransactionType(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getCategoryId(),
                transaction.getDescription(),
                transaction.getReferenceNumber(),
                transaction.getTransactionDate(),
                transactionId
        );
    }


    // =========================================================
    // STATUS CHANGE
    // =========================================================

    public int updateStatus(
            UUID transactionId,
            String fromStatus,
            String toStatus) {

        String sql = """
                UPDATE cash_transaction
                SET status = ?
                WHERE transaction_id = ?
                  AND status = ?
                """;

        return jdbcTemplate.update(
                sql,
                toStatus,
                transactionId,
                fromStatus
        );
    }


    // =========================================================
    // TRANSACTION CODES
    // =========================================================

    /**
     * Serialize code generation for the rest of the DB transaction so two
     * concurrent creates cannot pick the same next sequence number.
     */
    public void lockTransactionCodeGeneration() {

        jdbcTemplate.queryForList(
                "SELECT pg_advisory_xact_lock(hashtext('cash_transaction_code'))"
        );
    }

    public List<String> getTransactionCodesWithPrefix(String prefix) {

        return jdbcTemplate.queryForList(
                "SELECT transaction_code FROM cash_transaction WHERE transaction_code LIKE ?",
                String.class,
                prefix + "%"
        );
    }

    public boolean transactionCodeExists(String transactionCode) {

        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM cash_transaction WHERE transaction_code = ?)",
                Boolean.class,
                transactionCode
        );

        return Boolean.TRUE.equals(exists);
    }


    // =========================================================
    // BALANCE
    // =========================================================

    /** Sum of POSTED money flowing into the account (receipts and incoming transfers). */
    public BigDecimal getTotalIn(UUID cashAccountId) {

        return jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(amount), 0)
                FROM cash_transaction
                WHERE to_account_id = ?
                  AND status = 'POSTED'
                """,
                BigDecimal.class,
                cashAccountId
        );
    }

    /** Sum of POSTED money flowing out of the account (expenses and outgoing transfers). */
    public BigDecimal getTotalOut(UUID cashAccountId) {

        return jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(amount), 0)
                FROM cash_transaction
                WHERE from_account_id = ?
                  AND status = 'POSTED'
                """,
                BigDecimal.class,
                cashAccountId
        );
    }

    /**
     * Lock the cash account row for the rest of the DB transaction so that
     * concurrent postings against the same account cannot overdraw it.
     * Returns the account's is_active flag, or empty if it does not exist.
     */
    public Optional<Boolean> lockAccount(UUID cashAccountId) {

        return jdbcTemplate.queryForList(
                "SELECT is_active FROM cash_account WHERE cash_account_id = ? FOR UPDATE",
                Boolean.class,
                cashAccountId
        ).stream().findFirst();
    }


    // =========================================================
    // REFERENCE CHECKS
    // =========================================================

    /** Returns the category's is_active flag, or empty if it does not exist. */
    public Optional<Boolean> getCategoryActive(UUID categoryId) {

        return jdbcTemplate.queryForList(
                "SELECT is_active FROM cash_expense_category WHERE category_id = ?",
                Boolean.class,
                categoryId
        ).stream().findFirst();
    }

    public boolean employeeExists(String employeeCode) {

        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM employee WHERE employee_code = ?)",
                Boolean.class,
                employeeCode
        );

        return Boolean.TRUE.equals(exists);
    }
}
