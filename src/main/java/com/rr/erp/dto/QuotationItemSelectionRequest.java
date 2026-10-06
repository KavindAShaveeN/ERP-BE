package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuotationItemSelectionRequest {
    // Boolean (not primitive boolean) so Lombok emits getIsSelected/setIsSelected,
    // matching the "isSelected" JSON key the frontend sends verbatim.
    private Boolean isSelected;
}
