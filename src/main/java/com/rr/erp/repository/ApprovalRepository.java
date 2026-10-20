package com.rr.erp.repository;

import com.rr.erp.dto.ApprovalDtos.ActionDto;
import com.rr.erp.dto.ApprovalDtos.StepDef;
import com.rr.erp.dto.ApprovalDtos.StepRequest;
import com.rr.erp.dto.ApprovalDtos.WorkflowDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ApprovalRepository {

    /** A request row. */
    public record RequestRow(UUID requestId, String docType, UUID docId, String status,
                             String requestingProject, String destinationProject, Integer currentStepId,
                             String submittedBy, LocalDateTime submittedAt, LocalDateTime completedAt) {
    }

    /** One step frozen into a request, with its position. */
    public record RequestStep(int seq, StepDef step) {
    }

    private final JdbcTemplate jdbc;

    public ApprovalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    // ---- workflow definitions ----

    private static final String STEP_SELECT = """
            SELECT s.step_id, s.step_order, s.role_id, r.name AS role_name, s.scope, s.mode,
                   s.condition_field, s.condition_op, s.condition_value
            FROM approval_steps s JOIN roles r ON r.role_id = s.role_id
            """;

    private static final RowMapper<StepDef> STEP_MAPPER = (rs, i) -> new StepDef(
            rs.getInt("step_id"), rs.getInt("step_order"), rs.getInt("role_id"), rs.getString("role_name"),
            rs.getString("scope"), rs.getString("mode"),
            rs.getString("condition_field"), rs.getString("condition_op"), rs.getBigDecimal("condition_value"));

    public Optional<WorkflowDto> findActiveWorkflow(String docType) {
        List<WorkflowDto> list = jdbc.query("""
                SELECT workflow_id, doc_type, name, is_active FROM approval_workflows
                WHERE doc_type = ? AND is_active
                """, workflowMapper(), docType);
        return list.stream().findFirst();
    }

    public List<WorkflowDto> findActiveWorkflows() {
        return jdbc.query("""
                SELECT workflow_id, doc_type, name, is_active FROM approval_workflows
                WHERE is_active ORDER BY doc_type
                """, workflowMapper());
    }

    private RowMapper<WorkflowDto> workflowMapper() {
        return (rs, i) -> {
            int id = rs.getInt("workflow_id");
            return new WorkflowDto(id, rs.getString("doc_type"), rs.getString("name"), rs.getBoolean("is_active"),
                    findSteps(id));
        };
    }

    public List<StepDef> findSteps(int workflowId) {
        return jdbc.query(STEP_SELECT + " WHERE s.workflow_id = ? ORDER BY s.step_order", STEP_MAPPER, workflowId);
    }

    /** Retire the current workflow (in-flight requests keep their frozen steps) and start a new one. */
    public int replaceWorkflow(String docType, String name, List<StepRequest> steps) {
        jdbc.update("UPDATE approval_workflows SET is_active = false WHERE doc_type = ? AND is_active", docType);
        Integer workflowId = jdbc.queryForObject("""
                INSERT INTO approval_workflows (doc_type, name, is_active) VALUES (?, ?, true) RETURNING workflow_id
                """, Integer.class, docType, name);
        int order = 1;
        for (StepRequest s : steps) {
            jdbc.update("""
                    INSERT INTO approval_steps (workflow_id, step_order, role_id, scope, mode,
                                                condition_field, condition_op, condition_value)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, workflowId, order++, s.roleId(),
                    s.scope() == null ? "SAME_PROJECT" : s.scope(),
                    s.mode() == null ? "ANY_ONE" : s.mode(),
                    blankToNull(s.conditionField()), blankToNull(s.conditionOp()), s.conditionValue());
        }
        return workflowId == null ? 0 : workflowId;
    }

    public boolean roleExists(int roleId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM roles WHERE role_id = ? AND is_active", Integer.class, roleId);
        return n != null && n > 0;
    }


    // ---- requests ----

    private static final RowMapper<RequestRow> REQUEST_MAPPER = (rs, i) -> toRequestRow(rs);

    private static RequestRow toRequestRow(ResultSet rs) throws SQLException {
        int step = rs.getInt("current_step_id");
        Integer currentStep = rs.wasNull() ? null : step;
        return new RequestRow(
                rs.getObject("request_id", UUID.class), rs.getString("doc_type"), rs.getObject("doc_id", UUID.class),
                rs.getString("status"), rs.getString("requesting_project"), rs.getString("destination_project"),
                currentStep, rs.getString("submitted_by"),
                rs.getObject("submitted_at", LocalDateTime.class), rs.getObject("completed_at", LocalDateTime.class));
    }

    private static final String REQUEST_SELECT = """
            SELECT request_id, doc_type, doc_id, status, requesting_project, destination_project,
                   current_step_id, submitted_by, submitted_at, completed_at
            FROM approval_requests
            """;

    public Optional<RequestRow> findRequest(UUID requestId) {
        return jdbc.query(REQUEST_SELECT + " WHERE request_id = ?", REQUEST_MAPPER, requestId).stream().findFirst();
    }

    /** Locks the row so two approvers acting at once are handled one after the other. */
    public Optional<RequestRow> findRequestForUpdate(UUID requestId) {
        return jdbc.query(REQUEST_SELECT + " WHERE request_id = ? FOR UPDATE", REQUEST_MAPPER, requestId).stream().findFirst();
    }

    /** Latest request for a document. */
    public Optional<RequestRow> findLatestForDoc(String docType, UUID docId) {
        return jdbc.query(REQUEST_SELECT + " WHERE doc_type = ? AND doc_id = ? ORDER BY submitted_at DESC LIMIT 1",
                REQUEST_MAPPER, docType, docId).stream().findFirst();
    }

    public UUID insertRequest(String docType, UUID docId, String requestingProject, String destinationProject,
                              String submittedBy) {
        return jdbc.queryForObject("""
                INSERT INTO approval_requests (doc_type, doc_id, requesting_project, destination_project, submitted_by)
                VALUES (?, ?, ?, ?, ?) RETURNING request_id
                """, UUID.class, docType, docId, requestingProject, destinationProject, submittedBy);
    }

    public void freezeSteps(UUID requestId, List<StepDef> steps) {
        int seq = 1;
        for (StepDef s : steps) {
            jdbc.update("INSERT INTO approval_request_steps (request_id, step_id, seq) VALUES (?, ?, ?)",
                    requestId, s.stepId(), seq++);
        }
    }

    public List<RequestStep> findRequestSteps(UUID requestId) {
        return jdbc.query(STEP_SELECT + """
                JOIN approval_request_steps rq ON rq.step_id = s.step_id
                WHERE rq.request_id = ? ORDER BY rq.seq
                """, (rs, i) -> new RequestStep(i + 1, STEP_MAPPER.mapRow(rs, i)), requestId);
    }

    public void setCurrentStep(UUID requestId, Integer stepId) {
        jdbc.update("UPDATE approval_requests SET current_step_id = ? WHERE request_id = ?", stepId, requestId);
    }

    public void complete(UUID requestId, String status) {
        jdbc.update("""
                UPDATE approval_requests SET status = ?, current_step_id = NULL, completed_at = now()
                WHERE request_id = ?
                """, status, requestId);
    }

    public void insertAction(UUID requestId, Integer stepId, String actor, String action, String remark) {
        jdbc.update("""
                INSERT INTO approval_actions (request_id, step_id, actor_code, action, remark) VALUES (?, ?, ?, ?, ?)
                """, requestId, stepId, actor, action, remark);
    }

    public List<ActionDto> findActions(UUID requestId) {
        return jdbc.query("""
                SELECT a.actor_code, a.action, a.remark, a.acted_at, rs.seq
                FROM approval_actions a
                LEFT JOIN approval_request_steps rs ON rs.request_id = a.request_id AND rs.step_id = a.step_id
                WHERE a.request_id = ? ORDER BY a.acted_at, a.action_id
                """, (rs, i) -> {
            int seq = rs.getInt("seq");
            return new ActionDto(rs.getString("actor_code"), rs.getString("action"), rs.getString("remark"),
                    rs.getObject("acted_at", LocalDateTime.class), rs.wasNull() ? null : seq);
        }, requestId);
    }

    /** Employees who approved this step. */
    public List<String> findApprovers(UUID requestId, int stepId) {
        return jdbc.queryForList("""
                SELECT DISTINCT actor_code FROM approval_actions
                WHERE request_id = ? AND step_id = ? AND action = 'APPROVE'
                """, String.class, requestId, stepId);
    }

    /** Active employees holding the step's role in the step's project (or anywhere for scope ANY). */
    public List<String> findEligibleApprovers(int roleId, String projectCode, boolean anyProject) {
        return jdbc.queryForList("""
                SELECT DISTINCT ur.employee_code FROM user_roles ur
                JOIN roles r ON r.role_id = ur.role_id AND r.is_active
                WHERE ur.role_id = ?
                  AND (? OR ur.project_code IS NULL OR ur.project_code = ?)
                """, String.class, roleId, anyProject, projectCode);
    }

    /** Pending requests whose current step the employee could act on (before the permission check). */
    public List<RequestRow> findPendingFor(String employeeCode) {
        return jdbc.query("""
                SELECT r.request_id, r.doc_type, r.doc_id, r.status, r.requesting_project, r.destination_project,
                       r.current_step_id, r.submitted_by, r.submitted_at, r.completed_at
                FROM approval_requests r
                JOIN approval_steps s ON s.step_id = r.current_step_id
                WHERE r.status = 'PENDING' AND r.submitted_by <> ?
                  AND EXISTS (
                      SELECT 1 FROM user_roles ur JOIN roles ro ON ro.role_id = ur.role_id AND ro.is_active
                      WHERE ur.employee_code = ? AND ur.role_id = s.role_id
                        AND (s.scope = 'ANY' OR ur.project_code IS NULL OR ur.project_code =
                             CASE s.scope WHEN 'DESTINATION_PROJECT' THEN COALESCE(r.destination_project, r.requesting_project)
                                          ELSE r.requesting_project END))
                  AND NOT EXISTS (
                      SELECT 1 FROM approval_actions a
                      WHERE a.request_id = r.request_id AND a.step_id = s.step_id
                        AND a.actor_code = ? AND a.action = 'APPROVE')
                ORDER BY r.submitted_at
                """, REQUEST_MAPPER, employeeCode, employeeCode, employeeCode);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
