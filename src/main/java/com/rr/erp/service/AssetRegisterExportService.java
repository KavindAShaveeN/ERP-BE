package com.rr.erp.service;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataBarFormatting;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Exports the whole asset registry as a workbook with the structure of the company's "WMS Total
 * Assets" register — a Master sheet (code / description / category / total), an All sheet with every
 * asset, one sheet per type code, and a WO sheet holding the written-off assets — in a modern look. */
@Service
public class AssetRegisterExportService {

    private static final String[] HEADERS = {
            "RRC No", "Reg No", "Make", "Class of Vehicle", "Model", "Chasis No", "Machine SN", "Engine SN", "Capacity", "Location"
    };
    private static final String WRITE_OFF_SHEET = "WO";
    private static final String BLANK = "--";
    private static final Pattern WRITE_OFF_PREFIX = Pattern.compile("^(WOI|WO|W0)-", Pattern.CASE_INSENSITIVE);
    private static final Pattern NATURAL_CHUNK = Pattern.compile("\\d+|\\D+");
    private static final Pattern INVALID_SHEET_CHARS = Pattern.compile("[\\[\\]:*?/\\\\]");

    private static final Comparator<String> NATURAL_ORDER = (a, b) -> {
        Matcher left = NATURAL_CHUNK.matcher(a);
        Matcher right = NATURAL_CHUNK.matcher(b);
        while (left.find() && right.find()) {
            String x = left.group();
            String y = right.group();
            int result = Character.isDigit(x.charAt(0)) && Character.isDigit(y.charAt(0))
                    ? Long.compare(parseLong(x), parseLong(y))
                    : x.compareToIgnoreCase(y);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(a.length(), b.length());
    };

    private static long parseLong(String digits) {
        return digits.length() > 18 ? Long.MAX_VALUE : Long.parseLong(digits);
    }

    private record RegisterRow(String sheet, String sortKey, String categoryName, String[] cells) {
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssetRegisterExportService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public byte[] export() {

        Map<String, List<RegisterRow>> rowsBySheet = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (RegisterRow row : loadRows()) {
            rowsBySheet.computeIfAbsent(row.sheet(), key -> new ArrayList<>()).add(row);
        }
        rowsBySheet.values().forEach(rows -> rows.sort(Comparator.comparing(RegisterRow::sortKey, NATURAL_ORDER)));

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Styles styles = new Styles(workbook);

            Map<String, String> sheetNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (String code : rowsBySheet.keySet()) {
                sheetNames.put(code, safeSheetName(code));
            }

            XSSFSheet master = workbook.createSheet("Master");
            XSSFSheet all = workbook.createSheet("All");
            Map<String, XSSFSheet> codeSheets = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<String, String> entry : sheetNames.entrySet()) {
                codeSheets.put(entry.getKey(), workbook.createSheet(entry.getValue()));
            }

            master.setTabColor(rgb(Styles.TAB_MASTER));
            all.setTabColor(rgb(Styles.TAB_ALL));

            writeRegisterSheet(all, styles, rowsBySheet.values().stream().flatMap(List::stream).toList(), false);
            for (Map.Entry<String, XSSFSheet> entry : codeSheets.entrySet()) {
                List<RegisterRow> rows = rowsBySheet.get(entry.getKey());
                writeRegisterSheet(entry.getValue(), styles, rows, true);
                entry.getValue().setTabColor(rgb(lookFor(categoryOf(entry.getKey(), rows)).tab()));
            }
            writeMaster(master, styles, rowsBySheet, sheetNames);

            workbook.setForceFormulaRecalculation(true);
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build the Excel file", exception);
        }
    }

    private static String categoryOf(String code, List<RegisterRow> rows) {
        if (WRITE_OFF_SHEET.equalsIgnoreCase(code)) {
            return "Write-off";
        }
        return firstFilled(rows.isEmpty() ? null : rows.get(0).categoryName(), "Review");
    }

