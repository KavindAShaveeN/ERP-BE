package com.rr.erp.service;

import com.rr.erp.dto.PlantCostTrendRow;
import com.rr.erp.dto.PlantExpenseSummaryRow;
import com.rr.erp.dto.PlantFinanceSummaryRow;
import com.rr.erp.dto.PlantProductionCostRow;
import com.rr.erp.dto.PlantProductionLineRow;
import com.rr.erp.dto.PlantProductionProfitRow;
import com.rr.erp.dto.PlantRawMaterialCostRow;
import com.rr.erp.dto.PlantStockMovementRow;
import com.rr.erp.dto.PlantStockSummaryRow;
import com.rr.erp.repository.PlantReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PlantReportService {

    private final PlantReportRepository reportRepository;

    public PlantReportService(PlantReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<PlantProductionLineRow> getProductionLines(
            String projectCode, UUID recipeId, String status,
            LocalDate dateFrom, LocalDate dateTo, String productItemCode, String rawMaterialItemCode
    ) {
        return reportRepository.findProductionLines(
                projectCode, recipeId, status, dateFrom, dateTo, productItemCode, rawMaterialItemCode
        );
    }

    public List<PlantStockMovementRow> getStockMovements(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findStockMovements(projectCode, itemCode, dateFrom, dateTo);
    }

    public List<PlantProductionCostRow> getProductionCosts(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findProductionCosts(projectCode, itemCode, dateFrom, dateTo);
    }

    public List<PlantStockSummaryRow> getStockSummary(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findStockSummary(projectCode, itemCode, dateFrom, dateTo);
    }

    public List<PlantRawMaterialCostRow> getRawMaterialCosts(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findRawMaterialCosts(projectCode, itemCode, dateFrom, dateTo);
    }

    public List<PlantCostTrendRow> getCostTrend(
            String projectCode, String itemCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findCostTrend(projectCode, itemCode, dateFrom, dateTo);
    }

    public List<PlantProductionProfitRow> getProductionProfits(
            String projectCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findProductionProfits(projectCode, dateFrom, dateTo);
    }

    public List<PlantFinanceSummaryRow> getFinanceSummary(
            String projectCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findFinanceSummary(projectCode, dateFrom, dateTo);
    }

    public List<PlantExpenseSummaryRow> getExpenseSummary(
            String projectCode, LocalDate dateFrom, LocalDate dateTo
    ) {
        return reportRepository.findExpenseSummary(projectCode, dateFrom, dateTo);
    }
}
