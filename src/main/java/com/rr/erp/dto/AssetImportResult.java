package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssetImportResult {

    /** True when nothing was written (validation-only run). */
    private boolean dryRun;

    private int totalRows;

    /** Assets that were (or, for a dry run, would be) created. */
    private int created;

    /** Rows whose asset code is already registered — left untouched. */
    private int skippedExisting;

    private int failed;

    private List<RowIssue> failures = new ArrayList<>();

    /** Rows that were imported, but not exactly as written (renamed duplicate, fallback type, ...). */
    private List<RowIssue> warnings = new ArrayList<>();

    /** Per asset class, how many assets were (or would be) created. */
    private java.util.Map<String, Integer> createdByClass = new java.util.TreeMap<>();

    public record RowIssue(int row, String code, String message) {
    }
}
