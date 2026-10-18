package com.rr.erp.repository;

import com.rr.erp.entity.EmployeeCashSummary;
import com.rr.erp.entity.SalaryAdvance;
import com.rr.erp.entity.SalaryPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SalaryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    /** One recovery of an advance made by a salary payment. */
    public record Recovery(UUID advanceId, BigDecimal amount) {
    }

    // =========================================================
    // ADVANCE - SELECT / MAPPING
    // =========================================================

    private static final String ADVANCE_SELECT_SQL = """
            SELECT
                a.advance_id,
                a.advance_code,
                a.employee_code,
                emp.full_name AS employee_name,
                a.cash_account_id,
                ca.account_name AS cash_account_name,
                a.cash_transaction_id,
                ct.transaction_code AS cash_transaction_code,
                a.amount,
                a.recovered_amount,
                a.advance_date,
                a.status,
                a.remarks,
                a.created_by,
                a.created_at
            FROM salary_advance a
            LEFT JOIN employee emp
                ON a.employee_code = emp.employee_code
            LEFT JOIN cash_account ca
                ON a.cash_account_id = ca.cash_account_id
            LEFT JOIN cash_transaction ct
                ON a.cash_transaction_id = ct.transaction_id
            """;

    private static final RowMapper<SalaryAdvance> ADVANCE_MAPPER = (rs, rowNum) -> {

        SalaryAdvance advance = new SalaryAdvance();

        advance.setAdvanceId(rs.getObject("advance_id", UUID.class));
        advance.setAdvanceCode(rs.getString("advance_code"));
        advance.setEmployeeCode(rs.getString("employee_code"));
        advance.setEmployeeName(rs.getString("employee_name"));
        advance.setCashAccountId(rs.getObject("cash_account_id", UUID.class));
        advance.setCashAccountName(rs.getString("cash_account_name"));
        advance.setCashTransactionId(rs.getObject("cash_transaction_id", UUID.class));
        advance.setCashTransactionCode(rs.getString("cash_transaction_code"));
        advance.setAmount(rs.getBigDecimal("amount"));
        advance.setRecoveredAmount(rs.getBigDecimal("recovered_amount"));
        advance.setOutstandingAmount(
                SalaryAdvance.STATUS_VOID.equals(rs.getString("status"))
                        ? BigDecimal.ZERO
                        : rs.getBigDecimal("amount").subtract(rs.getBigDecimal("recovered_amount"))
        );
        advance.setAdvanceDate(rs.getObject("advance_date", LocalDate.class));
        advance.setStatus(rs.getString("status"));
        advance.setRemarks(rs.getString("remarks"));
        advance.setCreatedBy(rs.getString("created_by"));
        advance.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

        return advance;
    };


    // =========================================================
    // ADVANCE - QUERIES
    // =========================================================

    public List<SalaryAdvance> getAllAdvances(
            String employeeCode,
            UUID cashAccountId,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {

        StringBuilder sql = new StringBuilder(ADVANCE_SELECT_SQL)
                .append(" WHERE 1 = 1");

        List<Object> params = new ArrayList<>();

        if (employeeCode != null) {
            sql.append(" AND a.employee_code = ?");
            params.add(employeeCode);
        }

        if (cashAccountId != null) {
            sql.append(" AND a.cash_account_id = ?");
            params.add(cashAccountId);
        }

        if (status != null) {
            sql.append(" AND a.status = ?");
            params.add(status);
        }

        if (fromDate != null) {
            sql.append(" AND a.advance_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append(" AND a.advance_date <= ?");
            params.add(toDate);
        }

        sql.append(" ORDER BY a.advance_date DESC, a.created_at DESC");

        return jdbcTemplate.query(sql.toString(), ADVANCE_MAPPER, params.toArray());
    }

    public Optional<SalaryAdvance> getAdvanceById(UUID advanceId) {

        return jdbcTemplate.query(
                ADVANCE_SELECT_SQL + " WHERE a.advance_id = ?",
                ADVANCE_MAPPER,
                advanceId
        ).stream().findFirst();
    }

    /** The employee's advances that still have something to recover, oldest first, locked for update. */
    public List<SalaryAdvance> lockRecoverableAdvances(String employeeCode) {

        List<UUID> ids = jdbcTemplate.queryForList(
                """
                SELECT advance_id
                FROM salary_advance
                WHERE employee_code = ?
                  AND status IN ('OPEN', 'PARTIAL')
                ORDER BY advance_date, created_at
                FOR UPDATE
                """,
                UUID.class,
                employeeCode
        );

        List<SalaryAdvance> advances = new ArrayList<>();
        for (UUID id : ids) {
            getAdvanceById(id).ifPresent(advances::add);
        }
        return advances;
    }

    public UUID createAdvance(SalaryAdvance advance) {

        String sql = """
                INSERT INTO salary_advance (
                    advance_code,
                    employee_code,
                    cash_account_id,
                    cash_transaction_id,
                    amount,
                    advance_date,
                    status,
                    remarks,
                    created_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING advance_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                advance.getAdvanceCode(),
                advance.getEmployeeCode(),
                advance.getCashAccountId(),
                advance.getCashTransactionId(),
                advance.getAmount(),
                advance.getAdvanceDate(),
                SalaryAdvance.STATUS_OPEN,
                advance.getRemarks(),
                advance.getCreatedBy()
        );
    }

    /** Add (or, when negative, take back) recovered amount and refresh the status. */
    public int adjustAdvanceRecovery(UUID advanceId, BigDecimal delta) {

        String sql = """
                UPDATE salary_advance
                SET recovered_amount = recovered_amount + ?,
                    status = CASE
                        WHEN recovered_amount + ? = 0 THEN 'OPEN'
                        WHEN recovered_amount + ? = amount THEN 'CLOSED'
                        ELSE 'PARTIAL'
                    END
                WHERE advance_id = ?
                  AND status <> 'VOID'
                """;

        return jdbcTemplate.update(sql, delta, delta, delta, advanceId);
    }

    /** OPEN -> VOID only: an advance that has been partly recovered cannot be voided. */
    public int voidAdvance(UUID advanceId) {

        return jdbcTemplate.update(
                "UPDATE salary_advance SET status = 'VOID' WHERE advance_id = ? AND status = 'OPEN' AND recovered_amount = 0",
                advanceId
        );
    }


    // =========================================================
    // PAYMENT - SELECT / MAPPING
    // =========================================================

    private static final String PAYMENT_SELECT_SQL = """
            SELECT
                p.salary_payment_id,
                p.payment_code,
                p.employee_code,
                emp.full_name AS employee_name,
                p.salary_period,
                p.gross_amount,
                p.advance_deducted,
                p.net_amount,
                p.cash_account_id,
                ca.account_name AS cash_account_name,
                p.cash_transaction_id,
                ct.transaction_code AS cash_transaction_code,
                p.payment_date,
                p.status,
                p.remarks,
                p.created_by,
                p.created_at
            FROM salary_payment p
            LEFT JOIN employee emp
                ON p.employee_code = emp.employee_code
            LEFT JOIN cash_account ca
                ON p.cash_account_id = ca.cash_account_id
            LEFT JOIN cash_transaction ct
                ON p.cash_transaction_id = ct.transaction_id
            """;

    private static final RowMapper<SalaryPayment> PAYMENT_MAPPER = (rs, rowNum) -> {

        SalaryPayment payment = new SalaryPayment();

        payment.setSalaryPaymentId(rs.getObject("salary_payment_id", UUID.class));
        payment.setPaymentCode(rs.getString("payment_code"));
        payment.setEmployeeCode(rs.getString("employee_code"));
        payment.setEmployeeName(rs.getString("employee_name"));
        payment.setSalaryPeriod(rs.getString("salary_period"));
        payment.setGrossAmount(rs.getBigDecimal("gross_amount"));
        payment.setAdvanceDeducted(rs.getBigDecimal("advance_deducted"));
        payment.setNetAmount(rs.getBigDecimal("net_amount"));
        payment.setCashAccountId(rs.getObject("cash_account_id", UUID.class));
        payment.setCashAccountName(rs.getString("cash_account_name"));
        payment.setCashTransactionId(rs.getObject("cash_transaction_id", UUID.class));
        payment.setCashTransactionCode(rs.getString("cash_transaction_code"));
        payment.setPaymentDate(rs.getObject("payment_date", LocalDate.class));
        payment.setStatus(rs.getString("status"));
        payment.setRemarks(rs.getString("remarks"));
        payment.setCreatedBy(rs.getString("created_by"));
        payment.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

        return payment;
    };


    // =========================================================
    // PAYMENT - QUERIES
    // =========================================================

    public List<SalaryPayment> getAllPayments(
            String employeeCode,
            UUID cashAccountId,
            String salaryPeriod,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {

        StringBuilder sql = new StringBuilder(PAYMENT_SELECT_SQL)
                .append(" WHERE 1 = 1");

        List<Object> params = new ArrayList<>();

        if (employeeCode != null) {
            sql.append(" AND p.employee_code = ?");
            params.add(employeeCode);
        }

        if (cashAccountId != null) {
            sql.append(" AND p.cash_account_id = ?");
            params.add(cashAccountId);
        }

        if (salaryPeriod != null) {
            sql.append(" AND p.salary_period = ?");
            params.add(salaryPeriod);
        }

        if (status != null) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }

        if (fromDate != null) {
            sql.append(" AND p.payment_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append(" AND p.payment_date <= ?");
            params.add(toDate);
        }

        sql.append(" ORDER BY p.payment_date DESC, p.created_at DESC");

        return jdbcTemplate.query(sql.toString(), PAYMENT_MAPPER, params.toArray());
    }

    public Optional<SalaryPayment> getPaymentById(UUID salaryPaymentId) {

        return jdbcTemplate.query(
                PAYMENT_SELECT_SQL + " WHERE p.salary_payment_id = ?",
                PAYMENT_MAPPER,
                salaryPaymentId
        ).stream().findFirst();
    }

    public boolean paidPaymentExists(String employeeCode, String salaryPeriod) {

        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM salary_payment WHERE employee_code = ? AND salary_period = ? AND status = 'PAID')",
                Boolean.class,
                employeeCode,
                salaryPeriod
        );

        return Boolean.TRUE.equals(exists);
    }

    public UUID createPayment(SalaryPayment payment) {

        String sql = """
                INSERT INTO salary_payment (
                    payment_code,
                    employee_code,
                    salary_period,
                    gross_amount,
                    advance_deducted,
                    net_amount,
                    cash_account_id,
                    cash_transaction_id,
                    payment_date,
                    status,
                    remarks,
                    created_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING salary_payment_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                payment.getPaymentCode(),
                payment.getEmployeeCode(),
                payment.getSalaryPeriod(),
                payment.getGrossAmount(),
                payment.getAdvanceDeducted(),
                payment.getNetAmount(),
                payment.getCashAccountId(),
                payment.getCashTransactionId(),
                payment.getPaymentDate(),
                SalaryPayment.STATUS_PAID,
                payment.getRemarks(),
                payment.getCreatedBy()
        );
    }

    /** PAID -> VOID only. */
    public int voidPayment(UUID salaryPaymentId) {

        return jdbcTemplate.update(
                "UPDATE salary_payment SET status = 'VOID' WHERE salary_payment_id = ? AND status = 'PAID'",
                salaryPaymentId
        );
    }

    public void createRecovery(UUID salaryPaymentId, UUID advanceId, BigDecimal amount) {

        jdbcTemplate.update(
                "INSERT INTO salary_advance_recovery (salary_payment_id, advance_id, amount) VALUES (?, ?, ?)",
                salaryPaymentId,
                advanceId,
                amount
        );
    }

    public List<Recovery> getRecoveries(UUID salaryPaymentId) {

        return jdbcTemplate.query(
                "SELECT advance_id, amount FROM salary_advance_recovery WHERE salary_payment_id = ?",
                (rs, rowNum) -> new Recovery(rs.getObject("advance_id", UUID.class), rs.getBigDecimal("amount")),
                salaryPaymentId
        );
    }

    public void deleteRecoveries(UUID salaryPaymentId) {

        jdbcTemplate.update(
                "DELETE FROM salary_advance_recovery WHERE salary_payment_id = ?",
                salaryPaymentId
        );
    }


    // =========================================================
    // SUMMARY (per employee)
    // =========================================================

    private static final RowMapper<EmployeeCashSummary> SUMMARY_MAPPER = (rs, rowNum) -> {

        EmployeeCashSummary summary = new EmployeeCashSummary();

        summary.setEmployeeCode(rs.getString("employee_code"));
        summary.setEmployeeName(rs.getString("employee_name"));
        summary.setPettyCashBalance(rs.getBigDecimal("petty_cash_balance"));
        summary.setPettyCashReceived(rs.getBigDecimal("petty_cash_received"));
        summary.setPettyCashSpent(rs.getBigDecimal("petty_cash_spent"));
        summary.setAdvancesGiven(rs.getBigDecimal("advances_given"));
        summary.setAdvancesRecovered(rs.getBigDecimal("advances_recovered"));
        summary.setAdvanceOutstanding(rs.getBigDecimal("advance_outstanding"));
        summary.setSalaryGross(rs.getBigDecimal("salary_gross"));
        summary.setSalaryNetPaid(rs.getBigDecimal("salary_net_paid"));
        summary.setSalaryPaymentCount(rs.getInt("salary_payment_count"));

        return summary;
    };

    // Dates are inclusive. Timestamps compare against "to + 1 day" so the whole last day is included.
    private static final String SUMMARY_SQL = """
            SELECT
                e.employee_code,
                e.full_name AS employee_name,
                COALESCE((
                    SELECT SUM(ca.account_balance)
                    FROM cash_account ca
                    WHERE ca.employee_code = e.employee_code
                      AND ca.account_type = 'EMPLOYEE'
                ), 0) AS petty_cash_balance,
                COALESCE((
                    SELECT SUM(ct.amount)
                    FROM cash_transaction ct
                    JOIN cash_account ca ON ca.cash_account_id = ct.to_account_id
                    WHERE ca.employee_code = e.employee_code
                      AND ca.account_type = 'EMPLOYEE'
                      AND ct.status = 'POSTED'
                      AND (CAST(:fromDate AS date) IS NULL OR ct.transaction_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR ct.transaction_date < CAST(:toDate AS date) + 1)
                ), 0) AS petty_cash_received,
                COALESCE((
                    SELECT SUM(ct.amount)
                    FROM cash_transaction ct
                    JOIN cash_account ca ON ca.cash_account_id = ct.from_account_id
                    WHERE ca.employee_code = e.employee_code
                      AND ca.account_type = 'EMPLOYEE'
                      AND ct.transaction_type = 'EXPENSE'
                      AND ct.status = 'POSTED'
                      AND (CAST(:fromDate AS date) IS NULL OR ct.transaction_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR ct.transaction_date < CAST(:toDate AS date) + 1)
                ), 0) AS petty_cash_spent,
                COALESCE((
                    SELECT SUM(a.amount)
                    FROM salary_advance a
                    WHERE a.employee_code = e.employee_code
                      AND a.status <> 'VOID'
                      AND (CAST(:fromDate AS date) IS NULL OR a.advance_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR a.advance_date <= CAST(:toDate AS date))
                ), 0) AS advances_given,
                COALESCE((
                    SELECT SUM(r.amount)
                    FROM salary_advance_recovery r
                    JOIN salary_payment p ON p.salary_payment_id = r.salary_payment_id
                    WHERE p.employee_code = e.employee_code
                      AND p.status = 'PAID'
                      AND (CAST(:fromDate AS date) IS NULL OR p.payment_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR p.payment_date <= CAST(:toDate AS date))
                ), 0) AS advances_recovered,
                COALESCE((
                    SELECT SUM(a.amount - a.recovered_amount)
                    FROM salary_advance a
                    WHERE a.employee_code = e.employee_code
                      AND a.status <> 'VOID'
                ), 0) AS advance_outstanding,
                COALESCE((
                    SELECT SUM(p.gross_amount)
                    FROM salary_payment p
                    WHERE p.employee_code = e.employee_code
                      AND p.status = 'PAID'
                      AND (CAST(:fromDate AS date) IS NULL OR p.payment_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR p.payment_date <= CAST(:toDate AS date))
                ), 0) AS salary_gross,
                COALESCE((
                    SELECT SUM(p.net_amount)
                    FROM salary_payment p
                    WHERE p.employee_code = e.employee_code
                      AND p.status = 'PAID'
                      AND (CAST(:fromDate AS date) IS NULL OR p.payment_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR p.payment_date <= CAST(:toDate AS date))
                ), 0) AS salary_net_paid,
                (
                    SELECT COUNT(*)
                    FROM salary_payment p
                    WHERE p.employee_code = e.employee_code
                      AND p.status = 'PAID'
                      AND (CAST(:fromDate AS date) IS NULL OR p.payment_date >= CAST(:fromDate AS date))
                      AND (CAST(:toDate AS date) IS NULL OR p.payment_date <= CAST(:toDate AS date))
                ) AS salary_payment_count
            FROM employee e
            WHERE (CAST(:employeeCode AS varchar) IS NULL OR e.employee_code = CAST(:employeeCode AS varchar))
              AND (
                  CAST(:employeeCode AS varchar) IS NOT NULL
                  OR EXISTS (SELECT 1 FROM cash_account ca WHERE ca.employee_code = e.employee_code AND ca.account_type = 'EMPLOYEE')
                  OR EXISTS (SELECT 1 FROM salary_advance a WHERE a.employee_code = e.employee_code)
                  OR EXISTS (SELECT 1 FROM salary_payment p WHERE p.employee_code = e.employee_code)
              )
            ORDER BY e.employee_code
            """;

    public List<EmployeeCashSummary> getSummary(String employeeCode, LocalDate fromDate, LocalDate toDate) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("employeeCode", employeeCode, Types.VARCHAR)
                .addValue("fromDate", fromDate == null ? null : java.sql.Date.valueOf(fromDate), Types.DATE)
                .addValue("toDate", toDate == null ? null : java.sql.Date.valueOf(toDate), Types.DATE);

        return namedJdbcTemplate.query(SUMMARY_SQL, params, SUMMARY_MAPPER);
    }


    // =========================================================
    // CODES / LOOKUPS
    // =========================================================

    public void lockAdvanceCodeGeneration() {

        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(hashtext('salary_advance_code'))");
    }

    public void lockPaymentCodeGeneration() {

        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(hashtext('salary_payment_code'))");
    }

    public int getMaxAdvanceSequence(String prefix) {

        return maxSequence("salary_advance", "advance_code", prefix);
    }

    public int getMaxPaymentSequence(String prefix) {

        return maxSequence("salary_payment", "payment_code", prefix);
    }

    // table / column are only ever the constants above, never user input
    private int maxSequence(String table, String column, String prefix) {

        Integer max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(" + column + " FROM '[0-9]+$') AS integer)), 0) FROM "
                        + table + " WHERE " + column + " LIKE ?",
                Integer.class,
                prefix + "%"
        );

        return max == null ? 0 : max;
    }

    public Optional<UUID> getCategoryIdByCode(String categoryCode) {

        return jdbcTemplate.queryForList(
                "SELECT category_id FROM cash_expense_category WHERE category_code = ?",
                UUID.class,
                categoryCode
        ).stream().findFirst();
    }
}
