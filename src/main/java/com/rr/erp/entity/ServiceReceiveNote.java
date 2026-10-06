package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ServiceReceiveNote {

    private UUID serviceReceiveNoteId;
    private String serviceReceiveNoteCode;
    private UUID jobCardId;
    private String jobCardCode;
    private String requestingProjectCode;
    private UUID serviceRequestId;
    private java.util.List<UUID> serviceRequestIds;
    private String assetCode;
    private String itemCode;
    private BigDecimal quantity;
    private LocalDateTime receivedDate;
    private String receivedBy;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
