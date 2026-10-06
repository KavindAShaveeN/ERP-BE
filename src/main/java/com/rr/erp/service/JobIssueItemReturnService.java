package com.rr.erp.service;

import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.JobIssueItem;
import com.rr.erp.entity.JobIssueItemReturn;
import com.rr.erp.repository.JobCardRepository;
import com.rr.erp.repository.JobIssueItemRepository;
import com.rr.erp.repository.JobIssueItemReturnRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JobIssueItemReturnService {

    private final JobIssueItemReturnRepository jobIssueItemReturnRepository;
    private final JobIssueItemRepository jobIssueItemRepository;
    private final JobCardRepository jobCardRepository;
    private final ProjectStoreService projectStoreService;

    public JobIssueItemReturnService(
            JobIssueItemReturnRepository jobIssueItemReturnRepository,
            JobIssueItemRepository jobIssueItemRepository,
            JobCardRepository jobCardRepository,
            ProjectStoreService projectStoreService
    ) {
        this.jobIssueItemReturnRepository = jobIssueItemReturnRepository;
        this.jobIssueItemRepository = jobIssueItemRepository;
        this.jobCardRepository = jobCardRepository;
        this.projectStoreService = projectStoreService;
    }


    // =========================================================
    // CREATE — returns (part of) a previously issued line back to the
    // returning user's active project store (item.projectCode), restricted
    // to what's still outstanding. Falls back to the project the item was
    // originally issued from, then the job card's own project, if the
    // caller didn't supply one (backward compatibility with any older
    // client).
    // =========================================================
    @Transactional
    public JobIssueItemReturn createReturn(JobIssueItemReturn item) {

        if (item.getJobIssueItemId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobIssueItemId is required");
        }

        JobIssueItem source = jobIssueItemRepository.findById(item.getJobIssueItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job issue item not found"));

        JobCard jobCard = jobCardRepository.getJobCardById(source.getJobCardId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job card not found"));

        String returningProjectCode = item.getProjectCode() != null
                ? item.getProjectCode()
                : (source.getProjectCode() != null ? source.getProjectCode() : jobCard.getProjectCode());
        item.setProjectCode(returningProjectCode);

        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        BigDecimal alreadyReturned = jobIssueItemReturnRepository.getReturnedQuantity(source.getJobIssueItemId());
        BigDecimal outstanding = source.getQuantity().subtract(alreadyReturned);

        if (item.getQuantity().compareTo(outstanding) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot return " + item.getQuantity() + " of " + source.getItemCode()
                            + " — only " + outstanding + " is outstanding on that issue."
            );
        }

        item.setJobIssueItemReturnId(UUID.randomUUID());
        item.setJobCardId(source.getJobCardId());
        item.setItemCode(source.getItemCode());
        item.setUnitPrice(source.getUnitPrice());

        if (item.getReturnedDate() == null) {
            item.setReturnedDate(LocalDateTime.now());
        }

        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        jobIssueItemReturnRepository.insert(item);

        projectStoreService.receiveNewBatch(
                returningProjectCode,
                source.getItemCode(),
                item.getQuantity(),
                item.getReturnedDate().toLocalDate(),
                source.getUnitPrice() != null ? source.getUnitPrice() : BigDecimal.ZERO,
                StockBatchService.SOURCE_JOB_CARD_ITEM_RETURN,
                item.getJobIssueItemReturnId(),
                jobCard.getJobCardCode()
        );

        return item;
    }


    // =========================================================
    // GET BY JOB CARD
    // =========================================================
    public List<JobIssueItemReturn> getByJobCardId(UUID jobCardId) {
        return jobIssueItemReturnRepository.getByJobCardId(jobCardId);
    }
}
