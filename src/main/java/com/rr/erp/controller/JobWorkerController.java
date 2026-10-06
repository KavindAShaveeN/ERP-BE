package com.rr.erp.controller;

import com.rr.erp.entity.JobWorker;
import com.rr.erp.service.JobWorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/job-worker")
@CrossOrigin(origins = "*")
public class JobWorkerController {

    private final JobWorkerService jobWorkerService;

    public JobWorkerController(
            JobWorkerService jobWorkerService
    ) {
        this.jobWorkerService = jobWorkerService;
    }

    // =========================================================
    // POST /api/job-worker/
    // =========================================================

    @PostMapping("/")
    public ResponseEntity<JobWorker> createJobWorker(
            @RequestBody JobWorker jobWorker
    ) {

        JobWorker createdJobWorker =
                jobWorkerService.createJobWorker(jobWorker);

        return ResponseEntity.ok(createdJobWorker);
    }

    // =========================================================
    // PUT /api/job-worker/{jobWorkerId}
    // =========================================================

    @PutMapping("/{jobWorkerId}")
    public ResponseEntity<String> updateJobWorker(
            @PathVariable UUID jobWorkerId,
            @RequestBody JobWorker jobWorker
    ) {

        jobWorkerService.updateJobWorker(
                jobWorkerId,
                jobWorker
        );

        return ResponseEntity.ok(
                "Job worker updated successfully"
        );
    }

    // =========================================================
    // DELETE /api/job-worker/{jobWorkerId}
    // =========================================================

    @DeleteMapping("/{jobWorkerId}")
    public ResponseEntity<Void> deleteJobWorker(
            @PathVariable UUID jobWorkerId
    ) {

        jobWorkerService.deleteJobWorker(jobWorkerId);

        return ResponseEntity.noContent().build();
    }
}
