package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Subcontractor {

    private Integer subcontractorId;
    private String subcontractorName;
    private String phoneNumber;
    private String email;
    private String line1;
    private String line2;
    private String city;
    private String district;
    private Boolean isActive;

    // Not a column on the subcontractor table — populated from
    // subcontractor_project for responses, and read from the request
    // body on create/update to replace the project assignments.
    private List<String> projectCodes;
}
