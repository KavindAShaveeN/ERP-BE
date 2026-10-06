package com.rr.erp.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantProduction {

    private UUID productionId;

    private String productionCode;

    // The plant is simply the project this production belongs to — see
    // PlantRecipe.projectCode for why there is no separate plant registration.
    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotNull(message = "Production date is required")
    private LocalDate productionDate;

    // Null for manual production with no predefined recipe (e.g. a crusher).
    private UUID recipeId;

    // DRAFT, SUBMITTED, APPROVED, REJECTED, REVERSED
    private String status = "DRAFT";

    private String remarks;

    private String submittedBy;
    private LocalDateTime submittedDate;

    private String approvedBy;
    private LocalDateTime approvedDate;

    private String rejectedBy;
    private LocalDateTime rejectedDate;
    private String rejectionReason;

    private String reversedBy;
    private LocalDateTime reversedDate;
    private String reversalReason;

    private String createdBy;
    private LocalDateTime createdDate;

    @Valid
    @NotEmpty(message = "At least one raw material input is required")
    private List<PlantProductionInput> inputs = new ArrayList<>();

    @Valid
    @NotEmpty(message = "At least one finished-good output is required")
    private List<PlantProductionOutput> outputs = new ArrayList<>();

    // Other (non-material) expenses; optional. Included in the finished goods' unit cost.
    @Valid
    private List<PlantProductionExpense> expenses = new ArrayList<>();
}
