package com.rr.erp.repository;

import com.rr.erp.entity.MachineryAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MachineryAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            machinery_type AS "machineryType",
            make AS "make",
            model AS "model",
            country_of_manufacture AS "countryOfManufacture",
            year_of_manufacture AS "yearOfManufacture",
            manufacturer_serial_number AS "manufacturerSerialNumber",
            chassis_frame_number AS "chassisFrameNumber",
            registration_number AS "registrationNumber",
            colour AS "colour",
            engine_number AS "engineNumber",
            engine_make AS "engineMake",
            engine_model AS "engineModel",
            fuel_type AS "fuelType",
            engine_capacity_cc AS "engineCapacityCc",
            number_of_cylinders AS "numberOfCylinders",
            engine_power_hp AS "enginePowerHp",
            engine_power_kw AS "enginePowerKw",
            rated_rpm AS "ratedRpm",
            fuel_tank_capacity_l AS "fuelTankCapacityL",
            average_fuel_consumption AS "averageFuelConsumption",
            fuel_consumption_unit AS "fuelConsumptionUnit",
            meter_type AS "meterType",
            initial_meter_reading AS "initialMeterReading",
            operating_capacity AS "operatingCapacity",
            operating_capacity_unit AS "operatingCapacityUnit",
            machine_weight_kg AS "machineWeightKg",
            load_lift_capacity AS "loadLiftCapacity",
            bucket_blade_capacity AS "bucketBladeCapacity",
            running_system AS "runningSystem",
            number_of_tyres AS "numberOfTyres",
            front_tyre_size AS "frontTyreSize",
            rear_tyre_size AS "rearTyreSize",
            track_size_width AS "trackSizeWidth",
            tyre_track_brand AS "tyreTrackBrand",
            battery_brand AS "batteryBrand",
            battery_model AS "batteryModel",
            battery_voltage AS "batteryVoltage",
            battery_capacity_ah AS "batteryCapacityAh",
            number_of_batteries AS "numberOfBatteries"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MachineryAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(MachineryAssetDetail detail) {

        String sql = """
                INSERT INTO machinery_asset_detail (
                    asset_code, machinery_type, make, model, country_of_manufacture, year_of_manufacture,
                    manufacturer_serial_number, chassis_frame_number, registration_number, colour,
                    engine_number, engine_make, engine_model, fuel_type, engine_capacity_cc,
                    number_of_cylinders, engine_power_hp, engine_power_kw, rated_rpm, fuel_tank_capacity_l,
                    average_fuel_consumption, fuel_consumption_unit, meter_type, initial_meter_reading,
                    operating_capacity, operating_capacity_unit, machine_weight_kg, load_lift_capacity,
                    bucket_blade_capacity, running_system, number_of_tyres, front_tyre_size, rear_tyre_size,
                    track_size_width, tyre_track_brand, battery_brand, battery_model, battery_voltage,
                    battery_capacity_ah, number_of_batteries
                )
                VALUES (
                    :assetCode, :machineryType, :make, :model, :countryOfManufacture, :yearOfManufacture,
                    :manufacturerSerialNumber, :chassisFrameNumber, :registrationNumber, :colour,
                    :engineNumber, :engineMake, :engineModel, :fuelType, :engineCapacityCc,
                    :numberOfCylinders, :enginePowerHp, :enginePowerKw, :ratedRpm, :fuelTankCapacityL,
                    :averageFuelConsumption, :fuelConsumptionUnit, :meterType, :initialMeterReading,
                    :operatingCapacity, :operatingCapacityUnit, :machineWeightKg, :loadLiftCapacity,
                    :bucketBladeCapacity, :runningSystem, :numberOfTyres, :frontTyreSize, :rearTyreSize,
                    :trackSizeWidth, :tyreTrackBrand, :batteryBrand, :batteryModel, :batteryVoltage,
                    :batteryCapacityAh, :numberOfBatteries
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(MachineryAssetDetail detail) {

        String sql = """
                UPDATE machinery_asset_detail
                SET
                    machinery_type = :machineryType,
                    make = :make,
                    model = :model,
                    country_of_manufacture = :countryOfManufacture,
                    year_of_manufacture = :yearOfManufacture,
                    manufacturer_serial_number = :manufacturerSerialNumber,
                    chassis_frame_number = :chassisFrameNumber,
                    registration_number = :registrationNumber,
                    colour = :colour,
                    engine_number = :engineNumber,
                    engine_make = :engineMake,
                    engine_model = :engineModel,
                    fuel_type = :fuelType,
                    engine_capacity_cc = :engineCapacityCc,
                    number_of_cylinders = :numberOfCylinders,
                    engine_power_hp = :enginePowerHp,
                    engine_power_kw = :enginePowerKw,
                    rated_rpm = :ratedRpm,
                    fuel_tank_capacity_l = :fuelTankCapacityL,
                    average_fuel_consumption = :averageFuelConsumption,
                    fuel_consumption_unit = :fuelConsumptionUnit,
                    meter_type = :meterType,
                    initial_meter_reading = :initialMeterReading,
                    operating_capacity = :operatingCapacity,
                    operating_capacity_unit = :operatingCapacityUnit,
                    machine_weight_kg = :machineWeightKg,
                    load_lift_capacity = :loadLiftCapacity,
                    bucket_blade_capacity = :bucketBladeCapacity,
                    running_system = :runningSystem,
                    number_of_tyres = :numberOfTyres,
                    front_tyre_size = :frontTyreSize,
                    rear_tyre_size = :rearTyreSize,
                    track_size_width = :trackSizeWidth,
                    tyre_track_brand = :tyreTrackBrand,
                    battery_brand = :batteryBrand,
                    battery_model = :batteryModel,
                    battery_voltage = :batteryVoltage,
                    battery_capacity_ah = :batteryCapacityAh,
                    number_of_batteries = :numberOfBatteries
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<MachineryAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM machinery_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(MachineryAssetDetail.class)
        );
    }

    public Optional<MachineryAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM machinery_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<MachineryAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(MachineryAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(MachineryAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("machineryType", detail.getMachineryType())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("countryOfManufacture", detail.getCountryOfManufacture())
                .addValue("yearOfManufacture", detail.getYearOfManufacture())
                .addValue("manufacturerSerialNumber", detail.getManufacturerSerialNumber())
                .addValue("chassisFrameNumber", detail.getChassisFrameNumber())
                .addValue("registrationNumber", detail.getRegistrationNumber())
                .addValue("colour", detail.getColour())
                .addValue("engineNumber", detail.getEngineNumber())
                .addValue("engineMake", detail.getEngineMake())
                .addValue("engineModel", detail.getEngineModel())
                .addValue("fuelType", detail.getFuelType())
                .addValue("engineCapacityCc", detail.getEngineCapacityCc())
                .addValue("numberOfCylinders", detail.getNumberOfCylinders())
                .addValue("enginePowerHp", detail.getEnginePowerHp())
                .addValue("enginePowerKw", detail.getEnginePowerKw())
                .addValue("ratedRpm", detail.getRatedRpm())
                .addValue("fuelTankCapacityL", detail.getFuelTankCapacityL())
                .addValue("averageFuelConsumption", detail.getAverageFuelConsumption())
                .addValue("fuelConsumptionUnit", detail.getFuelConsumptionUnit())
                .addValue("meterType", detail.getMeterType())
                .addValue("initialMeterReading", detail.getInitialMeterReading())
                .addValue("operatingCapacity", detail.getOperatingCapacity())
                .addValue("operatingCapacityUnit", detail.getOperatingCapacityUnit())
                .addValue("machineWeightKg", detail.getMachineWeightKg())
                .addValue("loadLiftCapacity", detail.getLoadLiftCapacity())
                .addValue("bucketBladeCapacity", detail.getBucketBladeCapacity())
                .addValue("runningSystem", detail.getRunningSystem())
                .addValue("numberOfTyres", detail.getNumberOfTyres())
                .addValue("frontTyreSize", detail.getFrontTyreSize())
                .addValue("rearTyreSize", detail.getRearTyreSize())
                .addValue("trackSizeWidth", detail.getTrackSizeWidth())
                .addValue("tyreTrackBrand", detail.getTyreTrackBrand())
                .addValue("batteryBrand", detail.getBatteryBrand())
                .addValue("batteryModel", detail.getBatteryModel())
                .addValue("batteryVoltage", detail.getBatteryVoltage())
                .addValue("batteryCapacityAh", detail.getBatteryCapacityAh())
                .addValue("numberOfBatteries", detail.getNumberOfBatteries());
    }
}
