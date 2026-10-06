package com.rr.erp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ServiceScheduleDtos {

    private ServiceScheduleDtos() {}

    /** A spare part / oil needed by a service; itemCode is null when typed in manually. */
    public record PartDto(UUID partId, String itemCode, String partName, BigDecimal quantity, String unit) {}

    /** One service in the plan, e.g. "500 km service", due at atValue (and each cycle after). */
    public record ServiceDto(UUID serviceId, String name, BigDecimal atValue, String remarks,
                             Integer sortOrder, List<PartDto> parts) {}

    public record TemplateDto(UUID templateId, String assetTypeCode, String name, String meterUnit,
                              BigDecimal cycleLength, Boolean isActive, List<ServiceDto> services) {}

    /** One template service evaluated against one asset's meter reading and service history. */
    public record DueItemDto(String assetCode, String projectCode, String assetTypeCode,
                             UUID templateId, String meterUnit, UUID serviceId, String serviceName,
                             BigDecimal currentReading, BigDecimal lastDoneReading,
                             LocalDateTime lastDoneDate, BigDecimal windowStart,
                             BigDecimal nextDue, BigDecimal remaining, String status,
                             List<PartDto> parts) {}

    public record CompletedServicesRequest(List<UUID> serviceIds) {}
}
