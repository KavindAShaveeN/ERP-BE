package com.rr.erp.service;

import com.rr.erp.entity.QuotationRequest;
import com.rr.erp.entity.QuotationRequestItem;
import com.rr.erp.repository.QuotationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotationRequestService {

    private final QuotationRequestRepository repository;

    @Transactional
    public QuotationRequest createQuotationRequest(QuotationRequest request) {

        UUID quotationRequestId = UUID.randomUUID();
        request.setQuotationRequestId(quotationRequestId);

        LocalDateTime now = LocalDateTime.now();
        request.setCreatedAt(now);
        request.setUpdatedAt(now);

        if (request.getStatus() == null) {
            request.setStatus("Open");
        }

        repository.insertQuotationRequest(request);

        if (request.getItems() != null) {
            for (QuotationRequestItem item : request.getItems()) {
                item.setQuotationRequestItemId(UUID.randomUUID());
                repository.insertItem(quotationRequestId, item);
            }
        }

        if (request.getSupplierCodes() != null) {
            for (String supplierCode : request.getSupplierCodes()) {
                repository.insertSupplierCode(quotationRequestId, supplierCode);
            }
        }

        return repository.hydrate(request);
    }

    @Transactional
    public QuotationRequest updateQuotationRequest(UUID quotationRequestId, QuotationRequest request) {

        request.setUpdatedAt(LocalDateTime.now());

        int updated = repository.updateQuotationRequest(quotationRequestId, request);

        if (updated == 0) {
            throw new RuntimeException("Quotation request not found: " + quotationRequestId);
        }

        repository.deleteItemsByQuotationRequestId(quotationRequestId);

        if (request.getItems() != null) {
            for (QuotationRequestItem item : request.getItems()) {
                item.setQuotationRequestItemId(UUID.randomUUID());
                repository.insertItem(quotationRequestId, item);
            }
        }

        repository.deleteSuppliersByQuotationRequestId(quotationRequestId);

        if (request.getSupplierCodes() != null) {
            for (String supplierCode : request.getSupplierCodes()) {
                repository.insertSupplierCode(quotationRequestId, supplierCode);
            }
        }

        request.setQuotationRequestId(quotationRequestId);

        return repository.hydrate(request);
    }

    public List<QuotationRequest> getAllQuotationRequests() {
        return repository.hydrateAll(repository.findAll());
    }

    public List<QuotationRequest> getQuotationRequestsByMrId(UUID mrId) {
        return repository.hydrateAll(repository.findByMrId(mrId));
    }

    public QuotationRequest getQuotationRequestById(UUID quotationRequestId) {

        return repository.findById(quotationRequestId)
                .map(repository::hydrate)
                .orElseThrow(() -> new RuntimeException("Quotation request not found: " + quotationRequestId));
    }

    @Transactional
    public void deleteQuotationRequest(UUID quotationRequestId) {

        if (!repository.existsById(quotationRequestId)) {
            throw new RuntimeException("Quotation request not found: " + quotationRequestId);
        }

        repository.deleteById(quotationRequestId);
    }
}
