package com.rr.erp.controller;

import com.rr.erp.dto.ProjectResponseDTO;
import com.rr.erp.entity.Project;
import com.rr.erp.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "*")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @Valid @RequestBody Project request
    )
    {
        Project created = projectService.createProject(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {

        List<ProjectResponseDTO> projects =
                projectService.getAllProjects();

        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Integer projectId,
            @Valid @RequestBody Project projectRequest
    ) {

        Project updatedProject =
                projectService.updateProject(
                        projectId,
                        projectRequest
                );

        return ResponseEntity.ok(updatedProject);
    }
}
