package com.rr.erp.service;

import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.ServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;

/** Fault ownership is persisted, never inferred from description text or a job's remarks. */
@Service
@RequiredArgsConstructor
public class FaultWorkflowService {
    private final JdbcTemplate jdbc;

    public record FaultType(String faultCode, String label, String category) {}
    public record FaultReport(UUID requestFaultId, UUID serviceRequestId, String serviceRequestCode,
            String assetCode, String projectCode, String operatorName, String requestedDate,
            String faultCode, String description, String category, String status, UUID jobCardId,
            String jobCardCode, String requestType, String remarks) {}
    public record AssetBacklog(String assetCode, long requestCount, long faultTypeCount, long pendingCount,
            long assignedCount, long reviewCount, String oldestDate, long openJobs,
            String latestRequestDate, String requestedBy) {}
    public record BacklogPage(List<AssetBacklog> items, long total, int page, int size) {}
    public record WorkItem(UUID defectId, String description, String outcome, String reason,
            List<FaultReport> reports) {}

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
    public List<FaultType> types() {
        return jdbc.query("SELECT fault_code,label,category FROM fault_type WHERE is_active ORDER BY category,label",
                (rs, n) -> new FaultType(rs.getString(1), rs.getString(2), rs.getString(3)));
    }

    /** Called within the request write transaction; existing assignments make the request immutable. */
    public void lockRequestForEdit(ServiceRequest request) {
        var rows = jdbc.queryForList("SELECT asset_code,maintenance_works,is_approved FROM service_request WHERE service_request_id=? FOR UPDATE", request.getServiceRequestId());
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service request not found");
        boolean tracked = Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS(SELECT 1 FROM service_request_fault f JOIN job_defect_source s USING(request_fault_id)
                              WHERE f.service_request_id=?)
                    OR EXISTS(SELECT 1 FROM job_card WHERE service_request_id=?)
                    OR EXISTS(SELECT 1 FROM service_request_fault WHERE service_request_id=? AND reviewed_at IS NOT NULL)
                """, Boolean.class, request.getServiceRequestId(), request.getServiceRequestId(), request.getServiceRequestId()));
        var old = rows.get(0);
        if (tracked && (!Objects.equals(old.get("asset_code"), request.getAssetCode())
                || !Objects.equals(old.get("maintenance_works"), request.getMaintenanceWorks())
                || !Objects.equals(old.get("is_approved"), request.getIsApproved()))) {
            throw conflict("Faults, asset and approval cannot be changed after a request is linked to work.");
        }
    }

    public void syncRequest(ServiceRequest request) {
        List<String> labels = Arrays.stream(Objects.toString(request.getMaintenanceWorks(), "").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        String placeholder = URGENT_PLACEHOLDERS.get(request.getRequestType());
        if (labels.isEmpty() && placeholder == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one fault");
        // A breakdown/accident without catalogue faults is tracked as one general report so it can still be assigned to a job.
        if (labels.isEmpty()) labels = List.of(placeholder);
        var existing = jdbc.queryForList("SELECT description FROM service_request_fault WHERE service_request_id=?", String.class, request.getServiceRequestId());
        for (String label : labels) {
            if (existing.contains(label)) continue;
            var codes = jdbc.queryForList("SELECT fault_code FROM fault_type WHERE label=? AND is_active", String.class, label);
            if (label.equals(placeholder)) {
                jdbc.update("INSERT INTO service_request_fault(request_fault_id,service_request_id,fault_code,description) VALUES (?,?,NULL,?)",
                        UUID.randomUUID(), request.getServiceRequestId(), label);
                continue;
            }
            if (codes.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown fault: " + label);
            jdbc.update("INSERT INTO service_request_fault(request_fault_id,service_request_id,fault_code,description) VALUES (?,?,?,?)",
                    UUID.randomUUID(), request.getServiceRequestId(), codes.get(0), label);
        }
        for (String label : existing) {
            if (!labels.contains(label)) jdbc.update("DELETE FROM service_request_fault WHERE service_request_id=? AND description=?", request.getServiceRequestId(), label);
        }
    }

    private static final Map<String,String> URGENT_PLACEHOLDERS = Map.of(
            "BREAKDOWN", "Breakdown reported (see remarks)", "ACCIDENT", "Accident reported (see remarks)");

    private static final String REPORT_QUERY = """
            SELECT f.request_fault_id,f.service_request_id,r.service_request_code,r.asset_code,r.project_code,r.request_type,r.remarks,
                   r.operator_name,r.requested_date,f.fault_code,f.description,COALESCE(t.category,'Other') category,
                   CASE WHEN f.review_required THEN 'REVIEW' WHEN f.reviewed_resolved THEN 'RESOLVED' WHEN s.request_fault_id IS NULL THEN 'PENDING'
                        ELSE d.outcome END status,j.job_card_id,j.job_card_code
            FROM service_request_fault f JOIN service_request r USING(service_request_id)
            LEFT JOIN fault_type t USING(fault_code)
            LEFT JOIN job_defect_source s ON s.request_fault_id=f.request_fault_id AND s.released_at IS NULL
            LEFT JOIN defect d ON d.defect_id=s.defect_id
            LEFT JOIN job_card j ON j.job_card_id=d.job_card_id
            """;

    private List<FaultReport> reports(String clause, Object... args) {
        return jdbc.query(REPORT_QUERY + clause + " ORDER BY r.requested_date,f.description,f.request_fault_id", (rs, n) -> new FaultReport(
                rs.getObject("request_fault_id", UUID.class), rs.getObject("service_request_id", UUID.class),
                rs.getString("service_request_code"),rs.getString("asset_code"),rs.getString("project_code"),
                rs.getString("operator_name"),rs.getString("requested_date"),rs.getString("fault_code"),
                rs.getString("description"),rs.getString("category"),rs.getString("status"),
                rs.getObject("job_card_id", UUID.class),rs.getString("job_card_code"),rs.getString("request_type"),rs.getString("remarks")), args);
    }
    public List<FaultReport> assetFaults(String asset) { return reports(" WHERE r.is_approved AND r.asset_code=?", asset); }
    public List<FaultReport> requestFaults(UUID id) { return reports(" WHERE r.service_request_id=?", id); }

    public BacklogPage backlog(String query, int page, int size) {
        page = Math.max(0, page); size = Math.max(1, Math.min(100, size));
        String search = "%" + Objects.toString(query, "").trim().toLowerCase() + "%";
        String base = """
                WITH reports AS (
                """ + REPORT_QUERY + """
                 WHERE r.is_approved
                ), assets AS (
                  SELECT asset_code,count(DISTINCT service_request_id) request_count,
                    count(DISTINCT COALESCE(fault_code,description)) FILTER (WHERE status<>'RESOLVED') fault_count,
                    count(*) FILTER (WHERE status='PENDING') pending_count,
                    count(*) FILTER (WHERE status='ASSIGNED') assigned_count,
                    count(*) FILTER (WHERE status='REVIEW') review_count,
                    min(requested_date) FILTER (WHERE status IN ('PENDING','REVIEW')) oldest_date
                  FROM reports GROUP BY asset_code
                  HAVING bool_or(status<>'RESOLVED') AND bool_or(lower(asset_code || ' ' || project_code || ' ' || description || ' ' || COALESCE(service_request_code,'')) LIKE ?)
                )
                """;
        long total = Objects.requireNonNull(jdbc.queryForObject(base + "SELECT count(*) FROM assets", Long.class, search));
        var items = jdbc.query(base + """
                SELECT a.*,(SELECT count(*) FROM job_card j WHERE j.asset_code=a.asset_code AND NOT COALESCE(j.is_finished,false) AND NOT COALESCE(j.is_delivered,false)) open_jobs,
                       lr.requested_date latest_request_date,COALESCE(e.full_name,lr.submitted_by) requested_by
                FROM assets a
                LEFT JOIN LATERAL (SELECT r.requested_date,r.submitted_by FROM service_request r WHERE r.asset_code=a.asset_code AND r.is_approved
                                   ORDER BY r.requested_date DESC LIMIT 1) lr ON true
                LEFT JOIN employee e ON e.employee_code=lr.submitted_by
                ORDER BY oldest_date NULLS LAST,asset_code LIMIT ? OFFSET ?
                """, (rs,n) -> new AssetBacklog(rs.getString("asset_code"),rs.getLong("request_count"),rs.getLong("fault_count"),
                rs.getLong("pending_count"),rs.getLong("assigned_count"),rs.getLong("review_count"),rs.getString("oldest_date"),rs.getLong("open_jobs"),rs.getString("latest_request_date"),rs.getString("requested_by")), search,size,(long)page*size);
        return new BacklogPage(items,total,page,size);
    }

    /** Locks the asset as well as the job so concurrent jobs cannot overwrite asset availability. */
    public void lockAsset(String asset) {
        if (asset == null || jdbc.queryForList("SELECT asset_code_code FROM asset_code WHERE asset_code_code=? FOR UPDATE", String.class,asset).isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select a valid asset");
    }
    public void lockJob(UUID id) {
        if (jdbc.queryForList("SELECT job_card_id FROM job_card WHERE job_card_id=? FOR UPDATE", UUID.class,id).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Job card not found");
    }

    public void assign(JobCard job) {
        if (job.getServiceRequestId()!=null) throw conflict("New request links must come from selected faults, not a legacy request ID.");
        List<UUID> ids = job.getSelectedRequestFaultIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (Boolean.TRUE.equals(job.getIsFinished()) || Boolean.TRUE.equals(job.getIsDelivered())) throw conflict("New fault work must start on an open job.");
        if (ids.size()>500 || ids.stream().anyMatch(Objects::isNull)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select between 1 and 500 valid fault reports");
        if (new HashSet<>(ids).size()!=ids.size()) throw conflict("Duplicate fault selections");
        var selected = new ArrayList<FaultReport>();
        // Lock source request headers first, matching the request-edit lock order.
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbc.queryForList("SELECT service_request_id FROM service_request WHERE service_request_id IN (SELECT service_request_id FROM service_request_fault WHERE request_fault_id IN ("+placeholders+")) ORDER BY service_request_id FOR UPDATE", ids.toArray());
        jdbc.queryForList("SELECT request_fault_id FROM service_request_fault WHERE request_fault_id IN ("+placeholders+") ORDER BY request_fault_id FOR UPDATE", ids.toArray());
        selected.addAll(reports(" WHERE f.request_fault_id IN ("+placeholders+") AND r.is_approved", ids.toArray()));
        validateSelection(job.getAssetCode(), ids, selected);
        Map<String,List<FaultReport>> groups = new LinkedHashMap<>();
        for (FaultReport f : selected) groups.computeIfAbsent(Objects.toString(f.faultCode(),f.description()), k -> new ArrayList<>()).add(f);
        for (var group : groups.values()) {
            UUID defect = UUID.randomUUID();
            jdbc.update("INSERT INTO defect(defect_id,job_card_id,defect_description,fault_code,outcome) VALUES (?,?,?,?,'ASSIGNED')",
                    defect,job.getJobCardId(),group.get(0).description(),group.get(0).faultCode());
            for (FaultReport f : group) jdbc.update("INSERT INTO job_defect_source(defect_id,request_fault_id) VALUES (?,?)",defect,f.requestFaultId());
        }
    }

    static void validateSelection(String asset, List<UUID> ids, List<FaultReport> reports) {
        if (reports.size()!=ids.size() || reports.stream().anyMatch(f -> !Objects.equals(asset,f.assetCode()) || !"PENDING".equals(f.status())))
            throw conflict("Selected faults must be approved, pending and belong to this asset. Refresh the backlog and select again.");
    }

    public List<WorkItem> workItems(UUID job) {
        return jdbc.query("SELECT defect_id,defect_description,outcome,outcome_reason FROM defect WHERE job_card_id=? AND EXISTS(SELECT 1 FROM job_defect_source s WHERE s.defect_id=defect.defect_id) ORDER BY defect_description",
                (rs,n) -> new WorkItem(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4),
                        reports(" WHERE f.request_fault_id IN (SELECT request_fault_id FROM job_defect_source WHERE defect_id=?)",rs.getObject(1,UUID.class))),job);
    }

    @Transactional
    public void outcome(UUID job, UUID defect, String outcome, String reason) {
        lockJob(job);
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT COALESCE(is_finished,false) OR COALESCE(is_delivered,false) FROM job_card WHERE job_card_id=?",Boolean.class,job))) throw conflict("This job is closed");
        if (!Set.of("RESOLVED","DEFERRED").contains(Objects.toString(outcome,""))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose Resolved or Deferred");
        if ("DEFERRED".equals(outcome) && (reason==null || reason.isBlank())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A deferral reason is required");
        int updated = jdbc.update("UPDATE defect SET outcome=?,outcome_reason=?,outcome_at=CURRENT_TIMESTAMP WHERE defect_id=? AND job_card_id=? AND outcome='ASSIGNED' AND EXISTS(SELECT 1 FROM job_defect_source WHERE defect_id=defect.defect_id)",outcome,reason,defect,job);
        if (updated!=1) throw conflict("This work item has already been handled or does not belong to this job.");
        if ("DEFERRED".equals(outcome)) jdbc.update("UPDATE job_defect_source SET released_at=CURRENT_TIMESTAMP WHERE defect_id=? AND released_at IS NULL",defect);
    }

    @Transactional
    public void review(UUID fault, String reason, boolean resolved) {
        if (reason==null || reason.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Record the evidence for this legacy review");
        if (jdbc.update("UPDATE service_request_fault SET review_required=false,reviewed_resolved=?,review_note=?,reviewed_at=CURRENT_TIMESTAMP WHERE request_fault_id=? AND review_required",resolved,reason,fault)!=1) throw conflict("This fault no longer needs review");
    }

    public void validateUpdate(JobCard old, JobCard next) {
        boolean tracked = Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM defect d JOIN job_defect_source s USING(defect_id) WHERE d.job_card_id=?)",Boolean.class,old.getJobCardId()));
        if (!Objects.equals(old.getServiceRequestId(),next.getServiceRequestId())) throw conflict("Request links are managed through fault selection.");
        if (tracked && (!Objects.equals(old.getAssetCode(),next.getAssetCode()) || !Objects.equals(old.getParentJobCardId(),next.getParentJobCardId()))) throw conflict("Cannot change the asset or parent of a job with reported faults.");
        if (Boolean.TRUE.equals(next.getIsFinished()) || Boolean.TRUE.equals(next.getIsDelivered())) {
            if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM job_card WHERE parent_job_card_id=? AND NOT COALESCE(is_finished,false) AND NOT COALESCE(is_delivered,false))",Boolean.class,old.getJobCardId()))) throw conflict("Finish open sub-jobs before closing the main job.");

            if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM defect d JOIN job_defect_source s USING(defect_id) WHERE d.job_card_id=? AND d.outcome='ASSIGNED')",Boolean.class,old.getJobCardId()))) throw conflict("Resolve or defer every reported fault before finishing or delivering this job.");
        }
    }

    public void protectDefect(UUID defect) {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM job_defect_source WHERE defect_id=?)",Boolean.class,defect)))
            throw conflict("Reported work cannot be edited or deleted. Resolve it, or defer it back to the backlog.");
    }

    public <T extends List<JobCard>> T enrich(T jobs) {
        if (jobs.isEmpty()) return jobs;
        Map<UUID,Set<UUID>> links = new HashMap<>();
        String params=String.join(",",Collections.nCopies(jobs.size(),"?"));
        jdbc.query("SELECT DISTINCT d.job_card_id,f.service_request_id FROM defect d JOIN job_defect_source s USING(defect_id) JOIN service_request_fault f USING(request_fault_id) WHERE d.job_card_id IN ("+params+")",
                (org.springframework.jdbc.core.RowCallbackHandler)rs -> links.computeIfAbsent(rs.getObject(1,UUID.class),k->new LinkedHashSet<>()).add(rs.getObject(2,UUID.class)),jobs.stream().map(JobCard::getJobCardId).toArray());
        for (JobCard j: jobs) {
            Set<UUID> ids=links.computeIfAbsent(j.getJobCardId(),k->new LinkedHashSet<>());
            if(j.getServiceRequestId()!=null) ids.add(j.getServiceRequestId());
            j.setServiceRequestIds(new ArrayList<>(ids));
        }
        return jobs;
    }

    public List<com.rr.erp.entity.ServiceReceiveNote> enrichNotes(List<com.rr.erp.entity.ServiceReceiveNote> notes) {
        var jobs = notes.stream().map(note -> {
            JobCard job = new JobCard();
            job.setJobCardId(note.getJobCardId());
            job.setServiceRequestId(note.getServiceRequestId());
            return job;
        }).toList();
        enrich(jobs);
        for (int i=0;i<notes.size();i++) notes.get(i).setServiceRequestIds(jobs.get(i).getServiceRequestIds());
        return notes;
    }

    public String assetStatus(String asset) {
        return jdbc.queryForObject("""
                SELECT CASE WHEN count(*) FILTER (WHERE job_type IN ('Breakdown','Accident Repair'))>0 THEN 'Out of Service'
                WHEN count(*)>0 THEN 'Under Maintenance' ELSE 'Active' END
                FROM job_card WHERE asset_code=? AND NOT COALESCE(is_finished,false) AND NOT COALESCE(is_delivered,false)
                """,String.class,asset);
    }
}
