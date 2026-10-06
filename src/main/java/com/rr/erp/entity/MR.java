package com.rr.erp.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MR {

    private UUID mrId;
    private String mrCode;

    @NotBlank(message = "Requesting project code is required")
    private String requestingProjectCode;

    @NotBlank(message = "Destination project code is required")
    private String destinationProjectCode;

    @NotNull(message = "Requested date is required")
    private LocalDateTime requestedDate;

    @NotBlank(message = "Requested by is required")
    private String requestedBy;

    private LocalDateTime checkedDate;

    private String checkedBy;

    private LocalDateTime approvedDate;

    private String approvedBy;

    private String remark;

    private Boolean isApproved;

    @Valid
    @NotEmpty(message = "At least one MR item is required")
    private List<MRItem> items = new ArrayList<>();
}