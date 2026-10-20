package com.rr.erp.service;

import com.rr.erp.dto.ApprovalDtos.StepDef;
import com.rr.erp.dto.ApprovalDtos.WorkflowDto;
import com.rr.erp.repository.ApprovalRepository;
import com.rr.erp.repository.ApprovalRepository.RequestRow;
import com.rr.erp.repository.ApprovalRepository.RequestStep;
import com.rr.erp.repository.RbacRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApprovalServiceTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final UUID DOC_ID = UUID.randomUUID();

    private ApprovalRepository repository;
    private PermissionService permissions;
    private ApprovalCallback callback;
    private ApprovalService service;

    private final StepDef manager = step(1, 1, 10, "MANAGER", "ANY_ONE", null, null, null);
    private final StepDef md = step(2, 2, 20, "MD", "ANY_ONE", "totalQuantity", ">", new BigDecimal("100"));

    @BeforeEach
    void setUp() {
        repository = mock(ApprovalRepository.class);
        permissions = mock(PermissionService.class);
        callback = mock(ApprovalCallback.class);
        when(callback.docType()).thenReturn("MR");
        service = new ApprovalService(repository, mock(RbacRepository.class), permissions, List.of(callback));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static StepDef step(int id, int order, int roleId, String role, String mode,
                                String field, String op, BigDecimal value) {
        return new StepDef(id, order, roleId, role, "SAME_PROJECT", mode, field, op, value);
    }

    private static void loginAs(String employeeCode) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(employeeCode, null, List.of()));
    }

    private RequestRow pending(Integer currentStepId) {
        return new RequestRow(REQUEST_ID, "MR", DOC_ID, "PENDING", "SITE1", "HO", currentStepId,
                "eng", LocalDateTime.now(), null);
    }


    @Test
    void conditionalStepOnlyAppliesWhenConditionHolds() {
        assertTrue(ApprovalService.applies(md, Map.of("totalQuantity", new BigDecimal("101"))));
        assertFalse(ApprovalService.applies(md, Map.of("totalQuantity", new BigDecimal("100"))));
        assertFalse(ApprovalService.applies(md, Map.of("totalQuantity", new BigDecimal("5"))));
    }

    @Test
    void unconditionalStepAlwaysApplies() {
        assertTrue(ApprovalService.applies(manager, Map.of()));
    }

    @Test
    void conditionWithUnknownValueStillAsksForApproval() {
        assertTrue(ApprovalService.applies(md, Map.of()));
    }

    @Test
    void submitFreezesOnlyApplicableStepsAndStartsAtTheFirst() {
        when(repository.insertRequest(anyString(), any(), anyString(), any(), anyString())).thenReturn(REQUEST_ID);
        when(repository.findActiveWorkflow("MR")).thenReturn(Optional.of(
                new WorkflowDto(1, "MR", "MR approval", true, List.of(manager, md))));

        service.submit("MR", DOC_ID, "SITE1", "HO", "eng", Map.of("totalQuantity", new BigDecimal("5")));

        verify(repository).freezeSteps(REQUEST_ID, List.of(manager));
        verify(repository).setCurrentStep(REQUEST_ID, 1);
        verify(callback, never()).onApproved(any(), any());
    }

    @Test
    void submitWithNoApplicableStepApprovesImmediately() {
        when(repository.insertRequest(anyString(), any(), anyString(), any(), anyString())).thenReturn(REQUEST_ID);
        when(repository.findActiveWorkflow("MR")).thenReturn(Optional.empty());

        service.submit("MR", DOC_ID, "SITE1", "HO", "eng", Map.of());

        verify(repository).complete(REQUEST_ID, "APPROVED");
        verify(callback).onApproved(DOC_ID, null);
    }

    @Test
    void submitterCannotApproveOwnRequest() {
        loginAs("eng");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.approve(REQUEST_ID, null));

        assertEquals(403, e.getStatusCode().value());
        verify(repository, never()).insertAction(any(), any(), anyString(), anyString(), any());
    }

    @Test
    void employeeWithoutTheStepRoleCannotApprove() {
        loginAs("someone");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequestSteps(REQUEST_ID)).thenReturn(List.of(new RequestStep(1, manager)));
        when(repository.findEligibleApprovers(anyInt(), anyString(), anyBoolean())).thenReturn(List.of("boss"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.approve(REQUEST_ID, null));

        assertEquals(403, e.getStatusCode().value());
    }

    @Test
    void roleWithoutTheApprovePermissionCannotApprove() {
        loginAs("boss");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequestSteps(REQUEST_ID)).thenReturn(List.of(new RequestStep(1, manager)));
        when(repository.findEligibleApprovers(anyInt(), anyString(), anyBoolean())).thenReturn(List.of("boss"));
        when(permissions.hasFor(eq("boss"), eq("MR.APPROVE"), any(), anyBoolean())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.approve(REQUEST_ID, null));
    }

    @Test
    void approvingTheLastStepApprovesTheDocument() {
        loginAs("boss");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequest(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequestSteps(REQUEST_ID)).thenReturn(List.of(new RequestStep(1, manager)));
        when(repository.findEligibleApprovers(anyInt(), anyString(), anyBoolean())).thenReturn(List.of("boss"));
        when(permissions.hasFor(eq("boss"), eq("MR.APPROVE"), any(), anyBoolean())).thenReturn(true);

        service.approve(REQUEST_ID, "ok");

        verify(repository).insertAction(REQUEST_ID, 1, "boss", "APPROVE", "ok");
        verify(repository).complete(REQUEST_ID, "APPROVED");
        verify(callback).onApproved(DOC_ID, "boss");
    }

    @Test
    void approvingAMiddleStepMovesToTheNextStep() {
        loginAs("boss");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequest(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequestSteps(REQUEST_ID))
                .thenReturn(List.of(new RequestStep(1, manager), new RequestStep(2, md)));
        when(repository.findEligibleApprovers(anyInt(), anyString(), anyBoolean())).thenReturn(List.of("boss"));
        when(permissions.hasFor(eq("boss"), eq("MR.APPROVE"), any(), anyBoolean())).thenReturn(true);

        service.approve(REQUEST_ID, null);

        verify(repository).setCurrentStep(REQUEST_ID, 2);
        verify(callback, never()).onApproved(any(), any());
    }

    @Test
    void allModeWaitsForEveryApprover() {
        StepDef allStep = step(1, 1, 10, "MANAGER", "ALL", null, null, null);
        loginAs("boss1");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequest(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequestSteps(REQUEST_ID)).thenReturn(List.of(new RequestStep(1, allStep)));
        when(repository.findEligibleApprovers(anyInt(), anyString(), anyBoolean())).thenReturn(List.of("boss1", "boss2"));
        when(repository.findApprovers(REQUEST_ID, 1)).thenReturn(List.of("boss1"));
        when(permissions.hasFor(eq("boss1"), eq("MR.APPROVE"), any(), anyBoolean())).thenReturn(true);

        service.approve(REQUEST_ID, null);

        verify(repository, never()).complete(any(), anyString());
        verify(callback, never()).onApproved(any(), any());
    }

    @Test
    void rejectingEndsTheRequestAndRejectsTheDocument() {
        loginAs("boss");
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequest(REQUEST_ID)).thenReturn(Optional.of(pending(1)));
        when(repository.findRequestSteps(REQUEST_ID))
                .thenReturn(List.of(new RequestStep(1, manager), new RequestStep(2, md)));
        when(repository.findEligibleApprovers(anyInt(), anyString(), anyBoolean())).thenReturn(List.of("boss"));
        when(permissions.hasFor(eq("boss"), eq("MR.REJECT"), any(), anyBoolean())).thenReturn(true);

        service.reject(REQUEST_ID, "no budget");

        verify(repository).complete(REQUEST_ID, "REJECTED");
        verify(callback).onRejected(DOC_ID, "boss", "no budget");
        verify(callback, never()).onApproved(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void finishedRequestCannotBeActedOnAgain() {
        loginAs("boss");
        RequestRow done = new RequestRow(REQUEST_ID, "MR", DOC_ID, "APPROVED", "SITE1", "HO", null,
                "eng", LocalDateTime.now(), LocalDateTime.now());
        when(repository.findRequestForUpdate(REQUEST_ID)).thenReturn(Optional.of(done));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.approve(REQUEST_ID, null));

        assertEquals(409, e.getStatusCode().value());
    }
}
