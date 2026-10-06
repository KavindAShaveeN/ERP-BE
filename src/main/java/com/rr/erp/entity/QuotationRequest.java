package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class QuotationRequest {

    private UUID quotationRequestId;
    private String quotationRequestCode;

    // Optional — a quotation request can be raised without a source MR, with items
    // added manually. String (not UUID) because the frontend sends "" rather than
    // omitting the field when no MR is selected; blank is normalized to a null
    // mr_id column in the repository. No FK on that column, mirroring po.mr_id.
    private String mrId;

    private LocalDateTime requestDate;
    private String requestedBy;
    private LocalDate dueDate;
    private String remark;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> supplierCodes;
    private List<QuotationRequestItem> items;
}
