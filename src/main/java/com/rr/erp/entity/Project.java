package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Integer projectId;

    @NotBlank(message = "Project code is required")
    private String projectCode;
    @NotBlank(message = "Project name is required")
    private String projectName;
    private Integer projectTypeId;
    /** Plant sub-type (Batching/Crusher/Asphalt/Sand) — only populated when
     * projectTypeId refers to the "Plant" project type. */
    private String plantType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal value;
    private BigDecimal finishValue;
    private String contractNumber;
    private String contactNumber;
    private String client;
    private String area;
    private String district;
    /** Project site locations — stored in the separate `project_location` table,
     * not a column on `project` itself. */
    private List<String> locations = new ArrayList<>();
    private String bomLink;
    private String boqLink;
    private Integer projectStatusId;
    private String description;
    private LocalDate actualEndDate;
    private String pmCode;
    private String siteEngCode;
    private String asstEngCode;
    private String adminCode;
    private String skCode;
    private String qsCode;
    private String clientContactPerson;
    private String clientContactNumber;
    private String clientEmail;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
