package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProjectResponseDTO {

    private Integer projectId;
    private String projectCode;
    private String projectName;
    private Integer projectTypeId;
    private String projectTypeName;
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
    private List<String> locations;
    private String bomLink;
    private String boqLink;
    private Integer projectStatusId;
    private String projectStatusName;
    private String description;
    private LocalDate actualEndDate;
    private String pmCode;
    private String pmName;
    private String siteEngCode;
    private String siteEngName;
    private String asstEngCode;
    private String asstEngName;
    private String adminCode;
    private String adminName;
    private String skCode;
    private String skName;
    private String qsCode;
    private String qsName;
    private String clientContactPerson;
    private String clientContactNumber;
    private String clientEmail;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
