package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/** Current balance of a cash account, with its POSTED transaction totals. */
@Getter
@Setter
public class CashAccountBalance {

    private UUID cashAccountId;
    private String accountName;

    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal balance;
}
