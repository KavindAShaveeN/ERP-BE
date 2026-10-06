package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** One project/fuel-item row of the fuel consumption & distribution report. */
@Getter
@Setter
public class FuelConsumptionReport {

    private String projectCode;
    private String projectName;
    private String fuelItemCode;
    private String fuelItemName;
    private BigDecimal totalIssued;
    private Long issueCount;
}
