package com.rr.erp.controller;

import com.rr.erp.entity.ProjectType;
import com.rr.erp.service.ProjectTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectTypeController {

    private final ProjectTypeService projectTypeService;

    @GetMapping
    public ResponseEntity<List<ProjectType>> getAllProjectTypes() {
        return ResponseEntity.ok(
                projectTypeService.getAllProjectTypes()
        );
    }
}