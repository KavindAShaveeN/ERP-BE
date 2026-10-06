package com.rr.erp.controller;

import com.rr.erp.entity.JobIssueItemReturn;
import com.rr.erp.service.JobIssueItemReturnService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-issue-item-return")
@CrossOrigin
public class JobIssueItemReturnController {

    private final JobIssueItemReturnService jobIssueItemReturnService;

    public JobIssueItemReturnController(JobIssueItemReturnService jobIssueItemReturnService) {
        this.jobIssueItemReturnService = jobIssueItemReturnService;
    }


    // =========================================================
    // POST /api/job-issue-item-return/
    // =========================================================
    @PostMapping("/")
    public ResponseEntity<JobIssueItemReturn> createReturn(
            @RequestBody JobIssueItemReturn item
    ) {
        JobIssueItemReturn created = jobIssueItemReturnService.createReturn(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    // =========================================================
    // GET /api/job-issue-item-return/job-card/{jobCardId}
    // =========================================================
    @GetMapping("/job-card/{jobCardId}")
    public ResponseEntity<List<JobIssueItemReturn>> getByJobCardId(
            @PathVariable UUID jobCardId
    ) {
        return ResponseEntity.ok(jobIssueItemReturnService.getByJobCardId(jobCardId));
    }
}
