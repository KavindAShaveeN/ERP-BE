package com.rr.erp.controller;

import com.rr.erp.entity.Defect;
import com.rr.erp.service.DefectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/defect")
@CrossOrigin(origins = "*")
public class DefectController {

    private final DefectService defectService;

    public DefectController(DefectService defectService) {
        this.defectService = defectService;
    }


    // =========================================================
    // POST /api/defect/
    // ADD MULTIPLE DEFECTS
    // =========================================================

    @PostMapping("/")
    public ResponseEntity<String> createDefects(
            @RequestBody List<Defect> defects
    ) {

        defectService.createDefects(defects);

        return ResponseEntity.ok(
                "Defects created successfully"
        );
    }


    // =========================================================
    // PUT /api/defect/{defectId}
    // UPDATE DEFECT
    // =========================================================

    @PutMapping("/{defectId}")
    public ResponseEntity<String> updateDefect(
            @PathVariable UUID defectId,
            @RequestBody Defect defect
    ) {

        defectService.updateDefect(defectId, defect);

        return ResponseEntity.ok(
                "Defect updated successfully"
        );
    }


    // =========================================================
    // DELETE /api/defect/{defectId}
    // DELETE DEFECT
    // =========================================================

    @DeleteMapping("/{defectId}")
    public ResponseEntity<Void> deleteDefect(
            @PathVariable UUID defectId
    ) {

        defectService.deleteDefect(defectId);

        return ResponseEntity.noContent().build();
    }
}