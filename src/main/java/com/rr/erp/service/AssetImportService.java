package com.rr.erp.service;

import com.rr.erp.dto.AssetImportResult;
import com.rr.erp.dto.AssetImportResult.RowIssue;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Imports assets from an Excel register laid out like the company's "WMS Total Assets"
 * workbook: RRC No, Reg No, Make, Class of Vehicle, Model, Chasis No, Machine SN, Engine SN,
 * Capacity, Location. Each row becomes an asset_code + asset, keeping the register's own code. */
@Service
public class AssetImportService {

    private static final int MAX_CODE_LENGTH = 20;
    private static final Pattern WRITE_OFF_PREFIX = Pattern.compile("^(WOI|WO|W0)-", Pattern.CASE_INSENSITIVE);
    private static final Pattern HYPHEN_SPACES = Pattern.compile("\\s*-\\s*");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private static final Map<String, String> CLASS_BY_TYPE = new HashMap<>();

    private static void mapClass(String assetClass, String typeCodes) {
        for (String code : typeCodes.split(" ")) {
            CLASS_BY_TYPE.put(code, assetClass);
        }
    }

    static {
        mapClass("Vehicle", "AT BB BT DT HB HT LB MB PM PT SV TB TL TM TT WB CB DB HV SV-NR");
        mapClass("Machinery", "AC AP BL CC CM CNP CP CR CS DC DP DR EX FL LM MC MG MP PC PH PR PV RB RS SL TD TR VR WL WP BD WP-SP");
        mapClass("PlantEquipment", "AM BP BW CW FT SM SWP");
        mapClass("ElectricalEquipment", "EC LG LT PG SP VM WE WG WT");
        mapClass("PowerTool", "BC CSJ EP HJ JH TAG TAH TBD TCD TCO TCS TDIG TED TEP THB THD THW TIW TJS TMD TPG TSM");
        mapClass("LabEquipment", "TAM TCM TDG TDT THT TIT TMM TRS TTD TVC TWSD TDM THM TT-MM");
        mapClass("ITEquipment", "FD GS OEDC");
        mapClass("Other", "AA BA BG CN ST TTM WR LS-SF");
    }

    /** The asset class (which detail table / screen an asset belongs to) for a register type code. */
    static String assetClassFor(String typeCode) {
        String mapped = CLASS_BY_TYPE.get(typeCode);
        if (mapped != null) return mapped;
        if (typeCode.startsWith("OE-")) return "ITEquipment";
        if (typeCode.startsWith("LI-")) return "LabEquipment";
        if (typeCode.startsWith("SI-")) return "SurveyInstrument";
        if (typeCode.startsWith("HH-")) return "Other";
        if (typeCode.startsWith("LS-")) return "Machinery";
        return "Other";
    }

    private record TypeInfo(long categoryId, long subCategoryId, String name) {
    }

