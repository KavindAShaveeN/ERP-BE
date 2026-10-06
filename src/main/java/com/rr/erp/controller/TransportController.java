package com.rr.erp.controller;

import com.rr.erp.dto.TransportDtos.ActionRequest;
import com.rr.erp.dto.TransportDtos.AddVehicleRequest;
import com.rr.erp.dto.TransportDtos.AvailableGin;
import com.rr.erp.dto.TransportDtos.CollectRequest;
import com.rr.erp.dto.TransportDtos.CustodyEvent;
import com.rr.erp.dto.TransportDtos.GinReceiptVehicle;
import com.rr.erp.dto.TransportDtos.HeldGin;
import com.rr.erp.dto.TransportDtos.SiteHistory;
import com.rr.erp.dto.TransportDtos.IncomingDelivery;
import com.rr.erp.dto.TransportDtos.StopTask;
import com.rr.erp.dto.TransportDtos.TripRequest;
import com.rr.erp.dto.TransportDtos.TripSummary;
import com.rr.erp.dto.TransportDtos.TripView;
import com.rr.erp.dto.TransportDtos.UndeliveredResult;
import com.rr.erp.dto.TransportDtos.UpdateVehicleRequest;
import com.rr.erp.dto.TransportDtos.VehicleCandidate;
import com.rr.erp.dto.TransportDtos.VehicleView;
import com.rr.erp.service.TransportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Transport plan: vehicle pool, trips, hub custody and receiver tracking. Planning endpoints take the
 * caller's active project as {@code actingProjectCode} (query param, or in the body) and are refused
 * unless that project is a Head Quarters or Warehouse project. The hub, incoming and custody reads are
 * open — they are what site users see.
 */
