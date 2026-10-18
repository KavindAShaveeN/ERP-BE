package com.rr.erp.repository;

import com.rr.erp.entity.CashExpenseCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CashExpenseCategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_SQL = """
            SELECT
                category_id,
                category_code,
                category_name,
                is_active
            FROM cash_expense_category
            """;

    private static final RowMapper<CashExpenseCategory> ROW_MAPPER = (rs, rowNum) -> {

        CashExpenseCategory category = new CashExpenseCategory();

        category.setCategoryId(
                rs.getObject("category_id", UUID.class)
        );
        category.setCategoryCode(
                rs.getString("category_code")
        );
        category.setCategoryName(
                rs.getString("category_name")
        );
        category.setIsActive(
                rs.getBoolean("is_active")
        );

        return category;
    };


    // =========================================================
    // GET ALL (optional filter)
    // =========================================================

    public List<CashExpenseCategory> getAllCategories(Boolean isActive) {

        StringBuilder sql = new StringBuilder(SELECT_SQL)
                .append(" WHERE 1 = 1");

        List<Object> params = new ArrayList<>();

        if (isActive != null) {
            sql.append(" AND is_active = ?");
            params.add(isActive);
        }

        sql.append(" ORDER BY category_code");

        return jdbcTemplate.query(
                sql.toString(),
                ROW_MAPPER,
                params.toArray()
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<CashExpenseCategory> getCategoryById(UUID categoryId) {

        String sql = SELECT_SQL + " WHERE category_id = ?";

        return jdbcTemplate.query(
                sql,
                ROW_MAPPER,
                categoryId
        ).stream().findFirst();
    }


    // =========================================================
    // CREATE
    // =========================================================

    public UUID createCategory(CashExpenseCategory category) {

        String sql = """
                INSERT INTO cash_expense_category (
                    category_code,
                    category_name,
                    is_active
                )
                VALUES (?, ?, ?)
                RETURNING category_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                category.getCategoryCode(),
                category.getCategoryName(),
                category.getIsActive()
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public int updateCategory(
            UUID categoryId,
            CashExpenseCategory category) {

        String sql = """
                UPDATE cash_expense_category
                SET
                    category_code = ?,
                    category_name = ?,
                    is_active = ?
                WHERE category_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                category.getCategoryCode(),
                category.getCategoryName(),
                category.getIsActive(),
                categoryId
        );
    }


    // =========================================================
    // CODE EXISTS (case-insensitive, optionally excluding one row)
    // =========================================================

    public boolean categoryCodeExists(String categoryCode, UUID excludeId) {

        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1 FROM cash_expense_category
                    WHERE UPPER(category_code) = UPPER(?)
                      AND (?::uuid IS NULL OR category_id <> ?::uuid)
                )
                """,
                Boolean.class,
                categoryCode,
                excludeId,
                excludeId
        );

        return Boolean.TRUE.equals(exists);
    }
}
