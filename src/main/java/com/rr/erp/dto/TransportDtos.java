package com.rr.erp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Request/response shapes for the transport plan (vehicle pool, trips, hub custody, receiver tracking). */
public final class TransportDtos {

    private TransportDtos() {}

    // ---- Vehicle pool ----

    /** A Vehicle asset that can still be added to the pool. */
    public record VehicleCandidate(String assetCode, String description, String registrationNumber,
                                   String make, String model) {}

    /**
     * One pool vehicle. status is derived, never stored: AVAILABLE, ALLOCATED, IN_TRANSIT, RETURNING or
     * MAINTENANCE (statusDetail says why). currentLocation is the project the vehicle is at, null while it
     * is on the road (headingTo is then where it is going).
     */
    public record VehicleView(String assetCode, String description, String registrationNumber,
                              String make, String model, String defaultDriver,
                              String status, String statusDetail,
                              String currentLocation, String headingTo,
                              UUID tripId, String tripCode, String driverName,
                              String currentStopProject, String currentStopStatus) {}

    public record AddVehicleRequest(String assetCode, String defaultDriver,
                                    String actingProjectCode, String actorEmployeeCode) {}

    public record UpdateVehicleRequest(String defaultDriver, String actingProjectCode) {}

    // ---- GINs available for allocation ----

    /**
     * A GIN that can be put on a trip leaving {@code originProjectCode}. kind is NEW (issued by that
     * project, not through its gate yet), AT_HUB (held at that project as a hub) or RETURNED (came back
     * undelivered to that project).
     */
    public record AvailableGin(UUID ginId, String ginCode, LocalDateTime issuedDate, String issuedProjectCode,
                               String destinationProjectCode, int itemCount, int assetCount,
                               boolean isAuthorized, boolean isGateVerified, String kind, String heldFromTripCode) {}

    // ---- Trips ----

    public record StopInput(String projectCode, String stopType, LocalDateTime eta) {}

    public record GinInput(UUID ginId, String deliveryMode, String hubProjectCode) {}

    public record TripRequest(String actingProjectCode, String actorEmployeeCode,
                              String vehicleAssetCode, String driverName, String originProjectCode,
                              LocalDateTime plannedDeparture, String remarks,
                              List<StopInput> stops, List<GinInput> gins) {}

    /** The destination (or the hub) confirming goods held at a hub were collected by the destination's own vehicle. */
    public record CollectRequest(String actingProjectCode, String actorEmployeeCode,
                                 String releaseType, String vehicleNo, String driverName,
                                 LocalDateTime occurredAt, String remarks) {}

    /** occurredAt: when it really happened (entered by the project doing it); null means now. */
    public record ActionRequest(String actingProjectCode, String actorEmployeeCode, String remarks,
                                LocalDateTime occurredAt) {}

    public record StopView(UUID stopId, int seq, String projectCode, String stopType, String status,
                           LocalDateTime eta, LocalDateTime arrivedAt, LocalDateTime departedAt) {}

    public record TripGinView(UUID allocationId, UUID ginId, String ginCode, String issuedProjectCode,
                              String destinationProjectCode, String deliveryMode, UUID dropStopId,
                              String dropProjectCode, String custodyStatus, boolean isActive,
                              int itemCount, boolean isAuthorized, boolean isGateVerified,
                              String grnCode, boolean grnApproved, String nextTripCode) {}

    public record TripSummary(UUID tripId, String tripCode, String vehicleAssetCode, String vehicleNo,
                              String driverName, String originProjectCode, String tripType, String status,
                              LocalDateTime plannedDeparture, LocalDateTime actualDeparture,
                              int ginCount, int deliveredCount, int stopCount,
                              String nextStopProject, String nextStopStatus) {}

    public record TripView(UUID tripId, String tripCode, String vehicleAssetCode, String vehicleNo,
                           String driverName, String originProjectCode, String tripType, String status,
                           LocalDateTime plannedDeparture, LocalDateTime actualDeparture, String remarks,
                           String createdBy, String closedBy, LocalDateTime closedAt,
                           List<StopView> stops, List<TripGinView> gins, List<TripActivity> activity,
                           List<String> notices) {}

