package com.rr.erp.controller;

import com.rr.erp.entity.SupplierPayment;
import com.rr.erp.service.SupplierPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupplierPaymentController {

    private final SupplierPaymentService service;

    @PostMapping
    public ResponseEntity<SupplierPayment> createPayment(
            @RequestBody SupplierPayment payment
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createPayment(payment));
    }


    @GetMapping("/po/{poCode}")
    public ResponseEntity<List<SupplierPayment>>
    getByPoCode(
            @PathVariable String poCode
    ) {

        return ResponseEntity.ok(
                service.getByPoCode(poCode)
        );
    }


    @GetMapping("/all")
    public ResponseEntity<List<SupplierPayment>>
    getAllPayments() {

        return ResponseEntity.ok(
                service.getAllPayments()
        );
    }
}
