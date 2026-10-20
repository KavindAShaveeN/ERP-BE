package com.rr.erp.service;

import com.rr.erp.repository.MRRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Keeps the mr table's approval columns in step with the approval engine. */
@Component
public class MrApprovalCallback implements ApprovalCallback {

    private final MRRepository mrRepository;
    private final IssuePlanningService issuePlanningService;

    public MrApprovalCallback(MRRepository mrRepository, IssuePlanningService issuePlanningService) {
        this.mrRepository = mrRepository;
        this.issuePlanningService = issuePlanningService;
    }

    @Override
    public String docType() {
        return "MR";
    }

    @Override
    public void onApproved(UUID docId, String approver) {
        mrRepository.markApproved(docId, approver);
    }

    @Override
    public void onRejected(UUID docId, String rejectedBy, String remark) {
        mrRepository.markRejected(docId, rejectedBy);
        // A rejected MR must not keep stock reserved for it.
        issuePlanningService.onMrRejected(docId, rejectedBy);
    }
}
