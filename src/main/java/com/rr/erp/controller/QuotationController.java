package com.rr.erp.controller;

import com.rr.erp.dto.QuotationItemSelectionRequest;
import com.rr.erp.entity.Quotation;
import com.rr.erp.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuotationController {

    private final QuotationService service;

    @GetMapping("/all")
    public ResponseEntity<List<Quotation>> getAllQuotations() {
        return ResponseEntity.ok(service.getAllQuotations());
    }

    @GetMapping("/request/{quotationRequestId}")
    public ResponseEntity<List<Quotation>> getQuotationsByRequestId(@PathVariable UUID quotationRequestId) {
        return ResponseEntity.ok(service.getQuotationsByRequestId(quotationRequestId));
    }

    @GetMapping("/{quotationId}")
    public ResponseEntity<Quotation> getQuotationById(@PathVariable UUID quotationId) {
        return ResponseEntity.ok(service.getQuotationById(quotationId));
    }

    @PostMapping
    public ResponseEntity<Quotation> createQuotation(@RequestBody Quotation quotation) {
        return ResponseEntity.ok(service.createQuotation(quotation));
    }

    @PutMapping("/{quotationId}")
    public ResponseEntity<Quotation> updateQuotation(
            @PathVariable UUID quotationId,
            @RequestBody Quotation quotation
    ) {
        return ResponseEntity.ok(service.updateQuotation(quotationId, quotation));
    }

    @DeleteMapping("/{quotationId}")
    public ResponseEntity<Void> deleteQuotation(@PathVariable UUID quotationId) {
        service.deleteQuotation(quotationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{quotationItemId}/selection")
    public ResponseEntity<Void> setQuotationItemSelected(
            @PathVariable UUID quotationItemId,
            @RequestBody QuotationItemSelectionRequest request
    ) {
        service.setQuotationItemSelected(quotationItemId, Boolean.TRUE.equals(request.getIsSelected()));
        return ResponseEntity.noContent().build();
    }
}
