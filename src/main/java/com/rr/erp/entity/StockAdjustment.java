package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class StockAdjustment {

    private UUID stockAdjustmentId;

    private String stockAdjustmentCode;

    private String projectCode;

    private LocalDateTime adjustmentDate;

    private String reason;

    private String approvedBy;
    private LocalDateTime approvedDate;

    private Boolean isApproved;

    private String remarks;

    private List<StockAdjustmentItem> items;
}