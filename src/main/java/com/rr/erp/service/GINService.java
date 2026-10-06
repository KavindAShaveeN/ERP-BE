package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.AssetLocation;
import com.rr.erp.repository.TransportRepository;
import com.rr.erp.entity.GIN;
import com.rr.erp.entity.GINItem;
import com.rr.erp.repository.GINRepository;
import com.rr.erp.repository.GRNRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GINService {

    private final GINRepository ginRepository;
    private final GRNRepository grnRepository;
    private final ProjectStoreService projectStoreService;
    private final AssetLocationService assetLocationService;
    private final TransportRepository transportRepository;
    private final TransportService transportService;
    private final AssetPackService assetPackService;
    private final IssuePlanningService issuePlanningService;

    public GINService(
            GINRepository ginRepository,
            GRNRepository grnRepository,
            ProjectStoreService projectStoreService,
            AssetLocationService assetLocationService,
            AssetPackService assetPackService,
            IssuePlanningService issuePlanningService,
            TransportRepository transportRepository,
            TransportService transportService
    ) {
        this.ginRepository = ginRepository;
        this.grnRepository = grnRepository;
        this.projectStoreService = projectStoreService;
        this.assetLocationService = assetLocationService;
        this.assetPackService = assetPackService;
        this.issuePlanningService = issuePlanningService;
        this.transportRepository = transportRepository;
        this.transportService = transportService;
    }

    public PagedResponse<GIN> getCreatedGin(String issuedProjectCode, int page, int size) {

        List<GIN> ginList = ginRepository.getCreatedGin(issuedProjectCode, page, size);
        long totalElements = ginRepository.countCreatedGin(issuedProjectCode);

        return new PagedResponse<>(ginList, page, size, totalElements);
    }

    public PagedResponse<GIN> getIncomingGin(String receivedProjectCode, int page, int size) {

        List<GIN> ginList = ginRepository.getIncomingGin(receivedProjectCode, page, size);
        long totalElements = ginRepository.countIncomingGin(receivedProjectCode);

        return new PagedResponse<>(ginList, page, size, totalElements);
    }

    public PagedResponse<GIN> getAllGins(int page, int size) {

        List<GIN> ginList = ginRepository.getAllGins(page, size);
        long totalElements = ginRepository.countAllGins();

        return new PagedResponse<>(ginList, page, size, totalElements);
    }

    public PagedResponse<GIN> getGinsByForAssetCode(String forAssetCode, int page, int size) {

        List<GIN> ginList = ginRepository.getGinsByForAssetCode(forAssetCode, page, size);
        long totalElements = ginRepository.countGinsByForAssetCode(forAssetCode);

        return new PagedResponse<>(ginList, page, size, totalElements);
    }

    public PagedResponse<GIN> getGinsByReceivedBy(String employeeCode, int page, int size) {

        List<GIN> ginList = ginRepository.getGinsByReceivedBy(employeeCode, page, size);
        long totalElements = ginRepository.countGinsByReceivedBy(employeeCode);

        return new PagedResponse<>(ginList, page, size, totalElements);
    }

    // Outgoing gate-pass queue: authorized GINs this project hasn't seen leave the gate yet.
    public List<GIN> getPendingExitGate(String issuedProjectCode) {
        return ginRepository.findPendingExitGate(issuedProjectCode);
    }

    // Incoming gate-pass queue: GINs dispatched to this project, not yet confirmed arriving,
    // and with no GRN raised for them yet.
    public List<GIN> getPendingArrivalGate(String receivedProjectCode) {
        return ginRepository.findPendingArrivalGate(receivedProjectCode);
    }


    @Transactional
    public GIN createGin(GIN gin) {

        issuePlanningService.validateMrLines(gin);

        UUID ginId = UUID.randomUUID();
        gin.setGinId(ginId);

        // Default authorization status
        if (gin.getIsAuthorized() == null) {
            gin.setIsAuthorized(false);
        }

        // Default gate verification status
        if (gin.getIsGateVerified() == null) {
            gin.setIsGateVerified(false);
        }

        if (gin.getIsArrivalGateVerified() == null) {
            gin.setIsArrivalGateVerified(false);
        }

        ginRepository.createGin(gin);

        if (gin.getItems() != null) {

            for (GINItem item : gin.getItems()) {

                item.setGinItemId(UUID.randomUUID());
                item.setGinId(ginId);
                ginRepository.createGinItem(item);
            }

            assetPackService.saveSelections(AssetPackService.DOC_GIN, ginId, gin.getItems());

            // Stock is only deducted once a GIN is Authorized — a newly created GIN is
            // always Pending (see IssueStockForm.tsx), so this normally does nothing at
            // create time; it stays a real check in case a GIN is ever created
            // pre-authorized by another caller.
            if (Boolean.TRUE.equals(gin.getIsAuthorized())) {
                issueItems(gin, ginId);
                issuePlanningService.onGinAuthorized(gin, ginId);
                // Approved: if everything on its transport trip is now cleared, the vehicle leaves.
                transportService.onGinCleared(ginId, gin.getApprovedBy());
            }
        }

        return gin;
    }


    @Transactional
    public GIN updateGin(UUID ginId, GIN gin) {

        GIN existing = ginRepository.findById(ginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "GIN not found: " + ginId
                ));

        issuePlanningService.validateMrLines(gin);

        // Once authorized, a GIN's stock has already been deducted and it is no longer
        // editable (see WarehouseGinFormPage.tsx, which blocks editing once isAuthorized is
        // true) — enforced here too so nothing can re-deduct stock for it a second time.
        if (Boolean.TRUE.equals(existing.getIsAuthorized())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "GIN " + ginId + " has already been authorized and can no longer be edited."
            );
        }

        // Carry over gate-verification state the incoming payload doesn't know about (e.g. a
        // create/edit form built before this field existed) instead of nulling it out.
        if (gin.getIsGateVerified() == null) {
            gin.setIsGateVerified(existing.getIsGateVerified());
        }
        if (gin.getIsArrivalGateVerified() == null) {
            gin.setIsArrivalGateVerified(existing.getIsArrivalGateVerified());
        }
        if (gin.getIssuedBy() == null) {
            gin.setIssuedBy(existing.getIssuedBy());
        }

        int updatedRows = ginRepository.updateGin(ginId, gin);

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "GIN not found: " + ginId
            );
        }

        // Replace old items with new items
        ginRepository.deleteGinItems(ginId);

        if (gin.getItems() != null) {

            for (GINItem item : gin.getItems()) {

                item.setGinItemId(UUID.randomUUID());
                item.setGinId(ginId);
                ginRepository.createGinItem(item);
            }

            assetPackService.saveSelections(AssetPackService.DOC_GIN, ginId, gin.getItems());

            // existing was confirmed not-yet-authorized above, so isAuthorized being true
            // here means this call is exactly the Pending -> Authorized transition — the
            // one moment stock should be deducted. A plain edit (still Pending) must not
            // touch project_store at all.
            if (Boolean.TRUE.equals(gin.getIsAuthorized())) {
                issueItems(gin, ginId);
                issuePlanningService.onGinAuthorized(gin, ginId);
                // Approved: if everything on its transport trip is now cleared, the vehicle leaves.
                transportService.onGinCleared(ginId, gin.getApprovedBy());
            }
        }

        gin.setGinId(ginId);

        return gin;
    }

    /**
     * Records security's gate confirmation that this GIN's goods/vehicle actually left the
     * issuing project. Deliberately bypasses updateGin's "no edits after authorization" lock
     * (see updateGin above) — the gate check is independent of the approval workflow and can
     * happen before or after a GIN is authorized, and touches nothing but the three
     * gate-verification columns. Once recorded, updateGin locks the GIN from further edits.
     */
    @Transactional
    public GIN verifyGinAtGate(UUID ginId, String gateVerifiedBy) {

        GIN existing = ginRepository.findById(ginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "GIN not found: " + ginId
                ));

        if (Boolean.TRUE.equals(existing.getIsGateVerified())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "GIN " + ginId + " has already been gate-verified."
            );
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        ginRepository.updateGateVerification(ginId, gateVerifiedBy, now);

        existing.setGateVerifiedBy(gateVerifiedBy);
        existing.setGateVerifiedDate(now);
        existing.setIsGateVerified(true);

        // Through the exit gate: if everything on its transport trip is now cleared, the vehicle leaves.
        transportService.onGinCleared(ginId, gateVerifiedBy);

        return existing;
    }

    /**
     * Records the receiving project's security confirmation that this GIN's goods/vehicle
     * arrived at their gate. Normally this happens before a GRN exists for the GIN, and
     * GRNService copies the confirmation onto the GRN's own gate fields when it's later
     * created. But if a GRN was already raised for this GIN before security got to it (or the
     * GIN was ticked directly rather than through the Gate Passes queue, which hides GINs that
     * already have a GRN), that GRN's gate fields would otherwise never get set — so push the
     * confirmation onto it here too, if one already exists and isn't already gate-verified.
     * Requires the exit gate check to already be recorded, since an asset can't arrive
     * somewhere it hasn't left from.
     */
    @Transactional
    public GIN verifyGinArrivalAtGate(UUID ginId, String gateVerifiedBy) {

        GIN existing = ginRepository.findById(ginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "GIN not found: " + ginId
                ));

        if (!Boolean.TRUE.equals(existing.getIsGateVerified())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "GIN " + ginId + " has not left the issuing project's gate yet."
            );
        }

        if (Boolean.TRUE.equals(existing.getIsArrivalGateVerified())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "GIN " + ginId + " has already been confirmed arriving at the gate."
            );
        }

        // Held at a hub: the destination is taking it straight from there (released here). Still on its
        // way to the hub: it cannot arrive at its destination yet.
        transportService.prepareReceipt(ginId, null, null, "arrival gate confirmed");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        ginRepository.updateArrivalGateVerification(ginId, gateVerifiedBy, now);

        existing.setArrivalGateVerifiedBy(gateVerifiedBy);
        existing.setArrivalGateVerifiedDate(now);
        existing.setIsArrivalGateVerified(true);

        // The destination's gate saw it arrive: the trip's vehicle is at that stop.
        transportService.onGinArrivedAtGate(ginId);

        grnRepository.findByGinId(ginId).ifPresent(grn -> {
            if (!Boolean.TRUE.equals(grn.getIsGateVerified())) {
                grnRepository.updateGateVerification(grn.getGrnId(), gateVerifiedBy, now);
            }
        });

        return existing;
    }

    private void issueItems(GIN gin, UUID ginId) {

        for (GINItem item : gin.getItems()) {

            // A line carrying an asset code moves one specific registered asset: it leaves the
            // issuing project as IN_TRANSIT (validated against its location history) and never
            // touches the quantity stock engine.
            if (item.getAssetCode() != null && !item.getAssetCode().isBlank()) {
                issueAsset(gin, ginId, item.getAssetCode().trim());
                assetPackService.applySelections(AssetPackService.DOC_GIN, ginId, item.getAssetCode().trim());
                continue;
            }

            // Non-stock items: the user picked exactly which batch/description to draw from
            // (see GINItem#stockBatchId) — draw from that one batch instead of blind FIFO.
            if (item.getStockBatchId() != null) {
                projectStoreService.issueFromBatch(
                        item.getStockBatchId(),
                        gin.getIssuedProjectCode(),
                        item.getItemCode(),
                        item.getQuantity(),
                        gin.getIssuedDate().toLocalDate(),
                        StockBatchService.ACTION_GIN,
                        ginId,
                        item.getGinItemId()
                );
                continue;
            }

            // Dimensional items (see com.rr.erp.util.DimensionalItems) carry which exact
            // size the store is drawing down from (e.g. the 6m bars, not the 1m offcuts) —
            // lengthM/widthM are null for non-dimensional items, so this scopes FIFO only
            // when relevant and otherwise behaves exactly as before.
            projectStoreService.issueGoods(
                    gin.getIssuedProjectCode(),
                    item.getItemCode(),
                    item.getQuantity(),
                    gin.getIssuedDate().toLocalDate(),
                    StockBatchService.ACTION_GIN,
                    ginId,
                    item.getGinItemId(),
                    item.getLengthM(),
                    item.getWidthM()
            );
        }

        recordVehicleDispatch(gin);
    }

    /**
     * A GIN with a receiving project is a transfer: the asset goes IN_TRANSIT until that
     * project's GRN is approved. A GIN with no receiving project (personal / loan issues) has
     * no GRN to close it, so the asset stays at the issuing project and is recorded as held by
     * the person it was issued to.
     */
    private void issueAsset(GIN gin, UUID ginId, String assetCode) {

        boolean hasReceivingProject = gin.getReceivedProjectCode() != null
                && !gin.getReceivedProjectCode().isBlank();

        if (hasReceivingProject) {
            assetLocationService.dispatchAsset(
                    assetCode,
                    gin.getIssuedProjectCode(),
                    gin.getReceivedProjectCode(),
                    ginId,
                    gin.getGinCode(),
                    gin.getIssuedDate().toLocalDate(),
                    gin.getApprovedBy()
            );
            return;
        }

        String holder = gin.getReceiverName() != null && !gin.getReceiverName().isBlank()
                ? gin.getReceiverName()
                : gin.getReceivedPerson();

        if (holder == null || holder.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Asset " + assetCode + " needs a receiving project or a receiving person on the GIN."
            );
        }

        assetLocationService.assignAsset(
                assetCode,
                gin.getIssuedProjectCode(),
                AssetLocationService.MOVEMENT_ISSUE,
                holder,
                AssetLocationService.DOC_GIN,
                ginId,
                gin.getGinCode(),
                gin.getIssuedDate().toLocalDate(),
                gin.getApprovedBy()
        );
    }

    /**
     * Logs the dispatching vehicle as still at the issuing project the moment its GIN is
     * authorized — the first of the two location-history entries a transfer produces (the
     * second is logged by GRNService when the receiving project's GRN is approved).
     */
    private void recordVehicleDispatch(GIN gin) {

        if (gin.getVehicleAssetCode() == null || gin.getVehicleAssetCode().isBlank()) {
            return;
        }

        // A GIN on a transport trip: the trip writes the vehicle's location history (one entry per
        // stop) instead of one entry per GIN.
        if (gin.getGinId() != null && transportRepository.hasTripAllocation(gin.getGinId())) {
            return;
        }

        String toProject = gin.getReceivedProjectCode() != null && !gin.getReceivedProjectCode().isBlank()
                ? gin.getReceivedProjectCode()
                : gin.getIssuedProjectCode();

        AssetLocation location = new AssetLocation();
        location.setAssetLocationId(UUID.randomUUID());
        location.setAssetCode(gin.getVehicleAssetCode());
        location.setNewLocation(toProject);
        location.setFromLocation(gin.getIssuedProjectCode());
        location.setMovementType(AssetLocationService.MOVEMENT_DISPATCH);
        location.setSourceDocType(AssetLocationService.DOC_GIN);
        location.setSourceDocId(gin.getGinId());
        location.setChangedBy(gin.getApprovedBy());
        location.setChangedDate(gin.getIssuedDate().toLocalDate());
        location.setReason(
                "Dispatched via GIN " + gin.getGinCode()
                        + (gin.getReceivedProjectCode() != null ? " to " + gin.getReceivedProjectCode() : "")
        );
        location.setIsActive(true);

        assetLocationService.createAssetLocation(location);
    }
}