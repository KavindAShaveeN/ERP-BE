package com.rr.erp.entity;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Quotation {

    private UUID quotationId;
    private String quotationCode;
    private UUID quotationRequestId;
    private String supplierCode;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private Integer currencyId;
    private String paymentTerm;
    private String deliveryTerm;
    private String remark;
    private BigDecimal totalValue;
    private String status;
    private LocalDateTime receivedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuotationItem> items;
}
