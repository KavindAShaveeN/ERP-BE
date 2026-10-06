package com.rr.erp.controller;

import com.rr.erp.entity.JobStatusType;
import com.rr.erp.service.JobStatusTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-status-type")
@RequiredArgsConstructor
@CrossOrigin
public class JobStatusTypeController {

    private final JobStatusTypeService jobStatusTypeService;

    // GET /api/job_status_type/
    @GetMapping("/")
    public ResponseEntity<List<JobStatusType>> getAllJobStatusTypes() {

        return ResponseEntity.ok(
                jobStatusTypeService.getAllJobStatusTypes()
        );
    }
}