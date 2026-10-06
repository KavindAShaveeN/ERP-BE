package com.rr.erp.repository;

import com.rr.erp.entity.PlantProduction;
import com.rr.erp.entity.PlantProductionExpense;
import com.rr.erp.entity.PlantProductionInput;
import com.rr.erp.entity.PlantProductionOutput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PlantProductionRepository {

    private final JdbcTemplate jdbcTemplate;

    public PlantProductionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PlantProduction> productionRowMapper = (rs, rowNum) -> {
        PlantProduction production = new PlantProduction();

        production.setProductionId(rs.getObject("production_id", UUID.class));
        production.setProductionCode(rs.getString("production_code"));
        production.setProjectCode(rs.getString("project_code"));
        production.setProductionDate(rs.getObject("production_date", LocalDate.class));
        production.setRecipeId(rs.getObject("recipe_id", UUID.class));
        production.setStatus(rs.getString("status"));
        production.setRemarks(rs.getString("remarks"));
        production.setSubmittedBy(rs.getString("submitted_by"));
        production.setSubmittedDate(rs.getObject("submitted_date", LocalDateTime.class));
        production.setApprovedBy(rs.getString("approved_by"));
        production.setApprovedDate(rs.getObject("approved_date", LocalDateTime.class));
        production.setRejectedBy(rs.getString("rejected_by"));
        production.setRejectedDate(rs.getObject("rejected_date", LocalDateTime.class));
        production.setRejectionReason(rs.getString("rejection_reason"));
        production.setReversedBy(rs.getString("reversed_by"));
        production.setReversedDate(rs.getObject("reversed_date", LocalDateTime.class));
        production.setReversalReason(rs.getString("reversal_reason"));
        production.setCreatedBy(rs.getString("created_by"));
        production.setCreatedDate(rs.getObject("created_date", LocalDateTime.class));

        return production;
    };

    private final RowMapper<PlantProductionInput> inputRowMapper = (rs, rowNum) -> new PlantProductionInput(
            rs.getObject("production_input_id", UUID.class),
            rs.getObject("production_id", UUID.class),
            rs.getString("item_code"),
            rs.getInt("uom_id"),
            rs.getBigDecimal("planned_quantity"),
            rs.getBigDecimal("consumed_quantity"),
            rs.getBigDecimal("conversion_factor"),
            rs.getBigDecimal("stock_equivalent_qty")
    );

    private final RowMapper<PlantProductionOutput> outputRowMapper = (rs, rowNum) -> new PlantProductionOutput(
            rs.getObject("production_output_id", UUID.class),
            rs.getObject("production_id", UUID.class),
            rs.getString("item_code"),
            rs.getInt("uom_id"),
            rs.getBigDecimal("planned_quantity"),
            rs.getBigDecimal("produced_quantity"),
            (Boolean) rs.getObject("is_waste"),
            rs.getBigDecimal("conversion_factor"),
            rs.getBigDecimal("stock_equivalent_qty")
    );

    private final RowMapper<PlantProductionExpense> expenseRowMapper = (rs, rowNum) -> new PlantProductionExpense(
            rs.getObject("production_expense_id", UUID.class),
            rs.getObject("production_id", UUID.class),
            rs.getString("description"),
            rs.getBigDecimal("amount")
    );

    public PlantProduction insertProduction(PlantProduction production) {

        UUID productionId = production.getProductionId() != null
                ? production.getProductionId() : UUID.randomUUID();
        production.setProductionId(productionId);

        if (production.getCreatedDate() == null) {
            production.setCreatedDate(LocalDateTime.now());
        }

        String sql = """
                INSERT INTO plant_production (
                    production_id, production_code, project_code, production_date, recipe_id,
                    status, remarks, submitted_by, submitted_date, approved_by, approved_date,
                    rejected_by, rejected_date, rejection_reason,
                    reversed_by, reversed_date, reversal_reason,
                    created_by, created_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                production.getProductionId(),
                production.getProductionCode(),
                production.getProjectCode(),
                production.getProductionDate(),
                production.getRecipeId(),
                production.getStatus(),
                production.getRemarks(),
                production.getSubmittedBy(),
                production.getSubmittedDate(),
                production.getApprovedBy(),
                production.getApprovedDate(),
                production.getRejectedBy(),
                production.getRejectedDate(),
                production.getRejectionReason(),
                production.getReversedBy(),
                production.getReversedDate(),
                production.getReversalReason(),
                production.getCreatedBy(),
                production.getCreatedDate()
        );

        insertInputs(productionId, production.getInputs());
        insertOutputs(productionId, production.getOutputs());
        insertExpenses(productionId, production.getExpenses());

        return production;
    }

    public void insertInputs(UUID productionId, List<PlantProductionInput> inputs) {

        String sql = """
                INSERT INTO plant_production_input (
                    production_input_id, production_id, item_code, uom_id,
                    planned_quantity, consumed_quantity, conversion_factor, stock_equivalent_qty
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (PlantProductionInput input : inputs) {

            UUID id = UUID.randomUUID();
            input.setProductionInputId(id);
            input.setProductionId(productionId);

            jdbcTemplate.update(
                    sql, id, productionId, input.getItemCode(), input.getUomId(),
                    input.getPlannedQuantity(), input.getConsumedQuantity(),
                    input.getConversionFactor(), input.getStockEquivalentQty()
            );
        }
    }

    public void insertOutputs(UUID productionId, List<PlantProductionOutput> outputs) {

        String sql = """
                INSERT INTO plant_production_output (
                    production_output_id, production_id, item_code, uom_id,
                    planned_quantity, produced_quantity, is_waste, conversion_factor, stock_equivalent_qty
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (PlantProductionOutput output : outputs) {

            UUID id = UUID.randomUUID();
            output.setProductionOutputId(id);
            output.setProductionId(productionId);

            jdbcTemplate.update(
                    sql, id, productionId, output.getItemCode(), output.getUomId(),
                    output.getPlannedQuantity(), output.getProducedQuantity(), output.getIsWaste(),
                    output.getConversionFactor(), output.getStockEquivalentQty()
            );
        }
    }

    public void insertExpenses(UUID productionId, List<PlantProductionExpense> expenses) {

        if (expenses == null) {
            return;
        }

        String sql = """
                INSERT INTO plant_production_expense (
                    production_expense_id, production_id, description, amount
                )
                VALUES (?, ?, ?, ?)
                """;

        for (PlantProductionExpense expense : expenses) {

            UUID id = UUID.randomUUID();
            expense.setProductionExpenseId(id);
            expense.setProductionId(productionId);

            jdbcTemplate.update(sql, id, productionId, expense.getDescription().trim(), expense.getAmount());
        }
    }

    public void deleteExpensesByProductionId(UUID productionId) {
        jdbcTemplate.update("DELETE FROM plant_production_expense WHERE production_id = ?", productionId);
    }

    public void deleteInputsByProductionId(UUID productionId) {
        jdbcTemplate.update("DELETE FROM plant_production_input WHERE production_id = ?", productionId);
    }

    public void deleteOutputsByProductionId(UUID productionId) {
        jdbcTemplate.update("DELETE FROM plant_production_output WHERE production_id = ?", productionId);
    }

    public int updateProduction(UUID productionId, PlantProduction production) {

        String sql = """
                UPDATE plant_production
                SET
                    production_code = ?,
                    project_code = ?,
                    production_date = ?,
                    recipe_id = ?,
                    status = ?,
                    remarks = ?,
                    submitted_by = ?,
                    submitted_date = ?,
                    approved_by = ?,
                    approved_date = ?,
                    rejected_by = ?,
                    rejected_date = ?,
                    rejection_reason = ?,
                    reversed_by = ?,
                    reversed_date = ?,
                    reversal_reason = ?
                WHERE production_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                production.getProductionCode(),
                production.getProjectCode(),
                production.getProductionDate(),
                production.getRecipeId(),
                production.getStatus(),
                production.getRemarks(),
                production.getSubmittedBy(),
                production.getSubmittedDate(),
                production.getApprovedBy(),
                production.getApprovedDate(),
                production.getRejectedBy(),
                production.getRejectedDate(),
                production.getRejectionReason(),
                production.getReversedBy(),
                production.getReversedDate(),
                production.getReversalReason(),
                productionId
        );
    }

    public List<PlantProduction> findAll() {

        List<PlantProduction> productions = jdbcTemplate.query(
                "SELECT * FROM plant_production ORDER BY production_date DESC, production_code DESC",
                productionRowMapper
        );

        addLinesToProductions(productions);

        return productions;
    }

    public List<PlantProduction> findByProjectCode(String projectCode) {

        List<PlantProduction> productions = jdbcTemplate.query(
                "SELECT * FROM plant_production WHERE project_code = ? ORDER BY production_date DESC, production_code DESC",
                productionRowMapper, projectCode
        );

        addLinesToProductions(productions);

        return productions;
    }

    public Optional<PlantProduction> findById(UUID productionId) {

        List<PlantProduction> results = jdbcTemplate.query(
                "SELECT * FROM plant_production WHERE production_id = ?", productionRowMapper, productionId
        );

        if (results.isEmpty()) {
            return Optional.empty();
        }

        PlantProduction production = results.get(0);
        production.setInputs(findInputsByProductionId(productionId));
        production.setOutputs(findOutputsByProductionId(productionId));
        production.setExpenses(findExpensesByProductionId(productionId));

        return Optional.of(production);
    }

    public List<PlantProductionInput> findInputsByProductionId(UUID productionId) {
        return jdbcTemplate.query(
                "SELECT * FROM plant_production_input WHERE production_id = ? ORDER BY production_input_id",
                inputRowMapper, productionId
        );
    }

    public List<PlantProductionOutput> findOutputsByProductionId(UUID productionId) {
        return jdbcTemplate.query(
                "SELECT * FROM plant_production_output WHERE production_id = ? ORDER BY production_output_id",
                outputRowMapper, productionId
        );
    }

    public List<PlantProductionExpense> findExpensesByProductionId(UUID productionId) {
        return jdbcTemplate.query(
                "SELECT * FROM plant_production_expense WHERE production_id = ? ORDER BY production_expense_id",
                expenseRowMapper, productionId
        );
    }

    private void addLinesToProductions(List<PlantProduction> productions) {
        for (PlantProduction production : productions) {
            production.setInputs(findInputsByProductionId(production.getProductionId()));
            production.setOutputs(findOutputsByProductionId(production.getProductionId()));
            production.setExpenses(findExpensesByProductionId(production.getProductionId()));
        }
    }
}
