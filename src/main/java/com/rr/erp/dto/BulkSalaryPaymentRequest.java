package com.rr.erp.dto;

import com.rr.erp.entity.SalaryPayment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Salary payments for many employees, saved together: either every one is paid or none is. */
@Getter
@Setter
public class BulkSalaryPaymentRequest {

    @NotEmpty(message = "At least one salary payment is required")
    private List<@Valid SalaryPayment> payments;
}
