package com.rr.erp.controller;

import com.rr.erp.entity.Currency;
import com.rr.erp.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
@CrossOrigin
public class CurrencyController {

    private final CurrencyService service;

    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {

        return ResponseEntity.ok(
                service.getAllCurrencies()
        );
    }
}