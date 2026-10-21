package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** A finished product a Fabrication job card produces; credited to stock when the job is finished. */
@Getter
@Setter
public class JobCardOutput {
    private UUID jobCardOutputId;
    private UUID jobCardId;
    private String itemCode;
    /** Names the product on the stock batch, so different products under one item code stay distinct. */
    private String description;
    private BigDecimal quantity;
    /** Set when the output is posted to stock: the job's total cost spread over the output quantity. */
    private BigDecimal unitCost;
    /** Null until the job is finished and the output has been added to the store. */
    private LocalDateTime postedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
