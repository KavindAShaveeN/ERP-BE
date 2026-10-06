package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemOptionalOne {

    private Long itemOptionalOneId;

    @NotBlank(message = "Optional 1 code is required")
    private String itemOptionalOneCode;

    @NotBlank(message = "Optional 1 name is required")
    private String itemOptionalOneName;

    @NotNull(message = "Model id is required")
    private Long itemModelId;
}
