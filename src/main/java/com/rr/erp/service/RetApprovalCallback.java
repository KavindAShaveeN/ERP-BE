package com.rr.erp.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Moves stock (or records the rejection) when the approval engine finishes a stock return. */
@Component
public class RetApprovalCallback implements ApprovalCallback {

    private final StockReturnService service;

    // Lazy: StockReturnService itself depends on ApprovalService, which depends on every callback.
    public RetApprovalCallback(@Lazy StockReturnService service) {
        this.service = service;
    }

    @Override
    public String docType() {
        return StockReturnService.DOC_TYPE;
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
