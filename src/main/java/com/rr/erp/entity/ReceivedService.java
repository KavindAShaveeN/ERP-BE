package com.rr.erp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One GRN line received against a Service item code (see service_item table) -- recorded
 * here instead of a project_store stock batch, since services carry no quantity/UOM
 * tracking. See GRNService#receiveServiceItem for how this is populated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedService {

    private UUID receivedServiceId;

    private UUID grnId;

    private UUID grnItemId;

    // Denormalized in from grn.grn_code by the repository's SELECT join -- not its own
    // column -- so the "Received services" list doesn't need a second round trip per row.
    private String grnCode;

    private String itemCode;

    private String description;

    private BigDecimal quantity;

    private Integer uomId;

    private Integer unitPrice;

    private BigDecimal amount;

    private String projectCode;

    private String poCode;

    private String supplierCode;

    private LocalDate serviceDate;

    private String receivedBy;
}
