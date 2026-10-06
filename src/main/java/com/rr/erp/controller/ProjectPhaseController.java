package com.rr.erp.controller;

import com.rr.erp.entity.ProjectPhase;
import com.rr.erp.service.ProjectPhaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project_phase")
@CrossOrigin
public class ProjectPhaseController {

    private final ProjectPhaseService projectPhaseService;

    public ProjectPhaseController(ProjectPhaseService projectPhaseService) {
        this.projectPhaseService = projectPhaseService;
    }


    // POST api/project_phase/
    @PostMapping("/")
    public ResponseEntity<?> createProjectPhase(
            @RequestBody ProjectPhase projectPhase
    ) {

        Integer projectPhaseId =
                projectPhaseService.createProjectPhase(projectPhase);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "message", "Project phase created successfully",
                                "projectPhaseId", projectPhaseId
                        )
                );
    }


    // GET api/project_phase/{projectId}
    @GetMapping("/{projectId}")
    public ResponseEntity<List<ProjectPhase>>
    getProjectPhasesByProjectId(
            @PathVariable Integer projectId
    ) {

        List<ProjectPhase> projectPhases =
                projectPhaseService
                        .getProjectPhasesByProjectId(projectId);

        return ResponseEntity.ok(projectPhases);
    }


    // GET api/project_phase/all
    @GetMapping("/all")
    public ResponseEntity<List<ProjectPhase>>
    getAllProjectPhases() {

        return ResponseEntity.ok(
                projectPhaseService.getAllProjectPhases()
        );
    }


    // PUT api/project_phase/{projectPhaseId}
    @PutMapping("/{projectPhaseId}")
    public ResponseEntity<?> updateProjectPhase(
            @PathVariable Integer projectPhaseId,
            @RequestBody ProjectPhase projectPhase
    ) {

        boolean updated =
                projectPhaseService.updateProjectPhase(
                        projectPhaseId,
                        projectPhase
                );

        if (!updated) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "Project phase not found"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Project phase updated successfully"
                )
        );
    }


    // DELETE api/project_phase/{projectPhaseId}
    @DeleteMapping("/{projectPhaseId}")
    public ResponseEntity<?> deleteProjectPhase(
            @PathVariable Integer projectPhaseId
    ) {

        boolean deleted =
                projectPhaseService.deleteProjectPhase(projectPhaseId);

        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "Project phase not found"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Project phase deleted successfully"
                )
        );
    }
}