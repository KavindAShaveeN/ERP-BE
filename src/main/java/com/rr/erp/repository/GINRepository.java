package com.rr.erp.repository;

import com.rr.erp.entity.GIN;
import com.rr.erp.entity.GINItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GINRepository {

    private final JdbcTemplate jdbcTemplate;

    public GINRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Shared row mapper for the gate-pass queries below, which select the same column set
    // as the other GIN queries but don't need their own duplicated inline lambda.
    private static final RowMapper<GIN> GIN_ROW_MAPPER = (rs, rowNum) -> {

        GIN gin = new GIN();

        gin.setGinId(rs.getObject("gin_id", UUID.class));
        gin.setGinCode(rs.getString("gin_code"));
        gin.setGinTypeId(rs.getObject("gin_type_id", Integer.class));

        Timestamp issuedDate = rs.getTimestamp("issued_date");
        gin.setIssuedDate(issuedDate != null ? issuedDate.toLocalDateTime() : null);

        gin.setIssuedProjectCode(rs.getString("issued_project_code"));
        gin.setReceivedProjectCode(rs.getString("received_project_code"));
        gin.setReceivedPerson(rs.getString("received_by"));
        gin.setVehicleNo(rs.getString("vehicle_no"));
        gin.setVehicleAssetCode(rs.getString("vehicle_asset_code"));
        gin.setForAssetCode(rs.getString("for_asset_code"));
        gin.setIssuedBy(rs.getString("issued_by"));
        gin.setApprovedBy(rs.getString("approved_by"));

        Timestamp approvedDate = rs.getTimestamp("approved_date");
        gin.setApprovedDate(approvedDate != null ? approvedDate.toLocalDateTime() : null);

        gin.setIsAuthorized(rs.getObject("is_authorized", Boolean.class));

        Date expectedReturnDate = rs.getDate("expected_return_date");
        gin.setExpectedReturnDate(expectedReturnDate != null ? expectedReturnDate.toLocalDate() : null);

        gin.setReceiverName(rs.getString("receiver_name"));
        gin.setReceiverNIC(rs.getString("receiver_nic"));
        gin.setSubContractorId(rs.getInt("sub_contractor_id"));
        gin.setMrId(rs.getObject("mr_id", UUID.class));
        gin.setGateVerifiedBy(rs.getString("gate_verified_by"));

        Timestamp gateVerifiedDate = rs.getTimestamp("gate_verified_date");
        gin.setGateVerifiedDate(gateVerifiedDate != null ? gateVerifiedDate.toLocalDateTime() : null);

        gin.setIsGateVerified(rs.getObject("is_gate_verified", Boolean.class));
        gin.setArrivalGateVerifiedBy(rs.getString("arrival_gate_verified_by"));

        Timestamp arrivalGateVerifiedDate = rs.getTimestamp("arrival_gate_verified_date");
        gin.setArrivalGateVerifiedDate(
                arrivalGateVerifiedDate != null ? arrivalGateVerifiedDate.toLocalDateTime() : null
        );

        gin.setIsArrivalGateVerified(rs.getObject("is_arrival_gate_verified", Boolean.class));

        return gin;
    };

    public List<GIN> getCreatedGin(String issuedProjectCode, int page, int size) {

        String sql = """
                SELECT
                    gin_id,
                    gin_code,
                    gin_type_id,
                    issued_date,
                    issued_project_code,
                    received_project_code,
                    received_by,
                    vehicle_no,
                    vehicle_asset_code,
                    for_asset_code,
                    issued_by,
                    approved_by,
                    approved_date,
                    is_authorized,
                    expected_return_date,
                    receiver_name,
                    receiver_nic,
                    sub_contractor_id,
                    mr_id,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified,
                    arrival_gate_verified_by,
                    arrival_gate_verified_date,
                    is_arrival_gate_verified
                FROM gin
                WHERE issued_project_code = ?
                ORDER BY issued_date DESC
                LIMIT ? OFFSET ?
                """;

        List<GIN> ginList = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    GIN gin = new GIN();

                    gin.setGinId(rs.getObject("gin_id", UUID.class));
                    gin.setGinCode(rs.getString("gin_code"));
                    gin.setGinTypeId(rs.getObject("gin_type_id", Integer.class));

                    Timestamp issuedDate = rs.getTimestamp("issued_date");
                    gin.setIssuedDate(
                            issuedDate != null ? issuedDate.toLocalDateTime() : null
                    );

                    gin.setIssuedProjectCode(
                            rs.getString("issued_project_code")
                    );

                    gin.setReceivedProjectCode(
                            rs.getString("received_project_code")
                    );

                    gin.setReceivedPerson(
                            rs.getString("received_by")
                    );

                    gin.setVehicleNo(
                            rs.getString("vehicle_no")
                    );
                    gin.setVehicleAssetCode(
                            rs.getString("vehicle_asset_code")
                    );

                    gin.setForAssetCode(
                            rs.getString("for_asset_code")
                    );

                    gin.setIssuedBy(
                            rs.getString("issued_by")
                    );

                    gin.setApprovedBy(
                            rs.getString("approved_by")
                    );

                    Timestamp approvedDate = rs.getTimestamp("approved_date");
                    gin.setApprovedDate(
                            approvedDate != null ? approvedDate.toLocalDateTime() : null
                    );

                    gin.setIsAuthorized(
                            rs.getObject("is_authorized", Boolean.class)
                    );
                    Date expectedReturnDate = rs.getDate("expected_return_date");
                    gin.setExpectedReturnDate(
                            expectedReturnDate != null ? expectedReturnDate.toLocalDate() : null
                    );

                    gin.setReceiverName(
                            rs.getString("receiver_name")
                    );
                    gin.setReceiverNIC(
                            rs.getString("receiver_nic")
                    );
                    gin.setSubContractorId(
                            rs.getInt("sub_contractor_id")
                    );
                    gin.setMrId(
                            rs.getObject("mr_id", UUID.class)
                    );

                    gin.setGateVerifiedBy(
                            rs.getString("gate_verified_by")
                    );

                    Timestamp gateVerifiedDate = rs.getTimestamp("gate_verified_date");
                    gin.setGateVerifiedDate(
                            gateVerifiedDate != null ? gateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsGateVerified(
                            rs.getObject("is_gate_verified", Boolean.class)
                    );

                    gin.setArrivalGateVerifiedBy(
                            rs.getString("arrival_gate_verified_by")
                    );

                    Timestamp arrivalGateVerifiedDate = rs.getTimestamp("arrival_gate_verified_date");
                    gin.setArrivalGateVerifiedDate(
                            arrivalGateVerifiedDate != null ? arrivalGateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsArrivalGateVerified(
                            rs.getObject("is_arrival_gate_verified", Boolean.class)
                    );

                    return gin;
                },
                issuedProjectCode,
                size,
                page * size
        );

        // Get items for every GIN
        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }

        return ginList;
    }

    public long countCreatedGin(String issuedProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM gin
                WHERE issued_project_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, issuedProjectCode);

        return count == null ? 0 : count;
    }


    public List<GIN> getIncomingGin(String receivedProjectCode, int page, int size) {

        String sql = """
                SELECT
                    gin_id,
                    gin_code,
                    gin_type_id,
                    issued_date,
                    issued_project_code,
                    received_project_code,
                    received_by,
                    vehicle_no,
                    vehicle_asset_code,
                    for_asset_code,
                    issued_by,
                    approved_by,
                    approved_date,
                    is_authorized,
                    expected_return_date,
                    receiver_name,
                    receiver_nic,
                    sub_contractor_id,
                    mr_id,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified,
                    arrival_gate_verified_by,
                    arrival_gate_verified_date,
                    is_arrival_gate_verified
                FROM gin
                WHERE received_project_code = ?
                ORDER BY issued_date DESC
                LIMIT ? OFFSET ?
                """;

        List<GIN> ginList = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    GIN gin = new GIN();

                    gin.setGinId(rs.getObject("gin_id", UUID.class));
                    gin.setGinCode(rs.getString("gin_code"));
                    gin.setGinTypeId(rs.getObject("gin_type_id", Integer.class));

                    Timestamp issuedDate = rs.getTimestamp("issued_date");
                    gin.setIssuedDate(
                            issuedDate != null ? issuedDate.toLocalDateTime() : null
                    );

                    gin.setIssuedProjectCode(
                            rs.getString("issued_project_code")
                    );

                    gin.setReceivedProjectCode(
                            rs.getString("received_project_code")
                    );

                    gin.setReceivedPerson(
                            rs.getString("received_by")
                    );

                    gin.setVehicleNo(
                            rs.getString("vehicle_no")
                    );
                    gin.setVehicleAssetCode(
                            rs.getString("vehicle_asset_code")
                    );

                    gin.setForAssetCode(
                            rs.getString("for_asset_code")
                    );

                    gin.setIssuedBy(
                            rs.getString("issued_by")
                    );

                    gin.setApprovedBy(
                            rs.getString("approved_by")
                    );

                    Timestamp approvedDate = rs.getTimestamp("approved_date");
                    gin.setApprovedDate(
                            approvedDate != null ? approvedDate.toLocalDateTime() : null
                    );

                    gin.setIsAuthorized(
                            rs.getObject("is_authorized", Boolean.class)
                    );
                    Date expectedReturnDate = rs.getDate("expected_return_date");
                    gin.setExpectedReturnDate(
                            expectedReturnDate != null ? expectedReturnDate.toLocalDate() : null
                    );

                    gin.setReceiverName(
                            rs.getString("receiver_name")
                    );
                    gin.setReceiverNIC(
                            rs.getString("receiver_nic")
                    );
                    gin.setSubContractorId(
                            rs.getInt("sub_contractor_id")
                    );
                    gin.setMrId(
                            rs.getObject("mr_id", UUID.class)
                    );

                    gin.setGateVerifiedBy(
                            rs.getString("gate_verified_by")
                    );

                    Timestamp gateVerifiedDate = rs.getTimestamp("gate_verified_date");
                    gin.setGateVerifiedDate(
                            gateVerifiedDate != null ? gateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsGateVerified(
                            rs.getObject("is_gate_verified", Boolean.class)
                    );

                    gin.setArrivalGateVerifiedBy(
                            rs.getString("arrival_gate_verified_by")
                    );

                    Timestamp arrivalGateVerifiedDate = rs.getTimestamp("arrival_gate_verified_date");
                    gin.setArrivalGateVerifiedDate(
                            arrivalGateVerifiedDate != null ? arrivalGateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsArrivalGateVerified(
                            rs.getObject("is_arrival_gate_verified", Boolean.class)
                    );

                    return gin;
                },
                receivedProjectCode,
                size,
                page * size
        );

        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }

        return ginList;
    }

    public long countIncomingGin(String receivedProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM gin
                WHERE received_project_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, receivedProjectCode);

        return count == null ? 0 : count;
    }


    // Every GIN regardless of project — used by HQ-wide report views.
    public List<GIN> getAllGins(int page, int size) {

        String sql = """
                SELECT
                    gin_id,
                    gin_code,
                    gin_type_id,
                    issued_date,
                    issued_project_code,
                    received_project_code,
                    received_by,
                    vehicle_no,
                    vehicle_asset_code,
                    for_asset_code,
                    issued_by,
                    approved_by,
                    approved_date,
                    is_authorized,
                    expected_return_date,
                    receiver_name,
                    receiver_nic,
                    sub_contractor_id,
                    mr_id,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified,
                    arrival_gate_verified_by,
                    arrival_gate_verified_date,
                    is_arrival_gate_verified
                FROM gin
                ORDER BY issued_date DESC
                LIMIT ? OFFSET ?
                """;

        List<GIN> ginList = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    GIN gin = new GIN();

                    gin.setGinId(rs.getObject("gin_id", UUID.class));
                    gin.setGinCode(rs.getString("gin_code"));
                    gin.setGinTypeId(rs.getObject("gin_type_id", Integer.class));

                    Timestamp issuedDate = rs.getTimestamp("issued_date");
                    gin.setIssuedDate(
                            issuedDate != null ? issuedDate.toLocalDateTime() : null
                    );

                    gin.setIssuedProjectCode(
                            rs.getString("issued_project_code")
                    );

                    gin.setReceivedProjectCode(
                            rs.getString("received_project_code")
                    );

                    gin.setReceivedPerson(
                            rs.getString("received_by")
                    );

                    gin.setVehicleNo(
                            rs.getString("vehicle_no")
                    );
                    gin.setVehicleAssetCode(
                            rs.getString("vehicle_asset_code")
                    );

                    gin.setForAssetCode(
                            rs.getString("for_asset_code")
                    );

                    gin.setIssuedBy(
                            rs.getString("issued_by")
                    );

                    gin.setApprovedBy(
                            rs.getString("approved_by")
                    );

                    Timestamp approvedDate = rs.getTimestamp("approved_date");
                    gin.setApprovedDate(
                            approvedDate != null ? approvedDate.toLocalDateTime() : null
                    );

                    gin.setIsAuthorized(
                            rs.getObject("is_authorized", Boolean.class)
                    );
                    Date expectedReturnDate = rs.getDate("expected_return_date");
                    gin.setExpectedReturnDate(
                            expectedReturnDate != null ? expectedReturnDate.toLocalDate() : null
                    );

                    gin.setReceiverName(
                            rs.getString("receiver_name")
                    );
                    gin.setReceiverNIC(
                            rs.getString("receiver_nic")
                    );
                    gin.setSubContractorId(
                            rs.getInt("sub_contractor_id")
                    );
                    gin.setMrId(
                            rs.getObject("mr_id", UUID.class)
                    );

                    gin.setGateVerifiedBy(
                            rs.getString("gate_verified_by")
                    );

                    Timestamp gateVerifiedDate = rs.getTimestamp("gate_verified_date");
                    gin.setGateVerifiedDate(
                            gateVerifiedDate != null ? gateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsGateVerified(
                            rs.getObject("is_gate_verified", Boolean.class)
                    );

                    gin.setArrivalGateVerifiedBy(
                            rs.getString("arrival_gate_verified_by")
                    );

                    Timestamp arrivalGateVerifiedDate = rs.getTimestamp("arrival_gate_verified_date");
                    gin.setArrivalGateVerifiedDate(
                            arrivalGateVerifiedDate != null ? arrivalGateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsArrivalGateVerified(
                            rs.getObject("is_arrival_gate_verified", Boolean.class)
                    );

                    return gin;
                },
                size,
                page * size
        );

        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }

        return ginList;
    }

    public long countAllGins() {

        String sql = """
                SELECT COUNT(*)
                FROM gin
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }


    /** One GIN by id, items included — used to check its current is_authorized state before an update is applied. */
    public Optional<GIN> findById(UUID ginId) {

        String sql = """
                SELECT
                    gin_id,
                    gin_code,
                    gin_type_id,
                    issued_date,
                    issued_project_code,
                    received_project_code,
                    received_by,
                    vehicle_no,
                    vehicle_asset_code,
                    for_asset_code,
                    issued_by,
                    approved_by,
                    approved_date,
                    is_authorized,
                    expected_return_date,
                    receiver_name,
                    receiver_nic,
                    sub_contractor_id,
                    mr_id,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified,
                    arrival_gate_verified_by,
                    arrival_gate_verified_date,
                    is_arrival_gate_verified
                FROM gin
                WHERE gin_id = ?
                """;

        List<GIN> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    GIN gin = new GIN();

                    gin.setGinId(rs.getObject("gin_id", UUID.class));
                    gin.setGinCode(rs.getString("gin_code"));
                    gin.setGinTypeId(rs.getObject("gin_type_id", Integer.class));

                    Timestamp issuedDate = rs.getTimestamp("issued_date");
                    gin.setIssuedDate(
                            issuedDate != null ? issuedDate.toLocalDateTime() : null
                    );

                    gin.setIssuedProjectCode(rs.getString("issued_project_code"));
                    gin.setReceivedProjectCode(rs.getString("received_project_code"));
                    gin.setReceivedPerson(rs.getString("received_by"));
                    gin.setVehicleNo(rs.getString("vehicle_no"));
                    gin.setVehicleAssetCode(rs.getString("vehicle_asset_code"));
                    gin.setForAssetCode(rs.getString("for_asset_code"));
                    gin.setIssuedBy(rs.getString("issued_by"));
                    gin.setApprovedBy(rs.getString("approved_by"));

                    Timestamp approvedDate = rs.getTimestamp("approved_date");
                    gin.setApprovedDate(
                            approvedDate != null ? approvedDate.toLocalDateTime() : null
                    );

                    gin.setIsAuthorized(rs.getObject("is_authorized", Boolean.class));

                    Date expectedReturnDate = rs.getDate("expected_return_date");
                    gin.setExpectedReturnDate(
                            expectedReturnDate != null ? expectedReturnDate.toLocalDate() : null
                    );

                    gin.setReceiverName(rs.getString("receiver_name"));
                    gin.setReceiverNIC(rs.getString("receiver_nic"));
                    gin.setSubContractorId(rs.getInt("sub_contractor_id"));
                    gin.setMrId(rs.getObject("mr_id", UUID.class));
                    gin.setGateVerifiedBy(rs.getString("gate_verified_by"));

                    Timestamp gateVerifiedDate = rs.getTimestamp("gate_verified_date");
                    gin.setGateVerifiedDate(
                            gateVerifiedDate != null ? gateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsGateVerified(rs.getObject("is_gate_verified", Boolean.class));

                    gin.setArrivalGateVerifiedBy(rs.getString("arrival_gate_verified_by"));

                    Timestamp arrivalGateVerifiedDate = rs.getTimestamp("arrival_gate_verified_date");
                    gin.setArrivalGateVerifiedDate(
                            arrivalGateVerifiedDate != null ? arrivalGateVerifiedDate.toLocalDateTime() : null
                    );

                    gin.setIsArrivalGateVerified(rs.getObject("is_arrival_gate_verified", Boolean.class));

                    return gin;
                },
                ginId
        );

        if (results.isEmpty()) {
            return Optional.empty();
        }

        GIN gin = results.get(0);
        gin.setItems(getGinItems(ginId));

        return Optional.of(gin);
    }

    public List<GINItem> getGinItems(UUID ginId) {

        String sql = """
                SELECT
                    gin_item_id,
                    gin_id,
                    item_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    remarks,
                    issue_item_type_id,
                    unit_price,
                    amount,
                    length_m,
                    width_m,
                    asset_code,
                    mr_item_id
                FROM gin_item
                WHERE gin_id = ?
                """;

        List<GINItem> items = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    GINItem item = new GINItem();

                    item.setGinItemId(
                            rs.getObject("gin_item_id", UUID.class)
                    );

                    item.setGinId(
                            rs.getObject("gin_id", UUID.class)
                    );

                    item.setItemCode(
                            rs.getString("item_code")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setSize(
                            rs.getString("size")
                    );

                    item.setUom(
                            rs.getObject("uom_id", Integer.class)
                    );

                    item.setQuantity(
                            rs.getBigDecimal("quantity")
                    );

                    item.setRemarks(
                            rs.getString("remarks")
                    );
                    item.setIssueItemTypeId(
                            rs.getObject("issue_item_type_id", Integer.class)
                    );

                    item.setUnitPrice(
                            rs.getBigDecimal("unit_price")
                    );

                    item.setAmount(
                            rs.getBigDecimal("amount")
                    );

                    item.setLengthM(
                            rs.getBigDecimal("length_m")
                    );

                    item.setWidthM(
                            rs.getBigDecimal("width_m")
                    );

                    item.setAssetCode(
                            rs.getString("asset_code")
                    );

                    item.setMrItemId(
                            rs.getObject("mr_item_id", UUID.class)
                    );

                    return item;
                },
                ginId
        );

        AssetPackRepository.attachSelections(jdbcTemplate, "GIN", ginId, items);

        return items;
    }

    public void createGin(GIN gin) {

        String sql = """
                INSERT INTO gin (
                    gin_id,
                    gin_code,
                    gin_type_id,
                    issued_date,
                    issued_project_code,
                    received_project_code,
                    received_by,
                    vehicle_no,
                    vehicle_asset_code,
                    for_asset_code,
                    issued_by,
                    approved_by,
                    approved_date,
                    is_authorized,
                    expected_return_date,
                    receiver_name,
                    receiver_nic,
                    sub_contractor_id,
                    mr_id,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified,
                    arrival_gate_verified_by,
                    arrival_gate_verified_date,
                    is_arrival_gate_verified
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                gin.getGinId(),
                gin.getGinCode(),
                gin.getGinTypeId(),
                gin.getIssuedDate(),
                gin.getIssuedProjectCode(),
                gin.getReceivedProjectCode(),
                gin.getReceivedPerson(),
                gin.getVehicleNo(),
                gin.getVehicleAssetCode(),
                gin.getForAssetCode(),
                gin.getIssuedBy(),
                gin.getApprovedBy(),
                gin.getApprovedDate(),
                gin.getIsAuthorized(),
                gin.getExpectedReturnDate(),
                gin.getReceiverName(),
                gin.getReceiverNIC(),
                gin.getSubContractorId(),
                gin.getMrId(),
                gin.getGateVerifiedBy(),
                gin.getGateVerifiedDate(),
                gin.getIsGateVerified(),
                gin.getArrivalGateVerifiedBy(),
                gin.getArrivalGateVerifiedDate(),
                gin.getIsArrivalGateVerified()

        );
    }


    public void createGinItem(GINItem item) {

        String sql = """
                INSERT INTO gin_item (
                    gin_item_id,
                    gin_id,
                    item_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    remarks,
                    issue_item_type_id,
                    unit_price,
                    amount,
                    length_m,
                    width_m,
                    asset_code,
                    mr_item_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getGinItemId(),
                item.getGinId(),
                item.getItemCode(),
                item.getDescription(),
                item.getSize(),
                item.getUom(),
                item.getQuantity(),
                item.getRemarks(),
                item.getIssueItemTypeId(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getLengthM(),
                item.getWidthM(),
                item.getAssetCode(),
                item.getMrItemId()
        );
    }

    public int updateGin(UUID ginId, GIN gin) {

        String sql = """
                UPDATE gin
                SET
                    gin_code = ?,
                    gin_type_id = ?,
                    issued_date = ?,
                    issued_project_code = ?,
                    received_project_code = ?,
                    received_by = ?,
                    vehicle_no = ?,
                    vehicle_asset_code = ?,
                    for_asset_code = ?,
                    issued_by = ?,
                    approved_by = ?,
                    approved_date = ?,
                    is_authorized = ?,
                    expected_return_date = ?,
                    receiver_name = ?,
                    receiver_nic = ?,
                    sub_contractor_id = ?,
                    mr_id = ?,
                    gate_verified_by = ?,
                    gate_verified_date = ?,
                    is_gate_verified = ?,
                    arrival_gate_verified_by = ?,
                    arrival_gate_verified_date = ?,
                    is_arrival_gate_verified = ?
                WHERE gin_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                gin.getGinCode(),
                gin.getGinTypeId(),
                gin.getIssuedDate(),
                gin.getIssuedProjectCode(),
                gin.getReceivedProjectCode(),
                gin.getReceivedPerson(),
                gin.getVehicleNo(),
                gin.getVehicleAssetCode(),
                gin.getForAssetCode(),
                gin.getIssuedBy(),
                gin.getApprovedBy(),
                gin.getApprovedDate(),
                gin.getIsAuthorized(),
                gin.getExpectedReturnDate(),
                gin.getReceiverName(),
                gin.getReceiverNIC(),
                gin.getSubContractorId(),
                gin.getMrId(),
                gin.getGateVerifiedBy(),
                gin.getGateVerifiedDate(),
                gin.getIsGateVerified(),
                gin.getArrivalGateVerifiedBy(),
                gin.getArrivalGateVerifiedDate(),
                gin.getIsArrivalGateVerified(),
                ginId
        );
    }

    /** Sets only the gate-verification columns — used to record security's confirmation
     * after a GIN is already authorized, when the general updateGin path is locked. */
    public int updateGateVerification(UUID ginId, String gateVerifiedBy, java.time.LocalDateTime gateVerifiedDate) {

        String sql = """
                UPDATE gin
                SET
                    gate_verified_by = ?,
                    gate_verified_date = ?,
                    is_gate_verified = TRUE
                WHERE gin_id = ?
                """;

        return jdbcTemplate.update(sql, gateVerifiedBy, gateVerifiedDate, ginId);
    }

    /** Sets only the arrival gate-verification columns — the receiving project's security
     * confirming these goods/vehicle arrived at their gate, recorded before any GRN exists
     * for this GIN. */
    public int updateArrivalGateVerification(UUID ginId, String gateVerifiedBy, java.time.LocalDateTime gateVerifiedDate) {

        String sql = """
                UPDATE gin
                SET
                    arrival_gate_verified_by = ?,
                    arrival_gate_verified_date = ?,
                    is_arrival_gate_verified = TRUE
                WHERE gin_id = ?
                """;

        return jdbcTemplate.update(sql, gateVerifiedBy, gateVerifiedDate, ginId);
    }

    /** GINs dispatched from this project, not yet confirmed leaving through the exit gate —
     * what the issuing project's security sees on the Gate Passes page. Shown from the moment
     * a GIN is created, regardless of authorization state — the gate check is independent of
     * the approval workflow. */
    public List<GIN> findPendingExitGate(String issuedProjectCode) {

        String sql = """
                SELECT gin_id, gin_code, gin_type_id, issued_date, issued_project_code,
                       received_project_code, received_by, vehicle_no, vehicle_asset_code,
                       for_asset_code, issued_by, approved_by, approved_date, is_authorized, expected_return_date,
                       receiver_name, receiver_nic, sub_contractor_id, mr_id,
                       gate_verified_by, gate_verified_date, is_gate_verified,
                       arrival_gate_verified_by, arrival_gate_verified_date, is_arrival_gate_verified
                FROM gin
                WHERE issued_project_code = ?
                  AND is_gate_verified = FALSE
                ORDER BY issued_date
                """;

        List<GIN> ginList = jdbcTemplate.query(sql, GIN_ROW_MAPPER, issuedProjectCode);
        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }
        return ginList;
    }

    /** GINs addressed to this project that have left their source gate but have not yet been
     * confirmed arriving at this project's gate, and have no GRN raised for them yet — what
     * the receiving project's security sees on the Gate Passes page. */
    public List<GIN> findPendingArrivalGate(String receivedProjectCode) {

        String sql = """
                SELECT gin_id, gin_code, gin_type_id, issued_date, issued_project_code,
                       received_project_code, received_by, vehicle_no, vehicle_asset_code,
                       for_asset_code, issued_by, approved_by, approved_date, is_authorized, expected_return_date,
                       receiver_name, receiver_nic, sub_contractor_id, mr_id,
                       gate_verified_by, gate_verified_date, is_gate_verified,
                       arrival_gate_verified_by, arrival_gate_verified_date, is_arrival_gate_verified
                FROM gin
                WHERE received_project_code = ?
                  AND is_gate_verified = TRUE
                  AND is_arrival_gate_verified = FALSE
                  AND NOT EXISTS (SELECT 1 FROM grn WHERE grn.gin_id = gin.gin_id)
                  AND NOT EXISTS (
                      SELECT 1 FROM transport_trip_gin t
                      WHERE t.gin_id = gin.gin_id AND t.is_active = TRUE AND t.delivery_mode = 'VIA_HUB'
                        AND t.custody_status IN ('ALLOCATED', 'ON_VEHICLE', 'AT_HUB'))
                ORDER BY issued_date
                """;

        List<GIN> ginList = jdbcTemplate.query(sql, GIN_ROW_MAPPER, receivedProjectCode);
        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }
        return ginList;
    }

    /** GINs whose items were issued for one specific asset (for_asset_code) — e.g. every
     * spare part/consumable dispatched to fit a given vehicle/plant item. Newest first. */
    public List<GIN> getGinsByForAssetCode(String forAssetCode, int page, int size) {

        String sql = """
                SELECT gin_id, gin_code, gin_type_id, issued_date, issued_project_code,
                       received_project_code, received_by, vehicle_no, vehicle_asset_code,
                       for_asset_code, issued_by, approved_by, approved_date, is_authorized, expected_return_date,
                       receiver_name, receiver_nic, sub_contractor_id, mr_id,
                       gate_verified_by, gate_verified_date, is_gate_verified,
                       arrival_gate_verified_by, arrival_gate_verified_date, is_arrival_gate_verified
                FROM gin
                WHERE for_asset_code = ?
                ORDER BY issued_date DESC
                LIMIT ? OFFSET ?
                """;

        List<GIN> ginList = jdbcTemplate.query(sql, GIN_ROW_MAPPER, forAssetCode, size, page * size);
        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }
        return ginList;
    }

    public long countGinsByForAssetCode(String forAssetCode) {

        String sql = """
                SELECT COUNT(*)
                FROM gin
                WHERE for_asset_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, forAssetCode);

        return count == null ? 0 : count;
    }

    /** GINs received by one employee (received_by = their employee code). Newest first. */
    public List<GIN> getGinsByReceivedBy(String employeeCode, int page, int size) {

        String sql = """
                SELECT gin_id, gin_code, gin_type_id, issued_date, issued_project_code,
                       received_project_code, received_by, vehicle_no, vehicle_asset_code,
                       for_asset_code, issued_by, approved_by, approved_date, is_authorized, expected_return_date,
                       receiver_name, receiver_nic, sub_contractor_id, mr_id,
                       gate_verified_by, gate_verified_date, is_gate_verified,
                       arrival_gate_verified_by, arrival_gate_verified_date, is_arrival_gate_verified
                FROM gin
                WHERE received_by = ?
                ORDER BY issued_date DESC
                LIMIT ? OFFSET ?
                """;

        List<GIN> ginList = jdbcTemplate.query(sql, GIN_ROW_MAPPER, employeeCode, size, page * size);
        for (GIN gin : ginList) {
            gin.setItems(getGinItems(gin.getGinId()));
        }
        return ginList;
    }

    public long countGinsByReceivedBy(String employeeCode) {

        String sql = """
                SELECT COUNT(*)
                FROM gin
                WHERE received_by = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, employeeCode);

        return count == null ? 0 : count;
    }

    // =========================
    // DELETE OLD ITEMS ON UPDATE
    // =========================

    public void deleteGinItems(UUID ginId) {

        String sql = """
                DELETE FROM gin_item
                WHERE gin_id = ?
                """;

        jdbcTemplate.update(sql, ginId);
    }
}