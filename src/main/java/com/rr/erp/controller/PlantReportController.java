package com.rr.erp.controller;

import com.rr.erp.dto.PlantCostTrendRow;
import com.rr.erp.dto.PlantExpenseSummaryRow;
import com.rr.erp.dto.PlantFinanceSummaryRow;
import com.rr.erp.dto.PlantProductionCostRow;
import com.rr.erp.dto.PlantProductionLineRow;
import com.rr.erp.dto.PlantProductionProfitRow;
import com.rr.erp.dto.PlantRawMaterialCostRow;
import com.rr.erp.dto.PlantStockMovementRow;
import com.rr.erp.dto.PlantStockSummaryRow;
import com.rr.erp.service.PlantReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Filters: project (the plant — there is no separate plant registration, the
 * signed-in user's active project is the plant), product (finished-good item
 * code), raw material (item code), recipe and date range, as required.
 * {@code /lines} backs the daily production, raw-material consumption,
 * finished-goods production, recipe-vs-actual variance, and wastage/loss
 * reports — the frontend applies the lineType/isWaste distinction client-side
 * since it's the same underlying data (see PlantProductionLineRow).
 */
@RestController
@RequestMapping("/api/plant-report")
@CrossOrigin(origins = "*")
public class PlantReportController {

    private final PlantReportService reportService;

    public PlantReportController(PlantReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/lines")
    public ResponseEntity<List<PlantProductionLineRow>> getProductionLines(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) UUID recipeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String productItemCode,
            @RequestParam(required = false) String rawMaterialItemCode
    ) {
        return ResponseEntity.ok(reportService.getProductionLines(
                projectCode, recipeId, status, dateFrom, dateTo, productItemCode, rawMaterialItemCode
        ));
    }

    @GetMapping("/stock-movements")
    public ResponseEntity<List<PlantStockMovementRow>> getStockMovements(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getStockMovements(projectCode, itemCode, dateFrom, dateTo));
    }

    @GetMapping("/production-costs")
    public ResponseEntity<List<PlantProductionCostRow>> getProductionCosts(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getProductionCosts(projectCode, itemCode, dateFrom, dateTo));
    }

    @GetMapping("/stock-summary")
    public ResponseEntity<List<PlantStockSummaryRow>> getStockSummary(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getStockSummary(projectCode, itemCode, dateFrom, dateTo));
    }

    // ---- Finance / management reports — approved productions only ----

    @GetMapping("/raw-material-costs")
    public ResponseEntity<List<PlantRawMaterialCostRow>> getRawMaterialCosts(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getRawMaterialCosts(projectCode, itemCode, dateFrom, dateTo));
    }

    @GetMapping("/cost-trend")
    public ResponseEntity<List<PlantCostTrendRow>> getCostTrend(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getCostTrend(projectCode, itemCode, dateFrom, dateTo));
    }

    @GetMapping("/production-profits")
    public ResponseEntity<List<PlantProductionProfitRow>> getProductionProfits(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getProductionProfits(projectCode, dateFrom, dateTo));
    }

    @GetMapping("/finance-summary")
    public ResponseEntity<List<PlantFinanceSummaryRow>> getFinanceSummary(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getFinanceSummary(projectCode, dateFrom, dateTo));
    }

    @GetMapping("/expense-summary")
    public ResponseEntity<List<PlantExpenseSummaryRow>> getExpenseSummary(
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reportService.getExpenseSummary(projectCode, dateFrom, dateTo));
    }
}
