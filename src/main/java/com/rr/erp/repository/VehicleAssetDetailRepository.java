package com.rr.erp.repository;

import com.rr.erp.entity.VehicleAssetDetail;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VehicleAssetDetailRepository {

    private static final String SELECT_COLUMNS = """
            asset_code AS "assetCode",
            vehicle_class AS "vehicleClass",
            registration_number AS "registrationNumber",
            year_of_manufacture AS "yearOfManufacture",
            colour AS "colour",
            weight_kg AS "weightKg",
            make AS "make",
            model AS "model",
            country_of_manufacture AS "countryOfManufacture",
            manufacturer_serial_number AS "manufacturerSerialNumber",
            chassis_number AS "chassisNumber",
            body_type AS "bodyType",
            seating_loading_capacity AS "seatingLoadingCapacity",
            engine_number AS "engineNumber",
            engine_make AS "engineMake",
            engine_model AS "engineModel",
            engine_capacity_cc AS "engineCapacityCc",
            fuel_type AS "fuelType",
            number_of_cylinders AS "numberOfCylinders",
            fuel_tank_capacity_l AS "fuelTankCapacityL",
            average_fuel_consumption AS "averageFuelConsumption",
            fuel_consumption_unit AS "fuelConsumptionUnit",
            horsepower AS "horsepower",
            power_kw AS "powerKw",
            voltage AS "voltage",
            current_amps AS "currentAmps",
            rpm AS "rpm",
            number_of_tyres AS "numberOfTyres",
            front_tyre_size AS "frontTyreSize",
            rear_tyre_size AS "rearTyreSize",
            spare_tyre_size AS "spareTyreSize",
            tyre_make_brand AS "tyreMakeBrand",
            battery_make_brand AS "batteryMakeBrand",
            battery_model AS "batteryModel",
            battery_serial_number AS "batterySerialNumber",
            battery_voltage AS "batteryVoltage",
            battery_capacity_ah AS "batteryCapacityAh",
            number_of_batteries AS "numberOfBatteries",
            supplier AS "supplier",
            purchase_order_number AS "purchaseOrderNumber",
            invoice_receipt_number AS "invoiceReceiptNumber",
            purchase_date AS "purchaseDate",
            purchase_value AS "purchaseValue",
            currency AS "currency",
            received_date AS "receivedDate",
            warranty_expiry_date AS "warrantyExpiryDate",
            registration_date AS "registrationDate",
            registration_expiry_date AS "registrationExpiryDate",
            registration_document AS "registrationDocument",
            insurance_company AS "insuranceCompany",
            insurance_policy_number AS "insurancePolicyNumber",
            insurance_start_date AS "insuranceStartDate",
            insurance_expiry_date AS "insuranceExpiryDate",
            revenue_licence_number AS "revenueLicenceNumber",
            revenue_licence_start_date AS "revenueLicenceStartDate",
            revenue_licence_expiry_date AS "revenueLicenceExpiryDate",
            emission_test_start_date AS "emissionTestStartDate",
            emission_test_expiry_date AS "emissionTestExpiryDate",
            insurance_licence_documents AS "insuranceLicenceDocuments",
            assigned_date AS "assignedDate",
            owning_department AS "owningDepartment",
            cost_centre_account_code AS "costCentreAccountCode",
            account_description AS "accountDescription",
            vehicle_photograph AS "vehiclePhotograph",
            registration_certificate AS "registrationCertificate",
            insurance_document AS "insuranceDocument",
            revenue_licence_document AS "revenueLicenceDocument",
            purchase_invoice_document AS "purchaseInvoiceDocument",
            warranty_document AS "warrantyDocument",
            other_supporting_documents AS "otherSupportingDocuments"
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VehicleAssetDetailRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Every insurance / revenue licence / emission test expiry date recorded on a vehicle. */
    public List<com.rr.erp.dto.AssetExpiryEvent> findExpiryEvents() {
        String sql = """
                SELECT v.asset_code, a.description, v.registration_number, x.type, x.expiry_date
                FROM vehicle_asset_detail v
                JOIN asset a ON a.asset_code = v.asset_code
                CROSS JOIN LATERAL (VALUES
                    ('INSURANCE', v.insurance_expiry_date),
                    ('REVENUE_LICENCE', v.revenue_licence_expiry_date),
                    ('EMISSION_TEST', v.emission_test_expiry_date)
                ) AS x(type, expiry_date)
                WHERE x.expiry_date IS NOT NULL
                ORDER BY x.expiry_date, v.asset_code
                """;
        return jdbcTemplate.query(sql, (rs, i) -> new com.rr.erp.dto.AssetExpiryEvent(
                rs.getString("asset_code"),
                rs.getString("description"),
                rs.getString("registration_number"),
                rs.getString("type"),
                rs.getObject("expiry_date", java.time.LocalDate.class)));
    }

    public void insert(VehicleAssetDetail detail) {

        String sql = """
                INSERT INTO vehicle_asset_detail (
                    asset_code, vehicle_class, registration_number, year_of_manufacture, colour, weight_kg,
                    make, model, country_of_manufacture, manufacturer_serial_number, chassis_number, body_type,
                    seating_loading_capacity, engine_number, engine_make, engine_model, engine_capacity_cc,
                    fuel_type, number_of_cylinders, fuel_tank_capacity_l, average_fuel_consumption,
                    fuel_consumption_unit, horsepower, power_kw, voltage, current_amps, rpm, number_of_tyres,
                    front_tyre_size, rear_tyre_size, spare_tyre_size, tyre_make_brand, battery_make_brand,
                    battery_model, battery_serial_number, battery_voltage, battery_capacity_ah,
                    number_of_batteries, supplier, purchase_order_number, invoice_receipt_number, purchase_date,
                    purchase_value, currency, received_date, warranty_expiry_date, registration_date,
                    registration_expiry_date, registration_document, insurance_company, insurance_policy_number,
                    insurance_start_date, insurance_expiry_date, revenue_licence_number,
                    revenue_licence_start_date, revenue_licence_expiry_date, emission_test_start_date, emission_test_expiry_date, insurance_licence_documents,
                    assigned_date, owning_department, cost_centre_account_code, account_description,
                    vehicle_photograph, registration_certificate, insurance_document, revenue_licence_document,
                    purchase_invoice_document, warranty_document, other_supporting_documents
                )
                VALUES (
                    :assetCode, :vehicleClass, :registrationNumber, :yearOfManufacture, :colour, :weightKg,
                    :make, :model, :countryOfManufacture, :manufacturerSerialNumber, :chassisNumber, :bodyType,
                    :seatingLoadingCapacity, :engineNumber, :engineMake, :engineModel, :engineCapacityCc,
                    :fuelType, :numberOfCylinders, :fuelTankCapacityL, :averageFuelConsumption,
                    :fuelConsumptionUnit, :horsepower, :powerKw, :voltage, :currentAmps, :rpm, :numberOfTyres,
                    :frontTyreSize, :rearTyreSize, :spareTyreSize, :tyreMakeBrand, :batteryMakeBrand,
                    :batteryModel, :batterySerialNumber, :batteryVoltage, :batteryCapacityAh,
                    :numberOfBatteries, :supplier, :purchaseOrderNumber, :invoiceReceiptNumber, :purchaseDate,
                    :purchaseValue, :currency, :receivedDate, :warrantyExpiryDate, :registrationDate,
                    :registrationExpiryDate, :registrationDocument, :insuranceCompany, :insurancePolicyNumber,
                    :insuranceStartDate, :insuranceExpiryDate, :revenueLicenceNumber,
                    :revenueLicenceStartDate, :revenueLicenceExpiryDate, :emissionTestStartDate, :emissionTestExpiryDate, :insuranceLicenceDocuments,
                    :assignedDate, :owningDepartment, :costCentreAccountCode, :accountDescription,
                    :vehiclePhotograph, :registrationCertificate, :insuranceDocument, :revenueLicenceDocument,
                    :purchaseInvoiceDocument, :warrantyDocument, :otherSupportingDocuments
                )
                """;

        jdbcTemplate.update(sql, toParameters(detail));
    }

    public int update(VehicleAssetDetail detail) {

        String sql = """
                UPDATE vehicle_asset_detail
                SET
                    vehicle_class = :vehicleClass,
                    registration_number = :registrationNumber,
                    year_of_manufacture = :yearOfManufacture,
                    colour = :colour,
                    weight_kg = :weightKg,
                    make = :make,
                    model = :model,
                    country_of_manufacture = :countryOfManufacture,
                    manufacturer_serial_number = :manufacturerSerialNumber,
                    chassis_number = :chassisNumber,
                    body_type = :bodyType,
                    seating_loading_capacity = :seatingLoadingCapacity,
                    engine_number = :engineNumber,
                    engine_make = :engineMake,
                    engine_model = :engineModel,
                    engine_capacity_cc = :engineCapacityCc,
                    fuel_type = :fuelType,
                    number_of_cylinders = :numberOfCylinders,
                    fuel_tank_capacity_l = :fuelTankCapacityL,
                    average_fuel_consumption = :averageFuelConsumption,
                    fuel_consumption_unit = :fuelConsumptionUnit,
                    horsepower = :horsepower,
                    power_kw = :powerKw,
                    voltage = :voltage,
                    current_amps = :currentAmps,
                    rpm = :rpm,
                    number_of_tyres = :numberOfTyres,
                    front_tyre_size = :frontTyreSize,
                    rear_tyre_size = :rearTyreSize,
                    spare_tyre_size = :spareTyreSize,
                    tyre_make_brand = :tyreMakeBrand,
                    battery_make_brand = :batteryMakeBrand,
                    battery_model = :batteryModel,
                    battery_serial_number = :batterySerialNumber,
                    battery_voltage = :batteryVoltage,
                    battery_capacity_ah = :batteryCapacityAh,
                    number_of_batteries = :numberOfBatteries,
                    supplier = :supplier,
                    purchase_order_number = :purchaseOrderNumber,
                    invoice_receipt_number = :invoiceReceiptNumber,
                    purchase_date = :purchaseDate,
                    purchase_value = :purchaseValue,
                    currency = :currency,
                    received_date = :receivedDate,
                    warranty_expiry_date = :warrantyExpiryDate,
                    registration_date = :registrationDate,
                    registration_expiry_date = :registrationExpiryDate,
                    registration_document = :registrationDocument,
                    insurance_company = :insuranceCompany,
                    insurance_policy_number = :insurancePolicyNumber,
                    insurance_start_date = :insuranceStartDate,
                    insurance_expiry_date = :insuranceExpiryDate,
                    revenue_licence_number = :revenueLicenceNumber,
                    revenue_licence_start_date = :revenueLicenceStartDate,
                    revenue_licence_expiry_date = :revenueLicenceExpiryDate,
                    emission_test_start_date = :emissionTestStartDate,
                    emission_test_expiry_date = :emissionTestExpiryDate,
                    insurance_licence_documents = :insuranceLicenceDocuments,
                    assigned_date = :assignedDate,
                    owning_department = :owningDepartment,
                    cost_centre_account_code = :costCentreAccountCode,
                    account_description = :accountDescription,
                    vehicle_photograph = :vehiclePhotograph,
                    registration_certificate = :registrationCertificate,
                    insurance_document = :insuranceDocument,
                    revenue_licence_document = :revenueLicenceDocument,
                    purchase_invoice_document = :purchaseInvoiceDocument,
                    warranty_document = :warrantyDocument,
                    other_supporting_documents = :otherSupportingDocuments
                WHERE asset_code = :assetCode
                """;

        return jdbcTemplate.update(sql, toParameters(detail));
    }

    public List<VehicleAssetDetail> findAll() {
        return jdbcTemplate.query(
                "SELECT " + SELECT_COLUMNS + " FROM vehicle_asset_detail",
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(VehicleAssetDetail.class)
        );
    }

    public Optional<VehicleAssetDetail> findByAssetCode(String assetCode) {

        String sql = "SELECT " + SELECT_COLUMNS + " FROM vehicle_asset_detail WHERE asset_code = :assetCode";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("assetCode", assetCode);

        List<VehicleAssetDetail> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(VehicleAssetDetail.class)
        );

        return results.stream().findFirst();
    }

    private MapSqlParameterSource toParameters(VehicleAssetDetail detail) {
        return new MapSqlParameterSource()
                .addValue("assetCode", detail.getAssetCode())
                .addValue("vehicleClass", detail.getVehicleClass())
                .addValue("registrationNumber", detail.getRegistrationNumber())
                .addValue("yearOfManufacture", detail.getYearOfManufacture())
                .addValue("colour", detail.getColour())
                .addValue("weightKg", detail.getWeightKg())
                .addValue("make", detail.getMake())
                .addValue("model", detail.getModel())
                .addValue("countryOfManufacture", detail.getCountryOfManufacture())
                .addValue("manufacturerSerialNumber", detail.getManufacturerSerialNumber())
                .addValue("chassisNumber", detail.getChassisNumber())
                .addValue("bodyType", detail.getBodyType())
                .addValue("seatingLoadingCapacity", detail.getSeatingLoadingCapacity())
                .addValue("engineNumber", detail.getEngineNumber())
                .addValue("engineMake", detail.getEngineMake())
                .addValue("engineModel", detail.getEngineModel())
                .addValue("engineCapacityCc", detail.getEngineCapacityCc())
                .addValue("fuelType", detail.getFuelType())
                .addValue("numberOfCylinders", detail.getNumberOfCylinders())
                .addValue("fuelTankCapacityL", detail.getFuelTankCapacityL())
                .addValue("averageFuelConsumption", detail.getAverageFuelConsumption())
                .addValue("fuelConsumptionUnit", detail.getFuelConsumptionUnit())
                .addValue("horsepower", detail.getHorsepower())
                .addValue("powerKw", detail.getPowerKw())
                .addValue("voltage", detail.getVoltage())
                .addValue("currentAmps", detail.getCurrentAmps())
                .addValue("rpm", detail.getRpm())
                .addValue("numberOfTyres", detail.getNumberOfTyres())
                .addValue("frontTyreSize", detail.getFrontTyreSize())
                .addValue("rearTyreSize", detail.getRearTyreSize())
                .addValue("spareTyreSize", detail.getSpareTyreSize())
                .addValue("tyreMakeBrand", detail.getTyreMakeBrand())
                .addValue("batteryMakeBrand", detail.getBatteryMakeBrand())
                .addValue("batteryModel", detail.getBatteryModel())
                .addValue("batterySerialNumber", detail.getBatterySerialNumber())
                .addValue("batteryVoltage", detail.getBatteryVoltage())
                .addValue("batteryCapacityAh", detail.getBatteryCapacityAh())
                .addValue("numberOfBatteries", detail.getNumberOfBatteries())
                .addValue("supplier", detail.getSupplier())
                .addValue("purchaseOrderNumber", detail.getPurchaseOrderNumber())
                .addValue("invoiceReceiptNumber", detail.getInvoiceReceiptNumber())
                .addValue("purchaseDate", detail.getPurchaseDate())
                .addValue("purchaseValue", detail.getPurchaseValue())
                .addValue("currency", detail.getCurrency())
                .addValue("receivedDate", detail.getReceivedDate())
                .addValue("warrantyExpiryDate", detail.getWarrantyExpiryDate())
                .addValue("registrationDate", detail.getRegistrationDate())
                .addValue("registrationExpiryDate", detail.getRegistrationExpiryDate())
                .addValue("registrationDocument", detail.getRegistrationDocument())
                .addValue("insuranceCompany", detail.getInsuranceCompany())
                .addValue("insurancePolicyNumber", detail.getInsurancePolicyNumber())
                .addValue("insuranceStartDate", detail.getInsuranceStartDate())
                .addValue("insuranceExpiryDate", detail.getInsuranceExpiryDate())
                .addValue("revenueLicenceNumber", detail.getRevenueLicenceNumber())
                .addValue("revenueLicenceStartDate", detail.getRevenueLicenceStartDate())
                .addValue("revenueLicenceExpiryDate", detail.getRevenueLicenceExpiryDate())
                .addValue("emissionTestStartDate", detail.getEmissionTestStartDate())
                .addValue("emissionTestExpiryDate", detail.getEmissionTestExpiryDate())
                .addValue("insuranceLicenceDocuments", detail.getInsuranceLicenceDocuments())
                .addValue("assignedDate", detail.getAssignedDate())
                .addValue("owningDepartment", detail.getOwningDepartment())
                .addValue("costCentreAccountCode", detail.getCostCentreAccountCode())
                .addValue("accountDescription", detail.getAccountDescription())
                .addValue("vehiclePhotograph", detail.getVehiclePhotograph())
                .addValue("registrationCertificate", detail.getRegistrationCertificate())
                .addValue("insuranceDocument", detail.getInsuranceDocument())
                .addValue("revenueLicenceDocument", detail.getRevenueLicenceDocument())
                .addValue("purchaseInvoiceDocument", detail.getPurchaseInvoiceDocument())
                .addValue("warrantyDocument", detail.getWarrantyDocument())
                .addValue("otherSupportingDocuments", detail.getOtherSupportingDocuments());
    }
}
