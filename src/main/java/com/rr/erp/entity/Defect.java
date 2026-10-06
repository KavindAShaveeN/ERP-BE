package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Defect {

    private UUID defectId;

    private UUID jobCardId;

    private String defectDescription;
    private boolean reportedFault;
    private String outcome;
}