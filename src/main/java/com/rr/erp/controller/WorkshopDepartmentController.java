package com.rr.erp.controller;

import com.rr.erp.entity.WorkshopDepartment;
import com.rr.erp.service.WorkshopDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshop-department")
@RequiredArgsConstructor
@CrossOrigin
public class WorkshopDepartmentController {

    private final WorkshopDepartmentService workshopDepartmentService;

    // GET /api/workshop-department/
    @GetMapping("/")
    public ResponseEntity<List<WorkshopDepartment>> getAllWorkshopDepartments() {

        return ResponseEntity.ok(
                workshopDepartmentService.getAllWorkshopDepartments()
        );
    }
}
