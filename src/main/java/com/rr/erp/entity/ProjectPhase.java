package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectPhase {

    private Integer projectPhaseId;
    private Integer projectId;
    /** Parent phase within the same project — null for a top-level phase. Lets phases
     * nest arbitrarily deep (sub phase, sub-sub phase, sub-sub-sub phase, ...). */
    private Integer parentProjectPhaseId;
    private String projectPhaseCode;
    private String projectPhaseName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer statusId;
}