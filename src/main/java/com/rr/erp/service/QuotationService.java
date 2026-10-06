package com.rr.erp.service;

import com.rr.erp.entity.Quotation;
import com.rr.erp.entity.QuotationItem;
import com.rr.erp.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotationService {

    private final QuotationRepository repository;

    @Transactional
    public Quotation createQuotation(Quotation quotation) {

        UUID quotationId = UUID.randomUUID();
        quotation.setQuotationId(quotationId);

        LocalDateTime now = LocalDateTime.now();
        quotation.setCreatedAt(now);
        quotation.setUpdatedAt(now);

        if (quotation.getStatus() == null) {
            quotation.setStatus("Received");
        }

        repository.insertQuotation(quotation);

        if (quotation.getItems() != null) {
            for (QuotationItem item : quotation.getItems()) {
                item.setQuotationItemId(UUID.randomUUID());
                repository.insertItem(quotationId, item);
            }
        }

        return repository.hydrate(quotation);
    }

    @Transactional
    public Quotation updateQuotation(UUID quotationId, Quotation quotation) {

        quotation.setUpdatedAt(LocalDateTime.now());

        int updated = repository.updateQuotation(quotationId, quotation);

        if (updated == 0) {
            throw new RuntimeException("Quotation not found: " + quotationId);
        }

        repository.deleteItemsByQuotationId(quotationId);

        if (quotation.getItems() != null) {
            for (QuotationItem item : quotation.getItems()) {
                item.setQuotationItemId(UUID.randomUUID());
                repository.insertItem(quotationId, item);
            }
        }

        quotation.setQuotationId(quotationId);

        return repository.hydrate(quotation);
    }

    public List<Quotation> getAllQuotations() {
        return repository.hydrateAll(repository.findAll());
    }

    public List<Quotation> getQuotationsByRequestId(UUID quotationRequestId) {
        return repository.hydrateAll(repository.findByQuotationRequestId(quotationRequestId));
    }

    public Quotation getQuotationById(UUID quotationId) {

        return repository.findById(quotationId)
                .map(repository::hydrate)
                .orElseThrow(() -> new RuntimeException("Quotation not found: " + quotationId));
    }

    @Transactional
    public void deleteQuotation(UUID quotationId) {

        if (!repository.existsById(quotationId)) {
            throw new RuntimeException("Quotation not found: " + quotationId);
        }

        repository.deleteById(quotationId);
    }

    public void setQuotationItemSelected(UUID quotationItemId, boolean isSelected) {

        int updated = repository.updateItemSelection(quotationItemId, isSelected);

        if (updated == 0) {
            throw new RuntimeException("Quotation item not found: " + quotationItemId);
        }
    }
}