@RestController
@RequestMapping("/api/transport")
@CrossOrigin(origins = "*")
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    // ---- vehicle pool ----

    // GET /api/transport/vehicles?actingProjectCode=
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleView>> getVehicles(@RequestParam String actingProjectCode) {
        return ResponseEntity.ok(transportService.getVehicles(actingProjectCode));
    }

    // GET /api/transport/vehicles/candidates?actingProjectCode=
    @GetMapping("/vehicles/candidates")
    public ResponseEntity<List<VehicleCandidate>> getVehicleCandidates(@RequestParam String actingProjectCode) {
        return ResponseEntity.ok(transportService.getVehicleCandidates(actingProjectCode));
    }

    // POST /api/transport/vehicles
    @PostMapping("/vehicles")
    public ResponseEntity<VehicleView> addVehicle(@RequestBody AddVehicleRequest request) {
        return ResponseEntity.ok(transportService.addVehicle(request));
    }

    // PUT /api/transport/vehicles/{assetCode}
    @PutMapping("/vehicles/{assetCode}")
    public ResponseEntity<VehicleView> updateVehicle(
            @PathVariable String assetCode, @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(transportService.updateVehicle(assetCode, request));
    }

    // DELETE /api/transport/vehicles/{assetCode}?actingProjectCode=
    @DeleteMapping("/vehicles/{assetCode}")
    public ResponseEntity<Void> removeVehicle(
            @PathVariable String assetCode, @RequestParam String actingProjectCode) {
        transportService.removeVehicle(assetCode, actingProjectCode);
        return ResponseEntity.noContent().build();
    }

    // ---- GINs to allocate ----

    // GET /api/transport/gins/available?actingProjectCode=&originProjectCode=
    @GetMapping("/gins/available")
    public ResponseEntity<List<AvailableGin>> getAvailableGins(
            @RequestParam String actingProjectCode, @RequestParam String originProjectCode) {
        return ResponseEntity.ok(transportService.getAvailableGins(actingProjectCode, originProjectCode));
    }

    // ---- trips ----

    // GET /api/transport/trips?actingProjectCode=&status=
    @GetMapping("/trips")
    public ResponseEntity<List<TripSummary>> getTrips(
            @RequestParam String actingProjectCode, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(transportService.getTrips(actingProjectCode, status));
    }

    // GET /api/transport/trips/{tripId}?actingProjectCode=
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<TripView> getTrip(@PathVariable UUID tripId, @RequestParam String actingProjectCode) {
        return ResponseEntity.ok(transportService.getTrip(tripId, actingProjectCode));
    }

    // POST /api/transport/trips
    @PostMapping("/trips")
    public ResponseEntity<TripView> createTrip(@RequestBody TripRequest request) {
        return ResponseEntity.ok(transportService.createTrip(request));
    }

    // PUT /api/transport/trips/{tripId}  (only before the trip leaves)
    @PutMapping("/trips/{tripId}")
    public ResponseEntity<TripView> updateTrip(@PathVariable UUID tripId, @RequestBody TripRequest request) {
        return ResponseEntity.ok(transportService.updateTrip(tripId, request));
    }

    // POST /api/transport/trips/{tripId}/cancel
    @PostMapping("/trips/{tripId}/cancel")
    public ResponseEntity<TripView> cancelTrip(@PathVariable UUID tripId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.cancelTrip(tripId, request));
    }

    // POST /api/transport/trips/{tripId}/depart
    @PostMapping("/trips/{tripId}/depart")
    public ResponseEntity<TripView> depart(@PathVariable UUID tripId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.depart(tripId, request));
    }

    // POST /api/transport/trips/{tripId}/stops/{stopId}/arrive   (planner)
    @PostMapping("/trips/{tripId}/stops/{stopId}/arrive")
    public ResponseEntity<TripView> arriveAtStop(
            @PathVariable UUID tripId, @PathVariable UUID stopId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.arriveAtStop(tripId, stopId, request, false));
    }

    // POST /api/transport/trips/{tripId}/stops/{stopId}/unload   (planner; "unload-hub" kept for older callers)
    @PostMapping({"/trips/{tripId}/stops/{stopId}/unload", "/trips/{tripId}/stops/{stopId}/unload-hub"})
    public ResponseEntity<TripView> unloadAtStop(
            @PathVariable UUID tripId, @PathVariable UUID stopId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.unloadAtStop(tripId, stopId, request, false));
    }

    // POST /api/transport/trips/{tripId}/stops/{stopId}/depart   (planner)
    @PostMapping("/trips/{tripId}/stops/{stopId}/depart")
    public ResponseEntity<TripView> departFromStop(
            @PathVariable UUID tripId, @PathVariable UUID stopId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.departFromStop(tripId, stopId, request, false));
    }

    // ---- the stop's own project (a hub or destination) records its side, with real times ----

    // GET /api/transport/stops/{projectCode}
    @GetMapping("/stops/{projectCode}")
    public ResponseEntity<List<StopTask>> getStopTasks(@PathVariable String projectCode) {
        return ResponseEntity.ok(transportService.getStopTasks(projectCode));
    }

    // POST /api/transport/trips/{tripId}/stops/{stopId}/site-arrive
    @PostMapping("/trips/{tripId}/stops/{stopId}/site-arrive")
    public ResponseEntity<TripView> siteArrive(
            @PathVariable UUID tripId, @PathVariable UUID stopId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.arriveAtStop(tripId, stopId, request, true));
    }

    // POST /api/transport/trips/{tripId}/stops/{stopId}/site-unload
    @PostMapping("/trips/{tripId}/stops/{stopId}/site-unload")
    public ResponseEntity<TripView> siteUnload(
            @PathVariable UUID tripId, @PathVariable UUID stopId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.unloadAtStop(tripId, stopId, request, true));
    }

    // POST /api/transport/trips/{tripId}/stops/{stopId}/site-depart
    @PostMapping("/trips/{tripId}/stops/{stopId}/site-depart")
    public ResponseEntity<TripView> siteDepart(
            @PathVariable UUID tripId, @PathVariable UUID stopId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.departFromStop(tripId, stopId, request, true));
    }

    // POST /api/transport/trips/{tripId}/gins/{ginId}/undelivered
    @PostMapping("/trips/{tripId}/gins/{ginId}/undelivered")
    public ResponseEntity<UndeliveredResult> markUndelivered(
            @PathVariable UUID tripId, @PathVariable UUID ginId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.markUndelivered(tripId, ginId, request));
    }

    // POST /api/transport/trips/{tripId}/close
    @PostMapping("/trips/{tripId}/close")
    public ResponseEntity<TripView> closeTrip(@PathVariable UUID tripId, @RequestBody ActionRequest request) {
        return ResponseEntity.ok(transportService.closeTrip(tripId, request));
    }

    // ---- hub custody and receiver tracking (open reads) ----

    // GET /api/transport/hub/{projectCode}
    @GetMapping("/hub/{projectCode}")
    public ResponseEntity<List<HeldGin>> getHeldAtHub(@PathVariable String projectCode) {
        return ResponseEntity.ok(transportService.getHeldAtHub(projectCode));
    }

    // GET /api/transport/history/{projectCode}
    @GetMapping("/history/{projectCode}")
    public ResponseEntity<SiteHistory> getSiteHistory(@PathVariable String projectCode) {
        return ResponseEntity.ok(transportService.getSiteHistory(projectCode));
    }

    // GET /api/transport/incoming/{projectCode}
    @GetMapping("/incoming/{projectCode}")
    public ResponseEntity<List<IncomingDelivery>> getIncoming(@PathVariable String projectCode) {
        return ResponseEntity.ok(transportService.getIncoming(projectCode));
    }

    // POST /api/transport/gins/{ginId}/collect-from-hub  (destination or hub project; no trip needed)
    @PostMapping("/gins/{ginId}/collect-from-hub")
    public ResponseEntity<Void> collectFromHub(@PathVariable UUID ginId, @RequestBody CollectRequest request) {
        transportService.collectFromHub(ginId, request);
        return ResponseEntity.noContent().build();
    }

    // GET /api/transport/gins/{ginId}/receipt-vehicle
    @GetMapping("/gins/{ginId}/receipt-vehicle")
    public ResponseEntity<GinReceiptVehicle> getReceiptVehicle(@PathVariable UUID ginId) {
        return ResponseEntity.ok(transportService.getReceiptVehicle(ginId));
    }

    // GET /api/transport/gins/{ginId}/custody
    @GetMapping("/gins/{ginId}/custody")
    public ResponseEntity<List<CustodyEvent>> getCustodyEvents(@PathVariable UUID ginId) {
        return ResponseEntity.ok(transportService.getCustodyEvents(ginId));
    }
}