    private static class Candidate {
        int sheetRow;
        String rawCode;
        String code;
        String typeCode;
        boolean writtenOff;
        String regNo, make, classOfVehicle, model, chasisNo, machineSn, engineSn, capacity, location;
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssetImportService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public AssetImportResult importRegister(MultipartFile file, boolean dryRun) {

        AssetImportResult result = new AssetImportResult();
        result.setDryRun(dryRun);

        List<Candidate> candidates = readCandidates(file, result);
        result.setTotalRows(candidates.size());

        Map<String, TypeInfo> types = loadTypes();
        Set<String> existingCodes = new HashSet<>(
                jdbcTemplate.queryForList("SELECT asset_code_code FROM asset_code", new MapSqlParameterSource(), String.class));
        Set<String> existingAssets = new HashSet<>(
                jdbcTemplate.queryForList("SELECT asset_code FROM asset", new MapSqlParameterSource(), String.class));

        Map<String, Candidate> seenInFile = new HashMap<>();
        List<SqlParameterSource> codeInserts = new ArrayList<>();
        List<SqlParameterSource> assetInserts = new ArrayList<>();

        for (Candidate candidate : candidates) {

            String finalCode = candidate.code;
            Candidate first = seenInFile.get(finalCode);
            if (first != null) {
                // Same code twice in the file. A written-off asset keeps its register code (e.g. WO-LG-07)
                // when a live asset already took the plain one; otherwise the repeat gets a -DUP suffix.
                finalCode = candidate.writtenOff && !first.writtenOff
                        ? "WO-" + candidate.code
                        : candidate.code + "-DUP";
                result.getWarnings().add(new RowIssue(candidate.sheetRow, candidate.code,
                        "Code appears more than once in the file — imported as " + finalCode));
            }

            if (finalCode.length() > MAX_CODE_LENGTH) {
                fail(result, candidate, "Asset code is longer than " + MAX_CODE_LENGTH + " characters");
                continue;
            }

            if (existingAssets.contains(finalCode)) {
                result.setSkippedExisting(result.getSkippedExisting() + 1);
                if (first == null) {
                    seenInFile.put(candidate.code, candidate);
                }
                continue;
            }

            String typeCode = candidate.typeCode;
            TypeInfo type = types.get(typeCode);
            if (type == null) {
                // e.g. SV-NR-01 with no SV-NR type: fall back to the parent type (SV).
                String parent = typeCode;
                while (type == null && parent.contains("-")) {
                    parent = parent.substring(0, parent.lastIndexOf('-'));
                    type = types.get(parent);
                }
                if (type == null) {
                    fail(result, candidate, "Unknown asset type '" + typeCode + "' — add it as an asset sub category first");
                    continue;
                }
                result.getWarnings().add(new RowIssue(candidate.sheetRow, finalCode,
                        "Type '" + typeCode + "' not found — filed under '" + parent + "'"));
            }

            String assetClass = assetClassFor(typeCode);
            String description = firstNonBlank(candidate.classOfVehicle, type.name());
            String serial = firstNonBlank(candidate.machineSn, candidate.engineSn, candidate.chasisNo);
            String remarks = buildRemarks(candidate, finalCode);
            String location = "WRITE-OFF".equalsIgnoreCase(candidate.location) ? "" : candidate.location;

            if (!existingCodes.contains(finalCode)) {
                codeInserts.add(new MapSqlParameterSource()
                        .addValue("categoryId", type.categoryId())
                        .addValue("subCategoryId", type.subCategoryId())
                        .addValue("code", finalCode));
                existingCodes.add(finalCode);
            }

            assetInserts.add(new MapSqlParameterSource()
                    .addValue("code", finalCode)
                    .addValue("assetClass", assetClass)
                    .addValue("description", truncate(description, 255))
                    .addValue("serialNumber", emptyToNull(truncate(serial, 100)))
                    .addValue("department", emptyToNull(truncate(location, 150)))
                    .addValue("status", candidate.writtenOff ? "Disposed" : "Active")
                    .addValue("remarks", emptyToNull(truncate(remarks, 500))));

            existingAssets.add(finalCode);
            seenInFile.putIfAbsent(candidate.code, candidate);
            result.setCreated(result.getCreated() + 1);
            result.getCreatedByClass().merge(assetClass, 1, Integer::sum);
        }

        result.setFailed(result.getFailures().size());

        if (!dryRun) {
            if (!codeInserts.isEmpty()) {
                // asset_code no longer holds category / sub category: they come from its item_code,
                // so make sure every type used has one (same "CAT-SUB" convention as AssetCodeService).
                Set<Long> subCategoryIds = new HashSet<>();
                for (SqlParameterSource insert : codeInserts) {
                    subCategoryIds.add((Long) insert.getValue("subCategoryId"));
                }
                for (Long subCategoryId : subCategoryIds) {
                    jdbcTemplate.update("""
                            INSERT INTO item_code (item_code_code, item_code_name, item_category_id, item_subcategory_id)
                            SELECT c.item_category_code || '-' || s.item_subcategory_code,
                                   c.item_category_name || ',' || s.item_subcategory_name,
                                   c.item_category_id, s.item_subcategory_id
                            FROM item_subcategory s
                            JOIN item_category c ON c.item_category_id = s.item_category_id
                            WHERE s.item_subcategory_id = :subCategoryId
                              AND NOT EXISTS (
                                  SELECT 1 FROM item_code x
                                  WHERE x.item_code_code = c.item_category_code || '-' || s.item_subcategory_code)
                            """, new MapSqlParameterSource("subCategoryId", subCategoryId));
                }

                jdbcTemplate.batchUpdate("""
                        INSERT INTO asset_code (asset_code_code, item_code_id)
                        SELECT :code, ic.item_code_id
                        FROM item_subcategory s
                        JOIN item_category c ON c.item_category_id = s.item_category_id
                        JOIN item_code ic ON ic.item_code_code = c.item_category_code || '-' || s.item_subcategory_code
                        WHERE s.item_subcategory_id = :subCategoryId
                        """, codeInserts.toArray(new SqlParameterSource[0]));
            }
            if (!assetInserts.isEmpty()) {
                jdbcTemplate.batchUpdate("""
                        INSERT INTO asset (asset_code, asset_code_id, asset_class, description, serial_number,
                                           project_or_department, status, remarks, created_at, updated_at)
                        SELECT :code, ac.asset_code_id, :assetClass, :description, :serialNumber,
                               :department, :status, :remarks, now(), now()
                        FROM asset_code ac WHERE ac.asset_code_code = :code
                        """, assetInserts.toArray(new SqlParameterSource[0]));
            }
        }

        return result;
    }

    private List<Candidate> readCandidates(MultipartFile file, AssetImportResult result) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an Excel file to import");
        }

