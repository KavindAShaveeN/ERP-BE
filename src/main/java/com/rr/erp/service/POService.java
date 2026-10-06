package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.PO;
import com.rr.erp.entity.POItem;
import com.rr.erp.repository.POMaterialRequestRepository;
import com.rr.erp.repository.PORepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class POService {

    private final PORepository repository;
    private final POMaterialRequestRepository poMaterialRequestRepository;


    @Transactional
    public PO createPO(PO po) {

        UUID poId = UUID.randomUUID();

        po.setPoId(poId);

        if (po.getIsApproved() == null) {
            po.setIsApproved(false);
        }

        if (po.getApprovalStatus() == null) {
            po.setApprovalStatus(Boolean.TRUE.equals(po.getIsApproved()) ? "APPROVED" : "PENDING");
        }

        if (po.getStatus() == null) {
            po.setStatus("ACTIVE");
        }

        if (po.getPaymentType() == null) {
            po.setPaymentType("Credit");
        }

        applyTaxDefaults(po);

        repository.createPO(po);


        // Save PO items
        if (po.getItems() != null) {

            for (POItem item : po.getItems()) {

                item.setPoItemId(
                        UUID.randomUUID()
                );

                // Amount = Quantity x Unit Price
                if (item.getQuantity() != null
                        && item.getUnitPrice() != null) {

                    BigDecimal amount =
                            item.getUnitPrice().multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            );

                    item.setAmount(amount);
                }

                repository.createPOItem(
                        poId,
                        item
                );
            }
        }

        poMaterialRequestRepository.replaceLinks(poId, po.getRelatedMrIds(), po.getRequestedBy());

        recalculateTotals(poId);

        return findByIdWithItems(poId);
    }


    /**
     * SSCL has no separate on/off toggle in the UI — a filled-in rate means it applies,
     * a blank (zero) rate means it doesn't. ssclApplicable is derived here rather than
     * trusted from the request, so the backend stays authoritative regardless of what a
     * client sends.
     */
    private void applyTaxDefaults(PO po) {

        // A blank supplier code would violate fk_po_supplier — store it as NULL instead.
        if (po.getSupplierCode() != null && po.getSupplierCode().isBlank()) {
            po.setSupplierCode(null);
        }

        if (po.getSsclPercentage() == null) {
            po.setSsclPercentage(BigDecimal.ZERO);
        }

        po.setSsclApplicable(po.getSsclPercentage().compareTo(BigDecimal.ZERO) > 0);

        if (po.getVatPercentage() == null) {
            po.setVatPercentage(new BigDecimal("18"));
        }

        if (po.getSsclAmount() == null) {
            po.setSsclAmount(BigDecimal.ZERO);
        }

        if (po.getVatAmount() == null) {
            po.setVatAmount(BigDecimal.ZERO);
        }
    }


    /**
     * Recomputes SSCL/VAT for a PO from its current total_value and tax settings, then
     * persists the result. (Final PO Value itself isn't stored — PO.getFinalPoValue()
     * derives it from total_value + sscl_amount + vat_amount on every read.)
     * SSCL Amount = Total Value x SSCL Rate (0 if not applicable)
     * VAT Amount = (Total Value + SSCL Amount) x VAT Rate
     */
    @Transactional
    public void recalculateTotals(UUID poId) {

        PO po = findByIdWithItems(poId);

        if (po == null) {
            return;
        }

        BigDecimal totalValue =
                po.getTotalValue() != null ? po.getTotalValue() : BigDecimal.ZERO;

        BigDecimal ssclPercentage =
                po.getSsclPercentage() != null ? po.getSsclPercentage() : BigDecimal.ZERO;

        BigDecimal vatPercentage =
                po.getVatPercentage() != null ? po.getVatPercentage() : BigDecimal.ZERO;

        BigDecimal ssclAmount = Boolean.TRUE.equals(po.getSsclApplicable())
                ? totalValue.multiply(ssclPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal vatAmount = totalValue.add(ssclAmount)
                .multiply(vatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        repository.updateTotals(
                poId,
                ssclAmount,
                vatAmount
        );
    }


    private PO findByIdWithItems(UUID poId) {

        PO po = repository.findById(poId);

        if (po != null) {
            po.setItems(repository.getPOItems(poId));
            po.setRelatedMrIds(poMaterialRequestRepository.findMrIdsByPo(poId));
        }

        return po;
    }



    /**
     * Batch-loads items and related-MR-ids for a whole list of POs in two queries total
     * (WHERE po_id IN (...)) instead of two queries per PO — the per-PO loop this replaced
     * was an N+1 query pattern that made loading many POs at once (e.g. the company-wide
     * "all purchase orders" list) far slower than it needed to be.
     */
    private void attachItemsAndMrIds(List<PO> poList) {

        if (poList.isEmpty()) {
            return;
        }

        List<UUID> poIds = poList.stream().map(PO::getPoId).collect(Collectors.toList());

        Map<UUID, List<POItem>> itemsByPoId = repository.getPOItemsByPoIds(poIds);
        Map<UUID, List<UUID>> mrIdsByPoId = poMaterialRequestRepository.findMrIdsByPoIds(poIds);

        for (PO po : poList) {
            po.setItems(itemsByPoId.getOrDefault(po.getPoId(), Collections.emptyList()));
            po.setRelatedMrIds(mrIdsByPoId.getOrDefault(po.getPoId(), Collections.emptyList()));
        }
    }


    public List<PO> getCreatedPOs(
            String billToProjectCode
    ) {

        List<PO> poList =
                repository.getCreatedPOs(
                        billToProjectCode
                );

        attachItemsAndMrIds(poList);

        return poList;
    }


    public List<PO> getPOsByDestinationProject(
            String projectCode
    ) {

        List<PO> poList =
                repository.getPOsByDestinationProject(
                        projectCode
                );

        attachItemsAndMrIds(poList);

        return poList;
    }


    /**
     * Every PO regardless of project, one bounded page at a time — used by the HQ-wide
     * Purchase Orders list. Callers that genuinely need every PO (company-wide reports,
     * price-history hints) page through this via the frontend's fetchAllPages helper
     * rather than the backend ever loading the whole table into memory in one call.
     */
    public PagedResponse<PO> getAllPurchaseOrders(int page, int size) {

        List<PO> poList = repository.findAll(page, size);
        long totalElements = repository.countAll();

        attachItemsAndMrIds(poList);

        return new PagedResponse<>(poList, page, size, totalElements);
    }


    @Transactional
    public PO updatePO(
            UUID poId,
            PO po
    ) {

        applyTaxDefaults(po);

        int updated =
                repository.updatePO(
                        poId,
                        po
                );

        if (updated == 0) {
            throw new RuntimeException(
                    "Purchase Order not found: "
                            + poId
            );
        }


        // If items are provided,
        // replace existing PO items
        if (po.getItems() != null) {

            repository.deletePOItems(poId);

            for (POItem item : po.getItems()) {

                item.setPoItemId(
                        UUID.randomUUID()
                );

                if (item.getQuantity() != null
                        && item.getUnitPrice() != null) {

                    BigDecimal amount =
                            item.getUnitPrice().multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()
                                    )
                            );

                    item.setAmount(amount);
                }

                repository.createPOItem(
                        poId,
                        item
                );
            }
        }

        po.setPoId(poId);

        poMaterialRequestRepository.replaceLinks(poId, po.getRelatedMrIds(), po.getRequestedBy());

        recalculateTotals(poId);

        return findByIdWithItems(poId);
    }
}
