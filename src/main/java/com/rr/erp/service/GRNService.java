package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.AssetLocation;
import com.rr.erp.entity.GRN;
import com.rr.erp.entity.GRNItem;
import com.rr.erp.entity.POItem;
import com.rr.erp.entity.ReceivedService;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.repository.GINRepository;
import com.rr.erp.repository.GRNRepository;
import com.rr.erp.repository.PORepository;
import com.rr.erp.repository.ReceivedServiceRepository;
import com.rr.erp.repository.ServiceItemRepository;
import com.rr.erp.repository.StockReturnRepository;
import com.rr.erp.util.DimensionalItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GRNService {

    private static final Logger log = LoggerFactory.getLogger(GRNService.class);

    // Matches service_item_schema.sql / ServiceItemRepository -- a GRN line against a
    // registered Service item code carries no quantity/UOM tracking, so it is routed to
    // receiveServiceItem instead of the batch stock engine.
    private final GRNRepository grnRepository;
    private final PORepository poRepository;
    private final GINRepository ginRepository;
    private final StockReturnRepository stockReturnRepository;
    private final ProjectStoreService projectStoreService;
    private final StockBatchService stockBatchService;
    private final AssetLocationService assetLocationService;
    private final AssetPackService assetPackService;
    private final ServiceItemRepository serviceItemRepository;
    private final ReceivedServiceRepository receivedServiceRepository;
    private final TransportService transportService;

    public GRNService(
            GRNRepository grnRepository,
            PORepository poRepository,
            GINRepository ginRepository,
            StockReturnRepository stockReturnRepository,
            ProjectStoreService projectStoreService,
            StockBatchService stockBatchService,
            AssetLocationService assetLocationService,
            AssetPackService assetPackService,
            ServiceItemRepository serviceItemRepository,
            ReceivedServiceRepository receivedServiceRepository,
            TransportService transportService
    ) {
        this.grnRepository = grnRepository;
        this.poRepository = poRepository;
        this.ginRepository = ginRepository;
        this.stockReturnRepository = stockReturnRepository;
        this.projectStoreService = projectStoreService;
        this.stockBatchService = stockBatchService;
        this.assetLocationService = assetLocationService;
        this.assetPackService = assetPackService;
        this.serviceItemRepository = serviceItemRepository;
        this.receivedServiceRepository = receivedServiceRepository;
        this.transportService = transportService;
    }

    @Transactional
    public GRN createGRN(GRN grn) {

        // A GIN held at a hub can be received directly (the hold is released, with this GRN's vehicle);
        // one still on its way to the hub cannot.
        if (grn.getGinId() != null) {
            String vehicle = grn.getVehicleNo() != null && !grn.getVehicleNo().isBlank()
                    ? grn.getVehicleNo() : grn.getVehicleAssetCode();
            transportService.prepareReceipt(grn.getGinId(), vehicle, grn.getCheckedBy(),
                    "GRN " + (grn.getGrnCode() != null ? grn.getGrnCode() : ""));
        }

        validateGRN(grn);
        applyPoUomConversions(grn);
        calculateAmounts(grn);
        applyArrivalGateVerification(grn);

        // Assigned up front (instead of inside grnRepository.insertGRN) so it's
        // available below as the batch source_id.
        grn.setGrnId(UUID.randomUUID());

        try {
            GRN created = grnRepository.insertGRN(grn);

            // A GRN at the destination means the vehicle is there and the goods are unloaded.
            transportService.onGrnCreated(grn.getGinId());

            assetPackService.saveSelections(AssetPackService.DOC_GRN, grn.getGrnId(), grn.getItems());

            // Stock is only credited once a GRN is Approved — a newly created GRN is always
            // Pending (see GoodReceiveFormPage.tsx), so this normally does nothing at create
            // time; it stays a real check (rather than being dropped) in case a GRN is ever
            // created pre-approved by another caller.
            if (Boolean.TRUE.equals(grn.getIsApproved())) {
                for (GRNItem item : grn.getItems()) {

                    receiveItem(grn, grn.getGrnId(), item);
                }
                recordVehicleArrival(grn);
                transportService.onGinReceived(grn.getGinId(), grn.getFromProjectCode(), grn.getApprovedBy());
            }

            return created;

        } catch (DataIntegrityViolationException exception) {
            log.error(
                    "Failed to create GRN {}: {}",
                    grn.getGrnCode(),
                    exception.getMostSpecificCause().getMessage(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to create GRN. Check project, supplier, "
                            + "employee, item code and UOM values.",
                    exception
            );
        }
    }

    public PagedResponse<GRN> getByFromProjectCode(
            String fromProjectCode,
            int page,
            int size
    ) {

        String trimmedCode = fromProjectCode.trim();

        List<GRN> grns = grnRepository.findByFromProjectCode(trimmedCode, page, size);
        long totalElements = grnRepository.countByFromProjectCode(trimmedCode);

        return new PagedResponse<>(grns, page, size, totalElements);
    }

    public PagedResponse<GRN> getByToProjectCode(
            String toProjectCode,
            int page,
            int size
    ) {

        String trimmedCode = toProjectCode.trim();

        List<GRN> grns = grnRepository.findByToProjectCode(trimmedCode, page, size);
        long totalElements = grnRepository.countByToProjectCode(trimmedCode);

        return new PagedResponse<>(grns, page, size, totalElements);
    }

    public PagedResponse<GRN> getSupplierGRNs(int page, int size) {

        List<GRN> grns = grnRepository.findSupplierGRNs(page, size);
        long totalElements = grnRepository.countSupplierGRNs();

        return new PagedResponse<>(grns, page, size, totalElements);
    }

    public PagedResponse<GRN> getAllGrns(int page, int size) {

        List<GRN> grns = grnRepository.findAll(page, size);
        long totalElements = grnRepository.countAll();

        return new PagedResponse<>(grns, page, size, totalElements);
    }

    @Transactional
    public GRN updateGRN(UUID grnId, GRN grn) {

        GRN existing = grnRepository.findById(grnId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "GRN not found: " + grnId
                ));

        // Once approved, a GRN's stock has already been credited and its items are no longer
        // editable (see GrnEditFormPage.tsx, which hides the edit form once isApproved is
        // true) — enforced here too so nothing can re-credit stock for it a second time.
        if (Boolean.TRUE.equals(existing.getIsApproved())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "GRN " + grnId + " has already been approved and can no longer be edited."
            );
        }

        validateGRN(grn);
        applyPoUomConversions(grn);
        calculateAmounts(grn);

        try {
            int updatedRows = grnRepository.updateGRN(grnId, grn);

            if (updatedRows == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "GRN not found: " + grnId
                );
            }

            grnRepository.deleteItemsByGrnId(grnId);
            grnRepository.insertItems(grnId, grn.getItems());
            assetPackService.saveSelections(AssetPackService.DOC_GRN, grnId, grn.getItems());

            // existing was confirmed not-yet-approved above, so isApproved being true here
            // means this call is exactly the Pending -> Approved transition — the one moment
            // stock should be credited. A plain edit (still Pending, or rejected) must not
            // touch project_store at all.
            if (Boolean.TRUE.equals(grn.getIsApproved())) {
                for (GRNItem item : grn.getItems()) {

                    receiveItem(grn, grnId, item);
                }
                recordVehicleArrival(grn);
                transportService.onGinReceived(grn.getGinId(), grn.getFromProjectCode(), grn.getApprovedBy());
            }

            return grnRepository.findById(grnId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "GRN not found after update"
                    ));

        } catch (DataIntegrityViolationException exception) {
            log.error(
                    "Failed to update GRN {}: {}",
                    grnId,
                    exception.getMostSpecificCause().getMessage(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to update GRN. Check the supplied values.",
                    exception
            );
        }
    }

    /**
     * A GRN for an internal transfer/return normally has no separate gate check of its own —
     * the receiving project's security already confirmed the goods arriving at their gate
     * against the source GIN/return before this GRN was ever raised (see
     * GINService#verifyGinArrivalAtGate / StockReturnService#verifyStockReturnArrivalAtGate).
     * If the GRN itself wasn't already gate-verified some other way, copy that confirmation
     * across so the GRN displays it without security having to re-verify.
     */
    private void applyArrivalGateVerification(GRN grn) {

        if (Boolean.TRUE.equals(grn.getIsGateVerified())) {
            return;
        }

        if (grn.getGinId() != null) {
            ginRepository.findById(grn.getGinId()).ifPresent(gin -> {
                if (Boolean.TRUE.equals(gin.getIsArrivalGateVerified())) {
                    grn.setGateVerifiedBy(gin.getArrivalGateVerifiedBy());
                    grn.setGateVerifiedDate(gin.getArrivalGateVerifiedDate());
                    grn.setIsGateVerified(true);
                }
            });
        } else if (grn.getStockReturnId() != null) {
            stockReturnRepository.findById(grn.getStockReturnId()).ifPresent(stockReturn -> {
                if (Boolean.TRUE.equals(stockReturn.getIsArrivalGateVerified())) {
                    grn.setGateVerifiedBy(stockReturn.getArrivalGateVerifiedBy());
                    grn.setGateVerifiedDate(stockReturn.getArrivalGateVerifiedDate());
                    grn.setIsGateVerified(true);
                }
            });
        }
    }

    private void validateGRN(GRN grn) {

        if (Boolean.TRUE.equals(grn.getIsSupplierGRN())) {

            boolean hasSupplierCode = grn.getSupplierCode() != null && !grn.getSupplierCode().isBlank();

            if (!hasSupplierCode) {
                // A blank supplier code is only valid for a GRN raised against a genuine
                // multiple-supplier PO (see PO.supplierCode / PurchaseOrderFormPage's
                // isMultiSupplier, which is derived the same way) — every line must then carry
                // its own supplier name instead of one GRN-level supplier.
                validateMultiSupplierGrn(grn);
            }

            // fromProjectCode is kept (not nulled) even for a supplier GRN: it is the project
            // that filed/received the GRN, and the "created GRNs" list for a project is
            // queried by this column — nulling it here made every supplier GRN invisible to
            // the project that actually raised it.

        } else {

            if (grn.getFromProjectCode() == null
                    || grn.getFromProjectCode().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "From project code is required for an internal GRN"
                );
            }

            grn.setSupplierCode(null);
        }
    }

    /**
     * A supplier GRN with no header-level supplier code is only ever valid when it's raised
     * against a PO that was itself created as a multiple-supplier PO (po.supplier_code blank —
     * see PurchaseOrderFormPage's isMultiSupplier). Every line must then name its own supplier,
     * mirroring how that PO's own items each carry a supplierName instead of one PO-level
     * supplier.
     */
    private void validateMultiSupplierGrn(GRN grn) {

        if (grn.getPoCode() == null || grn.getPoCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier code is required for a supplier GRN, unless it is linked to a "
                            + "multiple-supplier purchase order."
            );
        }

        String poSupplierCode = poRepository.findSupplierCodeByPoCode(grn.getPoCode());

        if (poSupplierCode != null && !poSupplierCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supplier code is required for a supplier GRN — PO " + grn.getPoCode()
                            + " has a single supplier."
            );
        }

        for (GRNItem item : grn.getItems()) {
            if (item.getSupplierName() == null || item.getSupplierName().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Item " + item.getItemCode() + " needs a supplier name — every line on a "
                                + "multiple-supplier GRN must name its own supplier."
                );
            }
        }
    }

    /**
     * When a GRN line is against a PO (grn.poCode set) and the line's UOM differs from what
     * that PO line was ordered in (e.g. PO'd in TON, received into stores as NOS or BAG), the
     * clerk must supply a conversion factor ("1 unit of the PO's UOM = conversionFactor units
     * of this line's UOM"). The equivalent quantity in the PO's UOM is computed and persisted
     * alongside it, giving a permanent audit trail of how the stores quantity was reached. When
     * the UOMs already match (the common case), the factor defaults to 1 and the equivalent
     * quantity is just the received quantity itself.
     */
    private void applyPoUomConversions(GRN grn) {

        if (grn.getPoCode() == null || grn.getPoCode().isBlank()) {
            return;
        }

        for (GRNItem item : grn.getItems()) {

            POItem poItem = poRepository.findPOItemByPoCodeAndItemCode(
                    grn.getPoCode(), item.getItemCode());

            if (poItem == null || poItem.getUomId() == null) {
                continue;
            }

            if (poItem.getUomId().equals(item.getUomId())) {
                item.setConversionFactor(BigDecimal.ONE);
                item.setPoEquivalentQty(item.getQuantity());
                continue;
            }

            if (item.getConversionFactor() == null
                    || item.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A conversion factor is required for item " + item.getItemCode()
                                + " because it is being received in a different UOM than its "
                                + "PO line."
                );
            }

            item.setPoEquivalentQty(
                    item.getQuantity().divide(item.getConversionFactor(), 6, RoundingMode.HALF_UP)
            );
        }
    }

    private void calculateAmounts(GRN grn) {

        for (GRNItem item : grn.getItems()) {

            // Null for internal GRNs created directly from a GIN, which have no purchase
            // price (see GRNItem.unitPrice) — nothing to unbox, so there's no amount either.
            if (item.getUnitPrice() == null) {
                continue;
            }

            BigDecimal amount = item.getQuantity()
                    .multiply(BigDecimal.valueOf(item.getUnitPrice()));

            item.setAmount(amount);
        }
    }

    /**
     * A line carrying an asset code receives one specific registered asset (for a GRN tied to a
     * GIN, it must be one that GIN dispatched). It records the asset's location history via
     * {@link AssetLocationService#receiveAsset} and never touches the quantity stock engine.
     * Every other line goes through {@link #receiveItemIntoBatches}.
     */
    private void receiveItem(GRN grn, UUID grnId, GRNItem item) {

        if (item.getAssetCode() == null || item.getAssetCode().isBlank()) {

            // A Service item code (see service_item table) is never physical stock — route it
            // to the received-services log instead of the quantity stock engine, before the
            // GIN-dispatched-as-assets check below, which only ever applies to real stock.
            if (serviceItemRepository.existsByItemCodeCode(item.getItemCode())) {
                receiveServiceItem(grn, grnId, item);
                return;
            }

            // The paired GIN sent this item as tracked assets (never deducted from quantity
            // stock), so receiving it as plain quantity would invent stock and leave the
            // assets stuck in transit — the line must name the asset it is receiving.
            if (grn.getGinId() != null) {
                boolean sentAsAssets = ginRepository.getGinItems(grn.getGinId()).stream()
                        .anyMatch(ginItem -> ginItem.getAssetCode() != null
                                && !ginItem.getAssetCode().isBlank()
                                && ginItem.getItemCode() != null
                                && ginItem.getItemCode().equalsIgnoreCase(item.getItemCode()));

                if (sentAsAssets) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Item " + item.getItemCode() + " was dispatched on the linked GIN as tracked "
                                    + "assets — each GRN line must carry the asset code being received."
                    );
                }
            }

            receiveItemIntoBatches(grn, grnId, item);
            return;
        }

        String assetCode = item.getAssetCode().trim();

        if (grn.getGinId() != null) {
            boolean onGin = ginRepository.getGinItems(grn.getGinId()).stream()
                    .anyMatch(ginItem -> assetCode.equalsIgnoreCase(ginItem.getAssetCode()));

            if (!onGin) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Asset " + assetCode + " was not dispatched on the GIN linked to this GRN."
                );
            }
        }

        if (grn.getStockReturnId() != null) {
            boolean onReturn = stockReturnRepository.getItems(grn.getStockReturnId()).stream()
                    .anyMatch(returnItem -> assetCode.equalsIgnoreCase(returnItem.getAssetCode()));

            if (!onReturn) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Asset " + assetCode + " was not sent on the stock return linked to this GRN."
                );
            }
        }

        assetLocationService.receiveAsset(
                assetCode,
                grn.getFromProjectCode(),
                grnId,
                grn.getGrnCode(),
                grn.getGrnDate(),
                grn.getApprovedBy()
        );

        assetPackService.applySelections(AssetPackService.DOC_GRN, grnId, assetCode);
    }

    /**
     * A Service item code line (see service_item table / ServiceItemRepository) represents a
     * service rendered against a PO, not physical stock — it must never open a project_store
     * batch. Logged instead in received_service, purely as a receipt trail (cost, date, linked
     * PO, supplier) for the "Received services" page — see ReceivedServiceController.
     */
    private void receiveServiceItem(GRN grn, UUID grnId, GRNItem item) {

        ReceivedService receivedService = new ReceivedService();
        receivedService.setGrnId(grnId);
        receivedService.setGrnItemId(item.getGrnItemId());
        receivedService.setItemCode(item.getItemCode());
        receivedService.setDescription(item.getDescription());
        receivedService.setQuantity(item.getQuantity());
        receivedService.setUomId(item.getUomId());
        receivedService.setUnitPrice(item.getUnitPrice());
        receivedService.setAmount(item.getAmount());
        receivedService.setProjectCode(grn.getFromProjectCode());
        receivedService.setPoCode(grn.getPoCode());
        // A multi-supplier GRN carries no header-level supplier code — fall back to the line's
        // own supplier name (free text, same as POItem.supplierName) so the service still
        // shows a provider on the Received services page.
        receivedService.setSupplierCode(
                grn.getSupplierCode() != null && !grn.getSupplierCode().isBlank()
                        ? grn.getSupplierCode()
                        : item.getSupplierName()
        );
        receivedService.setServiceDate(grn.getGrnDate());
        receivedService.setReceivedBy(grn.getApprovedBy());

        receivedServiceRepository.insert(receivedService);
    }

    /**
     * Receives one GRN line into the stock of the project that is issuing/filing this GRN
     * (grn.fromProjectCode) — the store physically taking the goods in, regardless of what
     * toProjectCode says (for a supplier GRN that's fixed to HQ purely as a display/reporting
     * field, not the crediting target). A supplier GRN opens a brand-new batch, priced at the
     * line's own unit price — this is the only genuine entry point for new stock into the
     * system. An internal GRN (a transfer from another project's store, or a stock return
     * coming back) instead replays the exact batch(es) the paired GIN or stock return drew
     * from at the source project onto the receiver — same batch identity, same original cost,
     * no new batch and no blending. The GRN's own received quantity is what gets credited,
     * not however much the source action moved — a transfer can arrive short (breakage,
     * transit loss), and only what actually arrived should land in the receiver's stock; the
     * shortfall is simply not credited anywhere. If the GRN instead reports more than the
     * source ever moved for this item, the excess is credited as a new batch (no batch
     * identity to replay for it). If that GIN/stock-return link is missing or recorded
     * nothing for this item (shouldn't normally happen), it falls back to opening a new batch
     * at the item's most recently known cost.
     */
    private void receiveItemIntoBatches(GRN grn, UUID grnId, GRNItem item) {

        if (Boolean.TRUE.equals(grn.getIsSupplierGRN())) {

            BigDecimal unitCost = item.getUnitPrice() != null
                    ? BigDecimal.valueOf(item.getUnitPrice())
                    : BigDecimal.ZERO;

            if (DimensionalItems.isDimensionalItem(item.getItemCode())) {
                // quantity IS the piece/bar count for these items (existing business
                // convention — GRN/PO already work in bars).
                projectStoreService.receiveNewBatch(
                        grn.getFromProjectCode(),
                        item.getItemCode(),
                        item.getQuantity(),
                        grn.getGrnDate(),
                        unitCost,
                        StockBatchService.SOURCE_SUPPLIER_GRN,
                        grnId,
                        grn.getGrnCode(),
                        item.getLengthM(),
                        item.getWidthM(),
                        item.getQuantity().intValue(),
                        null,
                        item.getExpiryDate()
                );
                return;
            }

            projectStoreService.receiveNewBatch(
                    grn.getFromProjectCode(),
                    item.getItemCode(),
                    item.getQuantity(),
                    grn.getGrnDate(),
                    unitCost,
                    StockBatchService.SOURCE_SUPPLIER_GRN,
                    grnId,
                    grn.getGrnCode(),
                    null,
                    null,
                    null,
                    item.getDescription(),
                    item.getExpiryDate()
            );
            return;
        }

        List<StockActionAllocation> sourceAllocations;
        if (grn.getGinId() != null) {
            sourceAllocations = stockBatchService.getAllocationsForActionAndItem(
                    StockBatchService.ACTION_GIN, grn.getGinId(), item.getItemCode());
        } else if (grn.getStockReturnId() != null) {
            sourceAllocations = stockBatchService.getAllocationsForActionAndItem(
                    StockBatchService.ACTION_STOCK_RETURN, grn.getStockReturnId(), item.getItemCode());
        } else {
            sourceAllocations = List.of();
        }

        if (!sourceAllocations.isEmpty()) {

            BigDecimal issuedQty = sourceAllocations.stream()
                    .map(StockActionAllocation::getQtyTaken)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal receivedQty = item.getQuantity();

            List<StockActionAllocation> toCredit = receivedQty.compareTo(issuedQty) < 0
                    ? trimAllocationsToQuantity(sourceAllocations, receivedQty)
                    : sourceAllocations;

            projectStoreService.creditExistingBatches(
                    grn.getFromProjectCode(),
                    item.getItemCode(),
                    grn.getGrnDate(),
                    toCredit
            );

            BigDecimal excess = receivedQty.subtract(issuedQty);
            if (excess.compareTo(BigDecimal.ZERO) > 0) {
                if (DimensionalItems.isDimensionalItem(item.getItemCode())) {
                    projectStoreService.receiveNewBatch(
                            grn.getFromProjectCode(),
                            item.getItemCode(),
                            excess,
                            grn.getGrnDate(),
                            stockBatchService.mostRecentUnitCost(item.getItemCode()),
                            StockBatchService.SOURCE_INTERNAL_GRN,
                            grnId,
                            grn.getGrnCode(),
                            item.getLengthM(),
                            item.getWidthM(),
                            excess.intValue(),
                            null,
                            item.getExpiryDate()
                    );
                } else {
                    projectStoreService.receiveNewBatch(
                            grn.getFromProjectCode(),
                            item.getItemCode(),
                            excess,
                            grn.getGrnDate(),
                            stockBatchService.mostRecentUnitCost(item.getItemCode()),
                            StockBatchService.SOURCE_INTERNAL_GRN,
                            grnId,
                            grn.getGrnCode(),
                            null,
                            null,
                            null,
                            item.getDescription(),
                            item.getExpiryDate()
                    );
                }
            }
            return;
        }

        if (DimensionalItems.isDimensionalItem(item.getItemCode())) {
            projectStoreService.receiveNewBatch(
                    grn.getFromProjectCode(),
                    item.getItemCode(),
                    item.getQuantity(),
                    grn.getGrnDate(),
                    stockBatchService.mostRecentUnitCost(item.getItemCode()),
                    StockBatchService.SOURCE_INTERNAL_GRN,
                    grnId,
                    grn.getGrnCode(),
                    item.getLengthM(),
                    item.getWidthM(),
                    item.getQuantity().intValue(),
                    null,
                    item.getExpiryDate()
            );
            return;
        }

        projectStoreService.receiveNewBatch(
                grn.getFromProjectCode(),
                item.getItemCode(),
                item.getQuantity(),
                grn.getGrnDate(),
                stockBatchService.mostRecentUnitCost(item.getItemCode()),
                StockBatchService.SOURCE_INTERNAL_GRN,
                grnId,
                grn.getGrnCode(),
                null,
                null,
                null,
                item.getDescription(),
                item.getExpiryDate()
        );
    }

    /**
     * Caps a set of source batch allocations (already ordered oldest-batch-first by the
     * repository) down to {@code quantity}, taking whole batches off the front and, if needed,
     * a partial amount from the last one touched. Used when a GRN reports receiving less than
     * its paired GIN issued, so only the batches actually received get replayed onto the
     * receiving project — never the full amount that was sent.
     */
    private List<StockActionAllocation> trimAllocationsToQuantity(
            List<StockActionAllocation> allocations, BigDecimal quantity) {

        List<StockActionAllocation> trimmed = new ArrayList<>();
        BigDecimal remaining = quantity;

        for (StockActionAllocation allocation : allocations) {

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal take = allocation.getQtyTaken().min(remaining);

            trimmed.add(new StockActionAllocation(
                    allocation.getStockActionAllocationId(),
                    allocation.getStockBatchId(),
                    allocation.getActionType(),
                    allocation.getActionId(),
                    allocation.getActionItemId(),
                    allocation.getItemCode(),
                    take,
                    allocation.getUnitCost()
            ));

            remaining = remaining.subtract(take);
        }

        return trimmed;
    }

    /**
     * Logs the transporting vehicle's arrival at the project actually receiving the goods
     * (see {@link #receiveItemIntoBatches} — grn.fromProjectCode is that project regardless
     * of GRN type) the moment this GRN is approved — the second of the two location-history
     * entries a transfer produces, pairing with {@code GINService#recordVehicleDispatch}.
     */
    private void recordVehicleArrival(GRN grn) {

        if (grn.getVehicleAssetCode() == null || grn.getVehicleAssetCode().isBlank()) {
            return;
        }

        // A GIN carried on a transport trip: the trip already logged the vehicle's arrival at each stop.
        if (transportService.carriedOnTrip(grn.getGinId())) {
            return;
        }

        AssetLocation location = new AssetLocation();
        location.setAssetLocationId(UUID.randomUUID());
        location.setAssetCode(grn.getVehicleAssetCode());
        location.setNewLocation(grn.getFromProjectCode());
        location.setChangedBy(grn.getApprovedBy());
        location.setChangedDate(grn.getGrnDate());
        location.setReason("Received via GRN " + grn.getGrnCode());
        location.setIsActive(true);

        assetLocationService.createAssetLocation(location);
    }
}