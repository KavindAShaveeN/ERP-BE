package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** One compliance expiry (insurance, revenue licence, emission test) of a vehicle asset. */
@Getter
@Setter
@AllArgsConstructor
public class AssetExpiryEvent {

    private String assetCode;
    private String description;
    private String registrationNumber;
    /** INSURANCE, REVENUE_LICENCE or EMISSION_TEST. */
    private String type;
    private LocalDate expiryDate;
}
