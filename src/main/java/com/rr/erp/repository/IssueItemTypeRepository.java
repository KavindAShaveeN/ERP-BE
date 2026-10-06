package com.rr.erp.repository;

import com.rr.erp.entity.IssueItemType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IssueItemTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public IssueItemTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<IssueItemType> getAllIssueItemTypes() {

        String sql = """
                SELECT
                    issue_item_type_id AS "issueItemTypeId",
                    issue_item_type_name AS "issueItemTypeName"
                FROM issue_item_type
                ORDER BY issue_item_type_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    IssueItemType issueItemType = new IssueItemType();

                    issueItemType.setIssueItemTypeId(
                            rs.getInt("issueItemTypeId")
                    );

                    issueItemType.setIssueItemTypeName(
                            rs.getString("issueItemTypeName")
                    );

                    return issueItemType;
                }
        );
    }
}