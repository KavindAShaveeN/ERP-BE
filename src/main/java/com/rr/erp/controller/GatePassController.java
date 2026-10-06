package com.rr.erp.controller;

import com.rr.erp.dto.GatePassResponse;
import com.rr.erp.service.GatePassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * The security-guard-facing "Gate Passes" view: one call to see everything due through a
 * project's gate (GIN + stock return, outgoing + incoming), and one call per row to tick it
 * through. Deliberately thin — no create/edit, matching the "view and tick, that's all" brief.
 */
@RestController
@RequestMapping("/api/gate-pass")
@CrossOrigin(origins = "*")
public class GatePassController {

    private final GatePassService gatePassService;

    public GatePassController(GatePassService gatePassService) {
        this.gatePassService = gatePassService;
    }

    // GET /api/gate-pass/{projectCode}
    @GetMapping("/{projectCode}")
    public ResponseEntity<GatePassResponse> getGatePass(@PathVariable String projectCode) {
        return ResponseEntity.ok(gatePassService.getGatePass(projectCode));
    }

    // PUT /api/gate-pass/exit/{docType}/{id}?gateVerifiedBy=NAME
    @PutMapping("/exit/{docType}/{id}")
    public ResponseEntity<Void> confirmExit(
            @PathVariable String docType,
            @PathVariable UUID id,
            @RequestParam String gateVerifiedBy) {

        gatePassService.confirmExit(docType.toUpperCase(), id, gateVerifiedBy);
        return ResponseEntity.ok().build();
    }

    // PUT /api/gate-pass/arrival/{docType}/{id}?gateVerifiedBy=NAME
    @PutMapping("/arrival/{docType}/{id}")
    public ResponseEntity<Void> confirmArrival(
            @PathVariable String docType,
            @PathVariable UUID id,
            @RequestParam String gateVerifiedBy) {

        gatePassService.confirmArrival(docType.toUpperCase(), id, gateVerifiedBy);
        return ResponseEntity.ok().build();
    }
}
