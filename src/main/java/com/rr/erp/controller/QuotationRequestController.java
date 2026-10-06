package com.rr.erp.controller;

import com.rr.erp.entity.QuotationRequest;
import com.rr.erp.service.QuotationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotation-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuotationRequestController {

    private final QuotationRequestService service;

    @GetMapping("/all")
    public ResponseEntity<List<QuotationRequest>> getAllQuotationRequests() {
        return ResponseEntity.ok(service.getAllQuotationRequests());
    }

    @GetMapping("/mr/{mrId}")
    public ResponseEntity<List<QuotationRequest>> getQuotationRequestsByMrId(@PathVariable UUID mrId) {
        return ResponseEntity.ok(service.getQuotationRequestsByMrId(mrId));
    }

    @GetMapping("/{quotationRequestId}")
    public ResponseEntity<QuotationRequest> getQuotationRequestById(@PathVariable UUID quotationRequestId) {
        return ResponseEntity.ok(service.getQuotationRequestById(quotationRequestId));
    }

    @PostMapping
    public ResponseEntity<QuotationRequest> createQuotationRequest(@RequestBody QuotationRequest request) {
        return ResponseEntity.ok(service.createQuotationRequest(request));
    }

    @PutMapping("/{quotationRequestId}")
    public ResponseEntity<QuotationRequest> updateQuotationRequest(
            @PathVariable UUID quotationRequestId,
            @RequestBody QuotationRequest request
    ) {
        return ResponseEntity.ok(service.updateQuotationRequest(quotationRequestId, request));
    }

    @DeleteMapping("/{quotationRequestId}")
    public ResponseEntity<Void> deleteQuotationRequest(@PathVariable UUID quotationRequestId) {
        service.deleteQuotationRequest(quotationRequestId);
        return ResponseEntity.noContent().build();
    }
}
