package com.rr.erp.repository;

import com.rr.erp.entity.MeterReading;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class MeterReadingRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeterReadingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // =========================================================
    // POST - Create Meter Reading
    // =========================================================
    public int createMeterReading(MeterReading meterReading) {

        String sql = """
                INSERT INTO meter_reading (
                    meter_reading_id,
                    asset_code,
                    meter_type,
                    reading_date,
                    reading_value,
                    previous_reading,
                    usage_value,
                    remarks,
                    recorded_by,
                    submitted_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                meterReading.getMeterReadingId(),
                meterReading.getAssetCode(),
                meterReading.getMeterType(),
                meterReading.getReadingDate(),
                meterReading.getReadingValue(),
                meterReading.getPreviousReading(),
                meterReading.getUsageValue(),
                meterReading.getRemarks(),
                meterReading.getRecordedBy(),
                meterReading.getSubmittedAt()
        );
    }


    // =========================================================
    // GET - Meter Readings by RecordedBy
    // =========================================================
    public List<MeterReading> getMeterReadingsByRecordedBy(
            String recordedBy
    ) {

        String sql = """
                SELECT
                    meter_reading_id AS "meterReadingId",
                    asset_code AS "assetCode",
                    meter_type AS "meterType",
                    reading_date AS "readingDate",
                    reading_value AS "readingValue",
                    previous_reading AS "previousReading",
                    usage_value AS "usageValue",
                    remarks AS "remarks",
                    recorded_by AS "recordedBy",
                    submitted_at AS "submittedAt",
                    edited_at AS "editedAt"
                FROM meter_reading
                WHERE recorded_by = ?
                ORDER BY submitted_at DESC NULLS LAST, reading_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMeterReading(rs),
                recordedBy
        );
    }


    // =========================================================
    // GET - Meter Readings by Asset Code
    // =========================================================
    public List<MeterReading> getMeterReadingsByAssetCode(
            String assetCode
    ) {

        String sql = """
                SELECT
                    meter_reading_id AS "meterReadingId",
                    asset_code AS "assetCode",
                    meter_type AS "meterType",
                    reading_date AS "readingDate",
                    reading_value AS "readingValue",
                    previous_reading AS "previousReading",
                    usage_value AS "usageValue",
                    remarks AS "remarks",
                    recorded_by AS "recordedBy",
                    submitted_at AS "submittedAt",
                    edited_at AS "editedAt"
                FROM meter_reading
                WHERE asset_code = ?
                ORDER BY submitted_at DESC NULLS LAST, reading_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMeterReading(rs),
                assetCode
        );
    }


    // =========================================================
    // GET - Latest Meter Reading for an Asset
    // Source of truth for "previous reading" when creating/updating a
    // reading — ordered by submitted_at (actual submission time), not
    // reading_date, so same-day entries resolve to the one actually
    // entered most recently rather than the first one on that date.
    // excludeMeterReadingId lets an update look up the latest *other*
    // reading for the asset (pass null when creating).
    // =========================================================
    public MeterReading getLatestMeterReadingForAsset(
            String assetCode,
            UUID excludeMeterReadingId
    ) {

        String sql = """
                SELECT
                    meter_reading_id AS "meterReadingId",
                    asset_code AS "assetCode",
                    meter_type AS "meterType",
                    reading_date AS "readingDate",
                    reading_value AS "readingValue",
                    previous_reading AS "previousReading",
                    usage_value AS "usageValue",
                    remarks AS "remarks",
                    recorded_by AS "recordedBy",
                    submitted_at AS "submittedAt",
                    edited_at AS "editedAt"
                FROM meter_reading
                WHERE asset_code = ?
                  AND (?::uuid IS NULL OR meter_reading_id <> ?::uuid)
                ORDER BY submitted_at DESC NULLS LAST, reading_date DESC
                LIMIT 1
                """;

        List<MeterReading> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMeterReading(rs),
                assetCode,
                excludeMeterReadingId,
                excludeMeterReadingId
        );

        return results.isEmpty() ? null : results.get(0);
    }


    // =========================================================
    // GET - All Meter Readings
    // =========================================================
    public List<MeterReading> getAllMeterReadings() {

        String sql = """
                SELECT
                    meter_reading_id AS "meterReadingId",
                    asset_code AS "assetCode",
                    meter_type AS "meterType",
                    reading_date AS "readingDate",
                    reading_value AS "readingValue",
                    previous_reading AS "previousReading",
                    usage_value AS "usageValue",
                    remarks AS "remarks",
                    recorded_by AS "recordedBy",
                    submitted_at AS "submittedAt",
                    edited_at AS "editedAt"
                FROM meter_reading
                ORDER BY submitted_at DESC NULLS LAST, reading_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMeterReading(rs)
        );
    }


    // =========================================================
    // GET - Meter Reading by ID
    // Used internally after create/update
    // =========================================================
    public MeterReading getMeterReadingById(UUID meterReadingId) {

        String sql = """
                SELECT
                    meter_reading_id AS "meterReadingId",
                    asset_code AS "assetCode",
                    meter_type AS "meterType",
                    reading_date AS "readingDate",
                    reading_value AS "readingValue",
                    previous_reading AS "previousReading",
                    usage_value AS "usageValue",
                    remarks AS "remarks",
                    recorded_by AS "recordedBy",
                    submitted_at AS "submittedAt",
                    edited_at AS "editedAt"
                FROM meter_reading
                WHERE meter_reading_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> mapMeterReading(rs),
                meterReadingId
        );
    }


    // =========================================================
    // PUT - Update Meter Reading
    // recorded_by and submitted_at are never changed by an edit —
    // only the reading fields and edited_at move.
    // =========================================================
    public int updateMeterReading(
            UUID meterReadingId,
            MeterReading meterReading
    ) {

        String sql = """
                UPDATE meter_reading
                SET
                    asset_code = ?,
                    meter_type = ?,
                    reading_date = ?,
                    reading_value = ?,
                    previous_reading = ?,
                    usage_value = ?,
                    remarks = ?,
                    edited_at = ?
                WHERE meter_reading_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                meterReading.getAssetCode(),
                meterReading.getMeterType(),
                meterReading.getReadingDate(),
                meterReading.getReadingValue(),
                meterReading.getPreviousReading(),
                meterReading.getUsageValue(),
                meterReading.getRemarks(),
                meterReading.getEditedAt(),
                meterReadingId
        );
    }


    // =========================================================
    // Common Row Mapper
    // =========================================================
    private MeterReading mapMeterReading(
            ResultSet rs
    ) throws SQLException {

        MeterReading meterReading = new MeterReading();

        meterReading.setMeterReadingId(
                rs.getObject("meterReadingId", UUID.class)
        );

        meterReading.setAssetCode(
                rs.getObject("assetCode", String.class)
        );

        meterReading.setMeterType(
                rs.getString("meterType")
        );

        if (rs.getDate("readingDate") != null) {
            meterReading.setReadingDate(
                    rs.getDate("readingDate").toLocalDate()
            );
        }

        meterReading.setReadingValue(
                rs.getBigDecimal("readingValue")
        );

        meterReading.setPreviousReading(
                rs.getBigDecimal("previousReading")
        );

        meterReading.setUsageValue(
                rs.getBigDecimal("usageValue")
        );

        meterReading.setRemarks(
                rs.getString("remarks")
        );

        meterReading.setRecordedBy(
                rs.getString("recordedBy")
        );

        meterReading.setSubmittedAt(
                rs.getObject("submittedAt", java.time.LocalDateTime.class)
        );

        meterReading.setEditedAt(
                rs.getObject("editedAt", java.time.LocalDateTime.class)
        );

        return meterReading;
    }
}
