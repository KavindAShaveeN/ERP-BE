package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** "Confirm plan": the allocation the storekeeper entered for each MR line (0 clears it). */
@Getter
@Setter
public class IssuePlanConfirmRequest {

    private String storeCode;
    private String confirmedBy;
    private List<Line> lines;

    @Getter
    @Setter
    public static class Line {
        private UUID mrItemId;
        private BigDecimal qty;
    }
}
