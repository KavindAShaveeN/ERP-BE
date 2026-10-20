package com.rr.erp.controller;

import com.rr.erp.dto.ApprovalDtos.ApprovalRequestDto;
import com.rr.erp.dto.ApprovalDtos.DecisionRequest;
import com.rr.erp.dto.ApprovalDtos.WorkflowDto;
import com.rr.erp.dto.ApprovalDtos.WorkflowRequest;
import com.rr.erp.service.ApprovalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Approval inbox, approve / reject, and the Admin-configurable workflows. */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }


    // GET /api/approvals/pending - requests waiting on the logged-in user
    @GetMapping("/approvals/pending")
    public List<ApprovalRequestDto> pending() {
        return approvalService.pendingForMe();
    }

    // GET /api/approvals/MR/{docId} - latest approval request (with timeline) for a document
    @GetMapping("/approvals/{docType}/{docId}")
    public ApprovalRequestDto forDocument(@PathVariable String docType, @PathVariable UUID docId) {
        return approvalService.latestFor(docType.toUpperCase(), docId);
    }

    @PostMapping("/approvals/{requestId}/approve")
    public ApprovalRequestDto approve(@PathVariable UUID requestId, @Valid @RequestBody(required = false) DecisionRequest body) {
        return approvalService.approve(requestId, body == null ? null : body.remark());
    }

    @PostMapping("/approvals/{requestId}/reject")
    public ApprovalRequestDto reject(@PathVariable UUID requestId, @Valid @RequestBody(required = false) DecisionRequest body) {
        return approvalService.reject(requestId, body == null ? null : body.remark());
    }


    @GetMapping("/workflows")
    @PreAuthorize("@perm.has('WORKFLOW.MANAGE')")
    public List<WorkflowDto> workflows() {
        return approvalService.listWorkflows();
    }

    // GET /api/workflows/condition-fields - which fields a step condition can test, per document type
    @GetMapping("/workflows/condition-fields")
    @PreAuthorize("@perm.has('WORKFLOW.MANAGE')")
    public Map<String, List<String>> conditionFields() {
        return approvalService.conditionFields();
    }

    // PUT /api/workflows/MR - replaces the chain for that document type
    @PutMapping("/workflows/{docType}")
    @PreAuthorize("@perm.has('WORKFLOW.MANAGE')")
    public WorkflowDto saveWorkflow(@PathVariable String docType, @Valid @RequestBody WorkflowRequest request) {
        return approvalService.saveWorkflow(docType.toUpperCase(), request);
    }
}
