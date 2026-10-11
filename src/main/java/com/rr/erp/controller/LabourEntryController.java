package com.rr.erp.controller;

import com.rr.erp.entity.LabourEntry;
import com.rr.erp.service.LabourEntryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/labour-entries")
@CrossOrigin(origins = "*")
public class LabourEntryController {

    private final LabourEntryService labourEntryService;

    public LabourEntryController(LabourEntryService labourEntryService) {
        this.labourEntryService = labourEntryService;
    }

    // GET /api/labour-entries?from=&to=&projectCode=&employeeCode= — all filters optional. Backs both
    // the daily entry screen (from = to = the day) and the labour reports.
    @GetMapping
    public ResponseEntity<List<LabourEntry>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String employeeCode) {

        return ResponseEntity.ok(labourEntryService.search(from, to, projectCode, employeeCode));
    }

    // POST /api/labour-entries
    @PostMapping
    public ResponseEntity<LabourEntry> create(@RequestBody LabourEntry entry) {
        return ResponseEntity.ok(labourEntryService.create(entry));
    }

    // PUT /api/labour-entries/{labourEntryId}
    @PutMapping("/{labourEntryId}")
    public ResponseEntity<LabourEntry> update(
            @PathVariable UUID labourEntryId,
            @RequestBody LabourEntry entry) {

        return ResponseEntity.ok(labourEntryService.update(labourEntryId, entry));
    }

    // DELETE /api/labour-entries/{labourEntryId}
    @DeleteMapping("/{labourEntryId}")
    public ResponseEntity<String> delete(@PathVariable UUID labourEntryId) {

        if (labourEntryService.delete(labourEntryId)) {
            return ResponseEntity.ok("Labour entry removed successfully");
        }

        return ResponseEntity.notFound().build();
    }
}
