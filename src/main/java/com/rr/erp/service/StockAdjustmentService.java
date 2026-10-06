package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.StockAdjustment;
import com.rr.erp.entity.StockAdjustmentItem;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.repository.StockAdjustmentRepository;
import com.rr.erp.util.DimensionalItems;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockAdjustmentService {

    private final StockAdjustmentRepository repository;
    private final ProjectStoreService projectStoreService;
    private final StockBatchService stockBatchService;
    private final AssetLocationService assetLocationService;
    private final AssetService assetService;


    @Transactional
    public StockAdjustment createStockAdjustment(
            StockAdjustment adjustment
    ) {

        UUID stockAdjustmentId = UUID.randomUUID();

        adjustment.setStockAdjustmentId(
                stockAdjustmentId
        );

        if (adjustment.getIsApproved() == null) {
            adjustment.setIsApproved(false);
        }

        repository.createStockAdjustment(
                adjustment
        );

        if (adjustment.getItems() != null) {

            // Stock is only moved once an adjustment is Approved — a newly created
            // adjustment is always Pending (see StockAdjustmentFormPage.tsx), so this
            // normally does nothing at create time; it stays a real check in case an
            // adjustment is ever created pre-approved by another caller.
            boolean applyStock = Boolean.TRUE.equals(adjustment.getIsApproved());

            for (StockAdjustmentItem item :
                    adjustment.getItems()) {

                item.setStockAdjustmentItemId(
                        UUID.randomUUID()
                );

                if (applyStock) {
                    applyToStock(adjustment, stockAdjustmentId, item);
                }

                repository.createStockAdjustmentItem(
                        stockAdjustmentId,
                        item
                );
            }
        }

        return adjustment;
    }

    public PagedResponse<StockAdjustment> getCreatedAdjustments(
            String projectCode,
            int page,
            int size
    ) {

        List<StockAdjustment> adjustments =
                repository.getCreatedAdjustments(
                        projectCode,
                        page,
                        size
                );

        for (StockAdjustment adjustment :
                adjustments) {

            adjustment.setItems(
                    repository.getItems(
                            adjustment.getStockAdjustmentId()
                    )
            );
        }

        long totalElements = repository.countCreatedAdjustments(projectCode);

        return new PagedResponse<>(adjustments, page, size, totalElements);
    }

    public PagedResponse<StockAdjustment> getAllAdjustments(int page, int size) {

        List<StockAdjustment> adjustments =
                repository.getAllAdjustments(page, size);

        for (StockAdjustment adjustment :
                adjustments) {

            adjustment.setItems(
                    repository.getItems(
                            adjustment.getStockAdjustmentId()
                    )
            );
        }

        long totalElements = repository.countAllAdjustments();

        return new PagedResponse<>(adjustments, page, size, totalElements);
    }


    @Transactional
    public StockAdjustment updateStockAdjustment(
            UUID stockAdjustmentId,
            StockAdjustment adjustment
    ) {

        StockAdjustment existing = repository.findById(stockAdjustmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock adjustment not found: " + stockAdjustmentId
                ));

        // Once approved, an adjustment's stock has already been moved and it is no
        // longer editable (mirrors GINService.updateGin) — enforced here too so
        // nothing can re-apply stock for it a second time.
        if (Boolean.TRUE.equals(existing.getIsApproved())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock adjustment " + stockAdjustmentId
                            + " has already been approved and can no longer be edited."
            );
        }

        int updated =
                repository.updateStockAdjustment(
                        stockAdjustmentId,
                        adjustment
                );

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Stock adjustment not found: " + stockAdjustmentId
            );
        }

        /*
         * If items are provided,
         * replace existing items.
         */
        if (adjustment.getItems() != null) {

            repository.deleteItems(
                    stockAdjustmentId
            );

            // existing was confirmed not-yet-approved above, so isApproved being true
            // here means this call is exactly the Pending -> Approved transition — the
            // one moment stock should move. A plain edit (still Pending) or a Reject
            // decision (isApproved stays false) must not touch project_store at all.
            boolean applyStock = Boolean.TRUE.equals(adjustment.getIsApproved());

            for (StockAdjustmentItem item :
                    adjustment.getItems()) {

                item.setStockAdjustmentItemId(
                        UUID.randomUUID()
                );

                if (applyStock) {
                    applyToStock(adjustment, stockAdjustmentId, item);
                }

                repository.createStockAdjustmentItem(
                        stockAdjustmentId,
                        item
                );
            }
        }

        adjustment.setStockAdjustmentId(
                stockAdjustmentId
        );

        return adjustment;
    }

    /**
     * Applies one adjustment line to the project's live stock and FIFO
     * batches: a positive quantity opens a new batch at the entered unit
     * price (a "found more than the system says" correction); a negative
     * quantity consumes FIFO batches as a normal issue would, and the
     * line's unitPrice/adjustmentValue are overwritten with the actual
     * FIFO cost of what was removed, so the stored record matches what
     * really left inventory rather than a manually entered guess.
     */
    private void applyToStock(
            StockAdjustment adjustment,
            UUID stockAdjustmentId,
            StockAdjustmentItem item
    ) {

        if (item.getAssetCode() != null && !item.getAssetCode().isBlank()) {
            applyAsset(adjustment, stockAdjustmentId, item);
            return;
        }

        BigDecimal quantity = item.getAdjustmentQuantity();

        if (quantity == null || quantity.signum() == 0) {
            return;
        }

        if (quantity.signum() > 0) {

            BigDecimal unitCost = item.getUnitPrice() != null
                    ? item.getUnitPrice()
                    : BigDecimal.ZERO;

            if (DimensionalItems.isDimensionalItem(item.getItemCode())) {
                // quantity IS the piece/bar count for these items, same convention as GRN.
                projectStoreService.receiveNewBatch(
                        adjustment.getProjectCode(),
                        item.getItemCode(),
                        quantity,
                        adjustment.getAdjustmentDate().toLocalDate(),
                        unitCost,
                        StockBatchService.SOURCE_STOCK_ADJUSTMENT,
                        stockAdjustmentId,
                        adjustment.getStockAdjustmentCode(),
                        item.getLengthM(),
                        item.getWidthM(),
                        quantity.intValue()
                );
            } else {
                projectStoreService.receiveNewBatch(
                        adjustment.getProjectCode(),
                        item.getItemCode(),
                        quantity,
                        adjustment.getAdjustmentDate().toLocalDate(),
                        unitCost,
                        StockBatchService.SOURCE_STOCK_ADJUSTMENT,
                        stockAdjustmentId,
                        adjustment.getStockAdjustmentCode(),
                        null,
                        null,
                        null,
                        item.getDescription()
                );
            }

            item.setUnitPrice(unitCost);
            item.setAdjustmentValue(unitCost.multiply(quantity));
            return;
        }

        // Non-stock items: the user picked exactly which batch/description to write off
        // (see StockAdjustmentItem#stockBatchId) — draw from that one batch instead of blind FIFO.
        if (item.getStockBatchId() != null) {
            StockActionAllocation allocation = projectStoreService.issueFromBatch(
                    item.getStockBatchId(),
                    adjustment.getProjectCode(),
                    item.getItemCode(),
                    quantity.abs(),
                    adjustment.getAdjustmentDate().toLocalDate(),
                    StockBatchService.ACTION_STOCK_ADJUSTMENT,
                    stockAdjustmentId,
                    item.getStockAdjustmentItemId()
            );

            item.setUnitPrice(allocation.getUnitCost());
            item.setAdjustmentValue(allocation.getUnitCost().multiply(quantity));
            return;
        }

        List<StockActionAllocation> allocations = projectStoreService.issueGoods(
                adjustment.getProjectCode(),
                item.getItemCode(),
                quantity.abs(),
                adjustment.getAdjustmentDate().toLocalDate(),
                StockBatchService.ACTION_STOCK_ADJUSTMENT,
                stockAdjustmentId,
                item.getStockAdjustmentItemId(),
                item.getLengthM(),
                item.getWidthM()
        );

        BigDecimal actualUnitCost = stockBatchService.weightedAverageCost(allocations);

        item.setUnitPrice(actualUnitCost);
        // quantity is negative here, so this comes out negative — a reduction in value.
        item.setAdjustmentValue(actualUnitCost.multiply(quantity));
    }

    /**
     * Asset line: a decrease writes the asset off (it must be at the project; status becomes
     * STATUS_SCRAPPED), an increase records it as found at the project (status back to Active).
     * Quantity is forced to -1 / +1 and stock is never touched.
     */
    private void applyAsset(
            StockAdjustment adjustment,
            UUID stockAdjustmentId,
            StockAdjustmentItem item
    ) {

        String assetCode = item.getAssetCode().trim();
        BigDecimal quantity = item.getAdjustmentQuantity();

        if (quantity == null || quantity.signum() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Asset " + assetCode + " needs an adjustment of +1 (found) or -1 (written off)."
            );
        }

        item.setAdjustmentQuantity(quantity.signum() > 0 ? BigDecimal.ONE : BigDecimal.ONE.negate());

        java.time.LocalDate date = adjustment.getAdjustmentDate().toLocalDate();

        if (quantity.signum() < 0) {
            assetLocationService.adjustAssetOut(assetCode, adjustment.getProjectCode(), stockAdjustmentId,
                    adjustment.getStockAdjustmentCode(), date, adjustment.getApprovedBy());
            assetService.updateAssetStatus(assetCode, STATUS_SCRAPPED);
        } else {
            assetLocationService.adjustAssetIn(assetCode, adjustment.getProjectCode(), stockAdjustmentId,
                    adjustment.getStockAdjustmentCode(), date, adjustment.getApprovedBy());
            assetService.updateAssetStatus(assetCode, STATUS_ACTIVE);
        }
    }

    // Status a written-off asset ends up in — change here to switch between 'Disposed' and 'Inactive'.
    private static final String STATUS_SCRAPPED = "Disposed";
    private static final String STATUS_ACTIVE = "Active";
}
