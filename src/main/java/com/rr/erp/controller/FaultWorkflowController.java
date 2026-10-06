package com.rr.erp.controller;

import com.rr.erp.service.FaultWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-faults")
@RequiredArgsConstructor
@CrossOrigin
public class FaultWorkflowController {
    private final FaultWorkflowService service;
    public record Outcome(String outcome, String reason) {}
    public record Review(String reason, boolean resolved) {}
    @GetMapping("/types") public List<FaultWorkflowService.FaultType> types() { return service.types(); }
    @GetMapping("/backlog") public FaultWorkflowService.BacklogPage backlog(@RequestParam(defaultValue="") String query,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return service.backlog(query,page,size); }
    @GetMapping("/asset") public List<FaultWorkflowService.FaultReport> asset(@RequestParam String assetCode) { return service.assetFaults(assetCode); }
    @GetMapping("/request/{id}") public List<FaultWorkflowService.FaultReport> request(@PathVariable UUID id) { return service.requestFaults(id); }
    @GetMapping("/job/{id}") public List<FaultWorkflowService.WorkItem> job(@PathVariable UUID id) { return service.workItems(id); }
    @PutMapping("/job/{job}/defect/{defect}") public void outcome(@PathVariable UUID job,@PathVariable UUID defect,@RequestBody Outcome body) { service.outcome(job,defect,body.outcome(),body.reason()); }
    @PutMapping("/{id}/review") public void review(@PathVariable UUID id,@RequestBody Review body) { service.review(id,body.reason(),body.resolved()); }
}
