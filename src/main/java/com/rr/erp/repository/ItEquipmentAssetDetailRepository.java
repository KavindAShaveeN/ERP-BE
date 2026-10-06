package com.rr.erp.repository;

import com.rr.erp.entity.ItEquipmentAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItEquipmentAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            equipment_type AS "equipmentType",
            make AS "make",
            model AS "model",
            manufacturer_serial_number AS "manufacturerSerialNumber",
            asset_tag AS "assetTag",
            device_name_hostname AS "deviceNameHostname",
            year_of_manufacture AS "yearOfManufacture",
            colour AS "colour",
            processor_main_specification AS "processorMainSpecification",
            ram_capacity AS "ramCapacity",
            storage_capacity AS "storageCapacity",
            operating_system AS "operatingSystem",
            screen_size AS "screenSize",
            network_mac_address AS "networkMacAddress",
            ip_address AS "ipAddress",
            power_rating_capacity AS "powerRatingCapacity",
            voltage AS "voltage",
            included_accessories AS "includedAccessories",
            assigned_department AS "assignedDepartment",
            assigned_project_location AS "assignedProjectLocation",
            purchase_date AS "purchaseDate",
            warranty_expiry_date AS "warrantyExpiryDate",
            supplier AS "supplier",
            purchase_value AS "purchaseValue"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItEquipmentAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(ItEquipmentAssetDetail detail) {

        String sql = """
                INSERT INTO it_equipment_asset_detail (
                    asset_code, equipment_type, make, model, manufacturer_serial_number, asset_tag,
                    device_name_hostname, year_of_manufacture, colour, processor_main_specification,
                    ram_capacity, storage_capacity, operating_system, screen_size, network_mac_address,
                    ip_address, power_rating_capacity, voltage, included_accessories, assigned_department,
                    assigned_project_location, purchase_date, warranty_expiry_date, supplier, purchase_value
                )
                VALUES (
                    :assetCode, :equipmentType, :make, :model, :manufacturerSerialNumber, :assetTag,
                    :deviceNameHostname, :yearOfManufacture, :colour, :processorMainSpecification,
                    :ramCapacity, :storageCapacity, :operatingSystem, :screenSize, :networkMacAddress,
                    :ipAddress, :powerRatingCapacity, :voltage, :includedAccessories, :assignedDepartment,
                    :assignedProjectLocation, :purchaseDate, :warrantyExpiryDate, :supplier, :purchaseValue
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(ItEquipmentAssetDetail detail) {

        String sql = """
                UPDATE it_equipment_asset_detail
                SET
                    equipment_type = :equipmentType,
                    make = :make,
                    model = :model,
                    manufacturer_serial_number = :manufacturerSerialNumber,
                    asset_tag = :assetTag,
                    device_name_hostname = :deviceNameHostname,
                    year_of_manufacture = :yearOfManufacture,
                    colour = :colour,
                    processor_main_specification = :processorMainSpecification,
                    ram_capacity = :ramCapacity,
                    storage_capacity = :storageCapacity,
                    operating_system = :operatingSystem,
                    screen_size = :screenSize,
                    network_mac_address = :networkMacAddress,
                    ip_address = :ipAddress,
                    power_rating_capacity = :powerRatingCapacity,
                    voltage = :voltage,
                    included_accessories = :includedAccessories,
                    assigned_department = :assignedDepartment,
                    assigned_project_location = :assignedProjectLocation,
                    purchase_date = :purchaseDate,
                    warranty_expiry_date = :warrantyExpiryDate,
                    supplier = :supplier,
                    purchase_value = :purchaseValue
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<ItEquipmentAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM it_equipment_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(ItEquipmentAssetDetail.class)
        );
    }

    public Optional<ItEquipmentAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM it_equipment_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<ItEquipmentAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(ItEquipmentAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(ItEquipmentAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("equipmentType", detail.getEquipmentType())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("manufacturerSerialNumber", detail.getManufacturerSerialNumber())
                .addValue("assetTag", detail.getAssetTag())
                .addValue("deviceNameHostname", detail.getDeviceNameHostname())
                .addValue("yearOfManufacture", detail.getYearOfManufacture())
                .addValue("colour", detail.getColour())
                .addValue("processorMainSpecification", detail.getProcessorMainSpecification())
                .addValue("ramCapacity", detail.getRamCapacity())
                .addValue("storageCapacity", detail.getStorageCapacity())
                .addValue("operatingSystem", detail.getOperatingSystem())
                .addValue("screenSize", detail.getScreenSize())
                .addValue("networkMacAddress", detail.getNetworkMacAddress())
                .addValue("ipAddress", detail.getIpAddress())
                .addValue("powerRatingCapacity", detail.getPowerRatingCapacity())
                .addValue("voltage", detail.getVoltage())
                .addValue("includedAccessories", detail.getIncludedAccessories())
                .addValue("assignedDepartment", detail.getAssignedDepartment())
                .addValue("assignedProjectLocation", detail.getAssignedProjectLocation())
                .addValue("purchaseDate", detail.getPurchaseDate())
                .addValue("warrantyExpiryDate", detail.getWarrantyExpiryDate())
                .addValue("supplier", detail.getSupplier())
                .addValue("purchaseValue", detail.getPurchaseValue());
    }
}
