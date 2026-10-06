package com.rr.erp.controller;

import com.rr.erp.dto.JobCostSummaryRow;
import com.rr.erp.dto.LaborEntryRow;
import com.rr.erp.dto.ThirdPartyServiceReportRow;
import com.rr.erp.service.ServiceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/service-report")
@RequiredArgsConstructor
@CrossOrigin
public class ServiceReportController {

    private final ServiceReportService serviceReportService;

    // GET /api/service-report/jobs?from=yyyy-MM-dd&to=yyyy-MM-dd
    // Every job card (main and sub) created in the range, with cost + labour rolled up.
    @GetMapping("/jobs")
    public ResponseEntity<List<JobCostSummaryRow>> getJobCostSummaries(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                serviceReportService.getJobCostSummaries(from, to)
        );
    }

    // GET /api/service-report/labor?from=yyyy-MM-dd&to=yyyy-MM-dd
    // Hours each employee logged per job card, for job cards created in the range.
    @GetMapping("/labor")
    public ResponseEntity<List<LaborEntryRow>> getLaborEntries(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                serviceReportService.getLaborEntries(from, to)
        );
    }

    // GET /api/service-report/third-party-service?from=yyyy-MM-dd&to=yyyy-MM-dd
    // Every third-party service issue (item/asset sent out, and received back) with issued
    // date in the range, plus its job card context.
    @GetMapping("/third-party-service")
    public ResponseEntity<List<ThirdPartyServiceReportRow>> getThirdPartyServiceEntries(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                serviceReportService.getThirdPartyServiceEntries(from, to)
        );
    }
}
