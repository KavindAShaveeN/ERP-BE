package com.rr.erp.repository;

import com.rr.erp.entity.ElectricalEquipmentAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ElectricalEquipmentAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            equipment_type AS "equipmentType",
            make AS "make",
            model AS "model",
            manufacturer_serial_number AS "manufacturerSerialNumber",
            country_of_manufacture AS "countryOfManufacture",
            year_of_manufacture AS "yearOfManufacture",
            installation_type AS "installationType",
            rated_power AS "ratedPower",
            horsepower AS "horsepower",
            rated_voltage AS "ratedVoltage",
            rated_current AS "ratedCurrent",
            frequency AS "frequency",
            number_of_phases AS "numberOfPhases",
            speed_rpm AS "speedRpm",
            power_factor AS "powerFactor",
            efficiency_class AS "efficiencyClass",
            insulation_class AS "insulationClass",
            protection_rating AS "protectionRating",
            connection_type AS "connectionType",
            duty_type AS "dutyType",
            equipment_capacity AS "equipmentCapacity",
            capacity_unit AS "capacityUnit",
            input_rating AS "inputRating",
            output_rating AS "outputRating",
            pressure_rating AS "pressureRating",
            flow_rate AS "flowRate",
            cooling_method AS "coolingMethod",
            transformer_rating_kva AS "transformerRatingKva",
            generator_rating_kva AS "generatorRatingKva",
            pump_head_m AS "pumpHeadM",
            installation_date AS "installationDate",
            installation_location AS "installationLocation",
            panel_circuit_reference AS "panelCircuitReference",
            connected_load AS "connectedLoad",
            responsible_department AS "responsibleDepartment"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ElectricalEquipmentAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(ElectricalEquipmentAssetDetail detail) {

        String sql = """
                INSERT INTO electrical_equipment_asset_detail (
                    asset_code, equipment_type, make, model, manufacturer_serial_number,
                    country_of_manufacture, year_of_manufacture, installation_type, rated_power, horsepower,
                    rated_voltage, rated_current, frequency, number_of_phases, speed_rpm, power_factor,
                    efficiency_class, insulation_class, protection_rating, connection_type, duty_type,
                    equipment_capacity, capacity_unit, input_rating, output_rating, pressure_rating, flow_rate,
                    cooling_method, transformer_rating_kva, generator_rating_kva, pump_head_m,
                    installation_date, installation_location, panel_circuit_reference, connected_load,
                    responsible_department
                )
                VALUES (
                    :assetCode, :equipmentType, :make, :model, :manufacturerSerialNumber,
                    :countryOfManufacture, :yearOfManufacture, :installationType, :ratedPower, :horsepower,
                    :ratedVoltage, :ratedCurrent, :frequency, :numberOfPhases, :speedRpm, :powerFactor,
                    :efficiencyClass, :insulationClass, :protectionRating, :connectionType, :dutyType,
                    :equipmentCapacity, :capacityUnit, :inputRating, :outputRating, :pressureRating, :flowRate,
                    :coolingMethod, :transformerRatingKva, :generatorRatingKva, :pumpHeadM,
                    :installationDate, :installationLocation, :panelCircuitReference, :connectedLoad,
                    :responsibleDepartment
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(ElectricalEquipmentAssetDetail detail) {

        String sql = """
                UPDATE electrical_equipment_asset_detail
                SET
                    equipment_type = :equipmentType,
                    make = :make,
                    model = :model,
                    manufacturer_serial_number = :manufacturerSerialNumber,
                    country_of_manufacture = :countryOfManufacture,
                    year_of_manufacture = :yearOfManufacture,
                    installation_type = :installationType,
                    rated_power = :ratedPower,
                    horsepower = :horsepower,
                    rated_voltage = :ratedVoltage,
                    rated_current = :ratedCurrent,
                    frequency = :frequency,
                    number_of_phases = :numberOfPhases,
                    speed_rpm = :speedRpm,
                    power_factor = :powerFactor,
                    efficiency_class = :efficiencyClass,
                    insulation_class = :insulationClass,
                    protection_rating = :protectionRating,
                    connection_type = :connectionType,
                    duty_type = :dutyType,
                    equipment_capacity = :equipmentCapacity,
                    capacity_unit = :capacityUnit,
                    input_rating = :inputRating,
                    output_rating = :outputRating,
                    pressure_rating = :pressureRating,
                    flow_rate = :flowRate,
                    cooling_method = :coolingMethod,
                    transformer_rating_kva = :transformerRatingKva,
                    generator_rating_kva = :generatorRatingKva,
                    pump_head_m = :pumpHeadM,
                    installation_date = :installationDate,
                    installation_location = :installationLocation,
                    panel_circuit_reference = :panelCircuitReference,
                    connected_load = :connectedLoad,
                    responsible_department = :responsibleDepartment
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<ElectricalEquipmentAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM electrical_equipment_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(ElectricalEquipmentAssetDetail.class)
        );
    }

    public Optional<ElectricalEquipmentAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM electrical_equipment_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<ElectricalEquipmentAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(ElectricalEquipmentAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(ElectricalEquipmentAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("equipmentType", detail.getEquipmentType())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("manufacturerSerialNumber", detail.getManufacturerSerialNumber())
                .addValue("countryOfManufacture", detail.getCountryOfManufacture())
                .addValue("yearOfManufacture", detail.getYearOfManufacture())
                .addValue("installationType", detail.getInstallationType())
                .addValue("ratedPower", detail.getRatedPower())
                .addValue("horsepower", detail.getHorsepower())
                .addValue("ratedVoltage", detail.getRatedVoltage())
                .addValue("ratedCurrent", detail.getRatedCurrent())
                .addValue("frequency", detail.getFrequency())
                .addValue("numberOfPhases", detail.getNumberOfPhases())
                .addValue("speedRpm", detail.getSpeedRpm())
                .addValue("powerFactor", detail.getPowerFactor())
                .addValue("efficiencyClass", detail.getEfficiencyClass())
                .addValue("insulationClass", detail.getInsulationClass())
                .addValue("protectionRating", detail.getProtectionRating())
                .addValue("connectionType", detail.getConnectionType())
                .addValue("dutyType", detail.getDutyType())
                .addValue("equipmentCapacity", detail.getEquipmentCapacity())
                .addValue("capacityUnit", detail.getCapacityUnit())
                .addValue("inputRating", detail.getInputRating())
                .addValue("outputRating", detail.getOutputRating())
                .addValue("pressureRating", detail.getPressureRating())
                .addValue("flowRate", detail.getFlowRate())
                .addValue("coolingMethod", detail.getCoolingMethod())
                .addValue("transformerRatingKva", detail.getTransformerRatingKva())
                .addValue("generatorRatingKva", detail.getGeneratorRatingKva())
                .addValue("pumpHeadM", detail.getPumpHeadM())
                .addValue("installationDate", detail.getInstallationDate())
                .addValue("installationLocation", detail.getInstallationLocation())
                .addValue("panelCircuitReference", detail.getPanelCircuitReference())
                .addValue("connectedLoad", detail.getConnectedLoad())
                .addValue("responsibleDepartment", detail.getResponsibleDepartment());
    }
}
