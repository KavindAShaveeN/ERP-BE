package com.rr.erp.controller;


import com.rr.erp.entity.Designation;
import com.rr.erp.service.DesignationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designations")
@CrossOrigin(origins = "*")
public class DesignationController {

    private final DesignationService designationService;

    public DesignationController(
            DesignationService designationService
    ) {
        this.designationService = designationService;
    }

    @GetMapping
    public ResponseEntity<List<Designation>>
    getAllDesignations() {

        return ResponseEntity.ok(
                designationService.getAllDesignations()
        );
    }
}