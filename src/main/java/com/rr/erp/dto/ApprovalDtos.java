package com.rr.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Request / response shapes for approval workflows and approval requests. */
public final class ApprovalDtos {

    private ApprovalDtos() {
    }

    // ---- workflow definition ----

    public record StepDef(Integer stepId, int stepOrder, Integer roleId, String roleName, String scope, String mode,
                          String conditionField, String conditionOp, BigDecimal conditionValue) {
    }

    public record WorkflowDto(Integer workflowId, String docType, String name, boolean active, List<StepDef> steps) {
    }

    public record StepRequest(@NotNull Integer roleId, String scope, String mode,
                              String conditionField, String conditionOp, BigDecimal conditionValue) {
    }

    public record WorkflowRequest(@NotBlank @Size(max = 150) String name, @NotEmpty List<StepRequest> steps) {
    }

    // ---- requests in flight / history ----

    public record StepStatus(int seq, Integer stepId, String roleName, String scope, String mode, boolean current) {
    }

    public record ActionDto(String actorCode, String action, String remark, LocalDateTime actedAt, Integer stepSeq) {
    }

    public record ApprovalRequestDto(UUID requestId, String docType, UUID docId, String status,
                                     String requestingProject, String destinationProject,
                                     String submittedBy, LocalDateTime submittedAt, LocalDateTime completedAt,
                                     List<StepStatus> steps, List<ActionDto> actions) {
    }

    public record DecisionRequest(@Size(max = 500) String remark) {
    }
}
