package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Request body for POST /api/service-receive-note/ — created by the requesting project from
 * a delivered job card; the asset/item, requesting project and linked service request are all
 * pulled server-side from the job card rather than trusted from the client. */
@Getter
@Setter
public class ServiceReceiveNoteCreateRequest {

    private UUID jobCardId;
    private String receivedBy;
    private String remarks;
}
