package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** One employee's petty cash balance, salary advances and salary payments, from POSTED / non-void records. */
@Getter
@Setter
public class EmployeeCashSummary {

    private String employeeCode;
    private String employeeName;

    // Petty cash the employee holds (sum of their EMPLOYEE cash accounts)
    private BigDecimal pettyCashBalance;
    private BigDecimal pettyCashReceived;
    private BigDecimal pettyCashSpent;

    // Advances the employee has received
    private BigDecimal advancesGiven;
    private BigDecimal advancesRecovered;
    private BigDecimal advanceOutstanding;

    // Salary the employee has been paid
    private BigDecimal salaryGross;
    private BigDecimal salaryNetPaid;
    private Integer salaryPaymentCount;
}
