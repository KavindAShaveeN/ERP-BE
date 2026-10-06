package com.rr.erp.repository;

import com.rr.erp.entity.ServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceRequestRepository {

    private final JdbcTemplate jdbcTemplate;


    // =========================================================
    // CREATE SERVICE REQUEST
    // =========================================================
    public int createServiceRequest(ServiceRequest serviceRequest) {

        String sql = """
                INSERT INTO service_request (
                    service_request_id,
                    service_request_code,
                    submitted_by,
                    requested_date,
                    project_code,
                    asset_code,
                    operator_name,
                    phone_number,
                    maintenance_works,
                    request_type,
                    remarks,
                    is_approved,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        return jdbcTemplate.update(
                sql,
                serviceRequest.getServiceRequestId(),
                serviceRequest.getServiceRequestCode(),
                serviceRequest.getSubmittedBy(),
                serviceRequest.getRequestedDate(),
                serviceRequest.getProjectCode(),
                serviceRequest.getAssetCode(),
                serviceRequest.getOperatorName(),
                serviceRequest.getPhoneNumber(),
                serviceRequest.getMaintenanceWorks(),
                serviceRequest.getRequestType(),
                serviceRequest.getRemarks(),
                serviceRequest.getIsApproved()
        );
    }


    // =========================================================
    // UPDATE SERVICE REQUEST
    // =========================================================
    public int updateServiceRequest(ServiceRequest serviceRequest) {

        String sql = """
                UPDATE service_request
                SET
                    service_request_code = ?,
                    submitted_by = ?,
                    requested_date = ?,
                    project_code = ?,
                    asset_code = ?,
                    operator_name = ?,
                    phone_number = ?,
                    maintenance_works = ?,
                    request_type = ?,
                    remarks = ?,
                    is_approved = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE service_request_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                serviceRequest.getServiceRequestCode(),
                serviceRequest.getSubmittedBy(),
                serviceRequest.getRequestedDate(),
                serviceRequest.getProjectCode(),
                serviceRequest.getAssetCode(),
                serviceRequest.getOperatorName(),
                serviceRequest.getPhoneNumber(),
                serviceRequest.getMaintenanceWorks(),
                serviceRequest.getRequestType(),
                serviceRequest.getRemarks(),
                serviceRequest.getIsApproved(),
                serviceRequest.getServiceRequestId()
        );
    }


    // =========================================================
    // GET SERVICE REQUESTS BY PROJECT CODE
    // =========================================================
    public List<ServiceRequest> getServiceRequestsByProjectCode(
            String projectCode) {

        String sql = """
                SELECT
                    service_request_id AS "serviceRequestId",
                    service_request_code AS "serviceRequestCode",
                    submitted_by AS "submittedBy",
                    requested_date AS "requestedDate",
                    project_code AS "projectCode",
                    asset_code AS "assetCode",
                    operator_name AS "operatorName",
                    phone_number AS "phoneNumber",
                    maintenance_works AS "maintenanceWorks",
                    request_type AS "requestType",
                    remarks AS "remarks",
                    is_approved AS "isApproved",
                    created_at AS "createdAt",
                    updated_at AS "updatedAt"
                FROM service_request
                WHERE project_code = ?
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapServiceRequest(rs),
                projectCode
        );
    }


    // =========================================================
    // GET ALL SERVICE REQUESTS
    // =========================================================
    public List<ServiceRequest> getAllServiceRequests() {

        String sql = """
                SELECT
                    service_request_id AS "serviceRequestId",
                    service_request_code AS "serviceRequestCode",
                    submitted_by AS "submittedBy",
                    requested_date AS "requestedDate",
                    project_code AS "projectCode",
                    asset_code AS "assetCode",
                    operator_name AS "operatorName",
                    phone_number AS "phoneNumber",
                    maintenance_works AS "maintenanceWorks",
                    request_type AS "requestType",
                    remarks AS "remarks",
                    is_approved AS "isApproved",
                    created_at AS "createdAt",
                    updated_at AS "updatedAt"
                FROM service_request
                WHERE is_approved = TRUE
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapServiceRequest(rs)
        );
    }


    // =========================================================
    // MAPPER
    // =========================================================
    private ServiceRequest mapServiceRequest(
            java.sql.ResultSet rs) throws java.sql.SQLException {

        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setServiceRequestId(
                rs.getObject("serviceRequestId", UUID.class)
        );

        serviceRequest.setServiceRequestCode(
                rs.getString("serviceRequestCode")
        );

        serviceRequest.setSubmittedBy(
                rs.getString("submittedBy")
        );

        serviceRequest.setRequestedDate(
                rs.getObject("requestedDate", java.time.LocalDateTime.class)
        );

        serviceRequest.setProjectCode(
                rs.getString("projectCode")
        );

        serviceRequest.setAssetCode(
                rs.getString("assetCode")
        );

        serviceRequest.setOperatorName(
                rs.getString("operatorName")
        );

        serviceRequest.setPhoneNumber(
                rs.getString("phoneNumber")
        );

        serviceRequest.setMaintenanceWorks(
                rs.getString("maintenanceWorks")
        );

        serviceRequest.setRequestType(
                rs.getString("requestType")
        );

        serviceRequest.setRemarks(
                rs.getString("remarks")
        );

        serviceRequest.setIsApproved(
                rs.getBoolean("isApproved")
        );

        serviceRequest.setCreatedAt(
                rs.getObject("createdAt", java.time.LocalDateTime.class)
        );

        serviceRequest.setUpdatedAt(
                rs.getObject("updatedAt", java.time.LocalDateTime.class)
        );

        return serviceRequest;
    }
}