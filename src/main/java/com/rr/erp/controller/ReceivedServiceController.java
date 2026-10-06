package com.rr.erp.controller;

import com.rr.erp.entity.ReceivedService;
import com.rr.erp.service.ReceivedServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/received-services")
@CrossOrigin(origins = "*")
public class ReceivedServiceController {

    private final ReceivedServiceService receivedServiceService;

    public ReceivedServiceController(ReceivedServiceService receivedServiceService) {
        this.receivedServiceService = receivedServiceService;
    }

    @GetMapping
    public ResponseEntity<List<ReceivedService>> getAll() {
        return ResponseEntity.ok(receivedServiceService.getAll());
    }

    @GetMapping("/project/{projectCode}")
    public ResponseEntity<List<ReceivedService>> getByProjectCode(@PathVariable String projectCode) {
        return ResponseEntity.ok(receivedServiceService.getByProjectCode(projectCode));
    }
}
