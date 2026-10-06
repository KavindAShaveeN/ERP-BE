package com.rr.erp.service;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.MR;
import com.rr.erp.repository.MRRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MRService {

    private final MRRepository mrRepository;
    private final IssuePlanningService issuePlanningService;

    public MRService(MRRepository mrRepository, IssuePlanningService issuePlanningService) {
        this.mrRepository = mrRepository;
        this.issuePlanningService = issuePlanningService;
    }

    @Transactional
    public MR createMaterialRequest(MR mr) {

        try {
            return mrRepository.insertMR(mr);

        } catch (DataIntegrityViolationException exception) {

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

            // A rejected (or un-approved) MR must not keep stock reserved for it.
            if (!Boolean.TRUE.equals(mr.getIsApproved())) {
                issuePlanningService.onMrRejected(mrId, mr.getCheckedBy());
            }

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
}
