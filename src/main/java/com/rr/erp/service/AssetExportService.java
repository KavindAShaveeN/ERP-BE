package com.rr.erp.service;

import com.rr.erp.dto.AssetExportRequest;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class AssetExportService {

    /** One asset as read for export. Register fields (Reg No, Make, ...) are not separate
     * columns in the database — they live in the remarks text as "Label: value | ..." — so
     * they are parsed out of it on demand. */
    private static class ExportRow {
        String assetCode, typeCode, typeName, categoryName, assetClass, description, serialNumber,
                projectOrDepartment, ownershipType, status, condition, remarks, documentName;
        Timestamp createdAt, updatedAt;
        private Map<String, String> remarkFields;

        String remarkField(String label) {
            if (remarkFields == null) {
                remarkFields = new LinkedHashMap<>();
                if (remarks != null) {
                    for (String part : remarks.split("\\s\\|\\s")) {
                        int colon = part.indexOf(':');
                        if (colon > 0) {
                            remarkFields.put(part.substring(0, colon).trim(), part.substring(colon + 1).trim());
                        }
                    }
                }
            }
            return remarkFields.getOrDefault(label, "");
        }
    }

    public record ColumnInfo(String key, String header, String group, boolean defaultSelected) {
    }

    private record ColumnDef(ColumnInfo info, Function<ExportRow, Object> value) {
    }

    private static final Map<String, ColumnDef> COLUMNS = new LinkedHashMap<>();

    private static void column(String key, String header, String group, boolean defaultSelected, Function<ExportRow, Object> value) {
        COLUMNS.put(key, new ColumnDef(new ColumnInfo(key, header, group, defaultSelected), value));
    }

    static {
        column("assetCode", "Asset code", "Asset", true, r -> r.assetCode);
        column("typeCode", "Type code", "Asset", false, r -> r.typeCode);
        column("typeName", "Type", "Asset", false, r -> r.typeName);
        column("categoryName", "Category", "Asset", false, r -> r.categoryName);
        column("assetClass", "Asset class", "Asset", true, r -> r.assetClass);
        column("description", "Description", "Asset", true, r -> r.description);
        column("serialNumber", "Serial number", "Asset", true, r -> r.serialNumber);
        column("status", "Status", "Asset", true, r -> r.status);
        column("condition", "Condition", "Asset", false, r -> r.condition);
        column("ownershipType", "Ownership", "Asset", false, r -> r.ownershipType);
        column("projectOrDepartment", "Project / department / location", "Asset", false, r -> r.projectOrDepartment);
        column("documentName", "Document", "Asset", false, r -> r.documentName);
        column("remarks", "Remarks", "Asset", false, r -> r.remarks);
        column("createdAt", "Created", "Asset", false, r -> r.createdAt);
        column("updatedAt", "Last updated", "Asset", false, r -> r.updatedAt);

        column("regNo", "Reg No", "Register fields", false, r -> r.remarkField("Reg No"));
        column("make", "Make", "Register fields", false, r -> r.remarkField("Make"));
        column("model", "Model", "Register fields", false, r -> r.remarkField("Model"));
        column("chasisNo", "Chasis No", "Register fields", false, r -> r.remarkField("Chasis No"));
        column("machineSn", "Machine SN", "Register fields", false, r -> r.remarkField("Machine SN"));
        column("engineSn", "Engine SN", "Register fields", false, r -> r.remarkField("Engine SN"));
        column("capacity", "Capacity", "Register fields", false, r -> r.remarkField("Capacity"));
        column("location", "Location", "Register fields", false,
                r -> r.projectOrDepartment != null && !r.projectOrDepartment.isBlank()
                        ? r.projectOrDepartment : r.remarkField("Location"));
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssetExportService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ColumnInfo> getColumns() {
        List<ColumnInfo> infos = new ArrayList<>();
        COLUMNS.values().forEach(def -> infos.add(def.info()));
        return infos;
    }

    public byte[] export(AssetExportRequest request) {

        List<ColumnDef> selected = new ArrayList<>();
        List<String> requested = request.getColumns();
        if (requested == null || requested.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one column to export");
        }
        for (String key : requested) {
            ColumnDef def = COLUMNS.get(key);
            if (def == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown export column: " + key);
            }
            selected.add(def);
        }

        List<ExportRow> rows = loadRows(request);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Assets");

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(bold);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            int[] widths = new int[selected.size()];

            Row header = sheet.createRow(0);
            for (int c = 0; c < selected.size(); c++) {
                Cell cell = header.createCell(c);
                String title = selected.get(c).info().header();
                cell.setCellValue(title);
                cell.setCellStyle(headerStyle);
                widths[c] = title.length();
            }

            int rowIndex = 1;
            for (ExportRow exportRow : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int c = 0; c < selected.size(); c++) {
                    Object value = selected.get(c).value().apply(exportRow);
                    if (value == null) {
                        continue;
                    }
                    Cell cell = row.createCell(c);
                    if (value instanceof Timestamp timestamp) {
                        cell.setCellValue(timestamp);
                        cell.setCellStyle(dateStyle);
                        widths[c] = Math.max(widths[c], 16);
                    } else {
                        String text = value.toString();
                        // Excel cells are capped at 32767 characters.
                        cell.setCellValue(text.length() > 32000 ? text.substring(0, 32000) : text);
                        widths[c] = Math.max(widths[c], Math.min(text.length(), 60));
                    }
                }
            }

            for (int c = 0; c < widths.length; c++) {
                sheet.setColumnWidth(c, Math.min(widths[c] + 2, 60) * 256);
            }
            sheet.createFreezePane(0, 1);
            if (!rows.isEmpty()) {
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, rows.size(), 0, selected.size() - 1));
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build the Excel file", exception);
        }
    }

    private List<ExportRow> loadRows(AssetExportRequest request) {

        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.asset_code, a.asset_class, a.description, a.serial_number, a.project_or_department,
                    a.ownership_type, a.status, a.condition, a.remarks, a.document_name,
                    a.created_at, a.updated_at,
                    s.item_subcategory_code AS type_code,
                    s.item_subcategory_name AS type_name,
                    c.item_category_name    AS category_name
                FROM asset a
                LEFT JOIN asset_code ac ON ac.asset_code_id = a.asset_code_id
                LEFT JOIN item_code ic ON ic.item_code_id = ac.item_code_id
                LEFT JOIN item_subcategory s ON s.item_subcategory_id = ic.item_subcategory_id
                LEFT JOIN item_category c ON c.item_category_id = ic.item_category_id
                """);

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        List<String> codes = request.getAssetCodes();
        if (codes != null && !codes.isEmpty()) {
            sql.append("WHERE a.asset_code IN (:codes)\n");
            parameters.addValue("codes", codes);
        } else {
            sql.append("""
                    WHERE (CAST(:assetClass AS VARCHAR) IS NULL OR a.asset_class = CAST(:assetClass AS VARCHAR))
                      AND (CAST(:status AS VARCHAR) IS NULL OR a.status = CAST(:status AS VARCHAR))
                      AND (CAST(:search AS VARCHAR) IS NULL
                           OR a.asset_code ILIKE CAST(:search AS VARCHAR) ESCAPE '\\'
                           OR a.description ILIKE CAST(:search AS VARCHAR) ESCAPE '\\'
                           OR a.serial_number ILIKE CAST(:search AS VARCHAR) ESCAPE '\\')
                    """);
            String search = request.getSearch();
            String pattern = null;
            if (search != null && !search.isBlank()) {
                pattern = "%" + search.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            }
            parameters.addValue("search", pattern)
                    .addValue("assetClass", blankToNull(request.getAssetClass()))
                    .addValue("status", blankToNull(request.getStatus()));
        }
        sql.append("ORDER BY a.asset_code");

        return jdbcTemplate.query(sql.toString(), parameters, (rs, rowNum) -> {
            ExportRow row = new ExportRow();
            row.assetCode = rs.getString("asset_code");
            row.assetClass = rs.getString("asset_class");
            row.description = rs.getString("description");
            row.serialNumber = rs.getString("serial_number");
            row.projectOrDepartment = rs.getString("project_or_department");
            row.ownershipType = rs.getString("ownership_type");
            row.status = rs.getString("status");
            row.condition = rs.getString("condition");
            row.remarks = rs.getString("remarks");
            row.documentName = rs.getString("document_name");
            row.createdAt = rs.getTimestamp("created_at");
            row.updatedAt = rs.getTimestamp("updated_at");
            row.typeCode = rs.getString("type_code");
            row.typeName = rs.getString("type_name");
            row.categoryName = rs.getString("category_name");
            return row;
        });
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
