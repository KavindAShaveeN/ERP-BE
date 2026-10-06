package com.rr.erp.controller;

import com.rr.erp.entity.IntraProjectIssue;
import com.rr.erp.service.IntraProjectIssueService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/intra_project_issue")
@CrossOrigin
public class IntraProjectIssueController {

    private final IntraProjectIssueService service;

    public IntraProjectIssueController(IntraProjectIssueService service) {
        this.service = service;
    }

    // POST /api/intra_project_issue/
    @PostMapping("/")
    public ResponseEntity<IntraProjectIssue> createIntraProjectIssue(
            @RequestBody IntraProjectIssue intraProjectIssue
    ) {

        return ResponseEntity.ok(
                service.createIntraProjectIssue(intraProjectIssue)
        );
    }

    // PUT /api/intra_project_issue/{IntraProjectIssueId}
    @PutMapping("/{intraProjectIssueId}")
    public ResponseEntity<IntraProjectIssue> updateIntraProjectIssue(
            @PathVariable UUID intraProjectIssueId,
            @RequestBody IntraProjectIssue intraProjectIssue
    ) {

        return ResponseEntity.ok(
                service.updateIntraProjectIssue(intraProjectIssueId, intraProjectIssue)
        );
    }

    // GET /api/intra_project_issue/all
    @GetMapping("/all")
    public ResponseEntity<List<IntraProjectIssue>> getAllIntraProjectIssues() {

        return ResponseEntity.ok(
                service.getAllIntraProjectIssues()
        );
    }

    // GET /api/intra_project_issue/by-job-card/{jobCardId}
    @GetMapping("/by-job-card/{jobCardId}")
    public ResponseEntity<List<IntraProjectIssue>> getByJobCardId(
            @PathVariable UUID jobCardId
    ) {

        return ResponseEntity.ok(
                service.getByJobCardId(jobCardId)
        );
    }

    // GET /api/intra_project_issue/{IssuedProjectCode}
    @GetMapping("/{issuedProjectCode}")
    public ResponseEntity<List<IntraProjectIssue>> getIntraProjectIssues(
            @PathVariable String issuedProjectCode
    ) {

        return ResponseEntity.ok(
                service.getIntraProjectIssues(issuedProjectCode)
        );
    }
}
