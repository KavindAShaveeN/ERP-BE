package com.rr.erp.dto;

/** One employee and the username of their login, or null when they have no login yet. */
public record UserLoginDto(String employeeCode, String fullName, String userName) {
}
