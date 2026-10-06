package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.AssetLocation;
import com.rr.erp.entity.StockReturn;
import com.rr.erp.entity.StockReturnItem;
import com.rr.erp.repository.GRNRepository;
import com.rr.erp.repository.StockReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockReturnService {

    private final StockReturnRepository repository;
    private final GRNRepository grnRepository;
    private final ProjectStoreService projectStoreService;
    private final AssetLocationService assetLocationService;
    private final AssetService assetService;
    private final AssetPackService assetPackService;


    // POST
    @Transactional
    public StockReturn createStockReturn(
            StockReturn stockReturn
    ) {

        UUID stockReturnId = UUID.randomUUID();

        stockReturn.setStockReturnId(stockReturnId);

        if (stockReturn.getIsApproved() == null) {
            stockReturn.setIsApproved(false);
        }

        if (stockReturn.getIsGateVerified() == null) {
            stockReturn.setIsGateVerified(false);
        }

        validateReturnType(stockReturn);

        repository.createStockReturn(stockReturn);

        if (stockReturn.getItems() != null) {

            // Stock only moves once a return is Approved — a newly created return is
            // always Pending (see MaterialReturnFormPage.tsx), so this normally does
            // nothing at create time; it stays a real check in case a return is ever
            // created pre-approved by another caller.
            boolean applyStock = Boolean.TRUE.equals(stockReturn.getIsApproved());

            assetPackService.saveSelections(
                    AssetPackService.DOC_STOCK_RETURN, stockReturnId, stockReturn.getItems());

            for (StockReturnItem item :
                    stockReturn.getItems()) {

                item.setStockReturnItemId(
                        UUID.randomUUID()
                );

                if (applyStock) {
                    applyToStock(stockReturn, stockReturnId, item);
                }

                repository.createStockReturnItem(
                        stockReturnId,
                        item
                );
            }

            if (applyStock) {
                recordVehicleDispatch(stockReturn, stockReturnId);
            }
        }

        return stockReturn;
    }


    // GET CREATED
    public PagedResponse<StockReturn> getCreatedReturns(
            String fromProjectCode,
            int page,
            int size
    ) {

        List<StockReturn> returns =
                repository.getCreatedReturns(
                        fromProjectCode,
                        page,
                        size
                );

        addItems(returns);

        long totalElements = repository.countCreatedReturns(fromProjectCode);

        return new PagedResponse<>(returns, page, size, totalElements);
    }


    // GET INCOMING
    public PagedResponse<StockReturn> getIncomingReturns(
            String toProjectCode,
            int page,
            int size
    ) {

        List<StockReturn> returns =
                repository.getIncomingReturns(
                        toProjectCode,
                        page,
                        size
                );

        addItems(returns);

        long totalElements = repository.countIncomingReturns(toProjectCode);

        return new PagedResponse<>(returns, page, size, totalElements);
    }


    // GET ALL (HQ-wide reports)
    public PagedResponse<StockReturn> getAllReturns(int page, int size) {

        List<StockReturn> returns =
                repository.getAllReturns(page, size);

        addItems(returns);

        long totalElements = repository.countAllReturns();

        return new PagedResponse<>(returns, page, size, totalElements);
    }

    // Outgoing gate-pass queue: approved returns this project hasn't seen leave the gate yet.
    public List<StockReturn> getPendingExitGate(String fromProjectCode) {
        List<StockReturn> returns = repository.findPendingExitGate(fromProjectCode);
        addItems(returns);
        return returns;
    }

    // Incoming gate-pass queue: returns dispatched to this project, not yet confirmed
    // arriving, and with no GRN raised for them yet.
    public List<StockReturn> getPendingArrivalGate(String toProjectCode) {
        List<StockReturn> returns = repository.findPendingArrivalGate(toProjectCode);
        addItems(returns);
        return returns;
    }


    @Transactional
    public StockReturn updateStockReturn(
            UUID stockReturnId,
            StockReturn stockReturn
    ) {

        StockReturn existing = repository.findById(stockReturnId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock return not found: " + stockReturnId
                ));

        // Once approved, a return's stock has already been moved and it is no longer
        // editable — enforced here too so nothing can re-apply stock for it a second time.
        if (Boolean.TRUE.equals(existing.getIsApproved())) {
            throw new RuntimeException(
                    "Stock return " + stockReturnId
                            + " has already been approved and can no longer be edited."
            );
        }

        validateReturnType(stockReturn);

        int updated =
                repository.updateStockReturn(
                        stockReturnId,
                        stockReturn
                );

        if (updated == 0) {
            throw new RuntimeException(
                    "Stock return not found: "
                            + stockReturnId
            );
        }

        if (stockReturn.getItems() != null) {

            repository.deleteItems(stockReturnId);

            // existing was confirmed not-yet-approved above, so isApproved being true here
            // means this call is exactly the Pending -> Approved transition — the one moment
            // stock should move. A plain edit (still Pending) or a Reject decision
            // (isApproved stays false) must not touch project_store at all.
            boolean applyStock = Boolean.TRUE.equals(stockReturn.getIsApproved());

            assetPackService.saveSelections(
                    AssetPackService.DOC_STOCK_RETURN, stockReturnId, stockReturn.getItems());

            for (StockReturnItem item :
                    stockReturn.getItems()) {

                item.setStockReturnItemId(
                        UUID.randomUUID()
                );

                if (applyStock) {
                    applyToStock(stockReturn, stockReturnId, item);
                }

                repository.createStockReturnItem(
                        stockReturnId,
                        item
                );
            }

            if (applyStock) {
                recordVehicleDispatch(stockReturn, stockReturnId);
            }
        }

        stockReturn.setStockReturnId(stockReturnId);

        return stockReturn;
    }


    /**
     * Records security's gate confirmation for a stock return — mirrors GINService#verifyGinAtGate.
     * The gate check is independent of the approval workflow and can happen before or after a
     * return is approved; bypasses updateStockReturn's "no edits after approval" lock since it
     * only ever touches the three gate-verification columns. Once recorded, updateStockReturn
     * locks the return from further edits.
     */
    @Transactional
    public StockReturn verifyStockReturnAtGate(UUID stockReturnId, String gateVerifiedBy) {

        StockReturn existing = repository.findById(stockReturnId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock return not found: " + stockReturnId
                ));

        if (Boolean.TRUE.equals(existing.getIsGateVerified())) {
            throw new RuntimeException(
                    "Stock return " + stockReturnId + " has already been gate-verified."
            );
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        repository.updateGateVerification(stockReturnId, gateVerifiedBy, now);

        existing.setGateVerifiedBy(gateVerifiedBy);
        existing.setGateVerifiedDate(now);
        existing.setIsGateVerified(true);

        return existing;
    }

    /**
     * Records the receiving project's security confirmation that this return's goods arrived
     * at their gate. Mirrors GINService#verifyGinArrivalAtGate, including pushing the
     * confirmation onto a GRN that was already raised for this return before security got to
     * it, since that GRN's gate fields would otherwise never get set.
     */
    @Transactional
    public StockReturn verifyStockReturnArrivalAtGate(UUID stockReturnId, String gateVerifiedBy) {

        StockReturn existing = repository.findById(stockReturnId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock return not found: " + stockReturnId
                ));

        if (!Boolean.TRUE.equals(existing.getIsGateVerified())) {
            throw new RuntimeException(
                    "Stock return " + stockReturnId + " has not left the sending project's gate yet."
            );
        }

        if (Boolean.TRUE.equals(existing.getIsArrivalGateVerified())) {
            throw new RuntimeException(
                    "Stock return " + stockReturnId + " has already been confirmed arriving at the gate."
            );
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        repository.updateArrivalGateVerification(stockReturnId, gateVerifiedBy, now);

        existing.setArrivalGateVerifiedBy(gateVerifiedBy);
        existing.setArrivalGateVerifiedDate(now);
        existing.setIsArrivalGateVerified(true);

        grnRepository.findByStockReturnId(stockReturnId).ifPresent(grn -> {
            if (!Boolean.TRUE.equals(grn.getIsGateVerified())) {
                grnRepository.updateGateVerification(grn.getGrnId(), gateVerifiedBy, now);
            }
        });

        return existing;
    }

    private void addItems(
            List<StockReturn> returns
    ) {

        for (StockReturn stockReturn : returns) {

            stockReturn.setItems(
                    repository.getItems(
                            stockReturn.getStockReturnId()
                    )
            );
        }
    }

    private static final String RETURN_TYPE_SUPPLIER = "SUPPLIER";
    private static final String RETURN_TYPE_INTERNAL = "INTERNAL";

    /**
     * Supplier returns have no destination project — the stock leaves the
     * org back to the vendor — while internal returns are always
     * project-to-project, so each type needs the opposite field required.
     * Defaults a missing/blank returnType to INTERNAL to preserve existing
     * callers/rows created before this field existed.
     */
    private void validateReturnType(StockReturn stockReturn) {

        if (stockReturn.getReturnType() == null || stockReturn.getReturnType().isBlank()) {
            stockReturn.setReturnType(RETURN_TYPE_INTERNAL);
        }

        if (RETURN_TYPE_SUPPLIER.equals(stockReturn.getReturnType())) {
            if (stockReturn.getSupplierCode() == null || stockReturn.getSupplierCode().isBlank()) {
                throw new RuntimeException("Supplier is required for a supplier return.");
            }
            stockReturn.setToProjectCode(null);
        } else {
            if (stockReturn.getToProjectCode() == null || stockReturn.getToProjectCode().isBlank()) {
                throw new RuntimeException("To project is required for an internal return.");
            }
        }
    }

    /**
     * Approving a return only debits the returning project (fromProjectCode)
     * — the exact mirror of how authorizing a GIN only debits the issuing
     * project. Nothing credits the receiving project here, for either
     * return type: a SUPPLIER return's stock leaves the org entirely (there
     * is no receiving project to credit), and an INTERNAL return's
     * receiving project must instead file a real GRN against this return
     * (see GRNService.receiveItemIntoBatches, which replays the exact
     * batch(es) this debit draws from via stock_action_allocation) —
     * exactly how it would receive stock from a GIN or a supplier. This
     * keeps a stock return consistent with every other "stock enters a
     * project's store" path in the system, all of which go through a GRN.
     */
    private void applyToStock(
            StockReturn stockReturn,
            UUID stockReturnId,
            StockReturnItem item
    ) {

        // A registered asset line moves the ONE asset and never touches the quantity stock engine.
        if (item.getAssetCode() != null && !item.getAssetCode().isBlank()) {
            applyAsset(stockReturn, stockReturnId, item.getAssetCode().trim());
            assetPackService.applySelections(
                    AssetPackService.DOC_STOCK_RETURN, stockReturnId, item.getAssetCode().trim());
            return;
        }

        if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
            return;
        }

        BigDecimal quantity = item.getQuantity();

        // Non-stock items: the user picked exactly which batch/description to return
        // (see StockReturnItem#stockBatchId) — draw from that one batch instead of blind FIFO.
        if (item.getStockBatchId() != null) {
            projectStoreService.issueFromBatch(
                    item.getStockBatchId(),
                    stockReturn.getFromProjectCode(),
                    item.getItemCode(),
                    quantity,
                    stockReturn.getReturnDate().toLocalDate(),
                    StockBatchService.ACTION_STOCK_RETURN,
                    stockReturnId,
                    item.getStockReturnItemId()
            );
            return;
        }

        projectStoreService.issueGoods(
                stockReturn.getFromProjectCode(),
                item.getItemCode(),
                quantity,
                stockReturn.getReturnDate().toLocalDate(),
                StockBatchService.ACTION_STOCK_RETURN,
                stockReturnId,
                item.getStockReturnItemId()
        );
    }

    /**
     * INTERNAL return: the asset is dispatched to the receiving project and stays IN_TRANSIT until
     * that project's GRN receives it — the same two-step as a GIN. SUPPLIER return: the asset leaves
     * the org, so it must be at the returning project and is marked Inactive (its location is left
     * alone, as new_location must be a project).
     */
    private void applyAsset(StockReturn stockReturn, UUID stockReturnId, String assetCode) {

        if (RETURN_TYPE_SUPPLIER.equals(stockReturn.getReturnType())) {
            assetLocationService.requireAssetAt(assetCode, stockReturn.getFromProjectCode());
            assetService.updateAssetStatus(assetCode, ASSET_STATUS_INACTIVE);
            return;
        }

        assetLocationService.dispatchAsset(
                assetCode,
                stockReturn.getFromProjectCode(),
                stockReturn.getToProjectCode(),
                AssetLocationService.DOC_STOCK_RETURN,
                stockReturnId,
                stockReturn.getStockReturnCode(),
                stockReturn.getReturnDate().toLocalDate(),
                stockReturn.getApprovedBy()
        );
    }

    /**
     * Logs the transporting vehicle as leaving the returning project the moment an internal return
     * is approved — the first of the two location-history entries a transfer produces (the second is
     * logged by GRNService when the receiving project's GRN is approved), same as GINService. A
     * supplier return has no receiving project, so its vehicle is only kept on the document.
     */
    private void recordVehicleDispatch(StockReturn stockReturn, UUID stockReturnId) {

        if (RETURN_TYPE_SUPPLIER.equals(stockReturn.getReturnType())
                || stockReturn.getVehicleAssetCode() == null
                || stockReturn.getVehicleAssetCode().isBlank()) {
            return;
        }

        AssetLocation location = new AssetLocation();
        location.setAssetLocationId(UUID.randomUUID());
        location.setAssetCode(stockReturn.getVehicleAssetCode());
        location.setNewLocation(stockReturn.getToProjectCode());
        location.setFromLocation(stockReturn.getFromProjectCode());
        location.setMovementType(AssetLocationService.MOVEMENT_DISPATCH);
        location.setSourceDocType(AssetLocationService.DOC_STOCK_RETURN);
        location.setSourceDocId(stockReturnId);
        location.setChangedBy(stockReturn.getApprovedBy());
        location.setChangedDate(stockReturn.getReturnDate().toLocalDate());
        location.setReason("Dispatched via stock return " + stockReturn.getStockReturnCode()
                + " to " + stockReturn.getToProjectCode());
        location.setIsActive(true);

        assetLocationService.createAssetLocation(location);
    }

    private static final String ASSET_STATUS_INACTIVE = "Inactive";
}
