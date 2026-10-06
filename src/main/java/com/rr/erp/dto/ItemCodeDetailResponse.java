package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

/** An item code with the name of every level of its hierarchy resolved — item type (via its
 * category), category, sub category, ... down to optional 3. Levels the item code doesn't use
 * come back as null. */
@Getter
@Setter
public class ItemCodeDetailResponse {

    private Long itemCodeId;
    private String itemCodeCode;
    private String itemCodeName;

    private Long itemTypeId;
    private String itemTypeName;

    private Long itemCategoryId;
    private String itemCategoryName;

    private Long itemSubCategoryId;
    private String itemSubCategoryName;

    private Long itemSubSubCategoryId;
    private String itemSubSubCategoryName;

    private Long itemBrandId;
    private String itemBrandName;

    private Long itemModelId;
    private String itemModelName;

    private Long itemOptionalOneId;
    private String itemOptionalOneName;

    private Long itemOptionalTwoId;
    private String itemOptionalTwoName;

    private Long itemOptionalThreeId;
    private String itemOptionalThreeName;
}
