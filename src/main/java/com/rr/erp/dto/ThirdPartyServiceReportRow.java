package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** One third-party service issue line, with its job card context — the row shape behind the
 * third party services report (items/assets sent out and received back, plus cost). */
@Getter
@Setter
public class ThirdPartyServiceReportRow {

    private UUID threePServiceId;
    private UUID jobCardId;
    private String jobCardCode;
    private String parentJobCardCode;
    private String vehicleAssetCode;
    private String make;
    private String type;
    private String department;
    private String projectCode;

    private String item;
    private String serialNumber;
    private String assetCode;
    private String itemCode;
    private BigDecimal quantity;

    private String serviceProviderName;
    private LocalDateTime issuedDate;
    private String status;
    private LocalDateTime receivedDate;
    private String remarks;
    private BigDecimal serviceCharge;
}
