package com.rr.erp.service;

import com.rr.erp.dto.TransportDtos.ActionRequest;
import com.rr.erp.dto.TransportDtos.AddVehicleRequest;
import com.rr.erp.dto.TransportDtos.AvailableGin;
import com.rr.erp.dto.TransportDtos.CollectRequest;
import com.rr.erp.dto.TransportDtos.CustodyEvent;
import com.rr.erp.dto.TransportDtos.GinInput;
import com.rr.erp.dto.TransportDtos.GinReceiptVehicle;
import com.rr.erp.dto.TransportDtos.GinTripInfo;
import com.rr.erp.dto.TransportDtos.HeldGin;
import com.rr.erp.dto.TransportDtos.SiteHistory;
import com.rr.erp.dto.TransportDtos.SiteVisit;
import com.rr.erp.dto.TransportDtos.VisitGin;
import com.rr.erp.dto.TransportDtos.IncomingDelivery;
import com.rr.erp.dto.TransportDtos.IncomingStop;
import com.rr.erp.dto.TransportDtos.StopInput;
import com.rr.erp.dto.TransportDtos.StopTask;
import com.rr.erp.dto.TransportDtos.StopTaskGin;
import com.rr.erp.dto.TransportDtos.StopView;
import com.rr.erp.dto.TransportDtos.TripGinView;
import com.rr.erp.dto.TransportDtos.TripRequest;
import com.rr.erp.dto.TransportDtos.TripSummary;
import com.rr.erp.dto.TransportDtos.TripView;
import com.rr.erp.dto.TransportDtos.UndeliveredResult;
import com.rr.erp.dto.TransportDtos.UpdateVehicleRequest;
import com.rr.erp.dto.TransportDtos.VehicleCandidate;
import com.rr.erp.dto.TransportDtos.VehicleView;
import com.rr.erp.repository.TransportRepository;
import com.rr.erp.repository.TransportRepository.AllocationRow;
import com.rr.erp.repository.TransportRepository.HubArrivalRow;
import com.rr.erp.repository.TransportRepository.IncomingRow;
import com.rr.erp.repository.TransportRepository.StopRow;
import com.rr.erp.repository.TransportRepository.StopTaskRow;
import com.rr.erp.repository.TransportRepository.TripRow;
import com.rr.erp.repository.TransportRepository.VisitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Transport plan: the vehicle pool, trips (a vehicle run through ordered stops), the allocation of GINs
 * to trips and their custody trail. A GIN is either delivered DIRECT to its destination or goes VIA_HUB:
 * unloaded at a hub project that only holds it (no GRN, no stock movement) until a later local trip
 * carries it to the destination, where the normal arrival gate check and GRN happen. See
 * db/transport_plan_schema.sql.
 */
@Service
public class TransportService {

    public static final String MODE_DIRECT = "DIRECT";
    public static final String MODE_VIA_HUB = "VIA_HUB";

    public static final String STOP_HUB = "HUB";
    public static final String STOP_SITE = "SITE";
    public static final String STOP_BASE = "RETURN_TO_BASE";

    public static final String TRIP_PLANNED = "PLANNED";
    public static final String TRIP_IN_TRANSIT = "IN_TRANSIT";
    public static final String TRIP_RETURNING = "RETURNING";
    public static final String TRIP_COMPLETED = "COMPLETED";
    public static final String TRIP_CANCELLED = "CANCELLED";

    private static final Set<String> PLANNER_PROJECT_TYPES = Set.of("head quarters", "warehouse");

    private static final Logger log = LoggerFactory.getLogger(TransportService.class);

    private final TransportRepository repository;
    private final AssetLocationService assetLocationService;

    public TransportService(TransportRepository repository, AssetLocationService assetLocationService) {
        this.repository = repository;
        this.assetLocationService = assetLocationService;
    }

    // ================================================================ access

    /** Only Head Quarters and Warehouse projects plan transport; every other project sees tracking only. */
    public void requirePlanner(String actingProjectCode) {

        if (actingProjectCode == null || actingProjectCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "An active project is required to plan transport.");
        }

