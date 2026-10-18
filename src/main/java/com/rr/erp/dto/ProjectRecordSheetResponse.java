package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * The raw facts needed to fill a Daily Record Sheet (labour, plant, materials) for one project over a
 * date range. Every row carries its own date so the caller can build a daily, weekly, monthly or
 * whole-project sheet from the same data.
 */
@Getter
@Setter
public class ProjectRecordSheetResponse {

    private String projectCode;
    private LocalDate from;
    private LocalDate to;
    private List<LabourRow> labour;
    private List<PlantRow> plant;
    private List<MaterialRow> materials;

    /** One employee's stint on the project on one day (from the daily labour log). */
    @Getter
    @Setter
    public static class LabourRow {
        private LocalDate workDate;
        private String employeeCode;
        private String employeeName;
        /** The employee's designation, used as the "Trade" on the sheet. */
        private String trade;
        private LocalTime startTime;
        private LocalTime endTime;
        private BigDecimal hours;
        private String workDescription;
    }

    /** One asset that was on the project's site on one day, with that day's meter reading if recorded. */
    @Getter
    @Setter
    public static class PlantRow {
        private LocalDate workDate;
        private String assetCode;
        private String description;
        private String assetClass;
        private String meterType;
        private BigDecimal meterIn;
        private BigDecimal meterOut;
        /** Meter usage recorded that day (meter out minus meter in). */
        private BigDecimal usage;
    }

    /** One item issued to the project (or returned from it) on one day. */
    @Getter
    @Setter
    public static class MaterialRow {
        private LocalDate issueDate;
        /** ISSUE or RETURN. */
        private String entryType;
        private String documentCode;
        /** GENERAL / SUBCONTRACTOR / PERSONAL / LOAN / JOB_CARD. */
        private String issueType;
        private String itemCode;
        private String description;
        private String size;
        private String unit;
        private BigDecimal quantity;
        /** Cost of the line (quantity x unit price). */
        private BigDecimal amount;
        private String remarks;
    }
}
