package com.rr.erp.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/** An other (non-material) expense — labour, power, transport, etc. — added to a production's cost. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantProductionExpense {

    private UUID productionExpenseId;

    private UUID productionId;

    @NotBlank(message = "Expense description is required")
    @Size(max = 200, message = "Expense description must be 200 characters or fewer")
    private String description;

    @NotNull(message = "Expense amount is required")
    @DecimalMin(value = "0.01", message = "Expense amount must be greater than zero")
    private BigDecimal amount;
}
