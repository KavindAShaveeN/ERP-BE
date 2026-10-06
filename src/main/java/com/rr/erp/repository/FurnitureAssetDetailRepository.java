package com.rr.erp.repository;

import com.rr.erp.entity.FurnitureAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FurnitureAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            furniture_type AS "furnitureType",
            make AS "make",
            model AS "model",
            material AS "material",
            colour AS "colour",
            length AS "length",
            width AS "width",
            height AS "height",
            supplier AS "supplier",
            invoice_number AS "invoiceNumber",
            purchase_date AS "purchaseDate",
            warranty_period AS "warrantyPeriod"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public FurnitureAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(FurnitureAssetDetail detail) {

        String sql = """
                INSERT INTO furniture_asset_detail (
                    asset_code, furniture_type, make, model, material, colour, length, width, height,
                    supplier, invoice_number, purchase_date, warranty_period
                )
                VALUES (
                    :assetCode, :furnitureType, :make, :model, :material, :colour, :length, :width, :height,
                    :supplier, :invoiceNumber, :purchaseDate, :warrantyPeriod
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(FurnitureAssetDetail detail) {

        String sql = """
                UPDATE furniture_asset_detail
                SET
                    furniture_type = :furnitureType,
                    make = :make,
                    model = :model,
                    material = :material,
                    colour = :colour,
                    length = :length,
                    width = :width,
                    height = :height,
                    supplier = :supplier,
                    invoice_number = :invoiceNumber,
                    purchase_date = :purchaseDate,
                    warranty_period = :warrantyPeriod
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<FurnitureAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM furniture_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(FurnitureAssetDetail.class)
        );
    }

    public Optional<FurnitureAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM furniture_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<FurnitureAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(FurnitureAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(FurnitureAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("furnitureType", detail.getFurnitureType())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("material", detail.getMaterial())
                .addValue("colour", detail.getColour())
                .addValue("length", detail.getLength())
                .addValue("width", detail.getWidth())
                .addValue("height", detail.getHeight())
                .addValue("supplier", detail.getSupplier())
                .addValue("invoiceNumber", detail.getInvoiceNumber())
                .addValue("purchaseDate", detail.getPurchaseDate())
                .addValue("warrantyPeriod", detail.getWarrantyPeriod());
    }
}
