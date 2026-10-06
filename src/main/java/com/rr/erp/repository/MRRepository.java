package com.rr.erp.repository;

import com.rr.erp.entity.MR;
import com.rr.erp.entity.MRItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MRRepository {

    private final JdbcTemplate jdbcTemplate;

    public MRRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MR> mrRowMapper = (resultSet, rowNumber) -> {
        MR mr = new MR();

        mr.setMrId(resultSet.getObject("mr_id", UUID.class));
        mr.setMrCode(resultSet.getString("mr_code"));
        mr.setRequestingProjectCode(
                resultSet.getString("requesting_project_code")
        );
        mr.setDestinationProjectCode(
                resultSet.getString("destination_project_code")
        );
        mr.setRequestedDate(
                convertToLocalDateTime(resultSet.getTimestamp("requested_date"))
        );
        mr.setRequestedBy(resultSet.getString("requested_by"));
        mr.setCheckedDate(
                convertToLocalDateTime(resultSet.getTimestamp("checked_date"))
        );
        mr.setCheckedBy(resultSet.getString("checked_by"));
        mr.setApprovedDate(
                convertToLocalDateTime(resultSet.getTimestamp("approved_date"))
        );
        mr.setApprovedBy(resultSet.getString("approved_by"));
        mr.setRemark(resultSet.getString("remark"));

        Object approvedValue = resultSet.getObject("is_approved");

        mr.setIsApproved(
                approvedValue == null
                        ? null
                        : resultSet.getBoolean("is_approved")
        );

        return mr;
    };

    private final RowMapper<MRItem> mrItemRowMapper =
            (resultSet, rowNumber) -> {

                MRItem item = new MRItem();

                item.setMrItemId(
                        resultSet.getObject("mr_item_id", UUID.class)
                );
                item.setMrId(
                        resultSet.getObject("mr_id", UUID.class)
                );
                item.setItemCode(
                        resultSet.getString("item_code_code")
                );
                item.setDescription(
                        resultSet.getString("description")
                );
                item.setSize(resultSet.getString("size"));
                item.setUomId(resultSet.getInt("uom_id"));
                item.setQuantity(resultSet.getBigDecimal("quantity"));
                item.setPriority(resultSet.getString("priority"));
                item.setLineStatus(resultSet.getString("line_status"));
                item.setClosedReason(resultSet.getString("closed_reason"));
                item.setPriorityRank(resultSet.getObject("priority_rank", Integer.class));
                item.setRequiredDate(
                        convertToLocalDate(
                                resultSet.getDate("required_date")
                        )
                );

                return item;
            };

    public MR insertMR(MR mr) {

        String sql = """
                INSERT INTO mr (
                    mr_code,
                    requesting_project_code,
                    destination_project_code,
                    requested_date,
                    requested_by,
                    checked_date,
                    checked_by,
                    approved_date,
                    approved_by,
                    remark,
                    is_approved
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING mr_id
                """;

        UUID mrId = jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                mr.getMrCode(),
                mr.getRequestingProjectCode(),
                mr.getDestinationProjectCode(),
                mr.getRequestedDate(),
                mr.getRequestedBy(),
                mr.getCheckedDate(),
                mr.getCheckedBy(),
                mr.getApprovedDate(),
                mr.getApprovedBy(),
                mr.getRemark(),
                mr.getIsApproved()
        );

        insertItems(mrId, mr.getItems());

        return mr;
    }

    public void insertItems(UUID mrId, List<MRItem> items) {

        String sql = """
                INSERT INTO mr_item (
                    mr_id,
                    item_code_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    priority,
                    required_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (MRItem item : items) {

            UUID mrItemId = UUID.randomUUID();

            item.setMrItemId(mrItemId);
            item.setMrId(mrId);

            jdbcTemplate.update(
                    sql,
                    item.getMrId(),
                    item.getItemCode(),
                    item.getDescription(),
                    item.getSize(),
                    item.getUomId(),
                    item.getQuantity(),
                    item.getPriority(),
                    item.getRequiredDate()
            );
        }
    }

    public List<MR> findByDestinationProjectCode(
            String destinationProjectCode,
            int page,
            int size
    ) {

        String sql = """
                SELECT
                    mr_id,
                    mr_code,
                    requesting_project_code,
                    destination_project_code,
                    requested_date,
                    requested_by,
                    checked_date,
                    checked_by,
                    approved_date,
                    approved_by,
                    remark,
                    is_approved
                FROM mr
                WHERE destination_project_code = ? and is_approved = true
                ORDER BY requested_date DESC, mr_code DESC
                LIMIT ? OFFSET ?
                """;

        List<MR> materialRequests = jdbcTemplate.query(
                sql,
                mrRowMapper,
                destinationProjectCode,
                size,
                page * size
        );

        for (MR mr : materialRequests) {
            mr.setItems(findItemsByMrId(mr.getMrId()));
        }

        return materialRequests;
    }

    public long countByDestinationProjectCode(String destinationProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM mr
                WHERE destination_project_code = ? and is_approved = true
                """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                destinationProjectCode
        );

        return count == null ? 0 : count;
    }

    public Optional<MR> findById(UUID mrId) {

        String sql = """
                SELECT
                    mr_id,
                    mr_code,
                    requesting_project_code,
                    destination_project_code,
                    requested_date,
                    requested_by,
                    checked_date,
                    checked_by,
                    approved_date,
                    approved_by,
                    remark,
                    is_approved
                FROM mr
                WHERE mr_id = ?
                """;

        List<MR> results = jdbcTemplate.query(
                sql,
                mrRowMapper,
                mrId
        );

        if (results.isEmpty()) {
            return Optional.empty();
        }

        MR mr = results.get(0);
        mr.setItems(findItemsByMrId(mrId));

        return Optional.of(mr);
    }

    public List<MRItem> findItemsByMrId(UUID mrId) {

        String sql = """
                SELECT
                    mr_item_id,
                    mr_id,
                    item_code_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    priority,
                    required_date,
                    line_status,
                    closed_reason,
                    priority_rank
                FROM mr_item
                WHERE mr_id = ?
                ORDER BY required_date, mr_item_id
                """;

        return jdbcTemplate.query(
                sql,
                mrItemRowMapper,
                mrId
        );
    }

    public int updateMR(UUID mrId, MR mr) {

        String sql = """
                UPDATE mr
                SET
                    mr_code = ?,
                    requesting_project_code = ?,
                    destination_project_code = ?,
                    requested_date = ?,
                    requested_by = ?,
                    checked_date = ?,
                    checked_by = ?,
                    approved_date = ?,
                    approved_by = ?,
                    remark = ?,
                    is_approved = ?
                WHERE mr_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                mr.getMrCode(),
                mr.getRequestingProjectCode(),
                mr.getDestinationProjectCode(),
                mr.getRequestedDate(),
                mr.getRequestedBy(),
                mr.getCheckedDate(),
                mr.getCheckedBy(),
                mr.getApprovedDate(),
                mr.getApprovedBy(),
                mr.getRemark(),
                mr.getIsApproved(),
                mrId
        );
    }

    public void deleteItemsByMrId(UUID mrId) {

        String sql = """
                DELETE FROM mr_item
                WHERE mr_id = ?
                """;

        jdbcTemplate.update(sql, mrId);
    }

    /**
     * Brings an MR's lines in line with an edited payload WITHOUT replacing every row, so a
     * line keeps its mr_item_id (and with it its line_status, reservations and forward-plan
     * rows). Lines whose id matches an existing line are updated in place; lines with no
     * (or an unknown) id are inserted; existing lines missing from the payload are deleted
     * (their reservations and plan rows go with them via ON DELETE CASCADE).
     */
    public void syncItems(UUID mrId, List<MRItem> items) {

        List<UUID> existingIds = jdbcTemplate.query(
                "SELECT mr_item_id FROM mr_item WHERE mr_id = ?",
                (rs, rowNum) -> rs.getObject("mr_item_id", UUID.class),
                mrId
        );

        List<UUID> keptIds = new java.util.ArrayList<>();
        List<MRItem> toInsert = new java.util.ArrayList<>();

        for (MRItem item : items) {

            if (item.getMrItemId() != null && existingIds.contains(item.getMrItemId())) {

                jdbcTemplate.update(
                        """
                        UPDATE mr_item
                        SET item_code_code = ?, description = ?, size = ?, uom_id = ?,
                            quantity = ?, priority = ?, required_date = ?
                        WHERE mr_item_id = ?
                        """,
                        item.getItemCode(),
                        item.getDescription(),
                        item.getSize(),
                        item.getUomId(),
                        item.getQuantity(),
                        item.getPriority(),
                        item.getRequiredDate(),
                        item.getMrItemId()
                );
                keptIds.add(item.getMrItemId());

            } else {
                toInsert.add(item);
            }
        }

        for (UUID existingId : existingIds) {
            if (!keptIds.contains(existingId)) {
                jdbcTemplate.update("DELETE FROM mr_item WHERE mr_item_id = ?", existingId);
            }
        }

        insertItems(mrId, toInsert);
    }

    public boolean existsById(UUID mrId) {

        String sql = """
                SELECT COUNT(*)
                FROM mr
                WHERE mr_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                mrId
        );

        return count != null && count > 0;
    }

    private static LocalDate convertToLocalDate(Date date) {

        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime convertToLocalDateTime(Timestamp timestamp) {

        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public List<MR> findByRequestingProjectCode(
            String requestingProjectCode,
            int page,
            int size
    ) {

        String sql = """
            SELECT
                mr_id,
                mr_code,
                requesting_project_code,
                destination_project_code,
                requested_date,
                requested_by,
                checked_date,
                checked_by,
                approved_date,
                approved_by,
                remark,
                is_approved
            FROM mr
            WHERE requesting_project_code = ?
            ORDER BY requested_date DESC, mr_code DESC
            LIMIT ? OFFSET ?
            """;

        List<MR> materialRequests = jdbcTemplate.query(
                sql,
                mrRowMapper,
                requestingProjectCode,
                size,
                page * size
        );

        for (MR mr : materialRequests) {
            mr.setItems(findItemsByMrId(mr.getMrId()));
        }

        return materialRequests;
    }

    public long countByRequestingProjectCode(String requestingProjectCode) {

        String sql = """
            SELECT COUNT(*)
            FROM mr
            WHERE requesting_project_code = ?
            """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                requestingProjectCode
        );

        return count == null ? 0 : count;
    }

    public List<MR> findAll(int page, int size) {

        String sql = """
            SELECT
                mr_id,
                mr_code,
                requesting_project_code,
                destination_project_code,
                requested_date,
                requested_by,
                checked_date,
                checked_by,
                approved_date,
                approved_by,
                remark,
                is_approved
            FROM mr
            WHERE is_approved = TRUE
            ORDER BY requested_date DESC, mr_code DESC
            LIMIT ? OFFSET ?
            """;

        List<MR> materialRequests = jdbcTemplate.query(
                sql,
                mrRowMapper,
                size,
                page * size
        );

        for (MR mr : materialRequests) {
            mr.setItems(findItemsByMrId(mr.getMrId()));
        }

        return materialRequests;
    }

    public long countAll() {

        String sql = """
            SELECT COUNT(*)
            FROM mr
            WHERE is_approved = TRUE
            """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }

    // Every MR regardless of status, for HQ-wide reporting — unlike findAll()
    // above (approved-only, used to offer MRs as a quotation/PO source), reports
    // need pending/rejected MRs too.
    public List<MR> findAllAnyStatus(int page, int size) {

        String sql = """
            SELECT
                mr_id,
                mr_code,
                requesting_project_code,
                destination_project_code,
                requested_date,
                requested_by,
                checked_date,
                checked_by,
                approved_date,
                approved_by,
                remark,
                is_approved
            FROM mr
            ORDER BY requested_date DESC, mr_code DESC
            LIMIT ? OFFSET ?
            """;

        List<MR> materialRequests = jdbcTemplate.query(
                sql,
                mrRowMapper,
                size,
                page * size
        );

        for (MR mr : materialRequests) {
            mr.setItems(findItemsByMrId(mr.getMrId()));
        }

        return materialRequests;
    }

    public long countAllAnyStatus() {

        String sql = """
            SELECT COUNT(*)
            FROM mr
            """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }
}