package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinLocationRequest {
    private String projectCode;
    private String itemCode;
    private String binLocation;
}
