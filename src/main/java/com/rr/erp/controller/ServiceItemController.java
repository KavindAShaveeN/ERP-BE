package com.rr.erp.controller;

import com.rr.erp.entity.ServiceItem;
import com.rr.erp.service.ServiceItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-items")
@CrossOrigin(origins = "*")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    public ServiceItemController(ServiceItemService serviceItemService) {
        this.serviceItemService = serviceItemService;
    }

    @PostMapping
    public ResponseEntity<ServiceItem> createServiceItem(@Valid @RequestBody ServiceItem request) {
        ServiceItem saved = serviceItemService.addServiceItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ServiceItem>> getAllServiceItems() {
        return ResponseEntity.ok(serviceItemService.getAllServiceItems());
    }

    @GetMapping("/{serviceItemId}")
    public ResponseEntity<ServiceItem> getServiceItemById(@PathVariable Long serviceItemId) {
        return ResponseEntity.ok(serviceItemService.getServiceItemById(serviceItemId));
    }

    @PutMapping("/{serviceItemId}")
    public ResponseEntity<ServiceItem> updateServiceItem(
            @PathVariable Long serviceItemId,
            @Valid @RequestBody ServiceItem request
    ) {
        ServiceItem updated = serviceItemService.updateServiceItem(serviceItemId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{serviceItemId}")
    public ResponseEntity<Void> deleteServiceItem(@PathVariable Long serviceItemId) {
        serviceItemService.deleteServiceItem(serviceItemId);
        return ResponseEntity.noContent().build();
    }
}
