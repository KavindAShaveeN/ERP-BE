package com.rr.erp.controller;

import com.rr.erp.dto.ServiceReceiveNoteCreateRequest;
import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.ServiceReceiveNote;
import com.rr.erp.service.ServiceReceiveNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-receive-note")
@RequiredArgsConstructor
@CrossOrigin
public class ServiceReceiveNoteController {

    private final ServiceReceiveNoteService serviceReceiveNoteService;


    // =========================================================
    // POST /api/service-receive-note/
    // =========================================================
    @PostMapping("/")
    public ResponseEntity<ServiceReceiveNote> createServiceReceiveNote(
            @RequestBody ServiceReceiveNoteCreateRequest request) {

        ServiceReceiveNote created = serviceReceiveNoteService.createServiceReceiveNote(
                request.getJobCardId(),
                request.getReceivedBy(),
                request.getRemarks()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    // =========================================================
    // GET /api/service-receive-note/pending/{projectCode}
    // =========================================================
    @GetMapping("/pending/{projectCode}")
    public ResponseEntity<List<JobCard>> getPendingForProject(
            @PathVariable String projectCode) {

        return ResponseEntity.ok(
                serviceReceiveNoteService.getPendingForProject(projectCode)
        );
    }


    // =========================================================
    // GET /api/service-receive-note/all
    // =========================================================
    @GetMapping("/all")
    public ResponseEntity<List<ServiceReceiveNote>> getAll() {

        return ResponseEntity.ok(
                serviceReceiveNoteService.getAll()
        );
    }


    // =========================================================
    // GET /api/service-receive-note/project/{projectCode}
    // =========================================================
    @GetMapping("/project/{projectCode}")
    public ResponseEntity<List<ServiceReceiveNote>> getByRequestingProjectCode(
            @PathVariable String projectCode) {

        return ResponseEntity.ok(
                serviceReceiveNoteService.getByRequestingProjectCode(projectCode)
        );
    }


    // =========================================================
    // GET /api/service-receive-note/job-card/{jobCardId}
    // =========================================================
    @GetMapping("/job-card/{jobCardId}")
    public ResponseEntity<ServiceReceiveNote> getByJobCardId(
            @PathVariable UUID jobCardId) {

        return ResponseEntity.ok(
                serviceReceiveNoteService.getByJobCardId(jobCardId)
        );
    }


    // =========================================================
    // GET /api/service-receive-note/{serviceReceiveNoteId}
    // =========================================================
    @GetMapping("/{serviceReceiveNoteId}")
    public ResponseEntity<ServiceReceiveNote> getById(
            @PathVariable UUID serviceReceiveNoteId) {

        return ResponseEntity.ok(
                serviceReceiveNoteService.getById(serviceReceiveNoteId)
        );
    }
}
