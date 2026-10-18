package com.rr.erp.controller;

import com.rr.erp.entity.SalaryAdvance;
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
@RequestMapping("/api/salary_advances")
@RequiredArgsConstructor
@CrossOrigin
public class SalaryAdvanceController {

    private final SalaryService service;


    // GET /api/salary_advances?employeeCode=EMP001&cashAccountId=...&status=OPEN&fromDate=2026-10-01&toDate=2026-10-31
    @GetMapping
    public ResponseEntity<List<SalaryAdvance>> getAllAdvances(
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) UUID cashAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        return ResponseEntity.ok(
                service.getAllAdvances(employeeCode, cashAccountId, status, fromDate, toDate)
        );
    }


    // GET /api/salary_advances/{advanceId}
    @GetMapping("/{advanceId}")
    public ResponseEntity<SalaryAdvance> getAdvanceById(@PathVariable UUID advanceId) {

        return ResponseEntity.ok(service.getAdvanceById(advanceId));
    }


    // POST /api/salary_advances   (pays the advance from the cash account and posts the cash expense)
    @PostMapping
    public ResponseEntity<SalaryAdvance> createAdvance(@Valid @RequestBody SalaryAdvance advance) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAdvance(advance));
    }


    // PATCH /api/salary_advances/{advanceId}/void   (only while nothing has been recovered)
    @PatchMapping("/{advanceId}/void")
    public ResponseEntity<SalaryAdvance> voidAdvance(@PathVariable UUID advanceId) {

        return ResponseEntity.ok(service.voidAdvance(advanceId));
    }
}
