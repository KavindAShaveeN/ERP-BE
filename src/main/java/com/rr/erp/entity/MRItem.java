package com.rr.erp.entity;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MRItem {

    private UUID mrItemId;

    private UUID mrId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    private String description;

    private String size;

    @NotNull(message = "UOM ID is required")
    private Integer uomId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    private String priority;

    // OPEN, PARTLY_ISSUED, CLOSED, BACK_ORDERED or FORWARDED — maintained by the issue-planning
    // flow and GIN authorization, never sent by the MR forms (see create_issue_planning_dev.sql).
    private String lineStatus;

    private String closedReason;

    private Integer priorityRank;

    @NotNull(message = "Required date is required")
    private LocalDate requiredDate;
}