    private List<RegisterRow> loadRows() {

        return jdbcTemplate.query("""
                SELECT a.asset_code, a.description, a.project_or_department, a.status, a.remarks,
                       c.item_category_name AS category_name
                FROM asset a
                LEFT JOIN asset_code ac ON ac.asset_code_id = a.asset_code_id
                LEFT JOIN item_code ic ON ic.item_code_id = ac.item_code_id
                LEFT JOIN item_category c ON c.item_category_id = ic.item_category_id
                """, new MapSqlParameterSource(), (rs, rowNum) -> {

            String assetCode = rs.getString("asset_code");
            String status = rs.getString("status");
            Map<String, String> fields = AssetRemarks.parse(rs.getString("remarks"));

            boolean disposed = "Disposed".equalsIgnoreCase(status);
            String registerCode = fields.get("Register code");
            boolean writeOffCode = registerCode != null && WRITE_OFF_PREFIX.matcher(registerCode).find();

            // The register lists a written-off asset as "WO-<original code>" on its WO sheet.
            String rrcNo = registerCode != null && !registerCode.isBlank()
                    ? registerCode
                    : (disposed && !WRITE_OFF_PREFIX.matcher(assetCode).find() ? "WO-" + assetCode : assetCode);

            String sheet = disposed || writeOffCode
                    ? WRITE_OFF_SHEET
                    : assetCode.contains("-") ? assetCode.substring(0, assetCode.indexOf('-')).toUpperCase(Locale.ROOT) : assetCode.toUpperCase(Locale.ROOT);

            String location = disposed ? "WRITE-OFF" : firstFilled(fields.get("Location"), rs.getString("project_or_department"));

            String[] cells = {
                    rrcNo,
                    filled(fields.get("Reg No")),
                    filled(fields.get("Make")),
                    firstFilled(rs.getString("description"), BLANK),
                    filled(fields.get("Model")),
                    filled(fields.get("Chasis No")),
                    filled(fields.get("Machine SN")),
                    filled(fields.get("Engine SN")),
                    filled(fields.get("Capacity")),
                    filled(location),
            };

            String sortKey = WRITE_OFF_PREFIX.matcher(assetCode).replaceFirst("");
            return new RegisterRow(sheet, sortKey, rs.getString("category_name"), cells);
        });
    }

    private void writeRegisterSheet(XSSFSheet sheet, Styles styles, List<RegisterRow> rows, boolean withTotal) {

        sheet.setDisplayGridlines(false);

        Row header = sheet.createRow(0);
        header.setHeightInPoints(30f);
        for (int c = 0; c < HEADERS.length; c++) {
            Cell cell = header.createCell(c);
            cell.setCellValue(HEADERS[c]);
            cell.setCellStyle(styles.header);
        }
        // "Home" button: back to the Master sheet from any register sheet.
        Cell home = header.createCell(10);
        home.setCellValue("🏠 Home");
        home.setCellStyle(styles.homeButton);
        link(sheet, home, "Master");
        sheet.setColumnWidth(10, 13 * 256);

        if (withTotal) {
            Cell total = header.createCell(13);
            total.setCellValue("Total");
            total.setCellStyle(styles.totalHeader);
        }

        int[] widths = new int[HEADERS.length];
        for (int c = 0; c < HEADERS.length; c++) {
            widths[c] = HEADERS[c].length();
        }

        int rowIndex = 1;
        for (RegisterRow registerRow : rows) {
            Row row = sheet.createRow(rowIndex);
            CellStyle rowStyle = rowIndex % 2 == 0 ? styles.dataBanded : styles.data;
            String[] cells = registerRow.cells();
            for (int c = 0; c < cells.length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(cells[c]);
                cell.setCellStyle(rowStyle);
                widths[c] = Math.max(widths[c], cells[c].length());
            }
            // Helper column used by the register: the type code at the start of the RRC No.
            String typeCode = cells[0].contains("-") ? cells[0].substring(0, cells[0].indexOf('-')).trim() : cells[0].trim();
            formula(row.createCell(11), "TRIM(LEFT(A" + (rowIndex + 1) + ",FIND(\"-\",A" + (rowIndex + 1) + ")-1))", typeCode, null);
            rowIndex++;
        }

        if (withTotal) {
            Row first = sheet.getRow(1);
            if (first == null) {
                first = sheet.createRow(1);
            }
            formula(first.createCell(13), "MAX(COUNTA(A:A)-1,0)", null, (double) rows.size(), styles.totalValue);
            sheet.setColumnWidth(13, 13 * 256);
        }

        for (int c = 0; c < HEADERS.length; c++) {
            sheet.setColumnWidth(c, Math.min(Math.max(widths[c] + 3, 10), 46) * 256);
        }
        sheet.setColumnWidth(11, 6 * 256);
        sheet.createFreezePane(0, 1);

        if (!rows.isEmpty()) {
            sheet.setAutoFilter(new CellRangeAddress(0, rows.size(), 0, HEADERS.length - 1));
            highlightWriteOffLocations(sheet, rows.size());
        }
    }

