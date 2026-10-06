package com.rr.erp.service;

import com.rr.erp.entity.JobCard;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Pure unit tests. No Spring context or database connection is created. */
class FaultWorkflowServiceTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final FaultWorkflowService service = new FaultWorkflowService(jdbc);
    private final UUID job = UUID.randomUUID();
    private final UUID defect = UUID.randomUUID();

    private FaultWorkflowService.FaultReport report(UUID id, String asset, String status) {
        return new FaultWorkflowService.FaultReport(id,UUID.randomUUID(),"SR-1",asset,"P1","Operator",
                "2026-09-30","FAULT_012","Engine heating","Engine",status,null,null,"FAULTS",null);
    }
    @Test void acceptsReportsFromMultipleRequestsForOneAsset() {
        UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        assertDoesNotThrow(() -> FaultWorkflowService.validateSelection("A1", List.of(a,b),
                List.of(report(a,"A1","PENDING"),report(b,"A1","PENDING"))));
    }
    @Test void rejectsDifferentAssets() {
        UUID id=UUID.randomUUID();
        assertThrows(ResponseStatusException.class,() -> FaultWorkflowService.validateSelection("A1",List.of(id),List.of(report(id,"A2","PENDING"))));
    }
    @Test void rejectsStaleResolvedAssignedAndUnreviewedSelections() {
        UUID id=UUID.randomUUID();
        for (String status: List.of("RESOLVED","ASSIGNED","REVIEW"))
            assertThrows(ResponseStatusException.class,() -> FaultWorkflowService.validateSelection("A1",List.of(id),List.of(report(id,"A1",status))));
        assertThrows(ResponseStatusException.class,() -> FaultWorkflowService.validateSelection("A1",List.of(id),List.of()));
    }
    private void openJob() {
        when(jdbc.queryForList(anyString(),eq(UUID.class),eq(job))).thenReturn(List.of(job));
        when(jdbc.queryForObject(anyString(),eq(Boolean.class),eq(job))).thenReturn(false);
    }
    @Test void deferredWorkReleasesSourcesForAnotherJob() {
        openJob();
        when(jdbc.update(startsWith("UPDATE defect SET"),eq("DEFERRED"),eq("Awaiting parts"),eq(defect),eq(job))).thenReturn(1);
        service.outcome(job,defect,"DEFERRED","Awaiting parts");
        verify(jdbc).update(startsWith("UPDATE job_defect_source SET released_at"),eq(defect));
    }
    @Test void resolvedWorkKeepsSourcesClaimed() {
        openJob();
        when(jdbc.update(startsWith("UPDATE defect SET"),eq("RESOLVED"),eq("Repaired"),eq(defect),eq(job))).thenReturn(1);
        service.outcome(job,defect,"RESOLVED","Repaired");
        verify(jdbc,never()).update(startsWith("UPDATE job_defect_source"),eq(defect));
    }
    @Test void deferralRequiresReason() {
        openJob();
        assertThrows(ResponseStatusException.class,() -> service.outcome(job,defect,"DEFERRED"," "));
        verify(jdbc,never()).update(startsWith("UPDATE defect SET"),any(),any(),any(),any());
    }
    @Test void repeatedOutcomeCannotChangeAnEarlierDecision() {
        openJob();
        assertThrows(ResponseStatusException.class,() -> service.outcome(job,defect,"DEFERRED","Parts unavailable"));
        verify(jdbc,never()).update(startsWith("UPDATE job_defect_source"),eq(defect));
    }
    @Test void closedJobCannotChangeOutcomes() {
        openJob();
        when(jdbc.queryForObject(anyString(),eq(Boolean.class),eq(job))).thenReturn(true);
        assertThrows(ResponseStatusException.class,() -> service.outcome(job,defect,"RESOLVED","Done"));
    }
    @Test void legacyDirectLinkCannotBypassFaultSelection() {
        JobCard card=new JobCard(); card.setServiceRequestId(UUID.randomUUID());
        assertThrows(ResponseStatusException.class,() -> service.assign(card));
        verifyNoInteractions(jdbc);
    }
    @Test void manualJobWithoutRequestsRemainsSupported() {
        assertDoesNotThrow(() -> service.assign(new JobCard()));
        verifyNoInteractions(jdbc);
    }
    @Test void repeatedReportsCreateOneWorkItemWithTwoSourceLinks() {
        UUID a=UUID.randomUUID(), b=UUID.randomUUID();
        JobCard card=new JobCard(); card.setJobCardId(job); card.setAssetCode("A1");
        card.setSelectedRequestFaultIds(List.of(a,b));
        when(jdbc.query(contains("FROM service_request_fault f"),
                org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<FaultWorkflowService.FaultReport>>any(),
                any(Object[].class))).thenReturn(List.of(report(a,"A1","PENDING"),report(b,"A1","PENDING")));
        service.assign(card);
        verify(jdbc,times(1)).update(startsWith("INSERT INTO defect("),any(UUID.class),eq(job),eq("Engine heating"),eq("FAULT_012"));
        verify(jdbc,times(2)).update(startsWith("INSERT INTO job_defect_source"),any(UUID.class),any(UUID.class));
    }
    @Test void duplicateSelectionsAreRejectedBeforeWriting() {
        UUID id=UUID.randomUUID();
        JobCard card=new JobCard(); card.setSelectedRequestFaultIds(List.of(id,id));
        assertThrows(ResponseStatusException.class,() -> service.assign(card));
        verifyNoInteractions(jdbc);
    }
    @Test void legacyReviewRequiresEvidence() {
        assertThrows(ResponseStatusException.class,() -> service.review(UUID.randomUUID(),"",true));
        verifyNoInteractions(jdbc);
    }
    @Test void finishRejectsUnresolvedAssignedWork() {
        JobCard old=new JobCard(); old.setJobCardId(job); old.setAssetCode("A1");
        JobCard next=new JobCard(); next.setAssetCode("A1"); next.setIsFinished(true);
        when(jdbc.queryForObject(contains("d.outcome='ASSIGNED'"),eq(Boolean.class),eq(job))).thenReturn(true);
        assertThrows(ResponseStatusException.class,() -> service.validateUpdate(old,next));
    }
}
