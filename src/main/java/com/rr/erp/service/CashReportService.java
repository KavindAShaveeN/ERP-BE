package com.rr.erp.service;

import com.rr.erp.dto.CashReportResponse.PettyCashRow;
import com.rr.erp.dto.CashReportResponse.PurposeRow;
import com.rr.erp.dto.CashReportResponse.SalaryLine;
import com.rr.erp.dto.CashReportResponse.SalaryRow;
import com.rr.erp.dto.CashReportResponse.Statement;
import com.rr.erp.dto.CashReportResponse.StatementLine;
import com.rr.erp.entity.CashAccount;
import com.rr.erp.repository.CashAccountRepository;
import com.rr.erp.repository.CashReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashReportService {

    // "No start date" means from the beginning of time, so the opening balance is simply zero.
    private static final LocalDate EARLIEST = LocalDate.of(2000, 1, 1);

    private final CashReportRepository repository;
    private final CashAccountRepository cashAccountRepository;


    public Statement getStatement(UUID cashAccountId, LocalDate fromDate, LocalDate toDate) {

        CashAccount account = cashAccountRepository.getCashAccountById(cashAccountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cash account not found with id: " + cashAccountId));

        LocalDate from = fromDate == null ? EARLIEST : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        checkRange(from, to);

        BigDecimal opening = repository.getBalanceBefore(cashAccountId, from.atStartOfDay());
        List<StatementLine> lines = repository.getStatementLines(cashAccountId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        BigDecimal running = opening;
        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;
        for (StatementLine line : lines) {
            running = running.add(line.getMoneyIn()).subtract(line.getMoneyOut());
            line.setBalance(running);
            totalIn = totalIn.add(line.getMoneyIn());
            totalOut = totalOut.add(line.getMoneyOut());
        }

        Statement statement = new Statement();
        statement.setCashAccountId(cashAccountId);
        statement.setAccountName(account.getAccountName());
        statement.setAccountType(account.getAccountType());
        statement.setEmployeeName(account.getEmployeeName());
        statement.setFromDate(fromDate);
        statement.setToDate(to);
        statement.setOpeningBalance(opening);
        statement.setTotalIn(totalIn);
        statement.setTotalOut(totalOut);
        statement.setClosingBalance(running);
        statement.setLines(lines);
        return statement;
    }

    public List<PettyCashRow> getPettyCashReport(LocalDate fromDate, LocalDate toDate) {

        LocalDate from = fromDate == null ? EARLIEST : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        checkRange(from, to);
        return repository.getPettyCashReport(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    public List<SalaryRow> getSalaryReport(LocalDate fromDate, LocalDate toDate) {

        LocalDate from = fromDate == null ? EARLIEST : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        checkRange(from, to);
        return repository.getSalaryReport(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    public List<SalaryLine> getSalaryLines(String employeeCode, LocalDate fromDate, LocalDate toDate) {

        LocalDate from = fromDate == null ? EARLIEST : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        checkRange(from, to);
        return repository.getSalaryLines(employeeCode, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    public List<PurposeRow> getMainCashByPurpose(LocalDate fromDate, LocalDate toDate) {

        LocalDate from = fromDate == null ? EARLIEST : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        checkRange(from, to);
        return repository.getMainCashByPurpose(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    private static void checkRange(LocalDate from, LocalDate to) {

        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be on or before toDate");
        }
    }
}
