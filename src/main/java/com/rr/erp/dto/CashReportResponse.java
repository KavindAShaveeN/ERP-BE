package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Row and response shapes for the cash statement and the petty cash / salary / purpose reports. */
public class CashReportResponse {

    /** A bank-style statement of one cash account: opening balance, every posted movement, closing balance. */
    @Getter
    @Setter
    public static class Statement {
        private UUID cashAccountId;
        private String accountName;
        private String accountType;
        private String employeeName;
        private LocalDate fromDate;
        private LocalDate toDate;
        private BigDecimal openingBalance;
        private BigDecimal totalIn;
        private BigDecimal totalOut;
        private BigDecimal closingBalance;
        private List<StatementLine> lines;
    }

    @Getter
    @Setter
    public static class StatementLine {
        private UUID transactionId;
        private String transactionCode;
        private LocalDateTime transactionDate;
        private String transactionType;
        /** The account on the other side of the movement (or the expense category). */
        private String counterparty;
        private String purpose;
        private String employeeName;
        private String salaryKind;
        private String description;
        private String referenceNumber;
        private BigDecimal moneyIn;
        private BigDecimal moneyOut;
        /** Running balance after this line. */
        private BigDecimal balance;
    }

    /** One petty cash account over the period. */
    @Getter
    @Setter
    public static class PettyCashRow {
        private UUID cashAccountId;
        private String accountName;
        private String employeeCode;
        private String employeeName;
        private Boolean isActive;
        private BigDecimal openingBalance;
        private BigDecimal totalIn;
        private BigDecimal totalOut;
        private BigDecimal closingBalance;
    }

    /** What one employee received as salary advance / full salary over the period. */
    @Getter
    @Setter
    public static class SalaryRow {
        private String employeeCode;
        private String employeeName;
        private BigDecimal advances;
        private BigDecimal fullSalary;
        private BigDecimal total;
        private Integer transferCount;
        private LocalDateTime lastPaidAt;
    }

    /** One salary transfer to an employee. */
    @Getter
    @Setter
    public static class SalaryLine {
        private UUID transactionId;
        private String transactionCode;
        private LocalDateTime transactionDate;
        private String salaryKind;
        private BigDecimal amount;
        private String fromAccountName;
        private String toAccountName;
        private String description;
    }

    /** Money the main cash account gave out, grouped by transfer purpose. */
    @Getter
    @Setter
    public static class PurposeRow {
        private String purpose;
        private BigDecimal total;
        private Integer transferCount;
    }
}
