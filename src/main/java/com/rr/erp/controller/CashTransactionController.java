package com.rr.erp.controller;

import com.rr.erp.entity.CashAccountBalance;
import com.rr.erp.entity.CashTransaction;
import com.rr.erp.service.CashTransactionService;
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
@RequestMapping("/api/cash_transactions")
@RequiredArgsConstructor
@CrossOrigin
public class CashTransactionController {

    private final CashTransactionService service;


    // ==================================================
    // GET /api/cash_transactions?transactionType=EXPENSE&status=POSTED&accountId=...
    //     &categoryId=...&createdBy=EMP001&fromDate=2026-10-01&toDate=2026-10-31
    // ==================================================
    @GetMapping
    public ResponseEntity<List<CashTransaction>> getAllTransactions(
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {

        return ResponseEntity.ok(
                service.getAllTransactions(
                        transactionType,
                        status,
                        accountId,
                        categoryId,
                        createdBy,
                        fromDate,
                        toDate
                )
        );
    }


    // ==================================================
    // GET /api/cash_transactions/{transactionId}
    // ==================================================
    @GetMapping("/{transactionId}")
    public ResponseEntity<CashTransaction> getTransactionById(
            @PathVariable UUID transactionId
    ) {

        return ResponseEntity.ok(
                service.getTransactionById(transactionId)
        );
    }


    // ==================================================
    // GET /api/cash_transactions/code/{transactionCode}
    // ==================================================
    @GetMapping("/code/{transactionCode}")
    public ResponseEntity<CashTransaction> getTransactionByCode(
            @PathVariable String transactionCode
    ) {

        return ResponseEntity.ok(
                service.getTransactionByCode(transactionCode)
        );
    }


    // ==================================================
    // GET /api/cash_transactions/balance/{cashAccountId}
    // ==================================================
    @GetMapping("/balance/{cashAccountId}")
    public ResponseEntity<CashAccountBalance> getAccountBalance(
            @PathVariable UUID cashAccountId
    ) {

        return ResponseEntity.ok(
                service.getAccountBalance(cashAccountId)
        );
    }


    // ==================================================
    // POST /api/cash_transactions
    // ==================================================
    @PostMapping
    public ResponseEntity<CashTransaction> createTransaction(
            @Valid @RequestBody CashTransaction transaction
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.createTransaction(transaction)
        );
    }


    // ==================================================
    // PUT /api/cash_transactions/{transactionId}   (DRAFT only)
    // ==================================================
    @PutMapping("/{transactionId}")
    public ResponseEntity<CashTransaction> updateTransaction(
            @PathVariable UUID transactionId,
            @Valid @RequestBody CashTransaction transaction
    ) {

        return ResponseEntity.ok(
                service.updateTransaction(
                        transactionId,
                        transaction
                )
        );
    }


    // ==================================================
    // PATCH /api/cash_transactions/{transactionId}/post
    // ==================================================
    @PatchMapping("/{transactionId}/post")
    public ResponseEntity<CashTransaction> postTransaction(
            @PathVariable UUID transactionId
    ) {

        return ResponseEntity.ok(
                service.postTransaction(transactionId)
        );
    }


    // ==================================================
    // PATCH /api/cash_transactions/{transactionId}/void
    // ==================================================
    @PatchMapping("/{transactionId}/void")
    public ResponseEntity<CashTransaction> voidTransaction(
            @PathVariable UUID transactionId
    ) {

        return ResponseEntity.ok(
                service.voidTransaction(transactionId)
        );
    }
}
