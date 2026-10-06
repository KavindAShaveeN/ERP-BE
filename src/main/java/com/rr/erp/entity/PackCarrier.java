package com.rr.erp.entity;

import java.util.List;

// A document line that can carry an asset's pack-item selections.
public interface PackCarrier {

    String getAssetCode();

    List<PackSelection> getPackItems();

    void setPackItems(List<PackSelection> packItems);
}
