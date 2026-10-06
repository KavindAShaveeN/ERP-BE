package com.rr.erp.controller;

import com.rr.erp.dto.BinLocationRequest;
import com.rr.erp.dto.ItemCodeResponse;
import com.rr.erp.dto.ProjectWiseQuantityResponse;
import com.rr.erp.dto.ReorderLevelRequest;
import com.rr.erp.dto.StockMovementResponse;
import com.rr.erp.service.ProjectStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/project_store")
@CrossOrigin(origins = "*")
public class ProjectStoreController {

    private final ProjectStoreService projectStoreService;

    public ProjectStoreController(
            ProjectStoreService projectStoreService
    ) {
        this.projectStoreService = projectStoreService;
    }

    @GetMapping("/item_codes/{projectCode}")
    public ResponseEntity<List<ItemCodeResponse>> getItemCodesByProjectCode(
            @PathVariable String projectCode
    ) {

        return ResponseEntity.ok(
                projectStoreService.getItemCodesByProjectCode(projectCode)
        );
    }

    @GetMapping("/all_item_codes")
    public ResponseEntity<List<ItemCodeResponse>> getAllItemQuantities(
            @RequestParam(required = false) Integer projectTypeId
    ) {

        return ResponseEntity.ok(
                projectStoreService.getAllItemQuantities(projectTypeId)
        );
    }

    @GetMapping("/projectwisequantity/{itemCode}")
    public ResponseEntity<List<ProjectWiseQuantityResponse>> getProjectWiseQuantityByItemCode(
            @PathVariable String itemCode
    ) {

        return ResponseEntity.ok(
                projectStoreService.getProjectWiseQuantityByItemCode(itemCode)
        );
    }

    @GetMapping("/history/{projectCode}/{itemCode}")
    public ResponseEntity<List<StockMovementResponse>> getItemHistory(
            @PathVariable String projectCode,
            @PathVariable String itemCode
    ) {

        return ResponseEntity.ok(
                projectStoreService.getItemHistory(projectCode, itemCode)
        );
    }

    @GetMapping("/history/{itemCode}")
    public ResponseEntity<List<StockMovementResponse>> getAllProjectsItemHistory(
            @PathVariable String itemCode,
            @RequestParam(required = false) Integer projectTypeId
    ) {

        return ResponseEntity.ok(
                projectStoreService.getAllProjectsItemHistory(itemCode, projectTypeId)
        );
    }

    @PutMapping("/reorder_level")
    public ResponseEntity<Void> setReorderLevel(
            @RequestBody ReorderLevelRequest request
    ) {

        projectStoreService.setReorderLevel(
                request.getProjectCode(),
                request.getItemCode(),
                request.getReorderLevel(),
                request.getReorderQuantity()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bin_location")
    public ResponseEntity<Void> setBinLocation(
            @RequestBody BinLocationRequest request
    ) {

        projectStoreService.setBinLocation(
                request.getProjectCode(),
                request.getItemCode(),
                request.getBinLocation()
        );

        return ResponseEntity.noContent().build();
    }
}