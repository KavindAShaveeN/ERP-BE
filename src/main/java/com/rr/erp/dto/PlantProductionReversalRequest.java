package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantProductionReversalRequest {

    private String reversedBy;
    private String reason;
}
