package com.rr.erp.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

/** Return actionable validation reasons without exposing database exception details. */
@RestControllerAdvice(assignableTypes = {FaultWorkflowController.class, JobCardController.class,
        ServiceRequestController.class, DefectController.class})
public class ServiceWorkflowExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> validation(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message",
                exception.getReason() == null ? "Unable to complete this operation" : exception.getReason()));
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String,String>> duplicate() {
        return ResponseEntity.status(409).body(Map.of("message",
                "A job code or fault assignment was already saved. Refresh the list and check the existing job before retrying."));
    }
}
