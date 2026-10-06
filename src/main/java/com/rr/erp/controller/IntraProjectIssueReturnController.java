package com.rr.erp.controller;

import com.rr.erp.entity.IntraProjectIssueReturn;
import com.rr.erp.service.IntraProjectIssueReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/intra_project_issue_return")
@CrossOrigin
public class IntraProjectIssueReturnController {

    private final IntraProjectIssueReturnService service;

    public IntraProjectIssueReturnController(IntraProjectIssueReturnService service) {
        this.service = service;
    }

    // POST /api/intra_project_issue_return/
    @PostMapping("/")
    public ResponseEntity<IntraProjectIssueReturn> createReturn(
            @RequestBody IntraProjectIssueReturn intraProjectIssueReturn
    ) {

        return ResponseEntity.ok(
                service.createReturn(intraProjectIssueReturn)
        );
    }

    // PUT /api/intra_project_issue_return/{intraProjectIssueReturnId}
    @PutMapping("/{intraProjectIssueReturnId}")
    public ResponseEntity<IntraProjectIssueReturn> updateReturn(
            @PathVariable UUID intraProjectIssueReturnId,
            @RequestBody IntraProjectIssueReturn intraProjectIssueReturn
    ) {

        return ResponseEntity.ok(
                service.updateReturn(intraProjectIssueReturnId, intraProjectIssueReturn)
        );
    }

    // GET /api/intra_project_issue_return/all
    @GetMapping("/all")
    public ResponseEntity<List<IntraProjectIssueReturn>> getAllReturns() {

        return ResponseEntity.ok(
                service.getAllReturns()
        );
    }

    // GET /api/intra_project_issue_return/by-issue/{intraProjectIssueId}
    @GetMapping("/by-issue/{intraProjectIssueId}")
    public ResponseEntity<List<IntraProjectIssueReturn>> getReturnsByIssue(
            @PathVariable UUID intraProjectIssueId
    ) {

        return ResponseEntity.ok(
                service.getReturnsByIssue(intraProjectIssueId)
        );
    }

    // GET /api/intra_project_issue_return/by-job-card/{jobCardId}
    @GetMapping("/by-job-card/{jobCardId}")
    public ResponseEntity<List<IntraProjectIssueReturn>> getReturnsByJobCard(
            @PathVariable UUID jobCardId
    ) {

        return ResponseEntity.ok(
                service.getReturnsByJobCard(jobCardId)
        );
    }

    // GET /api/intra_project_issue_return/{issuedProjectCode}
    @GetMapping("/{issuedProjectCode}")
    public ResponseEntity<List<IntraProjectIssueReturn>> getReturnsByProject(
            @PathVariable String issuedProjectCode
    ) {

        return ResponseEntity.ok(
                service.getReturnsByProject(issuedProjectCode)
        );
    }
}
