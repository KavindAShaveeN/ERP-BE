package com.rr.erp.controller;

import com.rr.erp.entity.SPIssue;
import com.rr.erp.service.SPIssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sp_issue")
public class SPIssueController {

    private final SPIssueService spIssueService;

    public SPIssueController(SPIssueService spIssueService) {
        this.spIssueService = spIssueService;
    }

    /*
     * POST
     * /api/sp_issue/
     */
    @PostMapping("/")
    public ResponseEntity<SPIssue> createSPIssue(
            @RequestBody SPIssue spIssue
    ) {

        return ResponseEntity.ok(
                spIssueService.createSPIssue(spIssue)
        );
    }

    /*
     * GET
     * /api/sp_issue/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<SPIssue>> getAllSPIssues() {

        return ResponseEntity.ok(
                spIssueService.getAllSPIssues()
        );
    }

    /*
     * PUT
     * /api/sp_issue/{spIssueId}
     */
    @PutMapping("/{spIssueId}")
    public ResponseEntity<SPIssue> updateSPIssue(
            @PathVariable UUID spIssueId,
            @RequestBody SPIssue spIssue
    ) {

        return ResponseEntity.ok(
                spIssueService.updateSPIssue(
                        spIssueId,
                        spIssue
                )
        );
    }
}
