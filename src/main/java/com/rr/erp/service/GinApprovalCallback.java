package com.rr.erp.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Deducts stock (or records the rejection) when the approval engine finishes a GIN. */
@Component
public class GinApprovalCallback implements ApprovalCallback {

    private final GINService service;

    // Lazy: GINService itself depends on ApprovalService, which depends on every callback.
    public GinApprovalCallback(@Lazy GINService service) {
        this.service = service;
    }

    @Override
    public String docType() {
        return GINService.DOC_TYPE;
    }

    @Override
    public void onApproved(UUID docId, String approver) {
        service.approveFromWorkflow(docId, approver);
    }

    @Override
    public void onRejected(UUID docId, String rejectedBy, String remark) {
        service.rejectFromWorkflow(docId, rejectedBy);
    }
}
