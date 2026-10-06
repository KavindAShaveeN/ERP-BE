package com.rr.erp.controller;

import com.rr.erp.entity.Subcontractor;
import com.rr.erp.service.SubcontractorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subcontractors")
@CrossOrigin
public class SubcontractorController {

    private final SubcontractorService subcontractorService;

    public SubcontractorController(SubcontractorService subcontractorService) {
        this.subcontractorService = subcontractorService;
    }


    // POST api/subcontractors/
    @PostMapping("/")
    public ResponseEntity<?> createSubcontractor(
            @RequestBody Subcontractor subcontractor
    ) {

        Integer subcontractorId =
                subcontractorService.createSubcontractor(subcontractor);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "message", "Subcontractor created successfully",
                                "subcontractorId", subcontractorId
                        )
                );
    }


    // GET api/subcontractors/
    @GetMapping("/")
    public ResponseEntity<List<Subcontractor>> getAllSubcontractors() {

        return ResponseEntity.ok(
                subcontractorService.getAllSubcontractors()
        );
    }


    // GET api/subcontractors/{projectCode}
    @GetMapping("/{projectCode}")
    public ResponseEntity<List<Subcontractor>>
    getSubcontractorsByProjectCode(
            @PathVariable String projectCode
    ) {

        List<Subcontractor> subcontractors =
                subcontractorService
                        .getSubcontractorsByProjectCode(projectCode);

        return ResponseEntity.ok(subcontractors);
    }


    // GET api/subcontractors/record/{subcontractorId}
    @GetMapping("/record/{subcontractorId}")
    public ResponseEntity<?> getSubcontractorById(
            @PathVariable Integer subcontractorId
    ) {

        return subcontractorService.getSubcontractorById(subcontractorId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Subcontractor not found")));
    }


    // PUT api/subcontractors/{subcontractorId}
    @PutMapping("/{subcontractorId}")
    public ResponseEntity<?> updateSubcontractor(
            @PathVariable Integer subcontractorId,
            @RequestBody Subcontractor subcontractor
    ) {

        boolean updated =
                subcontractorService.updateSubcontractor(
                        subcontractorId,
                        subcontractor
                );

        if (!updated) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "Subcontractor not found"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Subcontractor updated successfully"
                )
        );
    }


    // DELETE api/subcontractors/{subcontractorId}
    @DeleteMapping("/{subcontractorId}")
    public ResponseEntity<?> deleteSubcontractor(
            @PathVariable Integer subcontractorId
    ) {

        boolean deleted =
                subcontractorService.deleteSubcontractor(subcontractorId);

        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    "Subcontractor not found"
                            )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Subcontractor deleted successfully"
                )
        );
    }
}
