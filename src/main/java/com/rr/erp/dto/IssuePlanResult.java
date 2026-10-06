package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssuePlanResult {

    private UUID planBatchId;
    private int linesSaved;
    /** Codes of the FWD- MRs created by agreeing a forward plan (empty after a plain confirm). */
    private List<String> forwardedMrCodes;
}
