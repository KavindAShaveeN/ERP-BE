package com.rr.erp.controller;

import com.rr.erp.entity.JobCheckList;
import com.rr.erp.service.JobCheckListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/job-check-list")
@CrossOrigin
@RequiredArgsConstructor
public class JobCheckListController {

    private final JobCheckListService jobCheckListService;


    // =========================================================
    // INSERT OR UPDATE CHECKLIST
    // =========================================================
    @PutMapping("/{jobCardId}")
    public ResponseEntity<String> upsertJobCheckList(
            @PathVariable UUID jobCardId,
            @RequestBody JobCheckList jobCheckList) {

        jobCheckListService.upsertJobCheckList(
                jobCardId,
                jobCheckList
        );

        return ResponseEntity.ok(
                "Job checklist saved successfully"
        );
    }


    // =========================================================
    // GET CHECKLIST
    // =========================================================
    @GetMapping("/{jobCardId}")
    public ResponseEntity<JobCheckList> getJobCheckList(
            @PathVariable UUID jobCardId) {

        return jobCheckListService
                .getJobCheckList(jobCardId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}