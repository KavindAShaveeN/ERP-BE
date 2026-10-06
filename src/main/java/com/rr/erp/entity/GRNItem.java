package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GRNItem implements PackCarrier {

    private UUID grnItemId;

    private UUID grnId;

    private String itemCode;

    private String description;

    // Set only on a line received against a multiple-supplier PO (see PO.supplierCode /
    // PurchaseOrderFormPage's isMultiSupplier) — free text, mirroring POItem.supplierName,
    // since GRN.supplierCode is blank for such a GRN and there is no single supplier to
    // attribute the whole document to.
    private String supplierName;

    private String size;
    private Integer uomId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    // Null for internal GRNs created directly from a GIN, which have no purchase price.
    //@Positive(message = "Unit price must be greater than zero")
    private Integer unitPrice;

    private BigDecimal amount;

    // Set when this GRN line's UOM differs from the linked PO line's UOM (e.g. PO in TON, received in NOS).
    // Clerk-entered at receipt time: "1 unit of the PO's UOM = conversionFactor units of this line's UOM".
    private BigDecimal conversionFactor;

    // quantity / conversionFactor, persisted at save time as an audit trail of the received qty in the PO's UOM.
    private BigDecimal poEquivalentQty;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems) — the size the bars/pieces
    // were received at. quantity remains the piece/bar count for these items. widthM is only
    // populated for area-tracked (sheet) items.
    private BigDecimal lengthM;
    private BigDecimal widthM;

    // Set when this line moves one specific registered asset (quantity 1). Such a line bypasses
    // the quantity stock engine — the asset's location history tracks it instead
    // (see asset_movement_tracking.sql).
    private String assetCode;

    // Expiry date for special items that expire, recorded at receipt; null for everything else.
    private java.time.LocalDate expiryDate;

    // Ticked/unticked pack items of the asset on this line; stored in asset_pack_transaction.
    private java.util.List<PackSelection> packItems;
}