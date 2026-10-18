package com.rr.erp.controller;

import com.rr.erp.dto.BulkSalaryPaymentRequest;
import com.rr.erp.entity.SalaryPayment;
import com.rr.erp.service.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salary_payments")
@RequiredArgsConstructor
@CrossOrigin
public class SalaryPaymentController {

    private final SalaryService service;


    // GET /api/salary_payments?employeeCode=EMP001&cashAccountId=...&salaryPeriod=2026-10&status=PAID&fromDate=...&toDate=...
    @GetMapping
    public ResponseEntity<List<SalaryPayment>> getAllPayments(
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) UUID cashAccountId,
            @RequestParam(required = false) String salaryPeriod,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        return ResponseEntity.ok(
                service.getAllPayments(employeeCode, cashAccountId, salaryPeriod, status, fromDate, toDate)
        );
    }


    // GET /api/salary_payments/{salaryPaymentId}
    @GetMapping("/{salaryPaymentId}")
    public ResponseEntity<SalaryPayment> getPaymentById(@PathVariable UUID salaryPaymentId) {

        return ResponseEntity.ok(service.getPaymentById(salaryPaymentId));
    }


    // POST /api/salary_payments   (recovers open advances, pays the net from the cash account)
    @PostMapping
    public ResponseEntity<SalaryPayment> createPayment(@Valid @RequestBody SalaryPayment payment) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(payment));
    }


    // POST /api/salary_payments/bulk   (all-or-nothing: pays every employee in the list)
    @PostMapping("/bulk")
    public ResponseEntity<List<SalaryPayment>> createBulkPayments(@Valid @RequestBody BulkSalaryPaymentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBulkPayments(request.getPayments()));
    }


    // PATCH /api/salary_payments/{salaryPaymentId}/void   (restores the recovered advances)
    @PatchMapping("/{salaryPaymentId}/void")
    public ResponseEntity<SalaryPayment> voidPayment(@PathVariable UUID salaryPaymentId) {

        return ResponseEntity.ok(service.voidPayment(salaryPaymentId));
    }
}
