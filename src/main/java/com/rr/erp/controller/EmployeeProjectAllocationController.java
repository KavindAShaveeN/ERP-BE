package com.rr.erp.controller;

import com.rr.erp.dto.EmployeeDayAllocationRequest;
import com.rr.erp.entity.EmployeeProjectAllocation;
import com.rr.erp.service.EmployeeProjectAllocationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee-project-allocations")
@CrossOrigin(origins = "*")
public class EmployeeProjectAllocationController {

    private final EmployeeProjectAllocationService allocationService;

    public EmployeeProjectAllocationController(EmployeeProjectAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    // GET /api/employee-project-allocations?from=&to=&employeeCode=&projectId= — all filters optional.
    @GetMapping
    public ResponseEntity<List<EmployeeProjectAllocation>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) Integer projectId) {

        return ResponseEntity.ok(allocationService.search(from, to, employeeCode, projectId));
    }

    // PUT /api/employee-project-allocations/day — replaces the listed employees' allocations for the day.
    @PutMapping("/day")
    public ResponseEntity<List<EmployeeProjectAllocation>> saveDay(@RequestBody EmployeeDayAllocationRequest request) {
        return ResponseEntity.ok(allocationService.saveDay(request));
    }

    // POST /api/employee-project-allocations/copy?from=&to=&assignedBy= — fills employees still empty on the target day.
    @PostMapping("/copy")
    public ResponseEntity<List<EmployeeProjectAllocation>> copyDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String assignedBy) {

        return ResponseEntity.ok(allocationService.copyDay(from, to, assignedBy));
    }

    // DELETE /api/employee-project-allocations/{allocationId}
    @DeleteMapping("/{allocationId}")
    public ResponseEntity<String> delete(@PathVariable UUID allocationId) {

        if (allocationService.delete(allocationId)) {
            return ResponseEntity.ok("Allocation removed successfully");
        }

        return ResponseEntity.notFound().build();
    }
}
