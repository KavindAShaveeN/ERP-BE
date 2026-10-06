package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MeterReading {

    private UUID meterReadingId;
    private String assetCode;
    private String meterType;
    private LocalDate readingDate;
    private BigDecimal readingValue;
    private BigDecimal previousReading;
    private BigDecimal usageValue;
    private String remarks;
    private String recordedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime editedAt;
}