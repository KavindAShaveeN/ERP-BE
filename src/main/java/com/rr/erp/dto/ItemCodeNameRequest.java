package com.rr.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCodeNameRequest {

    @NotBlank(message = "Item code name is required")
    @Size(max = 200, message = "Item code name cannot be longer than 200 characters")
    private String itemCodeName;
}
