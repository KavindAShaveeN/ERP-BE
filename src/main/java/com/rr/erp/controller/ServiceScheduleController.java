package com.rr.erp.controller;

import com.rr.erp.dto.ServiceScheduleDtos.CompletedServicesRequest;
import com.rr.erp.dto.ServiceScheduleDtos.DueItemDto;
import com.rr.erp.dto.ServiceScheduleDtos.TemplateDto;
import com.rr.erp.repository.ServiceScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-schedule")
@RequiredArgsConstructor
@CrossOrigin
public class ServiceScheduleController {

    private final ServiceScheduleRepository repository;

    @GetMapping("/templates")
    public List<TemplateDto> getTemplates() {
        return repository.getTemplates();
    }

    @PostMapping("/templates")
    public ResponseEntity<UUID> createTemplate(@RequestBody TemplateDto body) {
        UUID id = UUID.randomUUID();
        repository.insertTemplate(body, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<Void> updateTemplate(@PathVariable UUID id, @RequestBody TemplateDto body) {
        return repository.updateTemplate(id, body) > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        return repository.deleteTemplate(id) > 0
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/asset/{assetCode}")
    public List<DueItemDto> getForAsset(@PathVariable String assetCode) {
        return repository.getDueItems(assetCode, null);
    }

    // Always across every project — the upcoming-services list is a fleet-wide view, not
    // scoped to whatever project the caller happens to be in, so any projectCode the caller
    // sends is ignored rather than narrowing the result to one project.
    @GetMapping("/upcoming")
    public List<DueItemDto> getUpcoming() {
        return repository.getDueItems(null, null);
    }

    @GetMapping("/job-card/{jobCardId}/services")
    public List<UUID> getJobCardServices(@PathVariable UUID jobCardId) {
        return repository.getJobCardServiceIds(jobCardId);
    }

    @PutMapping("/job-card/{jobCardId}/services")
    public ResponseEntity<Void> setJobCardServices(@PathVariable UUID jobCardId,
                                                   @RequestBody CompletedServicesRequest body) {
        repository.replaceJobCardServices(jobCardId, body.serviceIds() == null ? List.of() : body.serviceIds());
        return ResponseEntity.noContent().build();
    }
}
