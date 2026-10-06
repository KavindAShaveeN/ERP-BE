package com.rr.erp.controller;

import com.rr.erp.entity.JobIssueItem;
import com.rr.erp.service.JobIssueItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-issue-item")
@CrossOrigin
public class JobIssueItemController {

    private final JobIssueItemService jobIssueItemService;

    public JobIssueItemController(
            JobIssueItemService jobIssueItemService
    ) {
        this.jobIssueItemService = jobIssueItemService;
    }


    // =========================================================
    // POST api/job-issue-item/
    // =========================================================
    @PostMapping("/")
    public ResponseEntity<List<JobIssueItem>> createJobIssueItems(
            @RequestBody List<JobIssueItem> items
    ) {

        List<JobIssueItem> savedItems =
                jobIssueItemService
                        .createJobIssueItems(items);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedItems);
    }


    // =========================================================
    // PUT api/job-issue-item/{jobIssueItemId}
    // =========================================================
    @PutMapping("/{jobIssueItemId}")
    public ResponseEntity<JobIssueItem> updateJobIssueItem(
            @PathVariable UUID jobIssueItemId,
            @RequestBody JobIssueItem item
    ) {

        JobIssueItem updatedItem =
                jobIssueItemService.updateJobIssueItem(
                        jobIssueItemId,
                        item
                );

        return ResponseEntity.ok(updatedItem);
    }
}