package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemOptionalTwo {

    private Long itemOptionalTwoId;

    @NotBlank(message = "Optional 2 code is required")
    private String itemOptionalTwoCode;

    @NotBlank(message = "Optional 2 name is required")
    private String itemOptionalTwoName;

    @NotNull(message = "Optional 1 id is required")
    private Long itemOptionalOneId;
}
