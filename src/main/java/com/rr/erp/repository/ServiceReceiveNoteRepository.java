package com.rr.erp.repository;

import com.rr.erp.entity.ServiceReceiveNote;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceReceiveNoteRepository {

    private final JdbcTemplate jdbcTemplate;


    // =========================================================
    // CREATE
    // =========================================================
    public int createServiceReceiveNote(ServiceReceiveNote note) {

        String sql = """
                INSERT INTO service_receive_note (
                    service_receive_note_id,
                    service_receive_note_code,
                    job_card_id,
                    job_card_code,
                    requesting_project_code,
                    service_request_id,
                    asset_code,
                    item_code,
                    quantity,
                    received_date,
                    received_by,
                    remarks,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                note.getServiceReceiveNoteId(),
                note.getServiceReceiveNoteCode(),
                note.getJobCardId(),
                note.getJobCardCode(),
                note.getRequestingProjectCode(),
                note.getServiceRequestId(),
                note.getAssetCode(),
                note.getItemCode(),
                note.getQuantity(),
                note.getReceivedDate(),
                note.getReceivedBy(),
                note.getRemarks(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }


    // Every code ever issued (not just for one project) — service_receive_note_code is
    // globally unique, prefixed with the requesting project code, mirroring the
    // FuelIssueRepository.getFuelIssueCodesForProject scan-and-retry pattern.
    public List<String> getCodesForProject(String requestingProjectCode) {

        String sql = """
                SELECT service_receive_note_code
                FROM service_receive_note
                WHERE requesting_project_code = ?
                """;

        return jdbcTemplate.queryForList(sql, String.class, requestingProjectCode);
    }


    // =========================================================
    // GET ALL
    // =========================================================
    public List<ServiceReceiveNote> getAll() {

        String sql = """
                SELECT * FROM service_receive_note
                ORDER BY received_date DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapServiceReceiveNote(rs));
    }


    // =========================================================
    // GET BY REQUESTING PROJECT
    // =========================================================
    public List<ServiceReceiveNote> getByRequestingProjectCode(String requestingProjectCode) {

        String sql = """
                SELECT * FROM service_receive_note
                WHERE requesting_project_code = ?
                ORDER BY received_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapServiceReceiveNote(rs),
                requestingProjectCode
        );
    }


    // =========================================================
    // GET BY JOB CARD
    // =========================================================
    public Optional<ServiceReceiveNote> getByJobCardId(UUID jobCardId) {

        String sql = """
                SELECT * FROM service_receive_note
                WHERE job_card_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapServiceReceiveNote(rs),
                jobCardId
        ).stream().findFirst();
    }


    // =========================================================
    // GET BY ID
    // =========================================================
    public Optional<ServiceReceiveNote> getById(UUID serviceReceiveNoteId) {

        String sql = """
                SELECT * FROM service_receive_note
                WHERE service_receive_note_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapServiceReceiveNote(rs),
                serviceReceiveNoteId
        ).stream().findFirst();
    }


    // =========================================================
    // MAPPER
    // =========================================================
    private ServiceReceiveNote mapServiceReceiveNote(ResultSet rs) throws SQLException {

        ServiceReceiveNote note = new ServiceReceiveNote();

        note.setServiceReceiveNoteId(rs.getObject("service_receive_note_id", UUID.class));
        note.setServiceReceiveNoteCode(rs.getString("service_receive_note_code"));
        note.setJobCardId(rs.getObject("job_card_id", UUID.class));
        note.setJobCardCode(rs.getString("job_card_code"));
        note.setRequestingProjectCode(rs.getString("requesting_project_code"));
        note.setServiceRequestId(rs.getObject("service_request_id", UUID.class));
        note.setAssetCode(rs.getString("asset_code"));
        note.setItemCode(rs.getString("item_code"));
        note.setQuantity(rs.getBigDecimal("quantity"));
        note.setReceivedDate(rs.getObject("received_date", java.time.LocalDateTime.class));
        note.setReceivedBy(rs.getString("received_by"));
        note.setRemarks(rs.getString("remarks"));
        note.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        note.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));

        return note;
    }
}
