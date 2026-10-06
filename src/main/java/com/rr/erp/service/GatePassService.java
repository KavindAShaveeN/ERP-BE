package com.rr.erp.service;

import com.rr.erp.dto.GatePassEntry;
import com.rr.erp.dto.GatePassResponse;
import com.rr.erp.dto.TransportDtos.GinTripInfo;
import com.rr.erp.entity.GIN;
import com.rr.erp.entity.StockReturn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aggregates GIN and stock-return traffic into the single "Gate Passes" queue security sees for
 * a project's gate — what's about to leave (outgoing) and what has left its source but hasn't
 * been confirmed arriving here yet (incoming). GRNs are deliberately left out: the arrival check
 * always happens before a GRN exists (see GINService#verifyGinArrivalAtGate).
 */
@Service
public class GatePassService {

    public static final String DOC_GIN = "GIN";
    public static final String DOC_RETURN = "RETURN";
    /** A GIN arriving at this gate only to be held as a hub (its destination is another project). */
    public static final String DOC_GIN_HUB = "GIN_HUB";

    private final GINService ginService;
    private final StockReturnService stockReturnService;
    private final TransportService transportService;

    public GatePassService(GINService ginService, StockReturnService stockReturnService,
                           TransportService transportService) {
        this.ginService = ginService;
        this.stockReturnService = stockReturnService;
        this.transportService = transportService;
    }

    public GatePassResponse getGatePass(String projectCode) {

        List<GIN> outgoingGins = ginService.getPendingExitGate(projectCode);
        List<GIN> incomingGins = ginService.getPendingArrivalGate(projectCode);

        // Trip + vehicle for GINs that are on a transport trip, so security sees which vehicle to expect.
        Map<UUID, GinTripInfo> tripInfo = new HashMap<>(transportService.getTripInfo(
                Stream.concat(outgoingGins.stream(), incomingGins.stream()).map(GIN::getGinId).toList()));

        // Outgoing: this project is always the GIN's issuing / return's sending project, so
        // the counterparty to show is where it's headed.
        List<GatePassEntry> outgoing = Stream.concat(
                outgoingGins.stream()
                        .map(gin -> fromGin(gin, gin.getReceivedProjectCode(), tripInfo.get(gin.getGinId()))),
                stockReturnService.getPendingExitGate(projectCode).stream()
                        .map(stockReturn -> fromReturn(stockReturn, stockReturn.getToProjectCode()))
        ).collect(Collectors.toList());

        // Incoming: this project is always the GIN's/return's destination, so the counterparty
        // to show is where it came from.
        List<GatePassEntry> incoming = Stream.concat(
                incomingGins.stream()
                        .map(gin -> fromGin(gin, gin.getIssuedProjectCode(), tripInfo.get(gin.getGinId()))),
                stockReturnService.getPendingArrivalGate(projectCode).stream()
                        .map(stockReturn -> fromReturn(stockReturn, stockReturn.getFromProjectCode()))
        ).collect(Collectors.toList());

        // Deliveries heading to this project as a hub: the vehicle arriving here to unload goods that belong
        // to another project. (Goods held here awaiting collection are not listed — nothing is arriving.)
        transportService.getHubArrivals(projectCode).forEach(arrival -> incoming.add(new GatePassEntry(
                arrival.ginId(), DOC_GIN_HUB, arrival.ginCode(), arrival.issuedDate(),
                arrival.issuedProjectCode(), arrival.vehicleNo(), arrival.itemCount(), arrival.tripCode(),
                arrival.destinationProjectCode())));

        return new GatePassResponse(outgoing, incoming);
    }

    public void confirmExit(String docType, UUID id, String gateVerifiedBy) {
        switch (docType) {
            case DOC_GIN -> ginService.verifyGinAtGate(id, gateVerifiedBy);
            case DOC_RETURN -> stockReturnService.verifyStockReturnAtGate(id, gateVerifiedBy);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown gate pass type: " + docType);
        }
    }

    public void confirmArrival(String docType, UUID id, String gateVerifiedBy) {
        switch (docType) {
            case DOC_GIN -> ginService.verifyGinArrivalAtGate(id, gateVerifiedBy);
            case DOC_GIN_HUB -> transportService.confirmHubArrival(id);
            case DOC_RETURN -> stockReturnService.verifyStockReturnArrivalAtGate(id, gateVerifiedBy);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown gate pass type: " + docType);
        }
    }

    private static GatePassEntry fromGin(GIN gin, String counterpartyProjectCode, GinTripInfo trip) {
        return new GatePassEntry(
                gin.getGinId(),
                DOC_GIN,
                gin.getGinCode(),
                gin.getIssuedDate(),
                counterpartyProjectCode,
                trip != null ? trip.vehicleNo() : gin.getVehicleNo(),
                gin.getItems() != null ? gin.getItems().size() : 0,
                trip != null ? trip.tripCode() : null,
                null
        );
    }

    private static GatePassEntry fromReturn(StockReturn stockReturn, String counterpartyProjectCode) {
        return new GatePassEntry(
                stockReturn.getStockReturnId(),
                DOC_RETURN,
                stockReturn.getStockReturnCode(),
                stockReturn.getReturnDate(),
                counterpartyProjectCode,
                stockReturn.getVehicleNo(),
                stockReturn.getItems() != null ? stockReturn.getItems().size() : 0,
                null,
                null
        );
    }
}
