package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One document passing through a project's gate, for the stripped-down security-guard "Gate
 * Passes" view: everything the guard needs to recognise the shipment and tick it through,
 * nothing else. Backed by either a GIN or a stock return (docType tells them apart) — never
 * a GRN, since the arrival check always happens before a GRN exists.
 */
@Getter
@Setter
@AllArgsConstructor
public class GatePassEntry {

    private UUID id;
    private String docType;          // "GIN" | "RETURN"
    private String code;             // ginCode / stockReturnCode
    private LocalDateTime date;
    private String counterpartyProjectCode; // where it's going (outgoing) or coming from (incoming)
    private String vehicleNo;
    private int itemCount;
    private String tripCode;         // transport trip carrying it, null when not on a trip
    private String hubFor;           // for a "GIN_HUB" entry: the destination project it is only passing through the hub to
}
