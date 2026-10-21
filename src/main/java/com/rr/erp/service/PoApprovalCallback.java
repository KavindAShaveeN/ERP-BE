package com.rr.erp.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records the decision on the purchase order when the approval engine finishes it. */
@Component
public class PoApprovalCallback implements ApprovalCallback {

    private final POService service;

    // Lazy: POService itself depends on ApprovalService, which depends on every callback.
    public PoApprovalCallback(@Lazy POService service) {
        this.service = service;
    }

    @Override
    public String docType() {
        return POService.DOC_TYPE;
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
