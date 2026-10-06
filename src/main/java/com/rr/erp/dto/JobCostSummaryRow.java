package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** One job card (main or sub) with its cost and labour totals rolled up — the row shape
 * behind the maintenance history, equipment uptime/downtime and department summary reports. */
@Getter
@Setter
public class JobCostSummaryRow {

    private UUID jobCardId;
    private String jobCardCode;
    private UUID parentJobCardId;
    private String parentJobCardCode;
    private String assetCode;
    private String make;
    private String type;
    private String jobType;
    private String department;
    private String projectCode;
    private BigDecimal meterReading;
    private Integer jobStatusTypeId;
    private String jobStatusTypeName;
    private Boolean isFinished;
    private Boolean isDelivered;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime jobCheckedDate;

    /** Authorized JOB_CARD-type intra project issue lines (spare parts) — same basis as the job cost page. */
    private BigDecimal sparePartsCost;
    private BigDecimal thirdPartyCost;
    private BigDecimal additionalCost;
    private BigDecimal totalCost;

    private BigDecimal laborHours;
    private Integer workerCount;
}
