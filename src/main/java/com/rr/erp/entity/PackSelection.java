package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

// One pack item ticked (included) or unticked on a GIN / GRN / return asset line.
// name and quantity are read-only, filled on load.
@Getter
@Setter
public class PackSelection {

    private Long packItemId;
    private Boolean included;
    private String name;
    private Integer quantity;
}
