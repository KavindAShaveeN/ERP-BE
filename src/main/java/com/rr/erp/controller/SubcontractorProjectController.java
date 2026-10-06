package com.rr.erp.controller;

import com.rr.erp.entity.SubcontractorProjectAssignment;
import com.rr.erp.service.SubcontractorProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subcontractor_project")
@CrossOrigin
public class SubcontractorProjectController {

    private final SubcontractorProjectService subcontractorProjectService;

    public SubcontractorProjectController(SubcontractorProjectService subcontractorProjectService) {
        this.subcontractorProjectService = subcontractorProjectService;
    }


    // GET api/subcontractor_project/{projectCode}
    @GetMapping("/{projectCode}")
    public ResponseEntity<List<SubcontractorProjectAssignment>> getAssignmentsByProjectCode(
            @PathVariable String projectCode
    ) {
        return ResponseEntity.ok(
                subcontractorProjectService.getAssignmentsByProjectCode(projectCode)
        );
    }


    // POST api/subcontractor_project/ — registers a subcontractor to a project, or
    // updates its contract link if it's already registered there.
    @PostMapping("/")
    public ResponseEntity<?> assign(
            @RequestBody SubcontractorProjectAssignment assignment
    ) {
        subcontractorProjectService.assign(
                assignment.getSubcontractorId(),
                assignment.getProjectCode(),
                assignment.getContractLink()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "Subcontractor registered to project successfully"));
    }


    // DELETE api/subcontractor_project/{subcontractorId}/{projectCode}
    @DeleteMapping("/{subcontractorId}/{projectCode}")
    public ResponseEntity<?> unassign(
            @PathVariable Integer subcontractorId,
            @PathVariable String projectCode
    ) {
        boolean removed = subcontractorProjectService.unassign(subcontractorId, projectCode);

        if (!removed) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Assignment not found"));
        }

        return ResponseEntity.ok(Map.of("message", "Subcontractor removed from project successfully"));
    }
}
