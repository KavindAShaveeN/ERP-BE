package com.rr.erp.controller;

import com.rr.erp.entity.ProjectStatus;
import com.rr.erp.service.ProjectStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-statuses")
@CrossOrigin(origins = "*")
public class ProjectStatusController {

    private final ProjectStatusService projectStatusService;

    public ProjectStatusController(
            ProjectStatusService projectStatusService
    ) {
        this.projectStatusService = projectStatusService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectStatus>>
    getAllProjectStatuses() {

        return ResponseEntity.ok(
                projectStatusService.getAllProjectStatuses()
        );
    }
}
