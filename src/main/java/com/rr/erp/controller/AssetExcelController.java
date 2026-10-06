package com.rr.erp.controller;

import com.rr.erp.dto.AssetExportRequest;
import com.rr.erp.dto.AssetImportResult;
import com.rr.erp.service.AssetExportService;
import com.rr.erp.service.AssetExportService.ColumnInfo;
import com.rr.erp.service.AssetImportService;
import com.rr.erp.service.AssetRegisterExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Excel export / import of the asset register. Literal paths, so they never clash with
 * AssetController's GET /api/assets/{assetCode}. */
@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*", exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
public class AssetExcelController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final AssetExportService exportService;
    private final AssetRegisterExportService registerExportService;
    private final AssetImportService importService;

    public AssetExcelController(
            AssetExportService exportService,
            AssetRegisterExportService registerExportService,
            AssetImportService importService
    ) {
        this.exportService = exportService;
        this.registerExportService = registerExportService;
        this.importService = importService;
    }

    /* The columns an export can include, in display order — the frontend's column picker is built from this. */
    @GetMapping("/export/columns")
    public ResponseEntity<List<ColumnInfo>> getExportColumns() {
        return ResponseEntity.ok(exportService.getColumns());
    }

    /* POST /api/assets/export — body: { columns, assetCodes?, search?, assetClass?, status? } */
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportAssets(@RequestBody AssetExportRequest request) {

        byte[] workbook = exportService.export(request);

        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("assets-" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8).build().toString())
                .body(workbook);
    }

    /* GET /api/assets/export/register — the whole registry as a workbook laid out like the
     * company's "WMS Total Assets" register (Master, All, one sheet per type code, WO). */
    @GetMapping("/export/register")
    public ResponseEntity<byte[]> exportRegister() {

        byte[] workbook = registerExportService.export();

        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("WMS Total Assets as at "
                                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ".xlsx",
                                StandardCharsets.UTF_8).build().toString())
                .body(workbook);
    }

    /* POST /api/assets/import?dryRun=true — multipart "file": the asset register workbook. */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssetImportResult> importAssets(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        return ResponseEntity.ok(importService.importRegister(file, dryRun));
    }
}
