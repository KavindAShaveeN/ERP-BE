package com.rr.erp.service;

import com.rr.erp.dto.ItemCodeResponse;
import com.rr.erp.dto.ProjectWiseQuantityResponse;
import com.rr.erp.dto.StockMovementResponse;
import com.rr.erp.entity.ProjectStore;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.entity.StockBatch;
import com.rr.erp.repository.ProjectStoreRepository;
import com.rr.erp.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectStoreService {

    private final ProjectStoreRepository projectStoreRepository;
    private final StockBatchService stockBatchService;
    private final StockMovementRepository stockMovementRepository;

    public ProjectStoreService(
            ProjectStoreRepository projectStoreRepository,
            StockBatchService stockBatchService,
            StockMovementRepository stockMovementRepository
    ) {
        this.projectStoreRepository = projectStoreRepository;
        this.stockBatchService = stockBatchService;
        this.stockMovementRepository = stockMovementRepository;
    }


    /*
     * =========================================================
     * 1a. GOODS RECEIVED — a brand-new batch
     *
     * For stock with no existing lot to attribute it to: a supplier
     * GRN line, or a positive stock adjustment. Opens a new FIFO
     * batch identity alongside the quantity_on_hand update, inside
     * this one transaction.
     * =========================================================
     */
    @Transactional
    public ProjectStore receiveNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate receivedDate,
            BigDecimal unitCost,
            String sourceType,
            UUID sourceId,
            String sourceReference
    ) {
        return receiveNewBatch(
                projectCode, itemCode, quantity, receivedDate, unitCost,
                sourceType, sourceId, sourceReference,
                null, null, null
        );
    }

    /**
     * Dimensional-item overload (see com.rr.erp.util.DimensionalItems): also records the
     * size/piece-count this batch is opened with. {@code lengthValue}/{@code widthValue}/
     * {@code pieceCount} are null for non-dimensional items, in which case this behaves
     * exactly like the plain overload.
     */
    @Transactional
    public ProjectStore receiveNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate receivedDate,
            BigDecimal unitCost,
            String sourceType,
            UUID sourceId,
            String sourceReference,
            BigDecimal lengthValue,
            BigDecimal widthValue,
            Integer pieceCount
    ) {
        return receiveNewBatch(
                projectCode, itemCode, quantity, receivedDate, unitCost,
                sourceType, sourceId, sourceReference,
                lengthValue, widthValue, pieceCount, null
        );
    }

    /**
     * Description-aware overload — see {@link StockBatchService#openNewBatch(String, String,
     * BigDecimal, BigDecimal, LocalDate, String, UUID, String, BigDecimal, BigDecimal, Integer,
     * String)}. {@code description} is optional.
     */
    @Transactional
    public ProjectStore receiveNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate receivedDate,
            BigDecimal unitCost,
            String sourceType,
            UUID sourceId,
            String sourceReference,
            BigDecimal lengthValue,
            BigDecimal widthValue,
            Integer pieceCount,
            String description
    ) {
        return receiveNewBatch(
                projectCode, itemCode, quantity, receivedDate, unitCost,
                sourceType, sourceId, sourceReference,
                lengthValue, widthValue, pieceCount, description, null
        );
    }

    /**
     * Expiry-aware overload — see {@link StockBatchService#openNewBatch(String, String,
     * BigDecimal, BigDecimal, LocalDate, String, UUID, String, BigDecimal, BigDecimal, Integer,
     * String, LocalDate)}. {@code expiryDate} is optional.
     */
    @Transactional
    public ProjectStore receiveNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate receivedDate,
            BigDecimal unitCost,
            String sourceType,
            UUID sourceId,
            String sourceReference,
            BigDecimal lengthValue,
            BigDecimal widthValue,
            Integer pieceCount,
            String description,
            LocalDate expiryDate
    ) {

        validateQuantity(quantity);

        projectStoreRepository.receiveGoods(
                projectCode,
                itemCode,
                quantity,
                receivedDate
        );

        stockBatchService.openNewBatch(
                projectCode,
                itemCode,
                quantity,
                unitCost,
                receivedDate,
                sourceType,
                sourceId,
                sourceReference,
                lengthValue,
                widthValue,
                pieceCount,
                description,
                expiryDate
        );

        return projectStoreRepository
                .getProjectStoreItem(projectCode, itemCode)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unable to update project store"
                        )
                );
    }

    /**
     * Credits the project_store ledger only, with no batch of its own — for a cut-return,
     * where StockBatchService#recordCutReturn already opens the resulting batch(es) itself
     * (each sized/priced per result line, not as one blended quantity/cost this method could
     * pass through). {@code quantity} is the total pieces returned across every result line.
     */
    @Transactional
    public void creditQuantityOnly(String projectCode, String itemCode, BigDecimal quantity, LocalDate receivedDate) {

        validateQuantity(quantity);

        projectStoreRepository.receiveGoods(
                projectCode,
                itemCode,
                quantity,
                receivedDate
        );
    }


    /*
     * =========================================================
     * 1b. GOODS RECEIVED — crediting an existing batch
     *
     * For an internal transfer or a stock return: the receiving
     * project is credited with the exact same batch(es) a FIFO
     * debit elsewhere just gave up, so the batch's identity and
     * cost survive the move unchanged. No new batch is opened.
     * =========================================================
     */
    @Transactional
    public ProjectStore creditExistingBatches(
            String projectCode,
            String itemCode,
            LocalDate receivedDate,
            List<StockActionAllocation> credits
    ) {

        BigDecimal quantity = credits.stream()
                .map(StockActionAllocation::getQtyTaken)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        validateQuantity(quantity);

        projectStoreRepository.receiveGoods(
                projectCode,
                itemCode,
                quantity,
                receivedDate
        );

        stockBatchService.creditBatches(projectCode, credits);

        return projectStoreRepository
                .getProjectStoreItem(projectCode, itemCode)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unable to update project store"
                        )
                );
    }


    /*
     * =========================================================
     * 2. GOODS ISSUED
     *
     * Also consumes FIFO cost batches for the quantity issued and
     * returns the allocations drawn from, so callers that need the
     * actual cost (e.g. a negative stock adjustment's value, or an
     * internal transfer's receiving side) have it.
     * =========================================================
     */
    @Transactional
    public List<StockActionAllocation> issueGoods(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate issuedDate,
            String actionType,
            UUID actionId,
            UUID actionItemId
    ) {
        return issueGoods(projectCode, itemCode, quantity, issuedDate, actionType, actionId, actionItemId, null, null);
    }

    /**
     * Size-scoped variant, for dimensional items (see com.rr.erp.util.DimensionalItems):
     * scopes the FIFO draw to batches of that exact size (e.g. the line's chosen 6m bars,
     * not the 1m offcuts). {@code lengthValue} null behaves exactly like the plain overload.
     */
    @Transactional
    public List<StockActionAllocation> issueGoods(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate issuedDate,
            String actionType,
            UUID actionId,
            UUID actionItemId,
            BigDecimal lengthValue,
            BigDecimal widthValue
    ) {

        validateQuantity(quantity);

        int updatedRows =
                projectStoreRepository.issueGoods(
                        projectCode,
                        itemCode,
                        quantity,
                        issuedDate
                );

        /*
         * No row was updated.
         *
         * Possible reasons:
         * 1. Item does not exist in project store
         * 2. Insufficient stock
         */
        if (updatedRows == 0) {

            BigDecimal availableQuantity =
                    projectStoreRepository
                            .getQuantityOnHand(
                                    projectCode,
                                    itemCode
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Item " + itemCode
                                                    + " is not available in project "
                                                    + projectCode
                                    )
                            );

            throw new RuntimeException(
                    "Insufficient stock for item "
                            + itemCode
                            + ". Available quantity: "
                            + availableQuantity
                            + ", requested quantity: "
                            + quantity
            );
        }

        return stockBatchService.issueFifo(
                projectCode,
                itemCode,
                quantity,
                actionType,
                actionId,
                actionItemId,
                lengthValue,
                widthValue
        );
    }

    /**
     * Explicit-batch variant — see {@link StockBatchService#issueFromBatch}. Used for
     * non-stock items, where the caller (the user, via a picker) already knows exactly
     * which batch/description it wants drawn from, instead of a blind FIFO walk.
     */
    @Transactional
    public StockActionAllocation issueFromBatch(
            UUID stockBatchId,
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate issuedDate,
            String actionType,
            UUID actionId,
            UUID actionItemId
    ) {

        validateQuantity(quantity);

        int updatedRows = projectStoreRepository.issueGoods(projectCode, itemCode, quantity, issuedDate);

        if (updatedRows == 0) {

            BigDecimal availableQuantity =
                    projectStoreRepository
                            .getQuantityOnHand(projectCode, itemCode)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Item " + itemCode
                                                    + " is not available in project "
                                                    + projectCode
                                    )
                            );

            throw new RuntimeException(
                    "Insufficient stock for item "
                            + itemCode
                            + ". Available quantity: "
                            + availableQuantity
                            + ", requested quantity: "
                            + quantity
            );
        }

        return stockBatchService.issueFromBatch(
                stockBatchId,
                projectCode,
                itemCode,
                quantity,
                actionType,
                actionId,
                actionItemId
        );
    }


    /*
     * =========================================================
     * REVERSE A PRODUCTION'S OWN FINISHED-GOODS BATCH
     *
     * Undoes exactly the batch a plant production record opened for one
     * output item when it was approved — used only by production reversal.
     * Refuses (via StockBatchService#reverseOwnBatch) if any of it has
     * already left the plant's stock.
     * =========================================================
     */
    @Transactional
    public void reverseProducedBatch(String projectCode, String itemCode, UUID productionId) {

        List<StockBatch> batches = stockBatchService.getBatchesBySource(
                StockBatchService.SOURCE_PLANT_PRODUCTION, productionId
        );

        for (StockBatch batch : batches) {

            if (!batch.getItemCode().equals(itemCode)) {
                continue;
            }

            stockBatchService.reverseOwnBatch(batch, projectCode);
            projectStoreRepository.reverseReceivedGoods(projectCode, itemCode, batch.getOriginalQty());
        }
    }


    /*
     * =========================================================
     * Quantity Validation
     * =========================================================
     */
    private void validateQuantity(BigDecimal quantity) {

        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quantity is required"
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    public List<ItemCodeResponse> getItemCodesByProjectCode(String projectCode) {

        List<ItemCodeResponse> items = projectStoreRepository.getItemCodesByProjectCode(projectCode);

        for (ItemCodeResponse item : items) {
            BigDecimal[] totals = stockMovementRepository.getTotals(projectCode, item.getItemCode());
            item.setTotalReceived(totals[0]);
            item.setTotalIssuedIntra(totals[1]);
            item.setTotalIssuedInterProject(totals[2]);
            item.setTotalReceivedInterProject(totals[3]);
            item.setTotalProduced(totals[4]);
            item.setTotalConsumed(totals[5]);
            item.setTotalJobIssued(totals[6]);
            item.setTotalJobReturned(totals[7]);
            item.setTotalFuelIssued(totals[8]);
        }

        return items;
    }

    public List<ItemCodeResponse> getAllItemQuantities(Integer projectTypeId) {

        List<ItemCodeResponse> items = projectStoreRepository.getAllItemQuantities(projectTypeId);

        for (ItemCodeResponse item : items) {
            BigDecimal[] totals = stockMovementRepository.getTotalsAllProjects(item.getItemCode(), projectTypeId);
            item.setTotalReceived(totals[0]);
            item.setTotalIssuedIntra(totals[1]);
            item.setTotalIssuedInterProject(totals[2]);
            item.setTotalReceivedInterProject(totals[3]);
            item.setTotalProduced(totals[4]);
            item.setTotalConsumed(totals[5]);
            item.setTotalJobIssued(totals[6]);
            item.setTotalJobReturned(totals[7]);
            item.setTotalFuelIssued(totals[8]);
        }

        return items;
    }

    public List<ProjectWiseQuantityResponse> getProjectWiseQuantityByItemCode(String itemCode) {

        List<ProjectWiseQuantityResponse> rows = projectStoreRepository.getProjectWiseQuantityByItemCode(itemCode);

        for (ProjectWiseQuantityResponse row : rows) {
            BigDecimal[] totals = stockMovementRepository.getTotals(row.getProjectCode(), itemCode);
            row.setTotalReceived(totals[0]);
            row.setTotalIssuedIntra(totals[1]);
            row.setTotalIssuedInterProject(totals[2]);
            row.setTotalReceivedInterProject(totals[3]);
            row.setTotalProduced(totals[4]);
            row.setTotalConsumed(totals[5]);
            row.setTotalJobIssued(totals[6]);
            row.setTotalJobReturned(totals[7]);
            row.setTotalFuelIssued(totals[8]);
        }

        return rows;
    }

    public List<StockMovementResponse> getItemHistory(String projectCode, String itemCode) {

        return stockMovementRepository.getHistory(projectCode, itemCode);
    }

    public List<StockMovementResponse> getAllProjectsItemHistory(String itemCode, Integer projectTypeId) {

        return stockMovementRepository.getHistoryAllProjects(itemCode, projectTypeId);
    }

    public void setReorderLevel(String projectCode, String itemCode,
                                BigDecimal reorderLevel, BigDecimal reorderQuantity) {

        if (reorderLevel == null || reorderLevel.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }

        if (reorderQuantity == null || reorderQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reorder quantity cannot be negative");
        }

        projectStoreRepository.setReorderLevel(projectCode, itemCode, reorderLevel, reorderQuantity);
    }

    // A blank bin location clears it (stored as NULL) rather than saving an empty string.
    public void setBinLocation(String projectCode, String itemCode, String binLocation) {

        String trimmed = binLocation == null ? null : binLocation.trim();

        if (trimmed != null && trimmed.length() > 50) {
            throw new IllegalArgumentException("Bin location cannot be longer than 50 characters");
        }

        projectStoreRepository.setBinLocation(
                projectCode,
                itemCode,
                trimmed == null || trimmed.isEmpty() ? null : trimmed
        );
    }
}
