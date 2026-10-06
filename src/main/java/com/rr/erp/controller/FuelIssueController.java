package com.rr.erp.controller;

import com.rr.erp.dto.FuelAssetSummary;
import com.rr.erp.dto.FuelConsumptionReport;
import com.rr.erp.entity.FuelIssue;
import com.rr.erp.service.FuelIssueService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fuel_issue")
@CrossOrigin
public class FuelIssueController {

    private final FuelIssueService service;

    public FuelIssueController(FuelIssueService service){
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<FuelIssue> createFuelIssue(
            @RequestBody FuelIssue fuelIssue
    ) {

        return ResponseEntity.ok(
                service.createFuelIssue(fuelIssue)
        );
    }



    @GetMapping("/issued/{projectCode}")
    public ResponseEntity<List<FuelIssue>>
    getIssuedFuel(
            @PathVariable String projectCode
    ) {

        return ResponseEntity.ok(
                service.getIssuedFuel(projectCode)
        );
    }


    @PutMapping("/received/{fuelIssueId}")
    public ResponseEntity<String> receiveFuel(
            @PathVariable UUID fuelIssueId
    ) {

        service.receiveFuel(
                fuelIssueId
        );

        return ResponseEntity.ok(
                "Fuel received successfully"
        );
    }

    @PutMapping("/delete/{fuelIssueId}")
    public ResponseEntity<String> deleteFuelIssue(
            @PathVariable UUID fuelIssueId
    ) {

        service.deleteFuelIssue(fuelIssueId);

        return ResponseEntity.ok(
                "Fuel issue deleted successfully"
        );
    }
    @GetMapping("/received/{receivedBy}")
    public ResponseEntity<List<FuelIssue>> getReceivedFuelIssues(
            @PathVariable String receivedBy
    ) {

        return ResponseEntity.ok(
                service.getReceivedFuelIssues(receivedBy)
        );
    }

    @GetMapping("/asset-summary")
    public ResponseEntity<List<FuelAssetSummary>> getFuelAssetSummary() {

        return ResponseEntity.ok(
                service.getFuelAssetSummary()
        );
    }

    @GetMapping("/asset/{assetCode}")
    public ResponseEntity<List<FuelIssue>> getFuelIssuesByAssetCode(
            @PathVariable String assetCode) {

        return ResponseEntity.ok(
                service.getFuelIssuesByAssetCode(assetCode)
        );
    }

    /** Total fuel consumption per project/fuel item. Omit projectCode for the cross-project distribution view. */
    @GetMapping("/report/consumption")
    public ResponseEntity<List<FuelConsumptionReport>> getFuelConsumptionReport(
            @RequestParam(required = false) String projectCode) {

        return ResponseEntity.ok(
                service.getFuelConsumptionReport(projectCode)
        );
    }
}