package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Getter
@Setter
public class FuelAssetSummary {

    private String assetCode;
    private String assetClass;

    private Long fuelGins;
    private BigDecimal totalConsumption;

    private Integer latestMeterReading;
    private Timestamp lastIssued;
}