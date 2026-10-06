package com.rr.erp.controller;

import com.rr.erp.dto.JobCardCostRequest;
import com.rr.erp.dto.JobCardResponse;
import com.rr.erp.entity.JobCard;
import com.rr.erp.service.JobCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-card")
@RequiredArgsConstructor
@CrossOrigin
public class JobCardController {

    private final JobCardService jobCardService;


    // =========================================================
    // POST /api/job-card/
    // =========================================================
    @PostMapping("/")
    public ResponseEntity<JobCard> createJobCard(
            @RequestBody JobCard jobCard) {

        JobCard createdJobCard =
                jobCardService.createJobCard(jobCard);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdJobCard);
    }


    // =========================================================
    // PUT /api/job-card/{jobCardId}
    // =========================================================
    @PutMapping("/{jobCardId}")
    public ResponseEntity<JobCard> updateJobCard(
            @PathVariable UUID jobCardId,
            @RequestBody JobCard jobCard) {

        JobCard updatedJobCard =
                jobCardService.updateJobCard(
                        jobCardId,
                        jobCard
                );

        return ResponseEntity.ok(updatedJobCard);
    }


    // =========================================================
    // GET /api/job-card/all
    // =========================================================
    @GetMapping("/all")
    public ResponseEntity<List<JobCard>> getAllJobCards() {

        return ResponseEntity.ok(
                jobCardService.getAllJobCards()
        );
    }


    // =========================================================
    // GET /api/job-card/specific/{jobCardId}
    // =========================================================
    @GetMapping("/specific/{jobCardId}")
    public ResponseEntity<JobCardResponse> getJobCardById(
            @PathVariable UUID jobCardId) {

        return ResponseEntity.ok(
                jobCardService.getJobCardById(jobCardId)
        );
    }

    @PutMapping("/{jobCardId}/cost")
    public ResponseEntity<String> upsertJobCardCost(
            @PathVariable UUID jobCardId,
            @RequestBody JobCardCostRequest request
    ) {

        jobCardService.upsertJobCardCost(jobCardId, request.getCost());

        return ResponseEntity.ok("Job card cost updated successfully");
    }

    @GetMapping("/type/{jobType}")
    public ResponseEntity<List<JobCard>> getJobCardsByJobType(
            @PathVariable String jobType) {

        return ResponseEntity.ok(
                jobCardService.getJobCardsByJobType(jobType)
        );
    }


    // =========================================================
    // GET /api/job-card/project/{projectCode}
    // =========================================================
    @GetMapping("/project/{projectCode}")
    public ResponseEntity<List<JobCard>> getJobCardsByProject(
            @PathVariable String projectCode) {

        return ResponseEntity.ok(
                jobCardService.getJobCardsByProject(projectCode)
        );
    }
}