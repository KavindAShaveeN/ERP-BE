package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.PO;
import com.rr.erp.entity.POItem;
import com.rr.erp.repository.POMaterialRequestRepository;
import com.rr.erp.repository.PORepository;
import com.rr.erp.repository.ThreePServiceRepository;
import com.rr.erp.security.SecurityUtil;
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
    private final ThreePServiceRepository threePServiceRepository;
    private final ApprovalService approvalService;

    static final String DOC_TYPE = "PO";


    @Transactional
    public PO createPO(PO po) {

        UUID poId = UUID.randomUUID();

        po.setPoId(poId);

        // A new PO is always a Draft; approval is owned by the approval engine.
        po.setIsApproved(false);
        po.setApprovalStatus("DRAFT");
        po.setApprovedBy(null);
        po.setApprovedDate(null);

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

        linkThreePServices(po);

        recalculateTotals(poId);

        return findByIdWithItems(poId);
    }

    /** Job card service PO: link the 3P rows, move Requested to PO Created, copy each line amount to its row. */
    private void linkThreePServices(PO po) {

        java.util.Set<UUID> ids = new java.util.LinkedHashSet<>();
        if (po.getThreePServiceIds() != null) {
            ids.addAll(po.getThreePServiceIds());
        }
        if (po.getItems() != null) {
            po.getItems().stream()
                    .map(POItem::getThreePServiceId)
                    .filter(java.util.Objects::nonNull)
                    .forEach(ids::add);
        }
        if (ids.isEmpty()) {
            return;
        }

        threePServiceRepository.markPoCreated(new java.util.ArrayList<>(ids), po.getPoCode(), po.getSupplierCode());

        if (po.getItems() != null) {
            for (POItem item : po.getItems()) {
                if (item.getThreePServiceId() != null && item.getAmount() != null) {
                    threePServiceRepository.setServiceCharge(item.getThreePServiceId(), po.getPoCode(), item.getAmount());
                }
            }
        }
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

        PO existing = repository.findById(poId);
        if (existing == null) {
            throw new RuntimeException("Purchase Order not found: " + poId);
        }

        // Once submitted, the content is frozen for the approvers; only a status change (hold / cancel / reactivate) may pass.
        if ("PENDING".equals(existing.getApprovalStatus())
                && java.util.Objects.equals(existing.getStatus(), po.getStatus())) {
            throw new RuntimeException("Purchase Order " + existing.getPoCode()
                    + " has been submitted for approval and can no longer be edited.");
        }

        // Approval is decided by the approval engine, never by the edit payload.
        po.setIsApproved(existing.getIsApproved());
        po.setApprovalStatus(existing.getApprovalStatus());
        po.setApprovedBy(existing.getApprovedBy());
        po.setApprovedDate(existing.getApprovedDate());

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


    /** Draft -> Requested: sends the PO to the approval engine (it then shows in "My approvals"). */
    @Transactional
    public PO submitPO(UUID poId) {

        PO existing = repository.findById(poId);
        if (existing == null) {
            throw new RuntimeException("Purchase Order not found: " + poId);
        }
        if (!"DRAFT".equals(existing.getApprovalStatus())) {
            throw new RuntimeException("This purchase order has already been submitted.");
        }
        if (!"ACTIVE".equals(existing.getStatus())) {
            throw new RuntimeException("Only an active purchase order can be submitted for approval.");
        }
        if (repository.getPOItems(poId).isEmpty()) {
            throw new RuntimeException("Add at least one item before submitting.");
        }

        String requestingProject = existing.getBillToProjectCode() != null
                ? existing.getBillToProjectCode() : existing.getProjectCode();

        // Marked first: with no approval step configured, submit() approves straight away and
        // the approval callback expects the PO to be in its submitted state.
        repository.markSubmitted(poId);
        approvalService.submit(DOC_TYPE, poId, requestingProject, existing.getProjectCode(),
                SecurityUtil.currentEmployeeCode(), Map.of());

        return findByIdWithItems(poId);
    }

    /** Called by the approval engine when the last approver approves. */
    @Transactional
    public void approveFromWorkflow(UUID poId, String approver) {
        repository.markApproved(poId, approver != null ? approver : SecurityUtil.currentEmployeeCodeOrNull());
    }

    /** Called by the approval engine on a rejection. */
    @Transactional
    public void rejectFromWorkflow(UUID poId, String rejectedBy) {
        repository.markRejected(poId, rejectedBy);
    }
}
