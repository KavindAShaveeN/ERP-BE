package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Everything a project's gate has pending right now: what's leaving (outgoing) and what has
 * left its source but hasn't been confirmed arriving here yet (incoming). */
@Getter
@Setter
@AllArgsConstructor
public class GatePassResponse {

    private List<GatePassEntry> outgoing;
    private List<GatePassEntry> incoming;
}
