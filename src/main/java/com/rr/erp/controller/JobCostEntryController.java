package com.rr.erp.controller;

import com.rr.erp.entity.JobCostEntry;
import com.rr.erp.service.JobCostEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-cost-entry")
@CrossOrigin(origins = "*")
public class JobCostEntryController {

    private final JobCostEntryService service;

    public JobCostEntryController(JobCostEntryService service) {
        this.service = service;
    }

    // POST /api/job-cost-entry/
    @PostMapping("/")
    public ResponseEntity<JobCostEntry> createJobCostEntry(
            @RequestBody JobCostEntry jobCostEntry
    ) {

        return ResponseEntity.ok(
                service.createJobCostEntry(jobCostEntry)
        );
    }

    // PUT /api/job-cost-entry/{jobCostEntryId}
    @PutMapping("/{jobCostEntryId}")
    public ResponseEntity<JobCostEntry> updateJobCostEntry(
            @PathVariable UUID jobCostEntryId,
            @RequestBody JobCostEntry jobCostEntry
    ) {

        return ResponseEntity.ok(
                service.updateJobCostEntry(
                        jobCostEntryId,
                        jobCostEntry
                )
        );
    }
}