package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SupplierPayment {

    private UUID supplierPaymentId;
    private String paymentCode;

    private String supplierCode;
    private String projectCode;

    private UUID poId;
    private String poCode;

    private LocalDate paymentDate;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String paymentReferenceNo;

    private String remarks;

    private String recordedBy;
    private LocalDateTime recordedDate;
}
