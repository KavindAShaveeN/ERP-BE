package com.rr.erp.service;

import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.JobCardOutput;
import com.rr.erp.repository.JobCardOutputRepository;
import com.rr.erp.repository.JobCardRepository;
import com.rr.erp.repository.JobCostEntryRepository;
import com.rr.erp.repository.JobIssueItemRepository;
import com.rr.erp.repository.JobIssueItemReturnRepository;
import com.rr.erp.repository.ThreePServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The finished products of a Fabrication job card. Outputs are planned while the job is open
 * and credited to the job's project store as new stock batches when the job is finished.
 */
@Service
@RequiredArgsConstructor
public class JobCardOutputService {

    public static final String JOB_TYPE_FABRICATION = "Fabrication";

    private final JobCardOutputRepository outputRepository;
    private final JobCardRepository jobCardRepository;
    private final JobIssueItemRepository jobIssueItemRepository;
    private final JobIssueItemReturnRepository jobIssueItemReturnRepository;
    private final ThreePServiceRepository threePServiceRepository;
    private final JobCostEntryRepository jobCostEntryRepository;
    private final ProjectStoreService projectStoreService;
    private final FaultWorkflowService faultWorkflow;

    public static boolean isFabrication(JobCard jobCard) {
        return JOB_TYPE_FABRICATION.equals(jobCard.getJobType());
    }

    public List<JobCardOutput> getByJobCardId(UUID jobCardId) {
        return outputRepository.getByJobCardId(jobCardId);
    }

    @Transactional
    public JobCardOutput create(JobCardOutput output) {

        faultWorkflow.lockJob(output.getJobCardId());
        requireOpenFabricationJob(output.getJobCardId());
        validate(output);

        return outputRepository.insert(output);
    }

    @Transactional
    public JobCardOutput update(UUID jobCardOutputId, JobCardOutput output) {

        JobCardOutput existing = outputRepository.findById(jobCardOutputId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Output not found"));

        faultWorkflow.lockJob(existing.getJobCardId());
        requireOpenFabricationJob(existing.getJobCardId());
        validate(output);

        if (outputRepository.update(jobCardOutputId, output) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This output has already been added to stock");
        }

        output.setJobCardOutputId(jobCardOutputId);
        output.setJobCardId(existing.getJobCardId());

        return output;
    }

    @Transactional
    public void delete(UUID jobCardOutputId) {

        JobCardOutput existing = outputRepository.findById(jobCardOutputId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Output not found"));

        faultWorkflow.lockJob(existing.getJobCardId());
        requireOpenFabricationJob(existing.getJobCardId());

        if (outputRepository.delete(jobCardOutputId) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This output has already been added to stock");
        }
    }

    /**
     * Called when a Fabrication job flips to finished (or delivered). Spreads the job's total cost
     * (issued materials net of returns + third-party service charges + additional cost entries) over
     * the output quantity and credits each output to the job's project store. Idempotent: outputs that
     * are already posted are skipped.
     */
    @Transactional
    public void postOutputs(JobCard jobCard) {

        List<JobCardOutput> pending = outputRepository.getByJobCardId(jobCard.getJobCardId()).stream()
                .filter(output -> output.getPostedAt() == null)
                .toList();

        if (pending.isEmpty()) {
            return;
        }

        if (jobCard.getProjectCode() == null || jobCard.getProjectCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This job card has no project store to receive the finished products");
        }

        BigDecimal totalQuantity = pending.stream()
                .map(JobCardOutput::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unitCost = totalCost(jobCard.getJobCardId())
                .divide(totalQuantity, 4, RoundingMode.HALF_UP);

        for (JobCardOutput output : pending) {

            String description = output.getDescription() != null && !output.getDescription().isBlank()
                    ? output.getDescription().trim()
                    : output.getItemCode();

            projectStoreService.receiveNewBatch(
                    jobCard.getProjectCode(),
                    output.getItemCode(),
                    output.getQuantity(),
                    LocalDate.now(),
                    unitCost,
                    StockBatchService.SOURCE_JOB_CARD_OUTPUT,
                    output.getJobCardOutputId(),
                    jobCard.getJobCardCode(),
                    null,
                    null,
                    null,
                    description
            );

            outputRepository.markPosted(output.getJobCardOutputId(), unitCost);
        }
    }

    /** Same components as the Calculate Cost page: issued items - returned items + 3rd party + extra entries. */
    private BigDecimal totalCost(UUID jobCardId) {

        BigDecimal issued = jobIssueItemRepository.getByJobCardId(jobCardId).stream()
                .map(item -> lineValue(item.getUnitPrice(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal returned = jobIssueItemReturnRepository.getByJobCardId(jobCardId).stream()
                .map(item -> lineValue(item.getUnitPrice(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal services = threePServiceRepository.getThreePServicesByJobCardId(jobCardId).stream()
                .map(service -> service.getServiceCharge() != null ? service.getServiceCharge() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal extras = jobCostEntryRepository.getJobCostEntriesByJobCardId(jobCardId).stream()
                .map(entry -> entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return issued.subtract(returned).add(services).add(extras).max(BigDecimal.ZERO);
    }

    private static BigDecimal lineValue(BigDecimal unitPrice, BigDecimal quantity) {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(quantity);
    }

    private void requireOpenFabricationJob(UUID jobCardId) {

        JobCard jobCard = jobCardRepository.getJobCardById(jobCardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job card not found"));

        if (!isFabrication(jobCard)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Finished products can only be recorded on a Fabrication job card");
        }

        if (Boolean.TRUE.equals(jobCard.getIsFinished()) || Boolean.TRUE.equals(jobCard.getIsDelivered())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This job card is finished and can no longer be changed");
        }
    }

    private void validate(JobCardOutput output) {

        if (output.getItemCode() == null || output.getItemCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select the item code of the finished product");
        }

        if (output.getQuantity() == null || output.getQuantity().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
    }
}
