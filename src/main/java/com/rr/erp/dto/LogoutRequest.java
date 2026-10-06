package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LogoutRequest {
    // The sessionId returned by POST /api/login.
    private UUID sessionId;
}
