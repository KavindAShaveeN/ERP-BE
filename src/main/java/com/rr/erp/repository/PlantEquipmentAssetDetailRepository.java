package com.rr.erp.repository;

import com.rr.erp.entity.PlantEquipmentAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlantEquipmentAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            plant_type AS "plantType",
            plant_name AS "plantName",
            plant_code_asset_number AS "plantCodeAssetNumber",
            make AS "make",
            model AS "model",
            manufacturer AS "manufacturer",
            manufacturer_serial_number AS "manufacturerSerialNumber",
            country_of_manufacture AS "countryOfManufacture",
            year_of_manufacture AS "yearOfManufacture",
            plant_configuration AS "plantConfiguration",
            production_capacity AS "productionCapacity",
            capacity_unit AS "capacityUnit",
            main_power_source AS "mainPowerSource",
            fuel_type AS "fuelType",
            assigned_project AS "assignedProject",
            installation_date AS "installationDate",
            commissioning_date AS "commissioningDate",
            purchase_date AS "purchaseDate",
            supplier AS "supplier",
            purchase_value AS "purchaseValue",
            warranty_expiry_date AS "warrantyExpiryDate",
            responsible_employee_department AS "responsibleEmployeeDepartment",
            gps_latitude AS "gpsLatitude",
            gps_longitude AS "gpsLongitude",
            plant_images AS "plantImages",
            supporting_documents AS "supportingDocuments"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PlantEquipmentAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(PlantEquipmentAssetDetail detail) {

        String sql = """
                INSERT INTO plant_equipment_asset_detail (
                    asset_code, plant_type, plant_name, plant_code_asset_number, make, model, manufacturer,
                    manufacturer_serial_number, country_of_manufacture, year_of_manufacture, plant_configuration,
                    production_capacity, capacity_unit, main_power_source, fuel_type, assigned_project,
                    installation_date, commissioning_date, purchase_date, supplier, purchase_value,
                    warranty_expiry_date, responsible_employee_department, gps_latitude, gps_longitude,
                    plant_images, supporting_documents
                )
                VALUES (
                    :assetCode, :plantType, :plantName, :plantCodeAssetNumber, :make, :model, :manufacturer,
                    :manufacturerSerialNumber, :countryOfManufacture, :yearOfManufacture, :plantConfiguration,
                    :productionCapacity, :capacityUnit, :mainPowerSource, :fuelType, :assignedProject,
                    :installationDate, :commissioningDate, :purchaseDate, :supplier, :purchaseValue,
                    :warrantyExpiryDate, :responsibleEmployeeDepartment, :gpsLatitude, :gpsLongitude,
                    :plantImages, :supportingDocuments
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(PlantEquipmentAssetDetail detail) {

        String sql = """
                UPDATE plant_equipment_asset_detail
                SET
                    plant_type = :plantType,
                    plant_name = :plantName,
                    plant_code_asset_number = :plantCodeAssetNumber,
                    make = :make,
                    model = :model,
                    manufacturer = :manufacturer,
                    manufacturer_serial_number = :manufacturerSerialNumber,
                    country_of_manufacture = :countryOfManufacture,
                    year_of_manufacture = :yearOfManufacture,
                    plant_configuration = :plantConfiguration,
                    production_capacity = :productionCapacity,
                    capacity_unit = :capacityUnit,
                    main_power_source = :mainPowerSource,
                    fuel_type = :fuelType,
                    assigned_project = :assignedProject,
                    installation_date = :installationDate,
                    commissioning_date = :commissioningDate,
                    purchase_date = :purchaseDate,
                    supplier = :supplier,
                    purchase_value = :purchaseValue,
                    warranty_expiry_date = :warrantyExpiryDate,
                    responsible_employee_department = :responsibleEmployeeDepartment,
                    gps_latitude = :gpsLatitude,
                    gps_longitude = :gpsLongitude,
                    plant_images = :plantImages,
                    supporting_documents = :supportingDocuments
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<PlantEquipmentAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM plant_equipment_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(PlantEquipmentAssetDetail.class)
        );
    }

    public Optional<PlantEquipmentAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM plant_equipment_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<PlantEquipmentAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(PlantEquipmentAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(PlantEquipmentAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("plantType", detail.getPlantType())
                .addValue("plantName", detail.getPlantName())
                .addValue("plantCodeAssetNumber", detail.getPlantCodeAssetNumber())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("manufacturer", detail.getManufacturer())
                .addValue("manufacturerSerialNumber", detail.getManufacturerSerialNumber())
                .addValue("countryOfManufacture", detail.getCountryOfManufacture())
                .addValue("yearOfManufacture", detail.getYearOfManufacture())
                .addValue("plantConfiguration", detail.getPlantConfiguration())
                .addValue("productionCapacity", detail.getProductionCapacity())
                .addValue("capacityUnit", detail.getCapacityUnit())
                .addValue("mainPowerSource", detail.getMainPowerSource())
                .addValue("fuelType", detail.getFuelType())
                .addValue("assignedProject", detail.getAssignedProject())
                .addValue("installationDate", detail.getInstallationDate())
                .addValue("commissioningDate", detail.getCommissioningDate())
                .addValue("purchaseDate", detail.getPurchaseDate())
                .addValue("supplier", detail.getSupplier())
                .addValue("purchaseValue", detail.getPurchaseValue())
                .addValue("warrantyExpiryDate", detail.getWarrantyExpiryDate())
                .addValue("responsibleEmployeeDepartment", detail.getResponsibleEmployeeDepartment())
                .addValue("gpsLatitude", detail.getGpsLatitude())
                .addValue("gpsLongitude", detail.getGpsLongitude())
                .addValue("plantImages", detail.getPlantImages())
                .addValue("supportingDocuments", detail.getSupportingDocuments());
    }
}
