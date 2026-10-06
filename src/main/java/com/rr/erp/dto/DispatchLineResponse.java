package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One reserved MR line, with the site and MR it belongs to — the building block of a
 * per-site dispatch GIN that covers several MRs going to the same site on one vehicle.
 */
@Getter
@Setter
public class DispatchLineResponse {

    private UUID mrId;
    private String mrCode;
    private UUID mrItemId;
    private String requestingProjectCode;
    private String requestingProjectName;
    private String itemCode;
    private String description;
    private String size;
    private Integer uomId;
    private String uomName;
    private BigDecimal allocatedQty;
    private String priority;
    private LocalDate requiredDate;
}
