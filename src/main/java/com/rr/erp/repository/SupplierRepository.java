package com.rr.erp.repository;


import com.rr.erp.entity.Supplier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class SupplierRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SupplierRepository(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addSupplier(Supplier supplierRequest) {

        String sql = """
                INSERT INTO supplier (
                    supplierCode,
                    supplierName,
                    line1,
                    line2,
                    city,
                    country,
                    phoneNumber,
                    email,
                    status,
                    telephoneNumber,
                    accountNumber,
                    taxRegistrationNumber,
                    description,
                    bankName,
                    contactPerson,
                    contactPersonNumber
                )
                VALUES (
                    :supplierCode,
                    :supplierName,
                    :line1,
                    :line2,
                    :city,
                    :country,
                    :phoneNumber,
                    :email,
                    :status,
                    :telephoneNo,
                    :accountNo,
                    :taxRegistrationNo,
                    :description,
                    :bankName,
                    :contactPerson,
                    :contactPersonNumber
                )
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("supplierCode", supplierRequest.getSupplierCode())
                .addValue("supplierName", supplierRequest.getSupplierName())
                .addValue("line1", supplierRequest.getLine1())
                .addValue("line2", supplierRequest.getLine2())
                .addValue("city", supplierRequest.getCity())
                .addValue("country", supplierRequest.getCountry())
                .addValue("phoneNumber", supplierRequest.getPhoneNumber())
                .addValue("email", supplierRequest.getEmail())
                .addValue(
                        "status",
                        supplierRequest.getStatus() != null
                                ? supplierRequest.getStatus()
                                : true
                )
                .addValue("telephoneNo",supplierRequest.getTelephoneNumber())
                .addValue("accountNo",supplierRequest.getAccountNumber())
                .addValue("taxRegistrationNo",supplierRequest.getTaxRegistrationNumber())
                .addValue("description", supplierRequest.getDescription())
                .addValue("bankName", supplierRequest.getBankName())
                .addValue("contactPerson", supplierRequest.getContactPerson())
                .addValue("contactPersonNumber", supplierRequest.getContactPersonNumber());

        return jdbcTemplate.update(sql, parameters);
    }

    public boolean existsBySupplierCode(String supplierCode) {

        String sql = """
                SELECT COUNT(*)
                FROM supplier
                WHERE supplierCode = :supplierCode
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("supplierCode", supplierCode);

        Integer count = jdbcTemplate.queryForObject(
                sql,
                parameters,
                Integer.class
        );

        return count != null && count > 0;
    }

    public List<Supplier> getAllSuppliers() {

        String sql = """
            SELECT
                supplierCode AS "supplierCode",
                supplierName AS "supplierName",
                line1,
                line2,
                city,
                country,
                phoneNumber AS "phoneNumber",
                email AS "email",
                status,
                telephoneNumber as "telephoneNumber",
                accountNumber as "accountNumber",
                taxRegistrationNumber as "taxRegistrationNumber",
                description,
                bankName as "bankName",
                contactPerson as "contactPerson",
                contactPersonNumber as "contactPersonNumber"
            FROM supplier
            ORDER BY supplierName
            """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(Supplier.class)
        );
    }

    public int updateSupplier(
            String supplierCode,
            Supplier supplierRequest
    ) {

        String sql = """
            UPDATE supplier
            SET
                supplierName = :supplierName,
                line1 = :line1,
                line2 = :line2,
                city = :city,
                country = :country,
                phoneNumber = :phoneNumber,
                email = :email,
                status = :status,
                telephoneNumber = :telephoneNo,
                accountNumber = :accountNo,
                taxRegistrationNumber = :taxRegistrationNo,
                description = :description,
                bankName = :bankName,
                contactPerson = :contactPerson,
                contactPersonNumber = :contactPersonNumber
            WHERE supplierCode = :supplierCode
            """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("supplierCode", supplierCode)
                .addValue("supplierName", supplierRequest.getSupplierName())
                .addValue("line1", supplierRequest.getLine1())
                .addValue("line2", supplierRequest.getLine2())
                .addValue("city", supplierRequest.getCity())
                .addValue("country", supplierRequest.getCountry())
                .addValue("phoneNumber", supplierRequest.getPhoneNumber())
                .addValue("email", supplierRequest.getEmail())
                .addValue("status", supplierRequest.getStatus())
                .addValue("telephoneNo",supplierRequest.getTelephoneNumber())
                .addValue("accountNo",supplierRequest.getAccountNumber())
                .addValue("taxRegistrationNo",supplierRequest.getTaxRegistrationNumber())
                .addValue("description", supplierRequest.getDescription())
                .addValue("bankName", supplierRequest.getBankName())
                .addValue("contactPerson", supplierRequest.getContactPerson())
                .addValue("contactPersonNumber", supplierRequest.getContactPersonNumber());

        return jdbcTemplate.update(sql, parameters);
    }
}
