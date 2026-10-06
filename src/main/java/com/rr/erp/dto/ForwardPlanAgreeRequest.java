package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The shortfall plan the user explicitly agreed: per MR line, how much goes to Purchasing
 * (action FORWARD) and how much stays open as a back-order at this store (BACK_ORDER).
 */
@Getter
@Setter
public class ForwardPlanAgreeRequest {

    private String storeCode;
    private String agreedBy;
    private List<Row> rows;

    @Getter
    @Setter
    public static class Row {
        private UUID mrItemId;
        private BigDecimal qty;
        /** FORWARD or BACK_ORDER. */
        private String action;
    }
}
