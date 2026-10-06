package com.rr.erp.service;

import com.rr.erp.dto.JobCardResponse;
import com.rr.erp.entity.JobCard;
import com.rr.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobCardService {

    private static final Logger log = LoggerFactory.getLogger(JobCardService.class);

    private final JobCardRepository jobCardRepository;
    private final FaultWorkflowService faultWorkflow;
    private final DefectRepository defectRepository;
    private final JobWorkerRepository jobWorkerRepository;
    private final JobIssueItemRepository jobIssueItemRepository;
    private final ThreePServiceRepository threePServiceRepository;
    private final JobCostEntryRepository jobCostEntryRepository;
    private final JobIssueItemReturnRepository jobIssueItemReturnRepository;
    private final AssetService assetService;
    private final AssetLocationService assetLocationService;


    // =========================================================
    // CREATE
    // =========================================================
    @org.springframework.transaction.annotation.Transactional
    public JobCard createJobCard(JobCard jobCard) {
        faultWorkflow.lockAsset(jobCard.getAssetCode());
        if (jobCard.getJobCardId() != null && jobCardRepository.getJobCardById(jobCard.getJobCardId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This job was already created. Open the existing job instead of submitting again.");
        }
        if (jobCard.getParentJobCardId() != null) {
            JobCard parent = jobCardRepository.getJobCardById(jobCard.getParentJobCardId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent job not found"));
            if (!java.util.Objects.equals(parent.getAssetCode(),jobCard.getAssetCode()) || parent.getParentJobCardId()!=null || (Boolean.TRUE.equals(parent.getIsFinished()) || Boolean.TRUE.equals(parent.getIsDelivered())))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select an open main job for the same asset");
        }

        // Generate UUID
        if (jobCard.getJobCardId() == null) {
            jobCard.setJobCardId(UUID.randomUUID());
        }

        // Created date
        if (jobCard.getCreatedDate() == null) {
            jobCard.setCreatedDate(LocalDateTime.now());
        }

        // Updated date
        jobCard.setUpdatedDate(LocalDateTime.now());

        // Default finished status
        if (jobCard.getIsFinished() == null) {
            jobCard.setIsFinished(false);
        }

        // Default delivered status
        if (jobCard.getIsDelivered() == null) {
            jobCard.setIsDelivered(false);
        }

        int result =
                jobCardRepository.createJobCard(jobCard);

        if (result == 0) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create job card"
            );
        }

        faultWorkflow.assign(jobCard);
        syncAssetStatus(jobCard);
        faultWorkflow.enrich(List.of(jobCard));
        return jobCard;
    }


    // =========================================================
    // UPDATE
    // =========================================================
    @org.springframework.transaction.annotation.Transactional
    public JobCard updateJobCard(
            UUID jobCardId,
            JobCard jobCard) {

        JobCard existing = jobCardRepository.getJobCardById(jobCardId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job card not found"));
        faultWorkflow.lockAsset(existing.getAssetCode());
        faultWorkflow.lockJob(jobCardId);
        existing = jobCardRepository.getJobCardById(jobCardId).orElseThrow();
        if (!java.util.Objects.equals(existing.getAssetCode(), jobCard.getAssetCode()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The asset cannot be changed after job creation");
        faultWorkflow.validateUpdate(existing,jobCard);

        boolean wasDelivered = Boolean.TRUE.equals(existing.getIsDelivered());

        jobCard.setJobCardId(jobCardId);
        jobCard.setUpdatedDate(LocalDateTime.now());

        int result =
                jobCardRepository.updateJobCard(
                        jobCardId,
                        jobCard
                );

        if (result == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Job card not found"
            );
        }

        syncAssetStatus(jobCard);

        if (Boolean.TRUE.equals(jobCard.getIsDelivered()) && !wasDelivered) {
            handleJobCardDelivered(jobCard);
        }

        faultWorkflow.enrich(List.of(jobCard));
        return jobCard;
    }


    /**
     * Derive availability from all open jobs for this asset, under the asset lock.
     * Closing one job must not hide another active breakdown or maintenance job.
     */
    private void syncAssetStatus(JobCard jobCard) {

        String assetCode = jobCard.getAssetCode();

        if (assetCode == null || assetCode.isBlank()) {
            return;
        }

        String status = faultWorkflow.assetStatus(assetCode);

        if (status == null) {
            return;
        }

        try {
            boolean updated = assetService.updateAssetStatus(assetCode, status);

            if (!updated) {
                log.warn(
                        "Could not sync asset status for job card {}: asset {} not found",
                        jobCard.getJobCardCode(),
                        assetCode
                );
            }
        } catch (Exception exception) {
            log.error(
                    "Failed to sync asset status for job card {} (asset {})",
                    jobCard.getJobCardCode(),
                    assetCode,
                    exception
            );
        }
    }


    /**
     * Fired once, the moment a job card's isDelivered flag first flips to true: the asset is
     * dispatched out of the workshop project, in transit to the requesting project (see
     * AssetLocationService#dispatchAssetFromJobCard). It stays IN_TRANSIT until the requesting
     * project confirms receipt themselves with a Service Receive Note (created from the
     * project's own Services page) — that confirmation is what finally records the asset as
     * being back at the requesting project, mirroring the GIN/GRN dispatch-then-receive pattern.
     * A secondary side effect — logged and swallowed on failure so a problem here never breaks
     * the status update itself.
     */
    private void handleJobCardDelivered(JobCard jobCard) {

        String assetCode = jobCard.getAssetCode();

        if (assetCode == null || assetCode.isBlank()) {
            return;
        }

        try {
            assetLocationService.dispatchAssetFromJobCard(
                    assetCode,
                    jobCard.getProjectCode(),
                    jobCard.getRequestingProjectCode(),
                    jobCard.getJobCardId(),
                    jobCard.getJobCardCode(),
                    LocalDate.now(),
                    jobCard.getJobCheckedBy()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to dispatch asset {} out of the workshop for job card {}",
                    assetCode,
                    jobCard.getJobCardCode(),
                    exception
            );
        }
    }


    // =========================================================
    // GET ALL
    // =========================================================
    public List<JobCard> getAllJobCards() {

        return faultWorkflow.enrich(jobCardRepository.getAllJobCards());
    }


    // =========================================================
    // GET SPECIFIC
    // =========================================================
    public JobCardResponse getJobCardById(UUID jobCardId) {
        JobCardResponse response = new JobCardResponse();

        response.setInitialDetails(jobCardRepository
                .getJobCardById(jobCardId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Job card not found"
                        )
                ));
        response.setDefectList(defectRepository
                .getDefectsByJobCardId(jobCardId));
        response.setEmployeeList(jobWorkerRepository
                .getJobWorkersByJobCardId(jobCardId));
        response.setIssueItemList(jobIssueItemRepository
                .getByJobCardId(jobCardId));
        response.setThreePServiceList(threePServiceRepository
                .getThreePServicesByJobCardId(jobCardId));
        response.setJobCostEntryList(jobCostEntryRepository
                .getJobCostEntriesByJobCardId(jobCardId));
        response.setIssueItemReturnList(jobIssueItemReturnRepository
                .getByJobCardId(jobCardId));
        response.setSubJobCards(jobCardRepository
                .getSubJobCards(jobCardId));
        faultWorkflow.enrich(List.of(response.getInitialDetails()));
        faultWorkflow.enrich(response.getSubJobCards());
        return response;
    }

    public void upsertJobCardCost(UUID jobCardId, BigDecimal cost) {

        jobCardRepository.upsertJobCardCost(jobCardId, cost);
    }

    public List<JobCard> getJobCardsByJobType(String jobType) {
        return faultWorkflow.enrich(jobCardRepository.getJobCardsByJobType(jobType));
    }

    public List<JobCard> getJobCardsByProject(String projectCode) {
        return faultWorkflow.enrich(jobCardRepository.getJobCardsByProject(projectCode));
    }
}