    /** One thing that happened on a trip to one GIN — who recorded it, where and when. */
    public record TripActivity(UUID eventId, UUID ginId, String ginCode, String eventType,
                               String locationProjectCode, String performedBy, LocalDateTime eventTime,
                               String remarks) {}

    public record StopTaskGin(String ginCode, String deliveryMode, String destinationProjectCode,
                              String custodyStatus) {}

    /**
     * A vehicle that is due at, or at, a project (a hub or a destination), with what that project can
     * record: arrival, unloading and departure, each with the time it really happened.
     */
    public record StopTask(UUID tripId, String tripCode, String vehicleNo, String driverName,
                           String originProjectCode, UUID stopId, int seq, String stopType, String status,
                           LocalDateTime eta, LocalDateTime arrivedAt, LocalDateTime departedAt,
                           boolean canArrive, boolean canUnload, boolean canDepart,
                           List<StopTaskGin> gins) {}

    /** Result of marking a GIN not delivered: the caller offers to raise a stock return for it. */
    public record UndeliveredResult(TripView trip, UUID ginId, String ginCode, String originProjectCode) {}

    // ---- Hub custody / receiver tracking ----

    public record HeldGin(UUID ginId, String ginCode, String issuedProjectCode, String destinationProjectCode,
                          int itemCount, String tripCode, LocalDateTime heldSince) {}

    public record IncomingStop(int seq, String projectCode, String stopType, String status, LocalDateTime eta) {}

    /**
     * One GIN on its way to a project, with where the vehicle is. stage: SCHEDULED, EN_ROUTE, NEXT_STOP,
     * AT_YOUR_GATE, AT_HUB, UNLOADED, NOT_DELIVERED. statusText is a ready-to-show sentence.
     */
    public record IncomingDelivery(UUID ginId, String ginCode, String issuedProjectCode, int itemCount,
                                   String deliveryMode, String custodyStatus,
                                   UUID tripId, String tripCode, String tripStatus,
                                   String vehicleAssetCode, String vehicleNo, String driverName,
                                   String dropProjectCode, LocalDateTime eta, int stopsBefore,
                                   String currentStopProject, String stage, String statusText,
                                   List<IncomingStop> stops) {}

    public record CustodyEvent(UUID eventId, UUID ginId, UUID tripId, String tripCode, String eventType,
                               String locationProjectCode, String performedBy, LocalDateTime eventTime,
                               String remarks) {}

    /**
     * The vehicle a GRN for this GIN should default to: the trip's vehicle when the GIN rides straight to its
     * destination. For goods routed via a hub the destination's own (or the hub's) vehicle brings them on, so
     * nothing is suggested (viaHub = true) and the receiver picks it.
     */
    public record GinReceiptVehicle(String vehicleAssetCode, String vehicleNo, boolean viaHub) {}

    /** One GIN of a vehicle visit: what it was, its GRN, and — if it was held there — how it left again. */
    public record VisitGin(String ginCode, String deliveryMode, String destinationProjectCode, String custodyStatus,
                           String grnCode, boolean grnApproved,
                           String releaseType, String releaseNote, LocalDateTime releasedAt, String nextTripCode) {}

    /** A vehicle's visit to a project (as a hub, a destination or the return point). */
    public record SiteVisit(UUID tripId, String tripCode, String vehicleNo, String driverName,
                            String originProjectCode, String stopType, String stopStatus,
                            LocalDateTime arrivedAt, LocalDateTime departedAt, List<VisitGin> gins) {}

    /** Goods this project received after they left a hub (collected by its own vehicle, or sent by the hub's). */
    public record HubRelease(String ginCode, String hubProjectCode, String issuedProjectCode, String releaseType,
                             String releaseNote, LocalDateTime releasedAt, String grnCode, boolean grnApproved) {}

    public record SiteHistory(List<SiteVisit> visits, List<HubRelease> hubReleases) {}

    /** Trip info shown beside a GIN on the gate pass queue. */
    public record GinTripInfo(String tripCode, String vehicleNo) {}
}
