package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProjectStore {

    private String projectCode;
    private String itemCode;

    private BigDecimal quantityOnHand;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQuantity;
    private String binLocation;

    private LocalDate lastReceivedDate;
    private LocalDate lastIssuedDate;
}