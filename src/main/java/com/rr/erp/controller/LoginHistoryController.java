package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.LoginHistory;
import com.rr.erp.service.LoginHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/login-history")
@CrossOrigin(origins = "*")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    public LoginHistoryController(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    // GET /api/login-history?page=0&size=20&employeeCode=&loginStatus=&fromDate=2026-09-01&toDate=2026-09-30
    // Every login attempt, newest first. All filters optional; toDate includes that whole day.
    @GetMapping
    public ResponseEntity<PagedResponse<LoginHistory>> getLoginHistory(
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String loginStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                loginHistoryService.getLoginHistory(employeeCode, loginStatus, fromDate, toDate, page, size)
        );
    }

    // GET /api/login-history/employee/{employeeCode}?page=0&size=20 — one employee's logins.
    @GetMapping("/employee/{employeeCode}")
    public ResponseEntity<PagedResponse<LoginHistory>> getLoginHistoryForEmployee(
            @PathVariable String employeeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                loginHistoryService.getLoginHistory(employeeCode, null, null, null, page, size)
        );
    }
}
