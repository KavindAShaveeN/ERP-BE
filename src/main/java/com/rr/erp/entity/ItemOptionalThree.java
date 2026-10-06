package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemOptionalThree {

    private Long itemOptionalThreeId;

    @NotBlank(message = "Optional 3 code is required")
    private String itemOptionalThreeCode;

    @NotBlank(message = "Optional 3 name is required")
    private String itemOptionalThreeName;

    @NotNull(message = "Optional 2 id is required")
    private Long itemOptionalTwoId;
}
