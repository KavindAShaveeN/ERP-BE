package com.rr.erp.service;

import com.rr.erp.dto.AssetAtProjectResponse;
import com.rr.erp.entity.AssetLocation;
import com.rr.erp.repository.AssetLocationRepository;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AssetLocationService {

    private final AssetLocationRepository assetLocationRepository;

    public AssetLocationService(
            AssetLocationRepository assetLocationRepository) {

        this.assetLocationRepository = assetLocationRepository;
    }

    public int createAssetLocation(AssetLocation assetLocation) {
        return assetLocationRepository.createAssetLocation(assetLocation);
    }

    public List<AssetLocation> getAssetLocationsByAssetCode(
            String assetCode) {

        return assetLocationRepository
                .getAssetLocationsByAssetCode(assetCode);
    }

    public List<AssetAtProjectResponse> getAssetsAtProject(String projectCode) {
        return assetLocationRepository.findAssetsAtProject(projectCode.trim());
    }

    public List<AssetAtProjectResponse> getAssetsAtProject(String projectCode, boolean includeDisposed) {
        return assetLocationRepository.findAssetsAtProject(projectCode.trim(), includeDisposed);
    }

    public int deleteAssetLocation(UUID assetLocationId) {
        return assetLocationRepository
                .deleteAssetLocation(assetLocationId);
    }

    /**
     * Display label for an asset that is on a dispatched GIN whose GRN has not been approved yet.
     * Never stored in asset_location.new_location (that column is a foreign key to project) - the
     * in-transit state is a latest entry whose movement_type is DISPATCH.
     */
    public static final String IN_TRANSIT = "IN_TRANSIT";

    public static final String MOVEMENT_DISPATCH = "DISPATCH";
    /** An asset on a GIN that is parked at a hub project: still in transit, like DISPATCH. */
    public static final String MOVEMENT_HUB_HOLD = "HUB_HOLD";
    public static final String MOVEMENT_RECEIPT = "RECEIPT";
    public static final String MOVEMENT_ISSUE = "ISSUE";
    public static final String MOVEMENT_RETURN = "RETURN";
    public static final String MOVEMENT_ADJUSTMENT = "ADJUSTMENT";

    public static final String DOC_GIN = "GIN";
    public static final String DOC_TRANSPORT_TRIP = "TRANSPORT_TRIP";
    public static final String DOC_GRN = "GRN";
    public static final String DOC_STOCK_RETURN = "STOCK_RETURN";
    public static final String DOC_STOCK_ADJUSTMENT = "STOCK_ADJUSTMENT";
    public static final String DOC_INTRA_PROJECT_ISSUE = "INTRA_PROJECT_ISSUE";
    public static final String DOC_INTRA_PROJECT_ISSUE_RETURN = "INTRA_PROJECT_ISSUE_RETURN";
    public static final String DOC_JOB_CARD = "JOB_CARD";
    public static final String DOC_SERVICE_RECEIVE_NOTE = "SERVICE_RECEIVE_NOTE";

    /**
     * GIN authorized: the asset leaves {@code fromProject}. It must currently be there (per its
     * latest location entry) — this is what stops the same asset being dispatched twice or from
     * a project it is not at. It becomes IN_TRANSIT until the receiving GRN is approved: the
     * DISPATCH entry records from_location = the issuing project and new_location = the receiving
     * project (a valid project for the foreign key); the DISPATCH movement type marks it as in
     * transit. With no receiving project on the GIN, new_location falls back to the issuing project.
     */
    public void dispatchAsset(String assetCode, String fromProject, String toProject,
                              UUID docUuid, String docCode, LocalDate date, String by) {
        dispatchAsset(assetCode, fromProject, toProject, DOC_GIN, docUuid, docCode, date, by);
    }

    public void dispatchAsset(String assetCode, String fromProject, String toProject, String docType,
                              UUID docUuid, String docCode, LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (isInTransit(latest)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " cannot be dispatched from " + fromProject
                            + " — it is already in transit."
            );
        }

        if (latest != null && !latest.getNewLocation().equalsIgnoreCase(fromProject)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " cannot be dispatched from " + fromProject
                            + " — its current location is " + latest.getNewLocation() + "."
            );
        }

        record(assetCode, toProject != null && !toProject.isBlank() ? toProject : fromProject, fromProject, MOVEMENT_DISPATCH, docType, docUuid, date, by, null,
                "Dispatched via " + docType + " " + docCode + (toProject != null ? " to " + toProject : ""));
    }

    /**
     * Asserts the asset is registered, at {@code project} and not in transit. Used by returns to
     * suppliers and adjustment write-offs, which change the asset's status without moving it.
     */
    public void requireAssetAt(String assetCode, String project) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (latest == null || isInTransit(latest) || !latest.getNewLocation().equalsIgnoreCase(project)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " is not at " + project + " (current location: "
                            + (latest == null ? "not recorded" : isInTransit(latest) ? IN_TRANSIT : latest.getNewLocation())
                            + ")."
            );
        }
    }

    /**
     * Stock adjustment write-off: the asset must be at {@code project}. The location is unchanged
     * (new_location is a project foreign key); an ADJUSTMENT entry records the event, and the caller
     * changes the asset's status.
     */
    public void adjustAssetOut(String assetCode, String project, UUID docUuid, String docCode,
                               LocalDate date, String by) {

        requireAssetAt(assetCode, project);

        record(assetCode, project, project, MOVEMENT_ADJUSTMENT, DOC_STOCK_ADJUSTMENT, docUuid, date, by, null,
                "Written off via stock adjustment " + docCode);
    }

    /** Stock adjustment "found": the asset is recorded as now being at {@code project}. */
    public void adjustAssetIn(String assetCode, String project, UUID docUuid, String docCode,
                              LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (isInTransit(latest) && !latest.getNewLocation().equalsIgnoreCase(project)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " is in transit to " + latest.getNewLocation() + ", not " + project + "."
            );
        }

        record(assetCode, project, latest == null ? null : latest.getNewLocation(), MOVEMENT_ADJUSTMENT,
                DOC_STOCK_ADJUSTMENT, docUuid, date, by, null,
                "Found via stock adjustment " + docCode);
    }

    /**
     * Intra-project issue/return: the asset stays at the project, only who holds it changes.
     * Issuing requires the asset to be at the project; the holder is cleared again on return.
     */
    public void assignAsset(String assetCode, String project, String movementType, String assignee,
                            String docType, UUID docUuid, String docCode, LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (latest == null || isInTransit(latest) || !latest.getNewLocation().equalsIgnoreCase(project)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " is not at " + project + " (current location: "
                            + (latest == null ? "not recorded" : isInTransit(latest) ? IN_TRANSIT : latest.getNewLocation())
                            + ")."
            );
        }

        String reason = (MOVEMENT_ISSUE.equals(movementType) ? "Issued via " : "Returned via ")
                + docType + " " + docCode;

        record(assetCode, project, project, movementType, docType, docUuid, date, by, assignee, reason);
    }

    /**
     * GRN receipt of a registered asset at {@code project}. If the asset is currently IN_TRANSIT
     * (dispatched via a linked GIN — see {@link #dispatchAsset}) this closes that transit out,
     * pairing the DISPATCH entry with a RECEIPT one at the receiving project. An asset that is
     * not in transit is simply recorded as now being at {@code project} — a GRN can also receive
     * an asset directly, with no dispatching GIN (e.g. one bought straight into this project).
     */
    public void receiveAsset(String assetCode, String project, UUID docUuid, String docCode,
                             LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (isInTransit(latest) && !latest.getNewLocation().equalsIgnoreCase(project)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " was dispatched to " + latest.getNewLocation()
                            + ", not " + project + "."
            );
        }

        String fromLocation = latest == null ? null : latest.getNewLocation();

        record(assetCode, project, fromLocation, MOVEMENT_RECEIPT, DOC_GRN, docUuid, date, by, null,
                "Received via GRN " + docCode);
    }

    /**
     * A job card marked Delivered sends the asset out of the workshop project, back toward the
     * project that requested the job — mirroring {@link #dispatchAsset} (GIN) rather than the
     * one-step {@code returnAssetFromJobCard} this replaced. The asset is IN_TRANSIT until the
     * requesting project confirms receipt with a Service Receive Note (see
     * {@link #receiveAssetForServiceReceiveNote}), just as a GIN dispatch waits on its GRN.
     */
    public void dispatchAssetFromJobCard(String assetCode, String fromProject, String toProject,
                                         UUID jobCardId, String jobCardCode, LocalDate date, String by) {

        if (assetCode == null || assetCode.isBlank() || toProject == null || toProject.isBlank()) {
            return;
        }

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (isInTransit(latest)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " cannot be dispatched from " + fromProject
                            + " — it is already in transit."
            );
        }

        if (latest != null && fromProject != null && !fromProject.isBlank()
                && !latest.getNewLocation().equalsIgnoreCase(fromProject)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " cannot be dispatched from " + fromProject
                            + " — its current location is " + latest.getNewLocation() + "."
            );
        }

        record(assetCode, toProject, fromProject, MOVEMENT_DISPATCH, DOC_JOB_CARD, jobCardId, date, by, null,
                "Dispatched from workshop via job card " + jobCardCode);
    }

    /**
     * Service Receive Note confirmation: the requesting project has physically received the
     * asset back. Closes out the job-card DISPATCH into a RECEIPT at {@code project}, mirroring
     * {@link #receiveAsset} (GRN). An asset that was never marked in transit (e.g. it never left
     * its home project) is simply recorded as received directly.
     */
    public void receiveAssetForServiceReceiveNote(String assetCode, String project, UUID srnId, String srnCode,
                                                  LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (isInTransit(latest) && !latest.getNewLocation().equalsIgnoreCase(project)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset " + assetCode + " was dispatched to " + latest.getNewLocation()
                            + ", not " + project + "."
            );
        }

        String fromLocation = latest == null ? null : latest.getNewLocation();

        record(assetCode, project, fromLocation, MOVEMENT_RECEIPT, DOC_SERVICE_RECEIVE_NOTE, srnId, date, by, null,
                "Received via Service Receive Note " + srnCode);
    }

    private static boolean isInTransit(AssetLocation latest) {
        return latest != null
                && (MOVEMENT_DISPATCH.equals(latest.getMovementType())
                || MOVEMENT_HUB_HOLD.equals(latest.getMovementType()));
    }

    /**
     * A transport trip unloads a GIN at a hub project: the asset on it stays IN_TRANSIT (a GRN at its
     * destination still closes that) but its history now shows where it is parked. new_location stays
     * the destination project so the receipt validation in {@link #receiveAsset} is unchanged;
     * from_location is the hub it is held at. An asset that is not in transit (its GIN was never
     * authorized, so it never left) is left alone.
     */
    public void holdAssetAtHub(String assetCode, String hubProject, String destinationProject,
                               UUID tripId, String tripCode, String ginCode, LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (!isInTransit(latest)) {
            return;
        }

        record(assetCode, destinationProject, hubProject, MOVEMENT_HUB_HOLD, DOC_TRANSPORT_TRIP, tripId, tripId,
                date, by, null,
                "Held at hub " + hubProject + " via trip " + tripCode + " (GIN " + ginCode
                        + ", on to " + destinationProject + ")");
    }

    /**
     * Goods held at a hub were collected by the destination's own vehicle: the asset leaves the hub for
     * its destination (DISPATCH again, so the destination's GRN receipt closes it as for any GIN). An asset
     * that is not in transit is left alone.
     */
    public void dispatchAssetFromHub(String assetCode, String hubProject, String destinationProject,
                                     UUID tripId, String tripCode, String ginCode, LocalDate date, String by) {

        AssetLocation latest = lockAndGetLatestLocation(assetCode);

        if (!isInTransit(latest)) {
            return;
        }

        record(assetCode, destinationProject, hubProject, MOVEMENT_DISPATCH, DOC_TRANSPORT_TRIP, tripId, tripId,
                date, by, null,
                "Collected from hub " + hubProject + " for GIN " + ginCode + ", on to " + destinationProject
                        + " (trip " + tripCode + ")");
    }

    /**
     * The trip vehicle's own location entry at a stop: DISPATCH when it leaves toward {@code toProject},
     * RECEIPT when it arrives at one. Mirrors the two entries a GIN/GRN pair writes for a vehicle.
     */
    public void recordVehicleMovement(String vehicleAssetCode, String newLocation, String fromLocation,
                                      String movementType, UUID tripId, String tripCode, LocalDate date,
                                      String by, String reason) {

        if (vehicleAssetCode == null || vehicleAssetCode.isBlank()) {
            return;
        }

        record(vehicleAssetCode, newLocation, fromLocation, movementType, DOC_TRANSPORT_TRIP, tripId, tripId,
                date, by, null, reason + " (trip " + tripCode + ")");
    }

    private AssetLocation lockAndGetLatestLocation(String assetCode) {

        if (!assetLocationRepository.lockAsset(assetCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Asset " + assetCode + " is not in the asset register."
            );
        }

        return assetLocationRepository.findLatestActiveLocation(assetCode).orElse(null);
    }

    private void record(String assetCode, String newLocation, String fromLocation, String movementType,
                        String docType, UUID docUuid, LocalDate date, String by, String assignee, String reason) {
        record(assetCode, newLocation, fromLocation, movementType, docType, docUuid, null, date, by, assignee, reason);
    }

    private void record(String assetCode, String newLocation, String fromLocation, String movementType,
                        String docType, UUID docUuid, UUID tripId, LocalDate date, String by, String assignee,
                        String reason) {

        AssetLocation location = new AssetLocation();
        location.setTripId(tripId);
        location.setAssetCode(assetCode);
        location.setNewLocation(newLocation);
        location.setFromLocation(fromLocation);
        location.setMovementType(movementType);
        location.setSourceDocType(docType);
        location.setSourceDocId(docUuid);
        location.setChangedBy(by);
        location.setChangedDate(date);
        location.setAssignedEmployee(assignee);
        location.setReason(reason);
        location.setIsActive(true);

        assetLocationRepository.createAssetLocation(location);
    }
}
