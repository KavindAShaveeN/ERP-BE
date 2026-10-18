package com.rr.erp.controller;

import com.rr.erp.dto.ProjectRecordSheetResponse;
import com.rr.erp.service.ProjectRecordSheetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/project-record-sheet")
@CrossOrigin(origins = "*")
public class ProjectRecordSheetController {

    private final ProjectRecordSheetService recordSheetService;

    public ProjectRecordSheetController(ProjectRecordSheetService recordSheetService) {
        this.recordSheetService = recordSheetService;
    }

    // GET /api/project-record-sheet?projectCode=&from=&to= — labour, plant and materials for the range.
    @GetMapping
    public ResponseEntity<ProjectRecordSheetResponse> get(
            @RequestParam String projectCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(recordSheetService.build(projectCode, from, to));
    }
}
