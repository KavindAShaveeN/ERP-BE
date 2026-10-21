package com.rr.erp.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Moves stock (or records the rejection) when the approval engine finishes a stock adjustment. */
@Component
public class AdjApprovalCallback implements ApprovalCallback {

    private final StockAdjustmentService service;

    // Lazy: StockAdjustmentService itself depends on ApprovalService, which depends on every callback.
    public AdjApprovalCallback(@Lazy StockAdjustmentService service) {
        this.service = service;
    }

    @Override
    public String docType() {
        return StockAdjustmentService.DOC_TYPE;
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
