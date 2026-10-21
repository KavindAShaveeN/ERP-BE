package com.rr.erp.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Credits stock (or records the rejection) when the approval engine finishes a GRN. */
@Component
public class GrnApprovalCallback implements ApprovalCallback {

    private final GRNService service;

    // Lazy: GRNService itself depends on ApprovalService, which depends on every callback.
    public GrnApprovalCallback(@Lazy GRNService service) {
        this.service = service;
    }

    @Override
    public String docType() {
        return GRNService.DOC_TYPE;
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
