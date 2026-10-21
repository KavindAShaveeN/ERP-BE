package com.rr.erp.controller;

import com.rr.erp.entity.ThreePService;
import com.rr.erp.service.ThreePServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/3p-service")
@CrossOrigin(origins = "*")
public class ThreePServiceController {

    private final ThreePServiceService service;

    public ThreePServiceController(ThreePServiceService service) {
        this.service = service;
    }

    // POST api/3p-service/
    @PostMapping("/")
    public ResponseEntity<ThreePService> createThreePService(
            @RequestBody ThreePService threePService
    ) {

        return ResponseEntity.ok(
                service.createThreePService(threePService)
        );
    }

    // POST api/3p-service/batch
    @PostMapping("/batch")
    public ResponseEntity<List<ThreePService>> createBatch(
            @RequestBody List<ThreePService> threePServices
    ) {

        return ResponseEntity.ok(service.createBatch(threePServices));
    }

    // GET api/3p-service/requests?status=all|Requested|PO Created ...
    @GetMapping("/requests")
    public ResponseEntity<List<ThreePService>> getRequests(
            @RequestParam(defaultValue = "all") String status
    ) {

        return ResponseEntity.ok(service.getRequests(status));
    }

    // GET api/3p-service/ready/{projectCode}
    @GetMapping("/ready/{projectCode}")
    public ResponseEntity<List<ThreePService>> getReady(@PathVariable String projectCode) {

        return ResponseEntity.ok(service.getReady(projectCode));
    }

    // PUT api/3p-service/{3pServiceId}
    @PutMapping("/{threePServiceId}")
    public ResponseEntity<ThreePService> updateThreePService(
            @PathVariable UUID threePServiceId,
            @RequestBody ThreePService threePService
    ) {

        return ResponseEntity.ok(
                service.updateThreePService(
                        threePServiceId,
                        threePService
                )
        );
    }

    // DELETE api/3p-service/{threePServiceId}
    @DeleteMapping("/{threePServiceId}")
    public ResponseEntity<Void> deleteThreePService(
            @PathVariable UUID threePServiceId
    ) {

        service.deleteThreePService(threePServiceId);

        return ResponseEntity.noContent().build();
    }
}