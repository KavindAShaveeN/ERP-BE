package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/** Balance of a cash account computed from its POSTED transactions. */
@Getter
@Setter
public class CashAccountBalance {

    private UUID cashAccountId;
    private String accountName;

    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal balance;
}
