package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignedProject {

    private String projectCode;
    private String projectName;
    private Integer projectTypeId;
    private String projectTypeName;
}