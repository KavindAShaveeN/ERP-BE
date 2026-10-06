package com.rr.erp.service;

import com.rr.erp.entity.ServiceRequest;
import com.rr.erp.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final FaultWorkflowService faultWorkflow;


    // =========================================================
    // CREATE
    // =========================================================
    @org.springframework.transaction.annotation.Transactional
    public ServiceRequest createServiceRequest(
            ServiceRequest serviceRequest) {

        if (serviceRequest.getServiceRequestId() == null) {
            serviceRequest.setServiceRequestId(
                    UUID.randomUUID()
            );
        }

        if (serviceRequest.getRequestedDate() == null) {
            serviceRequest.setRequestedDate(
                    LocalDateTime.now()
            );
        }

        normalizeRequestType(serviceRequest);

        if (serviceRequest.getIsApproved() == null) {
            serviceRequest.setIsApproved(false);
        }

        int result =
                serviceRequestRepository
                        .createServiceRequest(serviceRequest);

        if (result == 0) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create service request"
            );
        }

        serviceRequest.setCreatedAt(LocalDateTime.now());
        serviceRequest.setUpdatedAt(LocalDateTime.now());

        faultWorkflow.syncRequest(serviceRequest);
        return serviceRequest;
    }


    // =========================================================
    // UPDATE
    // =========================================================
    @org.springframework.transaction.annotation.Transactional
    public ServiceRequest updateServiceRequest(
            ServiceRequest serviceRequest) {

        if (serviceRequest.getServiceRequestId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service request ID is required"
            );
        }

        normalizeRequestType(serviceRequest);
        faultWorkflow.lockRequestForEdit(serviceRequest);
        int result =
                serviceRequestRepository
                        .updateServiceRequest(serviceRequest);

        if (result == 0) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Service request not found"
            );
        }

        serviceRequest.setUpdatedAt(LocalDateTime.now());

        faultWorkflow.syncRequest(serviceRequest);
        return serviceRequest;
    }


    // =========================================================
    // GET BY PROJECT
    // =========================================================
    public List<ServiceRequest> getServiceRequestsByProjectCode(
            String projectCode) {

        return serviceRequestRepository
                .getServiceRequestsByProjectCode(projectCode);
    }


    // =========================================================
    // GET ALL
    // =========================================================
    public List<ServiceRequest> getAllServiceRequests() {

        return serviceRequestRepository
                .getAllServiceRequests();
    }

    private static final java.util.Set<String> REQUEST_TYPES = java.util.Set.of("BREAKDOWN", "FAULTS", "ACCIDENT");

    /** Defaults a missing type to FAULTS and rejects anything outside the allowed set. */
    private static void normalizeRequestType(ServiceRequest serviceRequest) {
        String type = serviceRequest.getRequestType() == null || serviceRequest.getRequestType().isBlank()
                ? "FAULTS" : serviceRequest.getRequestType().trim().toUpperCase();
        if (!REQUEST_TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request type must be Breakdown, Faults or Accident");
        }
        serviceRequest.setRequestType(type);
        if (serviceRequest.getMaintenanceWorks() == null) {
            serviceRequest.setMaintenanceWorks("");
        }
        if (serviceRequest.getRemarks() != null && serviceRequest.getRemarks().isBlank()) {
            serviceRequest.setRemarks(null);
        }
    }
}