        List<Candidate> candidates = new ArrayList<>();
        DataFormatter formatter = new DataFormatter(Locale.ROOT);

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheet("All");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The sheet is empty");
            }

            Map<String, Integer> columns = new HashMap<>();
            for (Cell cell : header) {
                String title = formatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT);
                if (!title.isEmpty()) {
                    columns.putIfAbsent(title, cell.getColumnIndex());
                }
            }

            Integer codeColumn = columns.get("rrc no");
            if (codeColumn == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Column 'RRC No' not found. Expected columns: RRC No, Reg No, Make, Class of Vehicle, Model, "
                                + "Chasis No, Machine SN, Engine SN, Capacity, Location");
            }

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String raw = cellText(formatter, row, codeColumn);
                if (raw.isEmpty()) continue;

                Candidate candidate = new Candidate();
                candidate.sheetRow = r + 1;
                candidate.rawCode = raw;
                candidate.regNo = cellText(formatter, row, columns.get("reg no"));
                candidate.make = cellText(formatter, row, columns.get("make"));
                candidate.classOfVehicle = cellText(formatter, row, columns.get("class of vehicle"));
                candidate.model = cellText(formatter, row, columns.get("model"));
                candidate.chasisNo = cellText(formatter, row, columns.get("chasis no"));
                candidate.machineSn = cellText(formatter, row, columns.get("machine sn"));
                candidate.engineSn = cellText(formatter, row, columns.get("engine sn"));
                candidate.capacity = cellText(formatter, row, columns.get("capacity"));
                candidate.location = cellText(formatter, row, columns.get("location"));
                candidate.writtenOff = "WRITE-OFF".equalsIgnoreCase(candidate.location);

                String code = HYPHEN_SPACES.matcher(WHITESPACE.matcher(raw).replaceAll(" ").trim()).replaceAll("-");
                code = WRITE_OFF_PREFIX.matcher(code).replaceFirst("").toUpperCase(Locale.ROOT);

                int lastHyphen = code.lastIndexOf('-');
                if (lastHyphen <= 0 || lastHyphen == code.length() - 1) {
                    fail(result, sheetRow(r), raw, "Code is not in the form TYPE-NUMBER");
                    continue;
                }
                candidate.code = code;
                candidate.typeCode = code.substring(0, lastHyphen);
                candidates.add(candidate);
            }

        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ResponseStatusException status) {
                throw status;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not read the file as an Excel workbook: " + exception.getMessage());
        }

        return candidates;
    }

    private static int sheetRow(int zeroBasedRow) {
        return zeroBasedRow + 1;
    }

    private Map<String, TypeInfo> loadTypes() {
        Map<String, TypeInfo> types = new HashMap<>();
        jdbcTemplate.query("""
                SELECT s.item_subcategory_id, s.item_category_id, s.item_subcategory_code, s.item_subcategory_name
                FROM item_subcategory s
                JOIN item_category c ON c.item_category_id = s.item_category_id
                WHERE c.item_type_id = 1
                ORDER BY s.item_subcategory_id
                """, new MapSqlParameterSource(), rs -> {
            types.putIfAbsent(rs.getString("item_subcategory_code").trim().toUpperCase(Locale.ROOT),
                    new TypeInfo(rs.getLong("item_category_id"), rs.getLong("item_subcategory_id"),
                            rs.getString("item_subcategory_name")));
        });
        return types;
    }

    private static String buildRemarks(Candidate c, String finalCode) {
        List<String> parts = new ArrayList<>();
        addPart(parts, "Reg No", c.regNo);
        addPart(parts, "Make", c.make);
        addPart(parts, "Model", c.model);
        addPart(parts, "Chasis No", c.chasisNo);
        addPart(parts, "Machine SN", c.machineSn);
        addPart(parts, "Engine SN", c.engineSn);
        addPart(parts, "Capacity", c.capacity);
        addPart(parts, "Location", c.location);
        // The register's own spelling of the code (e.g. "AC - 05"), so an export can reproduce it.
        if (c.rawCode != null && !c.rawCode.equals(finalCode)) {
            addPart(parts, "Register code", c.rawCode);
        }
        return String.join(" | ", parts);
    }

    private static void addPart(List<String> parts, String label, String value) {
        if (value != null && !value.isEmpty() && !value.equals("--") && !value.equals("-")) {
            parts.add(label + ": " + value);
        }
    }

    private static String cellText(DataFormatter formatter, Row row, Integer column) {
        if (column == null) return "";
        Cell cell = row.getCell(column);
        if (cell == null) return "";
        String text = formatter.formatCellValue(cell);
        return WHITESPACE.matcher(text.replace(' ', ' ')).replaceAll(" ").trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty() && !value.equals("--") && !value.equals("-")) return value;
        }
        return "";
    }

    private static String truncate(String value, int max) {
        return value == null ? "" : value.length() > max ? value.substring(0, max) : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static void fail(AssetImportResult result, Candidate candidate, String message) {
        result.getFailures().add(new RowIssue(candidate.sheetRow, candidate.code, message));
    }

    private static void fail(AssetImportResult result, int row, String code, String message) {
        result.getFailures().add(new RowIssue(row, code, message));
    }
}
