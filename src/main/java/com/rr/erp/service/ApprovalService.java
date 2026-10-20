package com.rr.erp.service;

import com.rr.erp.dto.ApprovalDtos.ActionDto;
import com.rr.erp.dto.ApprovalDtos.ApprovalRequestDto;
import com.rr.erp.dto.ApprovalDtos.StepDef;
import com.rr.erp.dto.ApprovalDtos.StepRequest;
import com.rr.erp.dto.ApprovalDtos.StepStatus;
import com.rr.erp.dto.ApprovalDtos.WorkflowDto;
import com.rr.erp.dto.ApprovalDtos.WorkflowRequest;
import com.rr.erp.repository.ApprovalRepository;
import com.rr.erp.repository.ApprovalRepository.RequestRow;
import com.rr.erp.repository.ApprovalRepository.RequestStep;
import com.rr.erp.repository.RbacRepository;
import com.rr.erp.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Runs the approval chain an Admin configured for a document type.
 *
 * Submit freezes the steps that apply (a step with a condition applies only when the condition
 * holds for the document), then each step needs an employee who holds the step's role in the step's
 * project AND the document's APPROVE permission. The submitter can never approve their own document.
 */
@Service
public class ApprovalService {

    public static final String APPROVE = "APPROVE";
    public static final String REJECT = "REJECT";

    /** Fields a step condition may test, per document type. */
    public static final Map<String, List<String>> CONDITION_FIELDS = Map.of(
            "MR", List.of("itemCount", "totalQuantity"));

    private final ApprovalRepository repository;
    private final RbacRepository rbacRepository;
    private final PermissionService permissionService;
    private final Map<String, ApprovalCallback> callbacks = new HashMap<>();

    public ApprovalService(ApprovalRepository repository, RbacRepository rbacRepository,
                           PermissionService permissionService, List<ApprovalCallback> callbackList) {
        this.repository = repository;
        this.rbacRepository = rbacRepository;
        this.permissionService = permissionService;
        callbackList.forEach(c -> callbacks.put(c.docType(), c));
    }


    // ------------------------------------------------------------------ submit

    /**
     * Starts approval for a document. If the document type has no workflow, or none of its steps
     * apply, the document is approved straight away.
     */
    @Transactional
    public UUID submit(String docType, UUID docId, String requestingProject, String destinationProject,
                       String submittedBy, Map<String, BigDecimal> context) {

        UUID requestId = repository.insertRequest(docType, docId, requestingProject, destinationProject, submittedBy);

        List<StepDef> applicable = repository.findActiveWorkflow(docType)
                .map(WorkflowDto::steps).orElse(List.of()).stream()
                .filter(step -> applies(step, context))
                .toList();

        repository.freezeSteps(requestId, applicable);

        if (applicable.isEmpty()) {
            repository.complete(requestId, "APPROVED");
            callback(docType).onApproved(docId, null);
        } else {
            repository.setCurrentStep(requestId, applicable.get(0).stepId());
        }
        return requestId;
    }

    static boolean applies(StepDef step, Map<String, BigDecimal> context) {
        if (step.conditionField() == null) {
            return true;
        }
        BigDecimal actual = context == null ? null : context.get(step.conditionField());
        if (actual == null) {
            return true; // unknown value: ask for the approval rather than skip it
        }
        int cmp = actual.compareTo(step.conditionValue());
        return switch (step.conditionOp()) {
            case ">" -> cmp > 0;
            case ">=" -> cmp >= 0;
            case "<" -> cmp < 0;
            case "<=" -> cmp <= 0;
            case "=" -> cmp == 0;
            default -> true;
        };
    }


    // --------------------------------------------------------------------- act

    @Transactional
    public ApprovalRequestDto approve(UUID requestId, String remark) {
        return act(requestId, APPROVE, remark);
    }

    @Transactional
    public ApprovalRequestDto reject(UUID requestId, String remark) {
        return act(requestId, REJECT, remark);
    }

