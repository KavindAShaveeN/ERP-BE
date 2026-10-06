package com.rr.erp.repository;

import com.rr.erp.entity.PlantRecipe;
import com.rr.erp.entity.PlantRecipeInput;
import com.rr.erp.entity.PlantRecipeOutput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PlantRecipeRepository {

    private final JdbcTemplate jdbcTemplate;

    public PlantRecipeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PlantRecipe> recipeRowMapper = (rs, rowNum) -> {
        PlantRecipe recipe = new PlantRecipe();

        recipe.setRecipeId(rs.getObject("recipe_id", UUID.class));
        recipe.setRecipeCode(rs.getString("recipe_code"));
        recipe.setRecipeName(rs.getString("recipe_name"));
        recipe.setProjectCode(rs.getString("project_code"));
        recipe.setProductType(rs.getString("product_type"));
        recipe.setEffectiveDate(rs.getObject("effective_date", LocalDate.class));
        recipe.setIsActive((Boolean) rs.getObject("is_active"));
        recipe.setCreatedBy(rs.getString("created_by"));
        recipe.setCreatedDate(rs.getObject("created_date", LocalDateTime.class));

        return recipe;
    };

    private final RowMapper<PlantRecipeInput> inputRowMapper = (rs, rowNum) -> new PlantRecipeInput(
            rs.getObject("recipe_input_id", UUID.class),
            rs.getObject("recipe_id", UUID.class),
            rs.getString("item_code"),
            rs.getBigDecimal("quantity"),
            rs.getInt("uom_id"),
            rs.getBigDecimal("conversion_factor"),
            rs.getBigDecimal("stock_equivalent_qty")
    );

    private final RowMapper<PlantRecipeOutput> outputRowMapper = (rs, rowNum) -> new PlantRecipeOutput(
            rs.getObject("recipe_output_id", UUID.class),
            rs.getObject("recipe_id", UUID.class),
            rs.getString("item_code"),
            rs.getBigDecimal("quantity"),
            rs.getInt("uom_id"),
            (Boolean) rs.getObject("is_primary"),
            (Boolean) rs.getObject("is_waste"),
            rs.getBigDecimal("conversion_factor"),
            rs.getBigDecimal("stock_equivalent_qty")
    );

    public PlantRecipe insertRecipe(PlantRecipe recipe) {

        UUID recipeId = recipe.getRecipeId() != null ? recipe.getRecipeId() : UUID.randomUUID();
        recipe.setRecipeId(recipeId);

        if (recipe.getCreatedDate() == null) {
            recipe.setCreatedDate(LocalDateTime.now());
        }

        String sql = """
                INSERT INTO plant_recipe (
                    recipe_id, recipe_code, recipe_name, project_code, product_type,
                    effective_date, is_active, created_by, created_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                recipe.getRecipeId(),
                recipe.getRecipeCode(),
                recipe.getRecipeName(),
                recipe.getProjectCode(),
                recipe.getProductType(),
                recipe.getEffectiveDate(),
                recipe.getIsActive(),
                recipe.getCreatedBy(),
                recipe.getCreatedDate()
        );

        insertInputs(recipeId, recipe.getInputs());
        insertOutputs(recipeId, recipe.getOutputs());

        return recipe;
    }

    public void insertInputs(UUID recipeId, List<PlantRecipeInput> inputs) {

        String sql = """
                INSERT INTO plant_recipe_input (
                    recipe_input_id, recipe_id, item_code, quantity, uom_id,
                    conversion_factor, stock_equivalent_qty
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        for (PlantRecipeInput input : inputs) {

            UUID id = UUID.randomUUID();
            input.setRecipeInputId(id);
            input.setRecipeId(recipeId);

            jdbcTemplate.update(
                    sql, id, recipeId, input.getItemCode(), input.getQuantity(), input.getUomId(),
                    input.getConversionFactor(), input.getStockEquivalentQty()
            );
        }
    }

    public void insertOutputs(UUID recipeId, List<PlantRecipeOutput> outputs) {

        String sql = """
                INSERT INTO plant_recipe_output (
                    recipe_output_id, recipe_id, item_code, quantity, uom_id, is_primary, is_waste,
                    conversion_factor, stock_equivalent_qty
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (PlantRecipeOutput output : outputs) {

            UUID id = UUID.randomUUID();
            output.setRecipeOutputId(id);
            output.setRecipeId(recipeId);

            jdbcTemplate.update(
                    sql, id, recipeId, output.getItemCode(), output.getQuantity(),
                    output.getUomId(), output.getIsPrimary(), output.getIsWaste(),
                    output.getConversionFactor(), output.getStockEquivalentQty()
            );
        }
    }

    public void deleteInputsByRecipeId(UUID recipeId) {
        jdbcTemplate.update("DELETE FROM plant_recipe_input WHERE recipe_id = ?", recipeId);
    }

    public void deleteOutputsByRecipeId(UUID recipeId) {
        jdbcTemplate.update("DELETE FROM plant_recipe_output WHERE recipe_id = ?", recipeId);
    }

    public int updateRecipe(UUID recipeId, PlantRecipe recipe) {

        String sql = """
                UPDATE plant_recipe
                SET
                    recipe_code = ?,
                    recipe_name = ?,
                    project_code = ?,
                    product_type = ?,
                    effective_date = ?,
                    is_active = ?
                WHERE recipe_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                recipe.getRecipeCode(),
                recipe.getRecipeName(),
                recipe.getProjectCode(),
                recipe.getProductType(),
                recipe.getEffectiveDate(),
                recipe.getIsActive(),
                recipeId
        );
    }

    public List<PlantRecipe> findAll() {

        List<PlantRecipe> recipes = jdbcTemplate.query(
                "SELECT * FROM plant_recipe ORDER BY recipe_name", recipeRowMapper
        );

        addLinesToRecipes(recipes);

        return recipes;
    }

    public List<PlantRecipe> findByProjectCode(String projectCode) {

        List<PlantRecipe> recipes = jdbcTemplate.query(
                "SELECT * FROM plant_recipe WHERE project_code = ? ORDER BY recipe_name",
                recipeRowMapper, projectCode
        );

        addLinesToRecipes(recipes);

        return recipes;
    }

    public Optional<PlantRecipe> findById(UUID recipeId) {

        List<PlantRecipe> results = jdbcTemplate.query(
                "SELECT * FROM plant_recipe WHERE recipe_id = ?", recipeRowMapper, recipeId
        );

        if (results.isEmpty()) {
            return Optional.empty();
        }

        PlantRecipe recipe = results.get(0);
        recipe.setInputs(findInputsByRecipeId(recipeId));
        recipe.setOutputs(findOutputsByRecipeId(recipeId));

        return Optional.of(recipe);
    }

    public List<PlantRecipeInput> findInputsByRecipeId(UUID recipeId) {
        return jdbcTemplate.query(
                "SELECT * FROM plant_recipe_input WHERE recipe_id = ? ORDER BY recipe_input_id",
                inputRowMapper, recipeId
        );
    }

    public List<PlantRecipeOutput> findOutputsByRecipeId(UUID recipeId) {
        return jdbcTemplate.query(
                "SELECT * FROM plant_recipe_output WHERE recipe_id = ? ORDER BY recipe_output_id",
                outputRowMapper, recipeId
        );
    }

    private void addLinesToRecipes(List<PlantRecipe> recipes) {
        for (PlantRecipe recipe : recipes) {
            recipe.setInputs(findInputsByRecipeId(recipe.getRecipeId()));
            recipe.setOutputs(findOutputsByRecipeId(recipe.getRecipeId()));
        }
    }
}
