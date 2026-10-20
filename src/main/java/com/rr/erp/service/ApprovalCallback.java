package com.rr.erp.service;

import java.util.UUID;

/** Implemented once per document type: what to do to the document when its approval finishes. */
public interface ApprovalCallback {

    /** The doc_type this callback handles, e.g. "MR". */
    String docType();

    /** @param approver the employee who gave the final approval; null when the workflow needed no approver */
    void onApproved(UUID docId, String approver);

    void onRejected(UUID docId, String rejectedBy, String remark);
}
