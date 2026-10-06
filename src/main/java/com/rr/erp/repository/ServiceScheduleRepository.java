package com.rr.erp.repository;

import com.rr.erp.dto.ServiceScheduleDtos.DueItemDto;
import com.rr.erp.dto.ServiceScheduleDtos.PartDto;
import com.rr.erp.dto.ServiceScheduleDtos.ServiceDto;
import com.rr.erp.dto.ServiceScheduleDtos.TemplateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ServiceScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // TEMPLATES
    // =========================================================
    public List<TemplateDto> getTemplates() {

        Map<UUID, List<PartDto>> partsByService = loadPartsByService();

        Map<UUID, List<ServiceDto>> servicesByTemplate = new HashMap<>();
        jdbcTemplate.query("""
                SELECT service_id, template_id, name, at_value, remarks, sort_order
                FROM service_schedule_service
                ORDER BY at_value, sort_order
                """, rs -> {
            UUID serviceId = rs.getObject("service_id", UUID.class);
            servicesByTemplate.computeIfAbsent(rs.getObject("template_id", UUID.class), k -> new ArrayList<>())
                    .add(new ServiceDto(serviceId, rs.getString("name"), rs.getBigDecimal("at_value"),
                            rs.getString("remarks"), rs.getInt("sort_order"),
                            partsByService.getOrDefault(serviceId, List.of())));
        });

        return jdbcTemplate.query("""
                SELECT template_id, asset_type_code, name, meter_unit, cycle_length, is_active
                FROM service_schedule_template
                ORDER BY asset_type_code, meter_unit
                """, (rs, i) -> {
            UUID id = rs.getObject("template_id", UUID.class);
            return new TemplateDto(id, rs.getString("asset_type_code"), rs.getString("name"),
                    rs.getString("meter_unit"), rs.getBigDecimal("cycle_length"), rs.getBoolean("is_active"),
                    servicesByTemplate.getOrDefault(id, List.of()));
        });
    }

    private Map<UUID, List<PartDto>> loadPartsByService() {
        Map<UUID, List<PartDto>> parts = new HashMap<>();
        jdbcTemplate.query("""
                SELECT part_id, service_id, item_code, part_name, quantity, unit
                FROM service_schedule_service_part
                ORDER BY part_name
                """, rs -> {
            parts.computeIfAbsent(rs.getObject("service_id", UUID.class), k -> new ArrayList<>())
                    .add(new PartDto(rs.getObject("part_id", UUID.class), rs.getString("item_code"),
                            rs.getString("part_name"), rs.getBigDecimal("quantity"), rs.getString("unit")));
        });
        return parts;
    }

    @Transactional
    public void insertTemplate(TemplateDto t, UUID id) {
        jdbcTemplate.update("""
                INSERT INTO service_schedule_template (template_id, asset_type_code, name, meter_unit, cycle_length, is_active)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, t.assetTypeCode().toUpperCase(), t.name(), t.meterUnit(), t.cycleLength(),
                t.isActive() == null || t.isActive());
        int order = 0;
        for (ServiceDto service : t.services()) {
            insertService(id, service, order++);
        }
    }

    @Transactional
    public int updateTemplate(UUID id, TemplateDto t) {
        int n = jdbcTemplate.update("""
                UPDATE service_schedule_template
                SET asset_type_code = ?, name = ?, meter_unit = ?, cycle_length = ?, is_active = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE template_id = ?
                """, t.assetTypeCode().toUpperCase(), t.name(), t.meterUnit(), t.cycleLength(),
                t.isActive() == null || t.isActive(), id);
        if (n == 0) return 0;

        // Services are upserted by id (not deleted and re-inserted) so the history recorded in
        // job_card_service survives edits; only services dropped from the request are removed.
        List<UUID> keep = t.services().stream().map(ServiceDto::serviceId).filter(x -> x != null).toList();
        for (UUID existing : jdbcTemplate.queryForList(
                "SELECT service_id FROM service_schedule_service WHERE template_id = ?", UUID.class, id)) {
            if (!keep.contains(existing)) {
                jdbcTemplate.update("DELETE FROM service_schedule_service WHERE service_id = ?", existing);
            }
        }

        int order = 0;
        for (ServiceDto service : t.services()) {
            if (service.serviceId() != null) {
                jdbcTemplate.update("""
                        UPDATE service_schedule_service
                        SET name = ?, at_value = ?, remarks = ?, sort_order = ?
                        WHERE service_id = ? AND template_id = ?
                        """, service.name(), service.atValue(), service.remarks(), order,
                        service.serviceId(), id);
                jdbcTemplate.update("DELETE FROM service_schedule_service_part WHERE service_id = ?",
                        service.serviceId());
                insertParts(service.serviceId(), service.parts());
            } else {
                insertService(id, service, order);
            }
            order++;
        }
        return n;
    }

    private void insertService(UUID templateId, ServiceDto service, int sortOrder) {
        UUID serviceId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO service_schedule_service (service_id, template_id, name, at_value, remarks, sort_order)
                VALUES (?, ?, ?, ?, ?, ?)
                """, serviceId, templateId, service.name(), service.atValue(), service.remarks(), sortOrder);
        insertParts(serviceId, service.parts());
    }

    private void insertParts(UUID serviceId, List<PartDto> parts) {
        if (parts == null) return;
        for (PartDto part : parts) {
            jdbcTemplate.update("""
                    INSERT INTO service_schedule_service_part (part_id, service_id, item_code, part_name, quantity, unit)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, UUID.randomUUID(), serviceId, part.itemCode(), part.partName(),
                    part.quantity() == null ? BigDecimal.ONE : part.quantity(), part.unit());
        }
    }

    public int deleteTemplate(UUID id) {
        return jdbcTemplate.update("DELETE FROM service_schedule_template WHERE template_id = ?", id);
    }

    // =========================================================
    // COMPLETED SERVICES ON A JOB CARD
    // =========================================================
    @Transactional
    public void replaceJobCardServices(UUID jobCardId, List<UUID> serviceIds) {
        jdbcTemplate.update("DELETE FROM job_card_service WHERE job_card_id = ?", jobCardId);
        for (UUID serviceId : serviceIds) {
            jdbcTemplate.update("INSERT INTO job_card_service (job_card_id, service_id) VALUES (?, ?)",
                    jobCardId, serviceId);
        }
    }

    public List<UUID> getJobCardServiceIds(UUID jobCardId) {
        return jdbcTemplate.queryForList(
                "SELECT service_id FROM job_card_service WHERE job_card_id = ?", UUID.class, jobCardId);
    }

    // =========================================================
    // DUE ITEMS (template service x asset, with latest meter + last done)
    // =========================================================
    public List<DueItemDto> getDueItems(String assetCode, String projectCode) {

        Map<UUID, List<PartDto>> partsByService = loadPartsByService();

        String sql = """
                SELECT a.asset_code,
                       loc.new_location AS project_code,
                       t.asset_type_code, t.template_id, t.meter_unit, t.cycle_length,
                       s.service_id, s.name, s.at_value,
                       (SELECT m.reading_value FROM meter_reading m
                         WHERE m.asset_code = a.asset_code
                         ORDER BY m.submitted_at DESC NULLS LAST, m.reading_date DESC LIMIT 1) AS current_reading,
                       (SELECT MAX(jc.meter_reading) FROM job_card_service j
                          JOIN job_card jc ON jc.job_card_id = j.job_card_id
                         WHERE j.service_id = s.service_id AND jc.asset_code = a.asset_code
                           AND jc.is_finished = TRUE AND jc.meter_reading IS NOT NULL) AS last_done_reading,
                       (SELECT MAX(jc.updated_at) FROM job_card_service j
                          JOIN job_card jc ON jc.job_card_id = j.job_card_id
                         WHERE j.service_id = s.service_id AND jc.asset_code = a.asset_code
                           AND jc.is_finished = TRUE) AS last_done_date
                FROM asset a
                LEFT JOIN LATERAL (
                    SELECT l.new_location FROM asset_location l
                     WHERE l.asset_code = a.asset_code AND l.is_active = TRUE
                     ORDER BY l.changed_date DESC, l.created_at DESC LIMIT 1
                ) loc ON TRUE
                JOIN service_schedule_template t
                  ON t.asset_type_code = split_part(a.asset_code, '-', 1) AND t.is_active = TRUE
                JOIN service_schedule_service s ON s.template_id = t.template_id
                WHERE (?::text IS NULL OR a.asset_code = ?)
                  AND (?::text IS NULL OR loc.new_location = ?)
                ORDER BY a.asset_code, s.at_value
                """;

        List<DueItemDto> items = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            BigDecimal at = rs.getBigDecimal("at_value");
            BigDecimal cycle = rs.getBigDecimal("cycle_length");
            BigDecimal current = rs.getBigDecimal("current_reading");
            BigDecimal lastDone = rs.getBigDecimal("last_done_reading");
            Timestamp doneTs = rs.getTimestamp("last_done_date");

            BigDecimal nextDue;
            BigDecimal windowStart;
            if (lastDone == null) {
                nextDue = at;
                windowStart = BigDecimal.ZERO;
            } else if (cycle == null) {
                return; // a one-off service that has already been done
            } else {
                // The occurrence this job card fulfilled is the one nearest its reading, so a
                // service done a little early or late still counts for its own slot.
                BigDecimal k = lastDone.subtract(at).divide(cycle, 0, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
                nextDue = at.add(k.add(BigDecimal.ONE).multiply(cycle));
                windowStart = lastDone;
            }

            BigDecimal remaining = current == null ? null : nextDue.subtract(current);
            BigDecimal window = nextDue.subtract(windowStart);
            String status;
            if (remaining == null) status = "NO_READING";
            else if (remaining.signum() < 0) status = "OVERDUE";
            else if (remaining.compareTo(window.multiply(new BigDecimal("0.10"))) <= 0) status = "DUE_SOON";
            else status = "OK";

            UUID serviceId = rs.getObject("service_id", UUID.class);
            items.add(new DueItemDto(rs.getString("asset_code"), rs.getString("project_code"),
                    rs.getString("asset_type_code"), rs.getObject("template_id", UUID.class),
                    rs.getString("meter_unit"), serviceId, rs.getString("name"), current, lastDone,
                    doneTs == null ? null : doneTs.toLocalDateTime(), windowStart, nextDue, remaining,
                    status, partsByService.getOrDefault(serviceId, List.of())));
        }, assetCode, assetCode, projectCode, projectCode);
        return items;
    }
}
