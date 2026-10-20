package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.MR;
import com.rr.erp.entity.MRItem;
import com.rr.erp.repository.MRRepository;
import com.rr.erp.security.SecurityUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MRService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MRService.class);

    private final MRRepository mrRepository;
    private final IssuePlanningService issuePlanningService;
    private final ApprovalService approvalService;
    private final PermissionService permissionService;

    public MRService(MRRepository mrRepository, IssuePlanningService issuePlanningService,
                     ApprovalService approvalService, PermissionService permissionService) {
        this.mrRepository = mrRepository;
        this.issuePlanningService = issuePlanningService;
        this.approvalService = approvalService;
        this.permissionService = permissionService;
    }

    @Transactional
    public MR createMaterialRequest(MR mr) {

        // The client guesses FWD- codes from the approved list only, so it can repeat one
        // that already exists; the server owns the sequence.
        if (mr.getMrCode() != null && mr.getMrCode().matches("^FWD-[0-9]+$")) {
            mr.setMrCode(issuePlanningService.nextForwardMrCode());
        }

        // Who is asking comes from the login token; approval fields are owned by the approval engine.
        String actor = SecurityUtil.currentEmployeeCodeOrNull();
        if (actor != null) {
            permissionService.require("MR.CREATE", mr.getRequestingProjectCode());
            mr.setRequestedBy(actor);
        }
        mr.setIsApproved(false);
        mr.setCheckedBy(null);
        mr.setCheckedDate(null);
        mr.setApprovedBy(null);
        mr.setApprovedDate(null);

        try {
            MR created = mrRepository.insertMR(mr);
            approvalService.submit("MR", created.getMrId(), created.getRequestingProjectCode(),
                    created.getDestinationProjectCode(), created.getRequestedBy(), approvalContext(created));
            return mrRepository.findById(created.getMrId()).orElse(created);

        } catch (DataIntegrityViolationException exception) {

            log.warn("Material request insert failed: {}", exception.getMostSpecificCause().getMessage());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to create material request. Check project, "
                            + "employee, item code and UOM values."
            );
        }

    }

    public PagedResponse<MR> getByDestinationProjectCode(
            String destinationProjectCode,
            int page,
            int size
    ) {

        String trimmedCode = destinationProjectCode.trim();

        List<MR> materialRequests = mrRepository.findByDestinationProjectCode(
                trimmedCode,
                page,
                size
        );

        long totalElements = mrRepository.countByDestinationProjectCode(
                trimmedCode
        );

        return new PagedResponse<>(materialRequests, page, size, totalElements);
    }

    @Transactional
    public MR updateMaterialRequest(UUID mrId, MR mr) {

        if (!mrRepository.existsById(mrId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Material request not found: " + mrId
            );
        }

        // Approve / reject go through /api/approvals; an edit never changes the approval state.
        MR existing = mrRepository.findById(mrId).orElseThrow();
        String actor = SecurityUtil.currentEmployeeCodeOrNull();
        if (actor != null) {
            permissionService.require("MR.EDIT", existing.getRequestingProjectCode());
        }
        mr.setRequestedBy(existing.getRequestedBy());
        mr.setIsApproved(existing.getIsApproved());
        mr.setCheckedBy(existing.getCheckedBy());
        mr.setCheckedDate(existing.getCheckedDate());
        mr.setApprovedBy(existing.getApprovedBy());
        mr.setApprovedDate(existing.getApprovedDate());

        try {
            int updatedRows = mrRepository.updateMR(mrId, mr);

            if (updatedRows == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Material request not found: " + mrId
                );
            }

            /*
             * Bring the existing MR items in line with the PUT request,
             * keeping each line's id (and its status and reservations).
             */
            mrRepository.syncItems(mrId, mr.getItems());

            return mrRepository.findById(mrId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Material request not found after update"
                    ));

        } catch (DataIntegrityViolationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to update material request. Check project, "
                            + "employee, item code and UOM values."
            );
        }
    }

    /** An MR can be deleted by its requester (or anyone with MR.DELETE) until it is approved. */
    @Transactional
    public void deleteMaterialRequest(UUID mrId) {
        MR existing = mrRepository.findById(mrId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Material request not found: " + mrId));

        String actor = SecurityUtil.currentEmployeeCodeOrNull();
        if (actor != null && !actor.equals(existing.getRequestedBy())) {
            permissionService.require("MR.DELETE", existing.getRequestingProjectCode());
        }
        if (Boolean.TRUE.equals(existing.getIsApproved())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An approved material request cannot be deleted");
        }

        try {
            approvalService.deleteForDocument("MR", mrId);
            mrRepository.deleteById(mrId);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This material request is already used by another document and cannot be deleted");
        }
    }

    public PagedResponse<MR> getByRequestingProjectCode(
            String requestingProjectCode,
            int page,
            int size
    )
    {

        String trimmedCode = requestingProjectCode.trim();

        List<MR> materialRequests = mrRepository.findByRequestingProjectCode(
                trimmedCode,
                page,
                size
        );

        long totalElements = mrRepository.countByRequestingProjectCode(
                trimmedCode
        );

        return new PagedResponse<>(materialRequests, page, size, totalElements);
    }
    public PagedResponse<MR> getAllMaterialRequests(int page, int size) {

        List<MR> materialRequests = mrRepository.findAll(page, size);
        long totalElements = mrRepository.countAll();

        return new PagedResponse<>(materialRequests, page, size, totalElements);
    }

    public PagedResponse<MR> getAllMaterialRequestsForReport(int page, int size) {

        List<MR> materialRequests = mrRepository.findAllAnyStatus(page, size);
        long totalElements = mrRepository.countAllAnyStatus();

        return new PagedResponse<>(materialRequests, page, size, totalElements);
    }

    /** Values a workflow step condition can test. */
    private static Map<String, BigDecimal> approvalContext(MR mr) {
        BigDecimal total = mr.getItems().stream()
                .map(MRItem::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("itemCount", BigDecimal.valueOf(mr.getItems().size()), "totalQuantity", total);
    }
}
