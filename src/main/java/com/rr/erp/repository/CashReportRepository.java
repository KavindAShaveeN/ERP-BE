package com.rr.erp.repository;

import com.rr.erp.dto.CashReportResponse.PettyCashRow;
import com.rr.erp.dto.CashReportResponse.PurposeRow;
import com.rr.erp.dto.CashReportResponse.SalaryLine;
import com.rr.erp.dto.CashReportResponse.SalaryRow;
import com.rr.erp.dto.CashReportResponse.StatementLine;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** All dates are timestamps: from is inclusive, to is exclusive (callers pass the day after the last day). */
@Repository
@RequiredArgsConstructor
public class CashReportRepository {

    private final JdbcTemplate jdbcTemplate;


    // =========================================================
    // STATEMENT
    // =========================================================

    /** Net of POSTED money in minus out of the account before the given moment. */
    public BigDecimal getBalanceBefore(UUID cashAccountId, LocalDateTime before) {

        return jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(CASE WHEN to_account_id = ? THEN amount ELSE -amount END), 0)
                FROM cash_transaction
                WHERE status = 'POSTED'
                  AND (to_account_id = ? OR from_account_id = ?)
                  AND transaction_date < ?
                """,
                BigDecimal.class,
                cashAccountId, cashAccountId, cashAccountId, before
        );
    }

    /** Posted movements of the account in the period, oldest first (balances are filled in by the caller). */
    public List<StatementLine> getStatementLines(UUID cashAccountId, LocalDateTime from, LocalDateTime to) {

        return jdbcTemplate.query(
                """
                SELECT
                    ct.transaction_id,
                    ct.transaction_code,
                    ct.transaction_date,
                    ct.transaction_type,
                    ct.to_account_id = ? AS is_in,
                    ct.amount,
                    COALESCE(
                        CASE WHEN ct.to_account_id = ? THEN fa.account_name ELSE ta.account_name END,
                        cec.category_name
                    ) AS counterparty,
                    ct.purpose,
                    pe.full_name AS employee_name,
                    ct.salary_kind,
                    ct.description,
                    ct.reference_number
                FROM cash_transaction ct
                LEFT JOIN cash_account fa ON fa.cash_account_id = ct.from_account_id
                LEFT JOIN cash_account ta ON ta.cash_account_id = ct.to_account_id
                LEFT JOIN cash_expense_category cec ON cec.category_id = ct.category_id
                LEFT JOIN employee pe ON pe.employee_code = ct.employee_code
                WHERE ct.status = 'POSTED'
                  AND (ct.to_account_id = ? OR ct.from_account_id = ?)
                  AND ct.transaction_date >= ? AND ct.transaction_date < ?
                ORDER BY ct.transaction_date, ct.created_at
                """,
                (rs, rowNum) -> {
                    StatementLine line = new StatementLine();
                    boolean isIn = rs.getBoolean("is_in");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    line.setTransactionId(rs.getObject("transaction_id", UUID.class));
                    line.setTransactionCode(rs.getString("transaction_code"));
                    line.setTransactionDate(rs.getObject("transaction_date", LocalDateTime.class));
                    line.setTransactionType(rs.getString("transaction_type"));
                    line.setCounterparty(rs.getString("counterparty"));
                    line.setPurpose(rs.getString("purpose"));
                    line.setEmployeeName(rs.getString("employee_name"));
                    line.setSalaryKind(rs.getString("salary_kind"));
                    line.setDescription(rs.getString("description"));
                    line.setReferenceNumber(rs.getString("reference_number"));
                    line.setMoneyIn(isIn ? amount : BigDecimal.ZERO);
                    line.setMoneyOut(isIn ? BigDecimal.ZERO : amount);
                    return line;
                },
                cashAccountId, cashAccountId, cashAccountId, cashAccountId, from, to
        );
    }


    // =========================================================
    // PETTY CASH REPORT
    // =========================================================

    public List<PettyCashRow> getPettyCashReport(LocalDateTime from, LocalDateTime to) {

        return jdbcTemplate.query(
                """
                SELECT
                    ca.cash_account_id,
                    ca.account_name,
                    ca.employee_code,
                    e.full_name AS employee_name,
                    ca.is_active,
                    COALESCE(SUM(CASE WHEN ct.to_account_id = ca.cash_account_id THEN ct.amount ELSE -ct.amount END)
                        FILTER (WHERE ct.transaction_date < ?), 0) AS opening_balance,
                    COALESCE(SUM(ct.amount)
                        FILTER (WHERE ct.to_account_id = ca.cash_account_id
                                AND ct.transaction_date >= ? AND ct.transaction_date < ?), 0) AS total_in,
                    COALESCE(SUM(ct.amount)
                        FILTER (WHERE ct.from_account_id = ca.cash_account_id
                                AND ct.transaction_date >= ? AND ct.transaction_date < ?), 0) AS total_out
                FROM cash_account ca
                LEFT JOIN employee e ON e.employee_code = ca.employee_code
                LEFT JOIN cash_transaction ct
                    ON ct.status = 'POSTED'
                   AND (ct.to_account_id = ca.cash_account_id OR ct.from_account_id = ca.cash_account_id)
                WHERE ca.account_type = 'EMPLOYEE'
                GROUP BY ca.cash_account_id, ca.account_name, ca.employee_code, e.full_name, ca.is_active
                ORDER BY ca.account_name
                """,
                (rs, rowNum) -> {
                    PettyCashRow row = new PettyCashRow();
                    row.setCashAccountId(rs.getObject("cash_account_id", UUID.class));
                    row.setAccountName(rs.getString("account_name"));
                    row.setEmployeeCode(rs.getString("employee_code"));
                    row.setEmployeeName(rs.getString("employee_name"));
                    row.setIsActive(rs.getBoolean("is_active"));
                    row.setOpeningBalance(rs.getBigDecimal("opening_balance"));
                    row.setTotalIn(rs.getBigDecimal("total_in"));
                    row.setTotalOut(rs.getBigDecimal("total_out"));
                    row.setClosingBalance(row.getOpeningBalance().add(row.getTotalIn()).subtract(row.getTotalOut()));
                    return row;
                },
                from, from, to, from, to
        );
    }


    // =========================================================
    // SALARY REPORT
    // =========================================================

    public List<SalaryRow> getSalaryReport(LocalDateTime from, LocalDateTime to) {

        return jdbcTemplate.query(
                """
                SELECT
                    ct.employee_code,
                    e.full_name AS employee_name,
                    COALESCE(SUM(ct.amount) FILTER (WHERE ct.salary_kind = 'ADVANCE'), 0) AS advances,
                    COALESCE(SUM(ct.amount) FILTER (WHERE ct.salary_kind = 'FULL_SALARY'), 0) AS full_salary,
                    SUM(ct.amount) AS total,
                    COUNT(*) AS transfer_count,
                    MAX(ct.transaction_date) AS last_paid_at
                FROM cash_transaction ct
                JOIN employee e ON e.employee_code = ct.employee_code
                WHERE ct.status = 'POSTED'
                  AND ct.purpose = 'SALARY'
                  AND ct.transaction_date >= ? AND ct.transaction_date < ?
                GROUP BY ct.employee_code, e.full_name
                ORDER BY e.full_name
                """,
                (rs, rowNum) -> {
                    SalaryRow row = new SalaryRow();
                    row.setEmployeeCode(rs.getString("employee_code"));
                    row.setEmployeeName(rs.getString("employee_name"));
                    row.setAdvances(rs.getBigDecimal("advances"));
                    row.setFullSalary(rs.getBigDecimal("full_salary"));
                    row.setTotal(rs.getBigDecimal("total"));
                    row.setTransferCount(rs.getInt("transfer_count"));
                    row.setLastPaidAt(rs.getObject("last_paid_at", LocalDateTime.class));
                    return row;
                },
                from, to
        );
    }

    public List<SalaryLine> getSalaryLines(String employeeCode, LocalDateTime from, LocalDateTime to) {

        return jdbcTemplate.query(
                """
                SELECT
                    ct.transaction_id,
                    ct.transaction_code,
                    ct.transaction_date,
                    ct.salary_kind,
                    ct.amount,
                    fa.account_name AS from_account_name,
                    ta.account_name AS to_account_name,
                    ct.description
                FROM cash_transaction ct
                LEFT JOIN cash_account fa ON fa.cash_account_id = ct.from_account_id
                LEFT JOIN cash_account ta ON ta.cash_account_id = ct.to_account_id
                WHERE ct.status = 'POSTED'
                  AND ct.purpose = 'SALARY'
                  AND ct.employee_code = ?
                  AND ct.transaction_date >= ? AND ct.transaction_date < ?
                ORDER BY ct.transaction_date, ct.created_at
                """,
                (rs, rowNum) -> {
                    SalaryLine line = new SalaryLine();
                    line.setTransactionId(rs.getObject("transaction_id", UUID.class));
                    line.setTransactionCode(rs.getString("transaction_code"));
                    line.setTransactionDate(rs.getObject("transaction_date", LocalDateTime.class));
                    line.setSalaryKind(rs.getString("salary_kind"));
                    line.setAmount(rs.getBigDecimal("amount"));
                    line.setFromAccountName(rs.getString("from_account_name"));
                    line.setToAccountName(rs.getString("to_account_name"));
                    line.setDescription(rs.getString("description"));
                    return line;
                },
                employeeCode, from, to
        );
    }


    // =========================================================
    // MAIN CASH OUTFLOW BY PURPOSE
    // =========================================================

    public List<PurposeRow> getMainCashByPurpose(LocalDateTime from, LocalDateTime to) {

        return jdbcTemplate.query(
                """
                SELECT
                    COALESCE(ct.purpose, 'UNSPECIFIED') AS purpose,
                    SUM(ct.amount) AS total,
                    COUNT(*) AS transfer_count
                FROM cash_transaction ct
                JOIN cash_account fa ON fa.cash_account_id = ct.from_account_id
                WHERE ct.status = 'POSTED'
                  AND fa.account_type = 'MAIN_CASH'
                  AND ct.transaction_date >= ? AND ct.transaction_date < ?
                GROUP BY COALESCE(ct.purpose, 'UNSPECIFIED')
                ORDER BY total DESC
                """,
                (rs, rowNum) -> {
                    PurposeRow row = new PurposeRow();
                    row.setPurpose(rs.getString("purpose"));
                    row.setTotal(rs.getBigDecimal("total"));
                    row.setTransferCount(rs.getInt("transfer_count"));
                    return row;
                },
                from, to
        );
    }
}
