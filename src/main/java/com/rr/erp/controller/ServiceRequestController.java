package com.rr.erp.controller;

import com.rr.erp.entity.ServiceRequest;
import com.rr.erp.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-request")
@RequiredArgsConstructor
@CrossOrigin
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;


    // =========================================================
    // POST api/service-request/
    // =========================================================
    @PostMapping("/")
    public ResponseEntity<ServiceRequest> createServiceRequest(
            @RequestBody ServiceRequest serviceRequest) {

        ServiceRequest created =
                serviceRequestService
                        .createServiceRequest(serviceRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }


    // =========================================================
    // PUT api/service-request/
    // =========================================================
    @PutMapping("/")
    public ResponseEntity<ServiceRequest> updateServiceRequest(
            @RequestBody ServiceRequest serviceRequest) {

        ServiceRequest updated =
                serviceRequestService
                        .updateServiceRequest(serviceRequest);

        return ResponseEntity.ok(updated);
    }


    // =========================================================
    // GET api/service-request/created/{projectCode}
    // =========================================================
    @GetMapping("/created/{projectCode}")
    public ResponseEntity<List<ServiceRequest>>
    getServiceRequestsByProjectCode(
            @PathVariable String projectCode) {

        return ResponseEntity.ok(
                serviceRequestService
                        .getServiceRequestsByProjectCode(projectCode)
        );
    }


    // =========================================================
    // GET api/service-request/all
    // =========================================================
    @GetMapping("/all")
    public ResponseEntity<List<ServiceRequest>>
    getAllServiceRequests() {

        return ResponseEntity.ok(
                serviceRequestService
                        .getAllServiceRequests()
        );
    }
}