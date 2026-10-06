package com.rr.erp.repository;

import com.rr.erp.entity.PowerToolAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PowerToolAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            tool_type AS "toolType",
            make AS "make",
            model AS "model",
            manufacturer_serial_number AS "manufacturerSerialNumber",
            country_of_manufacture AS "countryOfManufacture",
            year_of_manufacture AS "yearOfManufacture",
            power_source AS "powerSource",
            intended_use AS "intendedUse",
            rated_power AS "ratedPower",
            voltage AS "voltage",
            current_amps AS "currentAmps",
            frequency AS "frequency",
            speed_rpm AS "speedRpm",
            capacity_tool_size AS "capacityToolSize",
            capacity_unit AS "capacityUnit",
            chuck_disc_blade_size AS "chuckDiscBladeSize",
            weight_kg AS "weightKg",
            battery_type AS "batteryType",
            battery_voltage AS "batteryVoltage",
            battery_capacity_ah AS "batteryCapacityAh",
            number_of_batteries AS "numberOfBatteries",
            charger_model_serial_number AS "chargerModelSerialNumber",
            fuel_type AS "fuelType",
            engine_capacity_cc AS "engineCapacityCc",
            fuel_tank_capacity_l AS "fuelTankCapacityL",
            engine_number AS "engineNumber",
            safety_class_protection_rating AS "safetyClassProtectionRating",
            included_accessories AS "includedAccessories",
            carrying_case_number AS "carryingCaseNumber"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PowerToolAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(PowerToolAssetDetail detail) {

        String sql = """
                INSERT INTO power_tool_asset_detail (
                    asset_code, tool_type, make, model, manufacturer_serial_number, country_of_manufacture,
                    year_of_manufacture, power_source, intended_use, rated_power, voltage, current_amps,
                    frequency, speed_rpm, capacity_tool_size, capacity_unit, chuck_disc_blade_size, weight_kg,
                    battery_type, battery_voltage, battery_capacity_ah, number_of_batteries,
                    charger_model_serial_number, fuel_type, engine_capacity_cc, fuel_tank_capacity_l,
                    engine_number, safety_class_protection_rating, included_accessories, carrying_case_number
                )
                VALUES (
                    :assetCode, :toolType, :make, :model, :manufacturerSerialNumber, :countryOfManufacture,
                    :yearOfManufacture, :powerSource, :intendedUse, :ratedPower, :voltage, :currentAmps,
                    :frequency, :speedRpm, :capacityToolSize, :capacityUnit, :chuckDiscBladeSize, :weightKg,
                    :batteryType, :batteryVoltage, :batteryCapacityAh, :numberOfBatteries,
                    :chargerModelSerialNumber, :fuelType, :engineCapacityCc, :fuelTankCapacityL,
                    :engineNumber, :safetyClassProtectionRating, :includedAccessories, :carryingCaseNumber
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(PowerToolAssetDetail detail) {

        String sql = """
                UPDATE power_tool_asset_detail
                SET
                    tool_type = :toolType,
                    make = :make,
                    model = :model,
                    manufacturer_serial_number = :manufacturerSerialNumber,
                    country_of_manufacture = :countryOfManufacture,
                    year_of_manufacture = :yearOfManufacture,
                    power_source = :powerSource,
                    intended_use = :intendedUse,
                    rated_power = :ratedPower,
                    voltage = :voltage,
                    current_amps = :currentAmps,
                    frequency = :frequency,
                    speed_rpm = :speedRpm,
                    capacity_tool_size = :capacityToolSize,
                    capacity_unit = :capacityUnit,
                    chuck_disc_blade_size = :chuckDiscBladeSize,
                    weight_kg = :weightKg,
                    battery_type = :batteryType,
                    battery_voltage = :batteryVoltage,
                    battery_capacity_ah = :batteryCapacityAh,
                    number_of_batteries = :numberOfBatteries,
                    charger_model_serial_number = :chargerModelSerialNumber,
                    fuel_type = :fuelType,
                    engine_capacity_cc = :engineCapacityCc,
                    fuel_tank_capacity_l = :fuelTankCapacityL,
                    engine_number = :engineNumber,
                    safety_class_protection_rating = :safetyClassProtectionRating,
                    included_accessories = :includedAccessories,
                    carrying_case_number = :carryingCaseNumber
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<PowerToolAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM power_tool_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(PowerToolAssetDetail.class)
        );
    }

    public Optional<PowerToolAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM power_tool_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<PowerToolAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(PowerToolAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(PowerToolAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("toolType", detail.getToolType())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("manufacturerSerialNumber", detail.getManufacturerSerialNumber())
                .addValue("countryOfManufacture", detail.getCountryOfManufacture())
                .addValue("yearOfManufacture", detail.getYearOfManufacture())
                .addValue("powerSource", detail.getPowerSource())
                .addValue("intendedUse", detail.getIntendedUse())
                .addValue("ratedPower", detail.getRatedPower())
                .addValue("voltage", detail.getVoltage())
                .addValue("currentAmps", detail.getCurrentAmps())
                .addValue("frequency", detail.getFrequency())
                .addValue("speedRpm", detail.getSpeedRpm())
                .addValue("capacityToolSize", detail.getCapacityToolSize())
                .addValue("capacityUnit", detail.getCapacityUnit())
                .addValue("chuckDiscBladeSize", detail.getChuckDiscBladeSize())
                .addValue("weightKg", detail.getWeightKg())
                .addValue("batteryType", detail.getBatteryType())
                .addValue("batteryVoltage", detail.getBatteryVoltage())
                .addValue("batteryCapacityAh", detail.getBatteryCapacityAh())
                .addValue("numberOfBatteries", detail.getNumberOfBatteries())
                .addValue("chargerModelSerialNumber", detail.getChargerModelSerialNumber())
                .addValue("fuelType", detail.getFuelType())
                .addValue("engineCapacityCc", detail.getEngineCapacityCc())
                .addValue("fuelTankCapacityL", detail.getFuelTankCapacityL())
                .addValue("engineNumber", detail.getEngineNumber())
                .addValue("safetyClassProtectionRating", detail.getSafetyClassProtectionRating())
                .addValue("includedAccessories", detail.getIncludedAccessories())
                .addValue("carryingCaseNumber", detail.getCarryingCaseNumber());
    }
}
