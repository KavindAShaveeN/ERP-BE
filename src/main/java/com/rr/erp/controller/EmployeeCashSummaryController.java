package com.rr.erp.controller;

import com.rr.erp.entity.EmployeeCashSummary;
import com.rr.erp.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee_cash_summary")
@RequiredArgsConstructor
@CrossOrigin
public class EmployeeCashSummaryController {

    private final SalaryService service;


    // GET /api/employee_cash_summary?employeeCode=EMP001&fromDate=2026-10-01&toDate=2026-10-31
    // Petty cash + advances + salary per employee. The date range limits the flows (petty cash received / spent,
    // advances given / recovered, salary paid); the petty cash balance and outstanding advance are always current.
    @GetMapping
    public ResponseEntity<List<EmployeeCashSummary>> getSummary(
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        return ResponseEntity.ok(service.getSummary(employeeCode, fromDate, toDate));
    }
}
