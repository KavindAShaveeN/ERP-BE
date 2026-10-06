package com.rr.erp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetCode {

    private Long assetCodeId;

    private String assetCodeCode;

    private Long itemCodeId;
}
