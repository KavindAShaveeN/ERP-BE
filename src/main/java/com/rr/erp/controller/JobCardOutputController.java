package com.rr.erp.controller;

import com.rr.erp.entity.JobCardOutput;
import com.rr.erp.service.JobCardOutputService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/job-card-output")
@RequiredArgsConstructor
@CrossOrigin
public class JobCardOutputController {

    private final JobCardOutputService service;

    // POST /api/job-card-output/
    @PostMapping("/")
    public ResponseEntity<JobCardOutput> create(@RequestBody JobCardOutput output) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(output));
    }

    // PUT /api/job-card-output/{jobCardOutputId}
    @PutMapping("/{jobCardOutputId}")
    public ResponseEntity<JobCardOutput> update(
            @PathVariable UUID jobCardOutputId,
            @RequestBody JobCardOutput output) {
        return ResponseEntity.ok(service.update(jobCardOutputId, output));
    }

    // DELETE /api/job-card-output/{jobCardOutputId}
    @DeleteMapping("/{jobCardOutputId}")
    public ResponseEntity<Void> delete(@PathVariable UUID jobCardOutputId) {
        service.delete(jobCardOutputId);
        return ResponseEntity.noContent().build();
    }
}