        boolean allowed = repository.findProjectTypeName(actingProjectCode.trim())
                .map(type -> PLANNER_PROJECT_TYPES.contains(type.trim().toLowerCase()))
                .orElse(false);

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Head Quarters or Warehouse projects can plan transport."
            );
        }
    }

    /** An employee code only if it exists — the audit columns are foreign keys to employee. */
    private String safeEmployee(String employeeCode) {
        return repository.employeeExists(employeeCode) ? employeeCode : null;
    }

    /**
     * Who the vehicle's location history credits with a movement. asset_location.changed_by is mandatory
     * and must be a real employee, but automatic steps (the vehicle leaving once every GIN is gate-cleared,
     * arrivals confirmed by a gate pass) have no planner clicking a button — so they fall back to whoever
     * triggered them, and failing that to the planner who created the trip.
     */
    private String movementActor(TripRow trip, String actor) {
        if (actor != null && !actor.isBlank() && repository.employeeExists(actor)) {
            return actor;
        }
        return trip.createdBy();
    }

    // ================================================================ vehicle pool

    public List<VehicleView> getVehicles(String actingProjectCode) {
        requirePlanner(actingProjectCode);
        return repository.findVehicles(null);
    }

    public List<VehicleCandidate> getVehicleCandidates(String actingProjectCode) {
        requirePlanner(actingProjectCode);
        return repository.findVehicleCandidates();
    }

    @Transactional
    public VehicleView addVehicle(AddVehicleRequest request) {

        requirePlanner(request.actingProjectCode());

        String assetCode = request.assetCode() == null ? "" : request.assetCode().trim();
        if (assetCode.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a vehicle asset to add.");
        }
        if (!repository.isVehicleAsset(assetCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Asset " + assetCode + " is not a registered Vehicle asset.");
        }

        repository.upsertVehicle(assetCode, blankToNull(request.defaultDriver()),
                safeEmployee(request.actorEmployeeCode()));

        return repository.findVehicles(assetCode).get(0);
    }

    @Transactional
    public VehicleView updateVehicle(String assetCode, UpdateVehicleRequest request) {

        requirePlanner(request.actingProjectCode());

        if (repository.updateVehicleDriver(assetCode, blankToNull(request.defaultDriver())) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle " + assetCode + " is not in the pool.");
        }

        return repository.findVehicles(assetCode).get(0);
    }

    @Transactional
    public void removeVehicle(String assetCode, String actingProjectCode) {

        requirePlanner(actingProjectCode);

        if (repository.hasOpenTrip(assetCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vehicle " + assetCode + " is on an open trip and cannot be removed from the pool.");
        }

        repository.deactivateVehicle(assetCode);
    }

    // ================================================================ GINs available for a trip

    public List<AvailableGin> getAvailableGins(String actingProjectCode, String originProjectCode) {

        requirePlanner(actingProjectCode);

        if (originProjectCode == null || originProjectCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An origin project is required.");
        }

        return repository.findAvailableGins(originProjectCode.trim(), null);
    }

    // ================================================================ trips: read

    public List<TripSummary> getTrips(String actingProjectCode, String status) {
        requirePlanner(actingProjectCode);
        return repository.findTrips(blankToNull(status));
    }

    public TripView getTrip(UUID tripId, String actingProjectCode) {
        requirePlanner(actingProjectCode);
        return buildView(requireTrip(tripId), null);
    }

    // ================================================================ trips: plan

    @Transactional
    public TripView createTrip(TripRequest request) {

        requirePlanner(request.actingProjectCode());

        UUID tripId = UUID.randomUUID();
        String tripCode = "TR-" + String.format("%05d", repository.nextTripNumber());

        String origin = requireText(request.originProjectCode(), "An origin project is required.");
        List<StopRow> stops = normalizeStops(tripId, origin, request.stops());
        VehicleView vehicle = requireAvailableVehicle(request.vehicleAssetCode(), null);

        repository.insertTrip(new TripRow(
                tripId, tripCode, vehicle.assetCode(),
                driverFor(request.driverName(), vehicle), origin, tripTypeOf(stops), TRIP_PLANNED,
                request.plannedDeparture(), null, blankToNull(request.remarks()),
                safeEmployee(request.actorEmployeeCode()), null, null));

        persistStopsAndAllocations(tripId, origin, stops, request.gins(), safeEmployee(request.actorEmployeeCode()), vehicle);

        return buildView(requireTrip(tripId), null);
    }

    @Transactional
    public TripView updateTrip(UUID tripId, TripRequest request) {

        requirePlanner(request.actingProjectCode());

        TripRow trip = repository.findTripForUpdate(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));

        if (!TRIP_PLANNED.equals(trip.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Trip " + trip.tripCode() + " has already left and can no longer be edited.");
        }

        // Start from a clean slate: put every GIN back where it was, then plan again from the request.
        releaseAllocations(tripId);
        repository.deleteStops(tripId);

        String origin = requireText(request.originProjectCode(), "An origin project is required.");
        List<StopRow> stops = normalizeStops(tripId, origin, request.stops());
        VehicleView vehicle = requireAvailableVehicle(request.vehicleAssetCode(), tripId);

        repository.updatePlannedTrip(new TripRow(
                tripId, trip.tripCode(), vehicle.assetCode(),
                driverFor(request.driverName(), vehicle), origin, tripTypeOf(stops), TRIP_PLANNED,
                request.plannedDeparture(), null, blankToNull(request.remarks()),
                trip.createdBy(), null, null));

        persistStopsAndAllocations(tripId, origin, stops, request.gins(), safeEmployee(request.actorEmployeeCode()), vehicle);

        return buildView(requireTrip(tripId), null);
    }

    @Transactional
    public TripView cancelTrip(UUID tripId, ActionRequest request) {

        requirePlanner(request.actingProjectCode());

        TripRow trip = lockTrip(tripId);
        if (!TRIP_PLANNED.equals(trip.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a trip that has not left yet can be cancelled.");
        }

        releaseAllocations(tripId);
        repository.setTripStatus(tripId, TRIP_CANCELLED);

        return buildView(requireTrip(tripId), null);
    }

    // ================================================================ trips: run

    /**
     * The vehicle leaves with the GINs that are approved and gate-cleared. Any others are taken off
     * the trip (they go back to the unallocated list) and reported in the result's notices.
     */
    @Transactional
    public TripView depart(UUID tripId, ActionRequest request) {

        requirePlanner(request.actingProjectCode());

        TripRow trip = lockTrip(tripId);
        if (!TRIP_PLANNED.equals(trip.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trip " + trip.tripCode() + " has already left.");
        }

        return buildView(requireTrip(tripId), doDepart(trip, safeEmployee(request.actorEmployeeCode()), false));
    }

    /**
     * The vehicle reached a stop. Recorded either by a planner or by the project the stop is at (siteSide),
     * with the time it really happened (the request's occurredAt; now when empty).
     */
    @Transactional
    public TripView arriveAtStop(UUID tripId, UUID stopId, ActionRequest request, boolean siteSide) {

        TripRow trip = lockTrip(tripId);
        requireRunning(trip);

        List<StopRow> stops = repository.findStops(tripId);
        StopRow stop = requireStop(stops, stopId);
        authorizeStopAction(request.actingProjectCode(), stop, siteSide);

        if (!"PENDING".equals(stop.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This stop is not waiting for the vehicle.");
        }
        if (stops.stream().anyMatch(s -> "ARRIVED".equals(s.status()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The vehicle is still at another stop — record it leaving there first.");
        }

        StopRow next = nextPendingStop(tripId);
        if (next == null || !next.stopId().equals(stopId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stops are visited in order — the next one is " + (next == null ? "none" : next.projectCode()) + ".");
        }

        LocalDateTime notBefore = trip.actualDeparture();
        for (StopRow earlier : stops) {
            if (earlier.seq() < stop.seq() && earlier.departedAt() != null
                    && (notBefore == null || earlier.departedAt().isAfter(notBefore))) {
                notBefore = earlier.departedAt();
            }
        }

        doArrive(trip, stops, stop, safeEmployee(request.actorEmployeeCode()),
                resolveTime(request.occurredAt(), notBefore));

        return buildView(requireTrip(tripId), null);
    }

    /**
     * Goods come off the vehicle at the stop it is at. At a hub they are held in custody (no GRN, no stock
     * movement); goods for the project itself are unloaded there awaiting its GRN.
     */
    @Transactional
    public TripView unloadAtStop(UUID tripId, UUID stopId, ActionRequest request, boolean siteSide) {

        TripRow trip = lockTrip(tripId);
        requireRunning(trip);

        StopRow stop = requireStop(repository.findStops(tripId), stopId);
        authorizeStopAction(request.actingProjectCode(), stop, siteSide);

        if (!"ARRIVED".equals(stop.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Record the vehicle arriving here before unloading.");
        }

        List<AllocationRow> onVehicle = liveAllocations(tripId).stream()
                .filter(a -> stopId.equals(a.dropStopId()) && "ON_VEHICLE".equals(a.custodyStatus()))
                .toList();

        if (onVehicle.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nothing on the vehicle is for this stop.");
        }

        String actor = safeEmployee(request.actorEmployeeCode());
        LocalDateTime at = resolveTime(request.occurredAt(), stop.arrivedAt());

        doUnloadAtHub(trip, stop, onVehicle.stream().filter(a -> MODE_VIA_HUB.equals(a.deliveryMode())).toList(),
                actor, blankToNull(request.remarks()), at);

        for (AllocationRow allocation : onVehicle) {
            if (MODE_DIRECT.equals(allocation.deliveryMode())) {
                repository.updateAllocation(allocation.allocationId(), "AT_DESTINATION", true);
                repository.insertEvent(allocation.ginId(), tripId, "UNLOADED_AT_DESTINATION", stop.projectCode(),
                        actor, blankToNull(request.remarks()), at);
            }
        }

        return buildView(requireTrip(tripId), null);
    }

    /** These GINs come off the vehicle at the hub stop and are held in custody (assets stay in transit). */
    private void doUnloadAtHub(TripRow trip, StopRow stop, List<AllocationRow> allocations, String actor,
                               String remarks, LocalDateTime at) {

        for (AllocationRow allocation : allocations) {
            repository.updateAllocation(allocation.allocationId(), "AT_HUB", true);
            repository.insertEvent(allocation.ginId(), trip.tripId(), "UNLOADED_TO_HUB", stop.projectCode(), actor,
                    remarks, at);

            for (String assetCode : repository.findGinAssetCodes(allocation.ginId())) {
                assetLocationService.holdAssetAtHub(
                        assetCode, stop.projectCode(), allocation.destinationProjectCode(),
                        trip.tripId(), trip.tripCode(), allocation.ginCode(), dateOf(at), actor);
            }
        }
    }

    /** The vehicle leaves a stop for the next one, at the time the stop's project (or a planner) records. */
    @Transactional
    public TripView departFromStop(UUID tripId, UUID stopId, ActionRequest request, boolean siteSide) {

        TripRow trip = lockTrip(tripId);
        requireRunning(trip);

        StopRow stop = requireStop(repository.findStops(tripId), stopId);
        authorizeStopAction(request.actingProjectCode(), stop, siteSide);

        if (!"ARRIVED".equals(stop.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The vehicle is not at this stop.");
        }
        if (STOP_BASE.equals(stop.stopType())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This is the return stop — close the trip instead.");
        }

        doLeaveStop(trip, stop, safeEmployee(request.actorEmployeeCode()), false,
                resolveTime(request.occurredAt(), stop.arrivedAt()));

        return buildView(requireTrip(tripId), null);
    }

    /** A planner may act on any stop; a stop's own project may record what happens at its stop. */
    private void authorizeStopAction(String actingProjectCode, StopRow stop, boolean siteSide) {

        if (siteSide && actingProjectCode != null && actingProjectCode.trim().equalsIgnoreCase(stop.projectCode())) {
            return;
        }

        requirePlanner(actingProjectCode);
    }

    /** The time something really happened: not in the future, not before {@code notBefore}; null means now. */
    private static LocalDateTime resolveTime(LocalDateTime requested, LocalDateTime notBefore) {

        if (requested == null) {
            return null;
        }
        if (requested.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The time cannot be in the future.");
        }
        if (notBefore != null && requested.isBefore(notBefore)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The time cannot be before the vehicle's previous step (" + notBefore.toString().replace('T', ' ')
                            .substring(0, 16) + ").");
        }
        return requested;
    }

    private static LocalDate dateOf(LocalDateTime at) {
        return at != null ? at.toLocalDate() : LocalDate.now();
    }

    /**
     * The GIN could not be delivered and stays on the vehicle. The caller (the UI) then asks the user
     * whether to raise a stock return for it — stock is never reversed automatically.
     */
    @Transactional
    public UndeliveredResult markUndelivered(UUID tripId, UUID ginId, ActionRequest request) {

        requirePlanner(request.actingProjectCode());

        TripRow trip = lockTrip(tripId);
        requireRunning(trip);

        AllocationRow allocation = repository.findActiveAllocationByGin(ginId)
                .filter(a -> a.tripId().equals(tripId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "That GIN is not on this trip."));

        if (!"ON_VEHICLE".equals(allocation.custodyStatus()) && !"AT_DESTINATION".equals(allocation.custodyStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "GIN " + allocation.ginCode() + " cannot be marked not delivered from its current state ("
                            + allocation.custodyStatus() + ").");
        }

        StopRow current = repository.findStops(tripId).stream()
                .filter(s -> "ARRIVED".equals(s.status())).findFirst().orElse(null);

        repository.updateAllocation(allocation.allocationId(), "UNDELIVERED", true);
        repository.insertEvent(ginId, tripId, "NOT_DELIVERED", current != null ? current.projectCode() : null,
                safeEmployee(request.actorEmployeeCode()), blankToNull(request.remarks()));

        return new UndeliveredResult(buildView(requireTrip(tripId), null), ginId, allocation.ginCode(),
                allocation.issuedProjectCode());
    }

    /**
     * The vehicle is back at the base and the store confirms it. Whatever is still on it (undelivered)
     * is recorded as returned to the store and becomes available to allocate again.
     */
    @Transactional
    public TripView closeTrip(UUID tripId, ActionRequest request) {

        requirePlanner(request.actingProjectCode());

        TripRow trip = lockTrip(tripId);
        requireRunning(trip);

        List<StopRow> stops = repository.findStops(tripId);
        StopRow base = stops.stream().filter(s -> STOP_BASE.equals(s.stopType())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "This trip has no return stop."));

        if (!"ARRIVED".equals(base.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Record the vehicle arriving back at " + base.projectCode() + " before closing the trip.");
        }

        String actor = safeEmployee(request.actorEmployeeCode());
        for (AllocationRow allocation : liveAllocations(tripId)) {
            if ("ON_VEHICLE".equals(allocation.custodyStatus()) || "UNDELIVERED".equals(allocation.custodyStatus())) {
                repository.markAllocationReturned(allocation.allocationId(), base.projectCode());
                repository.insertEvent(allocation.ginId(), tripId, "RETURNED_TO_STORE", base.projectCode(), actor,
                        blankToNull(request.remarks()));
            }
        }

        for (StopRow stop : stops) {
            if ("PENDING".equals(stop.status())) {
                repository.markStopSkipped(stop.stopId());
            }
        }
        repository.markStopDone(base.stopId(), null);
        repository.markTripClosed(tripId, actor);

        return buildView(requireTrip(tripId), null);
    }

    // ================================================================ shared steps (manual + automatic)

    /** Everything that happens when a planned trip leaves; returns the notices (GINs/stops left behind). */
    private List<String> doDepart(TripRow trip, String actor, boolean auto) {

        UUID tripId = trip.tripId();
        List<AllocationRow> live = liveAllocations(tripId);

        List<AllocationRow> cleared = new ArrayList<>();
        List<AllocationRow> notCleared = new ArrayList<>();
        for (AllocationRow allocation : live) {
            (allocation.authorized() && allocation.gateVerified() ? cleared : notCleared).add(allocation);
        }

        if (cleared.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "None of the GINs on this trip are approved and gate-cleared yet, so the vehicle cannot leave.");
        }

        List<String> notices = new ArrayList<>();
        for (AllocationRow allocation : notCleared) {
            removeAllocation(allocation);
            notices.add("GIN " + allocation.ginCode() + " was left off the trip: "
                    + (allocation.authorized() ? "it has not been through the gate yet." : "it is not approved yet."));
        }

        Set<UUID> usedStops = new HashSet<>();
        for (AllocationRow allocation : cleared) {
            repository.updateAllocation(allocation.allocationId(), "ON_VEHICLE", true);
            repository.insertEvent(allocation.ginId(), tripId, "LOADED", trip.originProjectCode(), actor,
                    auto ? AUTO_NOTE_DEPART : null);
            if (allocation.dropStopId() != null) {
                usedStops.add(allocation.dropStopId());
            }
        }

        // A hub or site stop nobody is dropping anything at any more is skipped.
        for (StopRow stop : repository.findStops(tripId)) {
            if (!STOP_BASE.equals(stop.stopType()) && !usedStops.contains(stop.stopId())) {
                repository.markStopSkipped(stop.stopId());
                notices.add("Stop at " + stop.projectCode() + " was skipped: nothing left to drop there.");
            }
        }

        repository.markTripDeparted(tripId);

        StopRow next = nextPendingStop(tripId);
        if (next != null) {
            assetLocationService.recordVehicleMovement(
                    trip.vehicleAssetCode(), next.projectCode(), trip.originProjectCode(),
                    AssetLocationService.MOVEMENT_DISPATCH, tripId, trip.tripCode(), LocalDate.now(),
                    movementActor(trip, actor),
                    "Left " + trip.originProjectCode() + " for " + next.projectCode());

            if (STOP_BASE.equals(next.stopType())) {
                repository.setTripStatus(tripId, TRIP_RETURNING);
            }
        }

        return notices;
    }

    /** The vehicle reaches {@code stop} (already validated as the next one in order). */
    private void doArrive(TripRow trip, List<StopRow> stops, StopRow stop, String actor, LocalDateTime at) {

        String from = previousProject(stops, stop, trip.originProjectCode());
        repository.markStopArrived(stop.stopId(), at);
        assetLocationService.recordVehicleMovement(
                trip.vehicleAssetCode(), stop.projectCode(), from, AssetLocationService.MOVEMENT_RECEIPT,
                trip.tripId(), trip.tripCode(), dateOf(at), movementActor(trip, actor),
                "Arrived at " + stop.projectCode());
    }

    /** The vehicle leaves {@code stop} (currently ARRIVED) for the next one. */
    private void doLeaveStop(TripRow trip, StopRow stop, String actor, boolean auto, LocalDateTime at) {

        UUID tripId = trip.tripId();
        List<AllocationRow> here = liveAllocations(tripId).stream()
                .filter(a -> stop.stopId().equals(a.dropStopId()) && "ON_VEHICLE".equals(a.custodyStatus()))
                .toList();

        AllocationRow stillForHub = here.stream().filter(a -> MODE_VIA_HUB.equals(a.deliveryMode())).findFirst().orElse(null);
        if (stillForHub != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "GIN " + stillForHub.ginCode() + " is still on the vehicle — unload it at the hub or mark it "
                            + "not delivered before leaving.");
        }

        // What is still on the vehicle for a site was unloaded there; the GRN there closes it.
        for (AllocationRow allocation : here) {
            repository.updateAllocation(allocation.allocationId(), "AT_DESTINATION", true);
            repository.insertEvent(allocation.ginId(), tripId, "UNLOADED_AT_DESTINATION", stop.projectCode(),
                    actor, auto ? "Automatic — the vehicle moved on to its next stop" : null, at);
        }

        repository.markStopDone(stop.stopId(), at);

        StopRow next = nextPendingStop(tripId);
        if (next != null) {
            assetLocationService.recordVehicleMovement(
                    trip.vehicleAssetCode(), next.projectCode(), stop.projectCode(),
                    AssetLocationService.MOVEMENT_DISPATCH, tripId, trip.tripCode(), dateOf(at),
                    movementActor(trip, actor),
                    "Left " + stop.projectCode() + " for " + next.projectCode());

            if (STOP_BASE.equals(next.stopType())) {
                repository.setTripStatus(tripId, TRIP_RETURNING);
            }
        }
    }

    // ================================================================ automation hooks
    //
    // Each of these mirrors a manual button on the trip page and is best-effort: it does nothing when the
    // trip isn't in the right state, and never makes the gate pass / approval / GRN that triggered it fail.
    // They are deliberately not @Transactional themselves (they join the caller's transaction) so a refused
    // step doesn't mark that transaction rollback-only.

    private static final String AUTO_NOTE_DEPART = "Automatic — all GINs on the trip are approved and gate-cleared";
    private static final String AUTO_NOTE_GRN = "Automatic — GRN raised at the destination";

    /**
     * A GIN on a planned trip was just approved or passed the exit gate. When every GIN on the trip is now
     * approved and gate-cleared, the vehicle is recorded as leaving. (With only some cleared, the planner
     * can still send the vehicle off manually.)
     */
    public void onGinCleared(UUID ginId, String clearedBy) {

        if (ginId == null) {
            return;
        }

        try {
            repository.findActiveAllocationByGin(ginId).ifPresent(allocation -> {
                TripRow trip = repository.findTripForUpdate(allocation.tripId()).orElse(null);
                if (trip == null || !TRIP_PLANNED.equals(trip.status())) {
                    return;
                }
                List<AllocationRow> live = liveAllocations(trip.tripId());
                if (live.isEmpty() || live.stream().anyMatch(a -> !(a.authorized() && a.gateVerified()))) {
                    return;
                }
                doDepart(trip, safeEmployee(clearedBy), true);
            });
        } catch (ResponseStatusException exception) {
            log.warn("Automatic departure skipped for GIN {}: {}", ginId, exception.getReason());
        }
    }

    /** The destination's security confirmed this GIN arriving at their gate: the vehicle is at that stop. */
    public void onGinArrivedAtGate(UUID ginId) {

        if (ginId == null) {
            return;
        }

        try {
            arriveForGin(ginId, false);
        } catch (ResponseStatusException exception) {
            log.warn("Automatic arrival skipped for GIN {}: {}", ginId, exception.getReason());
        }
    }

    /** A GRN was raised for this GIN at its destination: the vehicle is there and the goods are unloaded. */
    public void onGrnCreated(UUID ginId) {

        if (ginId == null) {
            return;
        }

        try {
            arriveForGin(ginId, false);

            repository.findActiveAllocationByGin(ginId).ifPresent(allocation -> {
                if (!"ON_VEHICLE".equals(allocation.custodyStatus()) || !MODE_DIRECT.equals(allocation.deliveryMode())
                        || allocation.dropStopId() == null) {
                    return;
                }
                TripRow trip = repository.findTrip(allocation.tripId()).orElse(null);
                if (trip == null || (!TRIP_IN_TRANSIT.equals(trip.status()) && !TRIP_RETURNING.equals(trip.status()))) {
                    return;
                }
                StopRow stop = repository.findStops(trip.tripId()).stream()
                        .filter(s -> s.stopId().equals(allocation.dropStopId())).findFirst().orElse(null);
                if (stop == null || !"ARRIVED".equals(stop.status())) {
                    return;
                }
                repository.updateAllocation(allocation.allocationId(), "AT_DESTINATION", true);
                repository.insertEvent(allocation.ginId(), trip.tripId(), "UNLOADED_AT_DESTINATION",
                        stop.projectCode(), null, AUTO_NOTE_GRN);
            });
        } catch (ResponseStatusException exception) {
            log.warn("Automatic unload skipped for GIN {}: {}", ginId, exception.getReason());
        }
    }

    /**
     * Brings the trip carrying {@code ginId} to the stop where that GIN is dropped, when it is on the
     * road. If the vehicle is still recorded at an earlier stop it is moved on first (only when that is
     * safe — goods still owed to a hub stop block it, and the planner handles that by hand).
     */
    private void arriveForGin(UUID ginId, boolean hubDelivery) {

        AllocationRow allocation = repository.findActiveAllocationByGin(ginId).orElse(null);
        if (allocation == null || allocation.dropStopId() == null
                || !(hubDelivery ? MODE_VIA_HUB : MODE_DIRECT).equals(allocation.deliveryMode())) {
            return;
        }

        TripRow trip = repository.findTripForUpdate(allocation.tripId()).orElse(null);
        if (trip == null || (!TRIP_IN_TRANSIT.equals(trip.status()) && !TRIP_RETURNING.equals(trip.status()))) {
            return;
        }

        List<StopRow> stops = repository.findStops(trip.tripId());
        StopRow target = stops.stream().filter(s -> s.stopId().equals(allocation.dropStopId())).findFirst().orElse(null);
        if (target == null || !"PENDING".equals(target.status())) {
            return;
        }

        StopRow at = stops.stream().filter(s -> "ARRIVED".equals(s.status())).findFirst().orElse(null);
        if (at != null) {
            doLeaveStop(trip, at, null, true, null);
        }

        StopRow next = nextPendingStop(trip.tripId());
        if (next == null || !next.stopId().equals(target.stopId())) {
            return;
        }

        doArrive(trip, repository.findStops(trip.tripId()), target, null, null);
    }

    /**
     * Goods held at a hub are collected by the destination's own vehicle — no trip is planned for this. The
     * hub's custody ends here, after which the destination can do its arrival gate check and raise its GRN
     * as for any GIN. Either the destination or the hub project can record it.
     */
    @Transactional
    public void collectFromHub(UUID ginId, CollectRequest request) {

        String acting = requireText(request.actingProjectCode(), "An active project is required.");

        AllocationRow allocation = repository.findActiveAllocationByGin(ginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "That GIN is not being held at a hub."));

        if (!"AT_HUB".equals(allocation.custodyStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "GIN " + allocation.ginCode() + " is not held at a hub right now (" + allocation.custodyStatus() + ").");
        }

        String hub = allocation.dropProjectCode();
        String destination = allocation.destinationProjectCode();
        if (!acting.equalsIgnoreCase(destination) && !acting.equalsIgnoreCase(hub)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the destination (" + destination + ") or the hub (" + hub + ") can record this collection.");
        }

        boolean hubDelivers = "DELIVERED_BY_HUB".equalsIgnoreCase(request.releaseType());
        String actor = safeEmployee(request.actorEmployeeCode());
        String vehicle = blankToNull(request.vehicleNo());
        String driver = blankToNull(request.driverName());
        String note = (hubDelivers ? "Being delivered by " + hub + "'s vehicle" : "Collected by " + destination + "'s own vehicle")
                + (vehicle != null ? " " + vehicle : "")
                + (driver != null ? ", driver " + driver : "")
                + (blankToNull(request.remarks()) != null ? " — " + request.remarks().trim() : "");

        releaseFromHub(allocation, actor, hubDelivers ? "DISPATCHED_FROM_HUB" : "COLLECTED_FROM_HUB", note,
                resolveTime(request.occurredAt(), null));
    }

    /**
     * Ends the hub's custody: the goods are on their way to the destination (collected by its own vehicle, or
     * delivered by the hub's). The allocation stays ACTIVE as COLLECTED until the destination's GRN is
     * approved, so the whole journey is followed to the end and stays visible on the trip.
     */
    private void releaseFromHub(AllocationRow allocation, String actor, String eventType, String note,
                                LocalDateTime at) {

        String hub = allocation.dropProjectCode();
        String destination = allocation.destinationProjectCode();

        repository.updateAllocation(allocation.allocationId(), "COLLECTED", true);
        repository.insertEvent(allocation.ginId(), allocation.tripId(), eventType, hub, actor, note, at);

        String tripCode = repository.findTrip(allocation.tripId()).map(TripRow::tripCode).orElse("");
        for (String assetCode : repository.findGinAssetCodes(allocation.ginId())) {
            assetLocationService.dispatchAssetFromHub(
                    assetCode, hub, destination, allocation.tripId(), tripCode, allocation.ginCode(),
                    dateOf(at), actor);
        }
    }

    /**
     * A GRN (or the arrival gate check) is being recorded for a GIN. If it is held at a hub, the destination
     * is taking it straight from there: the hold is released and the collection recorded, with the vehicle
     * the GRN names. If it is still on its way to the hub, the goods are not there to receive yet.
     */
    public void prepareReceipt(UUID ginId, String vehicleNote, String actorEmployeeCode, String how) {

        if (ginId == null) {
            return;
        }

        AllocationRow allocation = repository.findActiveAllocationByGin(ginId).orElse(null);
        if (allocation == null || !MODE_VIA_HUB.equals(allocation.deliveryMode())) {
            return;
        }

        if ("AT_HUB".equals(allocation.custodyStatus())) {
            String vehicle = blankToNull(vehicleNote);
            releaseFromHub(allocation, safeEmployee(actorEmployeeCode), "COLLECTED_FROM_HUB",
                    "Collected directly from the hub — " + how + (vehicle != null ? ", vehicle " + vehicle : ""), null);
            return;
        }

        if ("ALLOCATED".equals(allocation.custodyStatus()) || "ON_VEHICLE".equals(allocation.custodyStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This GIN is still on its way to the hub, so it cannot be received yet.");
        }
    }

    /** GINs on the road to this project as a hub — what the hub's gate expects to see arrive. */
    public List<HubArrivalRow> getHubArrivals(String hubProjectCode) {
        return repository.findHubArrivals(hubProjectCode);
    }

    /**
     * The hub's security confirmed this GIN arriving at their gate: the vehicle is at the hub stop and the
     * GIN comes off it, held at the hub until its destination collects it (or raises the GRN directly).
     */
    @Transactional
    public void confirmHubArrival(UUID ginId) {

        AllocationRow allocation = repository.findActiveAllocationByGin(ginId)
                .filter(a -> MODE_VIA_HUB.equals(a.deliveryMode()) && "ON_VEHICLE".equals(a.custodyStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "This GIN is not on a vehicle heading to a hub."));

        arriveForGin(ginId, true);

        TripRow trip = repository.findTripForUpdate(allocation.tripId()).orElseThrow();
        StopRow stop = repository.findStops(trip.tripId()).stream()
                .filter(s -> s.stopId().equals(allocation.dropStopId())).findFirst().orElseThrow();

        if (!"ARRIVED".equals(stop.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The vehicle cannot be at this hub yet — it still has earlier stops to make, or is at a stop "
                            + "whose goods are not unloaded.");
        }

        doUnloadAtHub(trip, stop, List.of(allocation), null, "Automatic — hub gate confirmed the arrival", null);
    }

    /**
     * Vehicles due at, or at, this project — what a hub or destination can record for itself:
     * the vehicle arriving, the goods being unloaded and the vehicle leaving, each with a real time.
     */
    public List<StopTask> getStopTasks(String projectCode) {

        List<StopTask> tasks = new ArrayList<>();
        Map<UUID, List<StopRow>> stopsByTrip = new HashMap<>();
        Map<UUID, List<AllocationRow>> allocationsByTrip = new HashMap<>();

        for (StopTaskRow row : repository.findStopTasks(requireText(projectCode, "A project is required."))) {

            StopRow stop = row.stop();
            List<StopRow> stops = stopsByTrip.computeIfAbsent(stop.tripId(), repository::findStops);
            List<AllocationRow> live = allocationsByTrip.computeIfAbsent(stop.tripId(),
                    id -> repository.findAllocations(id).stream().filter(AllocationRow::active).toList());

            boolean pending = "PENDING".equals(stop.status());
            boolean arrived = "ARRIVED".equals(stop.status());
            boolean anyArrived = stops.stream().anyMatch(x -> "ARRIVED".equals(x.status()));
            StopRow next = stops.stream().filter(x -> "PENDING".equals(x.status())).findFirst().orElse(null);

            List<AllocationRow> here = live.stream().filter(a -> stop.stopId().equals(a.dropStopId())).toList();
            boolean hasOnVehicle = here.stream().anyMatch(a -> "ON_VEHICLE".equals(a.custodyStatus()));
            boolean hubGoodsLeft = here.stream()
                    .anyMatch(a -> "ON_VEHICLE".equals(a.custodyStatus()) && MODE_VIA_HUB.equals(a.deliveryMode()));
            boolean isBase = STOP_BASE.equals(stop.stopType());

            tasks.add(new StopTask(
                    stop.tripId(), row.tripCode(), row.vehicleNo(), row.driverName(), row.originProjectCode(),
                    stop.stopId(), stop.seq(), stop.stopType(), stop.status(), stop.eta(), stop.arrivedAt(),
                    stop.departedAt(),
                    pending && !anyArrived && next != null && next.stopId().equals(stop.stopId()),
                    arrived && hasOnVehicle,
                    arrived && !isBase && !hubGoodsLeft,
                    here.stream()
                            .map(a -> new StopTaskGin(a.ginCode(), a.deliveryMode(), a.destinationProjectCode(),
                                    a.custodyStatus()))
                            .toList()));
        }

        return tasks;
    }

    /**
     * What a hub or destination can look back on: every vehicle that arrived (with the GRNs for what it
     * brought, and how goods held here were released again), plus goods received after leaving a hub.
     */
    public SiteHistory getSiteHistory(String projectCode) {

        String project = requireText(projectCode, "A project is required.");
        List<SiteVisit> visits = new ArrayList<>();

        for (VisitRow visit : repository.findVisits(project)) {

            List<VisitGin> gins = new ArrayList<>();
            for (AllocationRow allocation : repository.findAllocationsByDropStop(visit.stopId())) {

                var release = repository.findReleaseEvent(allocation.ginId(), allocation.tripId()).orElse(null);
                gins.add(new VisitGin(
                        allocation.ginCode(), allocation.deliveryMode(), allocation.destinationProjectCode(),
                        allocation.custodyStatus(), allocation.grnCode(), allocation.grnApproved(),
                        release != null ? release.eventType() : null,
                        release != null ? release.remarks() : null,
                        release != null ? release.eventTime() : null,
                        allocation.nextTripCode()));
            }

            visits.add(new SiteVisit(
                    visit.tripId(), visit.tripCode(), visit.vehicleNo(), visit.driverName(),
                    visit.originProjectCode(), visit.stopType(), visit.stopStatus(),
                    visit.arrivedAt(), visit.departedAt(), gins));
        }

        return new SiteHistory(visits, repository.findHubReleasesFor(project));
    }

    // ================================================================ hooks used by other modules

    /** The vehicle a GRN for this GIN should default to (see {@link GinReceiptVehicle}). */
    public GinReceiptVehicle getReceiptVehicle(UUID ginId) {

        AllocationRow allocation = repository.findActiveAllocationByGin(ginId).orElse(null);
        if (allocation == null) {
            return new GinReceiptVehicle(null, null, false);
        }
        if (MODE_VIA_HUB.equals(allocation.deliveryMode())) {
            return new GinReceiptVehicle(null, null, true);
        }

        return repository.findTrip(allocation.tripId())
                .map(trip -> new GinReceiptVehicle(trip.vehicleAssetCode(), repository.findVehicleNo(trip.vehicleAssetCode()), false))
                .orElse(new GinReceiptVehicle(null, null, false));
    }

    public boolean carriedOnTrip(UUID ginId) {
        return ginId != null && repository.hasTripAllocation(ginId);
    }

    /** The destination's GRN for a GIN was approved: the trip allocation for it is delivered. */
    public void onGinReceived(UUID ginId, String receivingProjectCode, String approvedBy) {

        if (ginId == null) {
            return;
        }

        repository.findActiveAllocationByGin(ginId).ifPresent(allocation -> {
            if ("AT_HUB".equals(allocation.custodyStatus())) {
                // Approved without the hub release having been recorded: it left the hub on its way here.
                releaseFromHub(allocation, safeEmployee(approvedBy), "COLLECTED_FROM_HUB",
                        "Collected from the hub — GRN approved", null);
            }
            repository.updateAllocation(allocation.allocationId(), "DELIVERED", false);
            repository.insertEvent(ginId, allocation.tripId(), "DELIVERED", receivingProjectCode,
                    safeEmployee(approvedBy), "GRN approved");
        });
    }

    public Map<UUID, GinTripInfo> getTripInfo(Collection<UUID> ginIds) {
        return repository.findTripInfoByGinIds(ginIds);
    }

    // ================================================================ hub + tracking (no planner check)

    public List<HeldGin> getHeldAtHub(String projectCode) {
        return repository.findHeldAtHub(requireText(projectCode, "A project is required."));
    }

    public List<CustodyEvent> getCustodyEvents(UUID ginId) {
        return repository.findEvents(ginId);
    }

    /** Every GIN on its way to {@code projectCode}, with where its vehicle is — read-only for receivers. */
    public List<IncomingDelivery> getIncoming(String projectCode) {

        String project = requireText(projectCode, "A project is required.");
        List<IncomingRow> rows = repository.findIncoming(project);
        Map<String, String> names = repository.findProjectNames();
        Map<UUID, List<StopRow>> stopsByTrip = new HashMap<>();
        List<IncomingDelivery> result = new ArrayList<>();

        for (IncomingRow row : rows) {

            List<StopRow> stops = stopsByTrip.computeIfAbsent(row.tripId(), repository::findStops);
            StopRow arrived = stops.stream().filter(s -> "ARRIVED".equals(s.status())).findFirst().orElse(null);
            StopRow upcoming = stops.stream().filter(s -> "PENDING".equals(s.status())).findFirst().orElse(null);
            StopRow focus = arrived != null ? arrived : upcoming;

            int stopsBefore = row.dropSeq() == null ? 0 : (int) stops.stream()
                    .filter(s -> s.seq() < row.dropSeq() && ("PENDING".equals(s.status()) || "ARRIVED".equals(s.status())))
                    .count();

            String vehicle = row.vehicleNo();
            String stage;
            String text;

            switch (row.custodyStatus()) {
                case "AT_HUB" -> {
                    stage = "AT_HUB";
                    text = "Held at hub " + label(names, row.dropProjectCode())
                            + " — raise the GRN directly, or record that you collected it.";
                }
                case "COLLECTED" -> {
                    stage = "EN_ROUTE";
                    text = "On its way to you from hub " + label(names, row.dropProjectCode())
                            + (row.legNote() != null ? " — " + row.legNote() : "")
                            + (row.legTime() != null ? " (left " + row.legTime().toString().replace('T', ' ').substring(0, 16) + ")" : "")
                            + ". Raise the GRN when it arrives.";
                }
                case "AT_DESTINATION" -> {
                    stage = "UNLOADED";
                    text = "Unloaded at your site — raise the GRN.";
                }
                case "UNDELIVERED" -> {
                    stage = "NOT_DELIVERED";
                    text = "Not delivered — the vehicle is taking it back to the store.";
                }
                case "ALLOCATED" -> {
                    stage = "SCHEDULED";
                    text = "Scheduled on trip " + row.tripCode() + " — vehicle " + vehicle + " has not left yet.";
                }
                default -> {
                    boolean viaHub = MODE_VIA_HUB.equals(row.deliveryMode());
                    boolean atDrop = arrived != null && row.dropSeq() != null && arrived.seq() == row.dropSeq();

                    if (viaHub) {
                        stage = "EN_ROUTE";
                        text = atDrop
                                ? "Vehicle " + vehicle + " is at hub " + label(names, row.dropProjectCode())
                                        + " — your goods will be held there, then sent on."
                                : "On its way to hub " + label(names, row.dropProjectCode()) + " first, "
                                        + "then a local trip to you (vehicle " + vehicle + ").";
                    } else if (atDrop) {
                        stage = "AT_YOUR_GATE";
                        text = "Vehicle " + vehicle + " is at your site now.";
                    } else if (stopsBefore == 0) {
                        stage = "NEXT_STOP";
                        text = "Vehicle " + vehicle + " is heading to you next"
                                + (focus != null && focus.stopId() != null && arrived != null
                                        ? " (leaving " + label(names, arrived.projectCode()) + ")" : "") + ".";
                    } else {
                        stage = "EN_ROUTE";
                        text = "Vehicle " + vehicle + " has " + stopsBefore + " stop" + (stopsBefore > 1 ? "s" : "")
                                + " before yours" + (focus != null
                                        ? (arrived != null ? ", currently at " : ", heading to ")
                                                + label(names, focus.projectCode()) : "") + ".";
                    }
                }
            }

            result.add(new IncomingDelivery(
                    row.ginId(), row.ginCode(), row.issuedProjectCode(), row.itemCount(),
                    row.deliveryMode(), row.custodyStatus(), row.tripId(), row.tripCode(), row.tripStatus(),
                    row.vehicleAssetCode(), row.vehicleNo(), row.driverName(),
                    row.dropProjectCode(), row.dropEta(), stopsBefore,
                    focus != null ? focus.projectCode() : null, stage, text,
                    stops.stream()
                            .map(s -> new IncomingStop(s.seq(), s.projectCode(), s.stopType(), s.status(), s.eta()))
                            .toList()));
        }

        return result;
    }

    // ================================================================ internals

    private TripRow requireTrip(UUID tripId) {
        return repository.findTrip(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));
    }

    private TripRow lockTrip(UUID tripId) {
        return repository.findTripForUpdate(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));
    }

    private static void requireRunning(TripRow trip) {
        if (!TRIP_IN_TRANSIT.equals(trip.status()) && !TRIP_RETURNING.equals(trip.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Trip " + trip.tripCode() + " is not on the road (status " + trip.status() + ").");
        }
    }

    private static StopRow requireStop(List<StopRow> stops, UUID stopId) {
        return stops.stream().filter(s -> s.stopId().equals(stopId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stop not found on this trip."));
    }

    private StopRow nextPendingStop(UUID tripId) {
        return repository.findStops(tripId).stream()
                .filter(s -> "PENDING".equals(s.status())).findFirst().orElse(null);
    }

    /** The project the vehicle was last at before {@code stop}: the previous stop that was visited, else the origin. */
    private static String previousProject(List<StopRow> stops, StopRow stop, String origin) {
        String previous = origin;
        for (StopRow candidate : stops) {
            if (candidate.seq() >= stop.seq()) {
                break;
            }
            if ("DONE".equals(candidate.status())) {
                previous = candidate.projectCode();
            }
        }
        return previous;
    }

    /** Allocations still owned by the trip (not delivered, returned, handed over or removed). */
    private List<AllocationRow> liveAllocations(UUID tripId) {
        return repository.findAllocations(tripId).stream().filter(AllocationRow::active).toList();
    }

    private VehicleView requireAvailableVehicle(String assetCode, UUID currentTripId) {

        String code = requireText(assetCode, "Choose a vehicle for the trip.");
        List<VehicleView> found = repository.findVehicles(code);
        if (found.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle " + code + " is not in the vehicle pool.");
        }

        VehicleView vehicle = found.get(0);
        boolean ownTrip = currentTripId != null && currentTripId.equals(vehicle.tripId());
        if (!ownTrip && !"AVAILABLE".equals(vehicle.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vehicle " + code + " is not available (" + vehicle.status().toLowerCase().replace('_', ' ')
                            + (vehicle.statusDetail() != null ? ": " + vehicle.statusDetail() : "") + ").");
        }
        return vehicle;
    }

    private static String driverFor(String requested, VehicleView vehicle) {
        String driver = blankToNull(requested);
        return driver != null ? driver : vehicle.defaultDriver();
    }

    private static String tripTypeOf(List<StopRow> stops) {
        return stops.stream().anyMatch(s -> STOP_HUB.equals(s.stopType())) ? "HUB_DISTRIBUTION" : "DIRECT";
    }

    /**
     * Validates the requested stops and numbers them. The trip always ends with a RETURN_TO_BASE stop
     * (appended, defaulting to the origin, when the request has none).
     */
    private List<StopRow> normalizeStops(UUID tripId, String origin, List<StopInput> inputs) {

        if (!repository.projectExists(origin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown origin project: " + origin);
        }

        List<StopRow> stops = new ArrayList<>();
        boolean hasBase = false;
        int seq = 1;

        for (StopInput input : inputs == null ? List.<StopInput>of() : inputs) {

            if (hasBase) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The return stop has to be the last stop.");
            }

            String project = requireText(input.projectCode(), "Every stop needs a project.");
            String type = input.stopType() == null ? "" : input.stopType().trim().toUpperCase();
            if (!STOP_HUB.equals(type) && !STOP_SITE.equals(type) && !STOP_BASE.equals(type)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown stop type: " + input.stopType());
            }
            if (!repository.projectExists(project)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown project in stops: " + project);
            }

            hasBase = STOP_BASE.equals(type);
            stops.add(new StopRow(UUID.randomUUID(), tripId, seq++, project, type, "PENDING", input.eta(), null, null));
        }

        if (!hasBase) {
            stops.add(new StopRow(UUID.randomUUID(), tripId, seq, origin, STOP_BASE, "PENDING", null, null, null));
        }

        if (stops.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add at least one hub or site stop to the trip.");
        }

        return stops;
    }

    private void persistStopsAndAllocations(UUID tripId, String origin, List<StopRow> stops,
                                            List<GinInput> gins, String actor, VehicleView vehicle) {

        if (gins == null || gins.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocate at least one GIN to the trip.");
        }

        for (StopRow stop : stops) {
            repository.insertStop(stop);
        }

        Set<UUID> seen = new HashSet<>();
        for (GinInput input : gins) {

            if (input.ginId() == null || !seen.add(input.ginId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each GIN can only be listed once.");
            }

            AvailableGin gin = repository.findAvailableGins(origin, input.ginId()).stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "A selected GIN is no longer available at " + origin
                                    + " (it may already be on another trip, received, or issued by another project)."));

            String mode = input.deliveryMode() == null ? MODE_DIRECT : input.deliveryMode().trim().toUpperCase();
            UUID dropStopId;

            if (MODE_VIA_HUB.equals(mode)) {

                if ("AT_HUB".equals(gin.kind())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "GIN " + gin.ginCode() + " is already at a hub — send it directly to its destination.");
                }
                String hub = requireText(input.hubProjectCode(), "Choose a hub for GIN " + gin.ginCode() + ".");
                if (hub.equalsIgnoreCase(gin.destinationProjectCode()) || hub.equalsIgnoreCase(origin)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The hub for GIN " + gin.ginCode() + " must be a different project from its origin and destination.");
                }
                dropStopId = stopFor(stops, STOP_HUB, hub, gin.ginCode());

            } else if (MODE_DIRECT.equals(mode)) {
                // The destination may also be a hub on this trip — one stop then serves both.
                dropStopId = stopFor(stops, null, gin.destinationProjectCode(), gin.ginCode());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown delivery mode: " + input.deliveryMode());
            }

            UUID previous = null;
            if ("AT_HUB".equals(gin.kind())) {
                // Leaving the hub on this trip: the hub custody row is handed over, and restored if this
                // trip later drops the GIN or is cancelled.
                AllocationRow held = repository.findActiveAllocationByGin(gin.ginId()).orElseThrow();
                repository.updateAllocation(held.allocationId(), "HANDED_OVER", false);
                repository.insertEvent(gin.ginId(), tripId, "HANDED_TO_LOCAL_VEHICLE", origin, actor, null);
                previous = held.allocationId();
            }

            repository.insertAllocation(UUID.randomUUID(), tripId, gin.ginId(), mode, dropStopId, previous);

            // The GIN (and so the GRN raised from it) defaults to the vehicle that carries it.
            repository.fillGinVehicle(gin.ginId(), vehicle.assetCode(),
                    vehicle.registrationNumber() != null ? vehicle.registrationNumber() : vehicle.assetCode());
        }
    }

    private static UUID stopFor(List<StopRow> stops, String type, String project, String ginCode) {
        return stops.stream()
                .filter(s -> !STOP_BASE.equals(s.stopType()) && (type == null || type.equals(s.stopType()))
                        && s.projectCode().equalsIgnoreCase(project))
                .map(StopRow::stopId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "GIN " + ginCode + " needs a " + (STOP_HUB.equals(type) ? "hub" : "") + " stop at "
                                + project + " on the trip."));
    }

    /** Takes one allocation off its trip, restoring the hub custody it was handed over from, if any. */
    private void removeAllocation(AllocationRow allocation) {
        repository.updateAllocation(allocation.allocationId(), "REMOVED", false);
        repository.findTrip(allocation.tripId())
                .ifPresent(trip -> repository.clearGinVehicle(allocation.ginId(), trip.vehicleAssetCode()));
        if (allocation.previousAllocationId() != null) {
            repository.findAllocation(allocation.previousAllocationId()).ifPresent(previous -> {
                if ("HANDED_OVER".equals(previous.custodyStatus())) {
                    repository.updateAllocation(previous.allocationId(), "AT_HUB", true);
                }
            });
        }
    }

    private void releaseAllocations(UUID tripId) {
        for (AllocationRow allocation : repository.findAllocations(tripId)) {
            if (allocation.active()) {
                removeAllocation(allocation);
            }
        }
    }

    private TripView buildView(TripRow trip, List<String> notices) {

        List<StopRow> stops = repository.findStops(trip.tripId());
        List<AllocationRow> allocations = repository.findAllocations(trip.tripId());

        return new TripView(
                trip.tripId(), trip.tripCode(), trip.vehicleAssetCode(), repository.findVehicleNo(trip.vehicleAssetCode()),
                trip.driverName(), trip.originProjectCode(), trip.tripType(), trip.status(),
                trip.plannedDeparture(), trip.actualDeparture(), trip.remarks(),
                trip.createdBy(), trip.closedBy(), trip.closedAt(),
                stops.stream()
                        .map(s -> new StopView(s.stopId(), s.seq(), s.projectCode(), s.stopType(), s.status(),
                                s.eta(), s.arrivedAt(), s.departedAt()))
                        .toList(),
                allocations.stream()
                        .map(a -> new TripGinView(a.allocationId(), a.ginId(), a.ginCode(), a.issuedProjectCode(),
                                a.destinationProjectCode(), a.deliveryMode(), a.dropStopId(), a.dropProjectCode(),
                                a.custodyStatus(), a.active(), a.itemCount(), a.authorized(), a.gateVerified(),
                                a.grnCode(), a.grnApproved(), a.nextTripCode()))
                        .toList(),
                repository.findTripActivity(trip.tripId()),
                notices == null ? List.of() : notices);
    }

    private static String label(Map<String, String> names, String projectCode) {
        if (projectCode == null) {
            return "—";
        }
        String name = names.get(projectCode);
        return name == null || name.isBlank() ? projectCode : name + " (" + projectCode + ")";
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
