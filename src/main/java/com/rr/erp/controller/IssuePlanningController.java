package com.rr.erp.controller;

import com.rr.erp.dto.DispatchLineResponse;
import com.rr.erp.dto.ForwardPlanAgreeRequest;
import com.rr.erp.dto.IssuePlanConfirmRequest;
import com.rr.erp.dto.IssuePlanGridResponse;
import com.rr.erp.dto.IssuePlanResult;
import com.rr.erp.dto.PlannedAllocationResponse;
import com.rr.erp.service.IssuePlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issue-planning")
@CrossOrigin(origins = "*")
public class IssuePlanningController {

    private final IssuePlanningService issuePlanningService;

    public IssuePlanningController(IssuePlanningService issuePlanningService) {
        this.issuePlanningService = issuePlanningService;
    }

    /** The item x pending-MR planning grid for one store. */
    @GetMapping("/grid")
    public ResponseEntity<IssuePlanGridResponse> getGrid(@RequestParam String storeCode) {
        return ResponseEntity.ok(issuePlanningService.getGrid(storeCode));
    }

    /** Saves the storekeeper's per-line allocations as reservations. */
    @PostMapping("/confirm")
    public ResponseEntity<IssuePlanResult> confirmPlan(@RequestBody IssuePlanConfirmRequest request) {
        return ResponseEntity.ok(issuePlanningService.confirmPlan(request));
    }

    /** Records the user-agreed shortfall plan; FORWARD rows create the FWD- MR to Purchasing. */
    @PostMapping("/forward-plan/agree")
    public ResponseEntity<IssuePlanResult> agreeForwardPlan(@RequestBody ForwardPlanAgreeRequest request) {
        return ResponseEntity.ok(issuePlanningService.agreeForwardPlan(request));
    }

    /** Every reserved MR line at the store, across sites - grouped by site to build one GIN per site. */
    @GetMapping("/dispatch-lines")
    public ResponseEntity<List<DispatchLineResponse>> getDispatchLines(@RequestParam String storeCode) {
        return ResponseEntity.ok(issuePlanningService.getDispatchLines(storeCode));
    }

    /** Reserved quantities for one MR at a store — used to pre-fill the GIN. */
    @GetMapping("/allocations")
    public ResponseEntity<List<PlannedAllocationResponse>> getAllocations(
            @RequestParam UUID mrId,
            @RequestParam String storeCode
    ) {
        return ResponseEntity.ok(issuePlanningService.getPlannedAllocations(mrId, storeCode));
    }
}
