package com.rr.erp.controller;

import com.rr.erp.dto.CashReportResponse.PettyCashRow;
import com.rr.erp.dto.CashReportResponse.PurposeRow;
import com.rr.erp.dto.CashReportResponse.SalaryLine;
import com.rr.erp.dto.CashReportResponse.SalaryRow;
import com.rr.erp.dto.CashReportResponse.Statement;
import com.rr.erp.service.CashReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash_reports")
@RequiredArgsConstructor
@CrossOrigin
public class CashReportController {

    private final CashReportService service;

    // GET /api/cash_reports/statement/{cashAccountId}?fromDate=2026-10-01&toDate=2026-10-31
    @GetMapping("/statement/{cashAccountId}")
    public ResponseEntity<Statement> getStatement(
            @PathVariable UUID cashAccountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(service.getStatement(cashAccountId, fromDate, toDate));
    }

    // GET /api/cash_reports/petty-cash
    @GetMapping("/petty-cash")
    public ResponseEntity<List<PettyCashRow>> getPettyCashReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(service.getPettyCashReport(fromDate, toDate));
    }

    // GET /api/cash_reports/salary
    @GetMapping("/salary")
    public ResponseEntity<List<SalaryRow>> getSalaryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(service.getSalaryReport(fromDate, toDate));
    }

    // GET /api/cash_reports/salary/{employeeCode}
    @GetMapping("/salary/{employeeCode}")
    public ResponseEntity<List<SalaryLine>> getSalaryLines(
            @PathVariable String employeeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(service.getSalaryLines(employeeCode, fromDate, toDate));
    }

    // GET /api/cash_reports/main-cash-by-purpose
    @GetMapping("/main-cash-by-purpose")
    public ResponseEntity<List<PurposeRow>> getMainCashByPurpose(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(service.getMainCashByPurpose(fromDate, toDate));
    }
}
