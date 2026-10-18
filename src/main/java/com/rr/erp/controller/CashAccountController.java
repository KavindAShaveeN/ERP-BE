package com.rr.erp.controller;

import com.rr.erp.entity.CashAccount;
import com.rr.erp.service.CashAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash_accounts")
@RequiredArgsConstructor
@CrossOrigin
public class CashAccountController {

    private final CashAccountService service;


    // ==================================================
    // GET /api/cash_accounts?accountType=EMPLOYEE&employeeCode=EMP001&isActive=true
    // ==================================================
    @GetMapping
    public ResponseEntity<List<CashAccount>> getAllCashAccounts(
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) Boolean isActive
    ) {

        return ResponseEntity.ok(
                service.getAllCashAccounts(
                        accountType,
                        employeeCode,
                        isActive
                )
        );
    }


    // ==================================================
    // GET /api/cash_accounts/{cashAccountId}
    // ==================================================
    @GetMapping("/{cashAccountId}")
    public ResponseEntity<CashAccount> getCashAccountById(
            @PathVariable UUID cashAccountId
    ) {

        return ResponseEntity.ok(
                service.getCashAccountById(cashAccountId)
        );
    }


    // ==================================================
    // POST /api/cash_accounts
    // ==================================================
    @PostMapping
    public ResponseEntity<CashAccount> createCashAccount(
            @Valid @RequestBody CashAccount account
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.createCashAccount(account)
        );
    }


    // ==================================================
    // PUT /api/cash_accounts/{cashAccountId}
    // ==================================================
    @PutMapping("/{cashAccountId}")
    public ResponseEntity<CashAccount> updateCashAccount(
            @PathVariable UUID cashAccountId,
            @Valid @RequestBody CashAccount account
    ) {

        return ResponseEntity.ok(
                service.updateCashAccount(
                        cashAccountId,
                        account
                )
        );
    }


    // ==================================================
    // PATCH /api/cash_accounts/{cashAccountId}/status?isActive=false
    // ==================================================
    @PatchMapping("/{cashAccountId}/status")
    public ResponseEntity<CashAccount> updateActiveStatus(
            @PathVariable UUID cashAccountId,
            @RequestParam boolean isActive
    ) {

        return ResponseEntity.ok(
                service.updateActiveStatus(
                        cashAccountId,
                        isActive
                )
        );
    }
}