    /** Tints the Location cell rose when an asset is written off or marked as scrap. */
    private void highlightWriteOffLocations(XSSFSheet sheet, int lastDataRow) {
        XSSFSheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
        XSSFConditionalFormattingRule rule = formatting.createConditionalFormattingRule(
                "OR($J2=\"WRITE-OFF\",ISNUMBER(SEARCH(\"SCRAP\",$J2)))");
        rule.createFontFormatting().setFontColor(rgb("9F1239"));
        var fill = rule.createPatternFormatting();
        fill.setFillBackgroundColor(rgb("FFE4E6"));
        fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        formatting.addConditionalFormatting(new CellRangeAddress[]{new CellRangeAddress(1, lastDataRow, 9, 9)}, rule);
    }

    private void writeMaster(XSSFSheet master, Styles styles, Map<String, List<RegisterRow>> rowsBySheet, Map<String, String> sheetNames) {

        master.setDisplayGridlines(false);
        master.setColumnWidth(0, 11 * 256);
        master.setColumnWidth(1, 80 * 256);
        master.setColumnWidth(2, 18 * 256);
        master.setColumnWidth(3, 22 * 256);

        Row title = master.createRow(0);
        Cell titleCell = title.createCell(1);
        titleCell.setCellValue("RR CONSTRUCTION (PVT) LTD");
        titleCell.setCellStyle(styles.title);
        master.addMergedRegion(new CellRangeAddress(0, 3, 1, 3));

        Row subtitle = master.createRow(4);
        subtitle.setHeightInPoints(22f);
        Cell subtitleCell = subtitle.createCell(1);
        subtitleCell.setCellValue("Asset Register of Vehicles, Machinery & Power Tool Etc.");
        subtitleCell.setCellStyle(styles.subtitle);
        master.addMergedRegion(new CellRangeAddress(4, 4, 1, 3));
        for (int c = 2; c <= 3; c++) {
            subtitle.createCell(c).setCellStyle(styles.subtitle);
        }
        // Shortcut to the sheet holding every asset (the per-code sheets are linked from the Code column).
        Cell allLink = subtitle.createCell(0);
        allLink.setCellValue("📋 All");
        allLink.setCellStyle(styles.homeButton);
        link(master, allLink, "All");

        Row header = master.createRow(5);
        header.setHeightInPoints(28f);
        String[] titles = {"Code", "Asset Description", "Category", "Total"};
        for (int c = 0; c < titles.length; c++) {
            Cell cell = header.createCell(c);
            cell.setCellValue(titles[c]);
            cell.setCellStyle(styles.header);
        }

        int rowIndex = 6;
        for (Map.Entry<String, List<RegisterRow>> entry : rowsBySheet.entrySet()) {
            String code = entry.getKey();
            List<RegisterRow> rows = entry.getValue();
            boolean banded = (rowIndex - 6) % 2 == 1;

            Row row = master.createRow(rowIndex);
            row.setHeightInPoints(20f);
            Cell codeCell = row.createCell(0);
            codeCell.setCellValue(sheetNames.get(code));
            codeCell.setCellStyle(banded ? styles.masterCodeBanded : styles.masterCode);
            link(master, codeCell, sheetNames.get(code));

            String description = rows.isEmpty() ? "" : rows.get(0).cells()[3];
            String ref = "\"'\"&A" + (rowIndex + 1) + "&\"'!";
            formula(row.createCell(1), "IFERROR(INDIRECT(" + ref + "D2\"),\"\")", description, null,
                    banded ? styles.masterTextBanded : styles.masterText);

            String categoryName = categoryOf(code, rows);
            CategoryLook look = lookFor(categoryName);
            Cell category = row.createCell(2);
            category.setCellValue(look.icon() + "  " + categoryName);
            category.setCellStyle(styles.category(look));

            formula(row.createCell(3), "IFERROR(INDIRECT(" + ref + "N2\"),0)", null, (double) rows.size(),
                    banded ? styles.masterNumberBanded : styles.masterNumber);
            rowIndex++;
        }

        master.createFreezePane(0, 6);

        if (rowIndex > 6) {
            master.setAutoFilter(new CellRangeAddress(5, rowIndex - 1, 0, 3));

            // In-cell bars make the size of each type visible at a glance.
            XSSFSheetConditionalFormatting formatting = master.getSheetConditionalFormatting();
            XSSFConditionalFormattingRule bar = formatting.createConditionalFormattingRule(rgb(Styles.DATA_BAR));
            XSSFDataBarFormatting dataBar = bar.getDataBarFormatting();
            dataBar.getMinThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.NUMBER);
            dataBar.getMinThreshold().setValue(0d);
            dataBar.getMaxThreshold().setRangeType(ConditionalFormattingThreshold.RangeType.MAX);
            formatting.addConditionalFormatting(new CellRangeAddress[]{new CellRangeAddress(6, rowIndex - 1, 3, 3)}, bar);
        }
    }

    /** Makes the cell a clickable link to cell A1 of another sheet in the same workbook. */
    private static void link(XSSFSheet from, Cell cell, String targetSheet) {
        XSSFHyperlink hyperlink = from.getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
        hyperlink.setAddress("'" + targetSheet.replace("'", "''") + "'!A1");
        cell.setHyperlink(hyperlink);
    }

    /** Writes a formula plus its computed value, so viewers that don't recalculate still show it. */
    private static void formula(Cell cell, String formula, String cachedText, Double cachedNumber, CellStyle style) {
        formula(cell, formula, cachedText, cachedNumber);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private static void formula(Cell cell, String formula, String cachedText, Double cachedNumber) {
        cell.setCellFormula(formula);
        var ctCell = ((XSSFCell) cell).getCTCell();
        if (cachedText != null) {
            ctCell.setT(STCellType.STR);
            ctCell.setV(cachedText);
        } else if (cachedNumber != null) {
            ctCell.setV(String.valueOf(cachedNumber.longValue()));
        }
    }

    private static String safeSheetName(String code) {
        String name = INVALID_SHEET_CHARS.matcher(code).replaceAll("_");
        return name.length() > 31 ? name.substring(0, 31) : name;
    }

    private static String filled(String value) {
        return value == null || value.isBlank() ? BLANK : value.trim();
    }

    private static String firstFilled(String first, String second) {
        return first != null && !first.isBlank() ? first.trim() : second == null ? "" : second.trim();
    }

    private static XSSFColor rgb(String hex) {
        return new XSSFColor(new byte[]{
                (byte) Integer.parseInt(hex.substring(0, 2), 16),
                (byte) Integer.parseInt(hex.substring(2, 4), 16),
                (byte) Integer.parseInt(hex.substring(4, 6), 16)}, null);
    }

    /** Icon, soft fill, text colour and tab colour used for a Master-sheet category. */
    private record CategoryLook(String icon, String fill, String text, String tab) {
    }

    private static final CategoryLook DEFAULT_LOOK = new CategoryLook("🔍", "FFE4E6", "9F1239", "FDA4AF");

    private static final Map<String, CategoryLook> CATEGORY_LOOKS = Map.of(
            "machine", new CategoryLook("🚜", "FEF3C7", "92400E", "FCD34D"),
            "vehicle", new CategoryLook("🚚", "DBEAFE", "1E40AF", "93C5FD"),
            "tool", new CategoryLook("🔧", "DCFCE7", "166534", "86EFAC"),
            "software", new CategoryLook("💻", "EDE9FE", "5B21B6", "C4B5FD"),
            "other", new CategoryLook("📦", "E2E8F0", "334155", "CBD5E1"),
            "review", DEFAULT_LOOK,
            "write-off", new CategoryLook("🗑", "FEE2E2", "991B1B", "FCA5A5")
    );

    private static CategoryLook lookFor(String category) {
        return category == null ? DEFAULT_LOOK : CATEGORY_LOOKS.getOrDefault(category.trim().toLowerCase(Locale.ROOT), DEFAULT_LOOK);
    }

    /** Light, airy look: Segoe UI, pale-blue headings with dark-blue text, soft blue zebra rows,
     * light borders, underlined blue links, and colour-coded category pills on the Master sheet. */
    private static final class Styles {

        static final String FONT = "Segoe UI";
        static final String INK = "0F172A";
        static final String MUTED = "64748B";
        static final String LINK = "1D4ED8";
        static final String HEADER = "DBEAFE";
        static final String HEADER_TEXT = "1E3A8A";
        static final String ZEBRA = "F5F9FF";
        static final String LINE = "D6E4F5";
        static final String TAB_MASTER = "60A5FA";
        static final String TAB_ALL = "93C5FD";
        static final String DATA_BAR = "93C5FD";

        final XSSFCellStyle header, data, dataBanded, totalHeader, totalValue, title, subtitle, homeButton,
                masterCode, masterCodeBanded, masterText, masterTextBanded, masterNumber, masterNumberBanded;
        private final Map<String, XSSFCellStyle> categoryStyles = new java.util.HashMap<>();
        private final XSSFWorkbook workbook;

        Styles(XSSFWorkbook workbook) {
            this.workbook = workbook;

            header = style(font(10, true, HEADER_TEXT, false), HEADER, HorizontalAlignment.CENTER, true, true);
            data = style(font(10, false, INK, false), null, HorizontalAlignment.LEFT, false, true);
            dataBanded = style(font(10, false, INK, false), ZEBRA, HorizontalAlignment.LEFT, false, true);
            totalHeader = style(font(10, true, HEADER_TEXT, false), "BFDBFE", HorizontalAlignment.CENTER, false, true);
            totalValue = style(font(11, true, LINK, false), "EFF6FF", HorizontalAlignment.CENTER, false, true);
            homeButton = style(font(10, true, LINK, true), HEADER, HorizontalAlignment.CENTER, false, true);
            // Master codes are links to their sheet, so they read as underlined blue text.
            masterCode = style(font(10, true, LINK, true), null, HorizontalAlignment.CENTER, false, true);
            masterCodeBanded = style(font(10, true, LINK, true), ZEBRA, HorizontalAlignment.CENTER, false, true);
            masterText = style(font(10, false, INK, false), null, HorizontalAlignment.LEFT, false, true);
            masterTextBanded = style(font(10, false, INK, false), ZEBRA, HorizontalAlignment.LEFT, false, true);
            masterNumber = style(font(10, true, INK, false), null, HorizontalAlignment.CENTER, false, true);
            masterNumberBanded = style(font(10, true, INK, false), ZEBRA, HorizontalAlignment.CENTER, false, true);
            title = style(font(24, true, HEADER_TEXT, false), null, HorizontalAlignment.CENTER, false, false);
            subtitle = style(font(11, false, MUTED, false), null, HorizontalAlignment.CENTER, false, false);
            subtitle.setBorderBottom(BorderStyle.MEDIUM);
            subtitle.setBottomBorderColor(rgb("93C5FD"));
        }

        /** The soft-coloured "pill" cell for a category on the Master sheet. */
        XSSFCellStyle category(CategoryLook look) {
            return categoryStyles.computeIfAbsent(look.icon(),
                    key -> style(font(10, true, look.text(), false), look.fill(), HorizontalAlignment.CENTER, false, true));
        }

        private XSSFFont font(int size, boolean bold, String color, boolean underline) {
            XSSFFont font = workbook.createFont();
            font.setFontName(FONT);
            font.setFontHeightInPoints((short) size);
            font.setBold(bold);
            font.setColor(rgb(color));
            if (underline) {
                font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
            }
            return font;
        }

        private XSSFCellStyle style(XSSFFont font, String fill, HorizontalAlignment alignment, boolean wrap, boolean bordered) {
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(alignment);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(wrap);
            if (fill != null) {
                style.setFillForegroundColor(rgb(fill));
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            if (bordered) {
                XSSFColor line = rgb(LINE);
                style.setBorderLeft(BorderStyle.THIN);
                style.setLeftBorderColor(line);
                style.setBorderRight(BorderStyle.THIN);
                style.setRightBorderColor(line);
                style.setBorderTop(BorderStyle.THIN);
                style.setTopBorderColor(line);
                style.setBorderBottom(BorderStyle.THIN);
                style.setBottomBorderColor(line);
            }
            return style;
        }
    }
}
