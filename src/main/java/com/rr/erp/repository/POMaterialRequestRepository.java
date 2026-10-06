package com.rr.erp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class POMaterialRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public int link(UUID poId, UUID mrId, String linkedBy) {

        String sql = """
                INSERT INTO po_material_request (po_id, mr_id, linked_by)
                VALUES (?, ?, ?)
                ON CONFLICT (po_id, mr_id) DO NOTHING
                """;

        return jdbcTemplate.update(sql, poId, mrId, linkedBy);
    }

    public List<UUID> findMrIdsByPo(UUID poId) {

        String sql = """
                SELECT mr_id
                FROM po_material_request
                WHERE po_id = ?
                ORDER BY linked_date
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getObject("mr_id", UUID.class),
                poId
        );
    }

    // Batched equivalent of calling findMrIdsByPo() once per PO — one query for a whole
    // page of POs instead of one query per PO.
    public Map<UUID, List<UUID>> findMrIdsByPoIds(List<UUID> poIds) {

        if (poIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = poIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT po_id, mr_id FROM po_material_request WHERE po_id IN (" + placeholders + ") ORDER BY linked_date";

        Map<UUID, List<UUID>> mrIdsByPoId = new HashMap<>();

        jdbcTemplate.query(
                sql,
                rs -> {
                    UUID poId = rs.getObject("po_id", UUID.class);
                    UUID mrId = rs.getObject("mr_id", UUID.class);
                    mrIdsByPoId.computeIfAbsent(poId, key -> new ArrayList<>()).add(mrId);
                },
                poIds.toArray()
        );

        return mrIdsByPoId;
    }

    /**
     * Replaces this PO's full set of related-MR links with mrIds — the PO form submits
     * its whole "Related material requests" selection at once on every save. A null
     * mrIds leaves existing links untouched (distinguishes "field not sent" from "clear
     * all links", which a client sends as an empty list).
     */
    public void replaceLinks(UUID poId, List<UUID> mrIds, String linkedBy) {

        if (mrIds == null) {
            return;
        }

        jdbcTemplate.update(
                "DELETE FROM po_material_request WHERE po_id = ?",
                poId
        );

        for (UUID mrId : mrIds) {
            link(poId, mrId, linkedBy);
        }
    }
}
