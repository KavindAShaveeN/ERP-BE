package com.rr.erp.service;

import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.JobIssueItem;
import com.rr.erp.repository.JobCardRepository;
import com.rr.erp.repository.JobIssueItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JobIssueItemService {

    private final JobIssueItemRepository jobIssueItemRepository;
    private final JobCardRepository jobCardRepository;
    private final ProjectStoreService projectStoreService;

    public JobIssueItemService(
            JobIssueItemRepository jobIssueItemRepository,
            JobCardRepository jobCardRepository,
            ProjectStoreService projectStoreService
    ) {
        this.jobIssueItemRepository = jobIssueItemRepository;
        this.jobCardRepository = jobCardRepository;
        this.projectStoreService = projectStoreService;
    }


    // =========================================================
    // POST — issuing an item deducts it from the issuing user's active
    // project's store (item.projectCode), not necessarily the job card's
    // own project — a job card can be worked on by staff whose active
    // project differs from it (e.g. shared/HQ staff). Falls back to the
    // job card's project if the caller didn't supply one, for backward
    // compatibility with any older client.
    // =========================================================
    @Transactional
    public List<JobIssueItem> createJobIssueItems(
            List<JobIssueItem> items
    ) {

        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Job issue item list cannot be empty"
            );
        }

        Map<UUID, JobCard> jobCardCache = new HashMap<>();

        for (JobIssueItem item : items) {

            if (item.getJobIssueItemId() == null) {
                item.setJobIssueItemId(UUID.randomUUID());
            }

            JobCard jobCard = jobCardCache.computeIfAbsent(
                    item.getJobCardId(),
                    id -> jobCardRepository.getJobCardById(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Job card not found: " + id
                            ))
            );

            String issuingProjectCode = item.getProjectCode() != null
                    ? item.getProjectCode()
                    : jobCard.getProjectCode();
            item.setProjectCode(issuingProjectCode);

            LocalDate issueDate = item.getIssuedDate() != null
                    ? item.getIssuedDate().toLocalDate()
                    : LocalDate.now();

            try {
                projectStoreService.issueGoods(
                        issuingProjectCode,
                        item.getItemCode(),
                        item.getQuantity(),
                        issueDate,
                        StockBatchService.ACTION_JOB_CARD_ISSUE,
                        jobCard.getJobCardId(),
                        item.getJobIssueItemId()
                );
            } catch (RuntimeException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
            }
        }

        jobIssueItemRepository.saveAll(items);

        return items;
    }


    // =========================================================
    // PUT — item code and quantity are locked once issued; only
    // descriptive fields (part number, serial number, description,
    // unit price, issued date) can be changed. Correcting a wrong
    // item/quantity goes through a return, then a fresh issue.
    // =========================================================
    @Transactional
    public JobIssueItem updateJobIssueItem(
            UUID jobIssueItemId,
            JobIssueItem item
    ) {

        JobIssueItem existing = jobIssueItemRepository.findById(jobIssueItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Job issue item not found"
                ));

        boolean itemCodeChanged = item.getItemCode() != null && !item.getItemCode().equals(existing.getItemCode());
        boolean quantityChanged = item.getQuantity() != null && existing.getQuantity() != null
                && item.getQuantity().compareTo(existing.getQuantity()) != 0;

        if (itemCodeChanged || quantityChanged) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item code and quantity cannot be changed after issuing — return the item and issue it again."
            );
        }

        int updatedRows =
                jobIssueItemRepository.update(
                        jobIssueItemId,
                        item
                );

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Job issue item not found"
            );
        }

        item.setJobIssueItemId(jobIssueItemId);

        return item;
    }
}
