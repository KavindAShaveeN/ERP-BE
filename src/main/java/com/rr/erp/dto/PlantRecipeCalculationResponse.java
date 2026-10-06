package com.rr.erp.dto;

import com.rr.erp.entity.PlantProductionInput;
import com.rr.erp.entity.PlantProductionOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Scaled input/output lines, ready to seed a new production record's editable tables. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantRecipeCalculationResponse {

    private List<PlantProductionInput> inputs;
    private List<PlantProductionOutput> outputs;
}