    private ApprovalRequestDto act(UUID requestId, String action, String remark) {
        String actor = SecurityUtil.currentEmployeeCode();

        RequestRow request = repository.findRequestForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found"));

        if (!"PENDING".equals(request.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This request is already " + request.status().toLowerCase());
        }
        if (actor.equals(request.submittedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot approve or reject your own request");
        }

        List<RequestStep> steps = repository.findRequestSteps(requestId);
        RequestStep current = steps.stream()
                .filter(s -> s.step().stepId().equals(request.currentStepId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Request has no current step"));

        requireMayAct(actor, request, current.step(), action);

        repository.insertAction(requestId, current.step().stepId(), actor, action, remark);

        if (REJECT.equals(action)) {
            repository.complete(requestId, "REJECTED");
            callback(request.docType()).onRejected(request.docId(), actor, remark);
            return get(requestId);
        }

        if (!stepSatisfied(request, current.step())) {
            return get(requestId); // an ALL step still waiting for other approvers
        }

        RequestStep next = steps.stream().filter(s -> s.seq() == current.seq() + 1).findFirst().orElse(null);
        if (next == null) {
            repository.complete(requestId, "APPROVED");
            callback(request.docType()).onApproved(request.docId(), actor);
        } else {
            repository.setCurrentStep(requestId, next.step().stepId());
        }
        return get(requestId);
    }

    /** The project whose role assignments count for a step; null when the step accepts any project. */
    private static String stepProject(RequestRow request, StepDef step) {
        return switch (step.scope()) {
            case "DESTINATION_PROJECT" -> request.destinationProject() != null
                    ? request.destinationProject() : request.requestingProject();
            case "ANY" -> null;
            default -> request.requestingProject();
        };
    }

    private void requireMayAct(String actor, RequestRow request, StepDef step, String action) {
        String project = stepProject(request, step);
        boolean anyProject = project == null;

        List<String> eligible = repository.findEligibleApprovers(step.roleId(), project, anyProject);
        if (!eligible.contains(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This step needs the " + step.roleName() + " role" + (anyProject ? "" : " in project " + project));
        }
        String permission = request.docType() + "." + action;
        if (!permissionService.hasFor(actor, permission, project, anyProject)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission: " + permission);
        }
    }

    private boolean stepSatisfied(RequestRow request, StepDef step) {
        if ("ANY_ONE".equals(step.mode())) {
            return true;
        }
        // ALL: every eligible approver (except the submitter) must have approved
        String project = stepProject(request, step);
        Set<String> approved = Set.copyOf(repository.findApprovers(request.requestId(), step.stepId()));
        return repository.findEligibleApprovers(step.roleId(), project, project == null).stream()
                .filter(e -> !e.equals(request.submittedBy()))
                .allMatch(approved::contains);
    }


    // ------------------------------------------------------------------- reads

    public ApprovalRequestDto get(UUID requestId) {
        RequestRow request = repository.findRequest(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found"));
        return toDto(request);
    }

    public ApprovalRequestDto latestFor(String docType, UUID docId) {
        return repository.findLatestForDoc(docType, docId).map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No approval request for this document"));
    }

    /** Drops the approval history of a document that is being deleted. */
    @Transactional
    public void deleteForDocument(String docType, UUID docId) {
        repository.deleteForDocument(docType, docId);
    }

    /** Requests waiting on the logged-in employee. */
    public List<ApprovalRequestDto> pendingForMe() {
        String me = SecurityUtil.currentEmployeeCode();
        return repository.findPendingFor(me).stream()
                .filter(r -> {
                    StepDef step = repository.findRequestSteps(r.requestId()).stream()
                            .map(RequestStep::step).filter(s -> s.stepId().equals(r.currentStepId())).findFirst().orElse(null);
                    if (step == null) {
                        return false;
                    }
                    String project = stepProject(r, step);
                    return permissionService.hasFor(me, r.docType() + "." + APPROVE, project, project == null);
                })
                .map(this::toDto)
                .toList();
    }

    private ApprovalRequestDto toDto(RequestRow r) {
        List<StepStatus> steps = repository.findRequestSteps(r.requestId()).stream()
                .map(s -> new StepStatus(s.seq(), s.step().stepId(), s.step().roleName(), s.step().scope(), s.step().mode(),
                        s.step().stepId().equals(r.currentStepId())))
                .toList();
        List<ActionDto> actions = repository.findActions(r.requestId());
        return new ApprovalRequestDto(r.requestId(), r.docType(), r.docId(), r.status(), r.requestingProject(),
                r.destinationProject(), r.submittedBy(), r.submittedAt(), r.completedAt(), steps, actions,
                repository.findDocCode(r.docType(), r.docId()).orElse(null),
                repository.findEmployeeName(r.submittedBy()).orElse(null));
    }

    private ApprovalCallback callback(String docType) {
        ApprovalCallback cb = callbacks.get(docType);
        if (cb == null) {
            throw new IllegalStateException("No approval callback registered for " + docType);
        }
        return cb;
    }


    // ------------------------------------------------------- workflow management

    public List<WorkflowDto> listWorkflows() {
        return repository.findActiveWorkflows();
    }

    public Map<String, List<String>> conditionFields() {
        return CONDITION_FIELDS;
    }

    @Transactional
    public WorkflowDto saveWorkflow(String docType, WorkflowRequest request) {
        if (!callbacks.containsKey(docType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approval is not available for document type " + docType);
        }
        for (StepRequest s : request.steps()) {
            validateStep(docType, s);
        }
        repository.replaceWorkflow(docType, request.name().trim(), request.steps());
        rbacRepository.audit(SecurityUtil.currentEmployeeCodeOrNull(), "WORKFLOW", docType,
                "saved workflow '" + request.name().trim() + "' with " + request.steps().size() + " steps");
        return repository.findActiveWorkflow(docType).orElseThrow();
    }

    private void validateStep(String docType, StepRequest s) {
        if (!repository.roleExists(s.roleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown or inactive role: " + s.roleId());
        }
        if (s.scope() != null && !Set.of("SAME_PROJECT", "DESTINATION_PROJECT", "ANY").contains(s.scope())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid scope: " + s.scope());
        }
        if (s.mode() != null && !Set.of("ANY_ONE", "ALL").contains(s.mode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mode: " + s.mode());
        }
        boolean hasField = s.conditionField() != null && !s.conditionField().isBlank();
        boolean hasOp = s.conditionOp() != null && !s.conditionOp().isBlank();
        if (hasField != hasOp || hasField != (s.conditionValue() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A condition needs a field, an operator and a value");
        }
        if (hasField) {
            if (!CONDITION_FIELDS.getOrDefault(docType, List.of()).contains(s.conditionField())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown condition field: " + s.conditionField());
            }
            if (!Set.of(">", ">=", "<", "<=", "=").contains(s.conditionOp())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operator: " + s.conditionOp());
            }
        }
    }
}
