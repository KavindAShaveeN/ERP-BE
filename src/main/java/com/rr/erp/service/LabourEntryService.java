package com.rr.erp.service;

import com.rr.erp.entity.LabourEntry;
import com.rr.erp.repository.LabourEntryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LabourEntryService {

    private static final BigDecimal MAX_HOURS_PER_DAY = BigDecimal.valueOf(24);

    private final LabourEntryRepository labourEntryRepository;

    public LabourEntryService(LabourEntryRepository labourEntryRepository) {
        this.labourEntryRepository = labourEntryRepository;
    }

    public LabourEntry create(LabourEntry entry) {
        if (entry.getEmployeeCode() == null || entry.getEmployeeCode().isBlank()) {
            throw badRequest("Employee is required");
        }
        if (entry.getWorkDate() == null) {
            throw badRequest("Work date is required");
        }
        validateProjectAndHours(entry);
        validateDailyTotal(entry.getEmployeeCode(), entry.getWorkDate(), entry.getHours(), null);

        try {
            labourEntryRepository.create(entry);
        } catch (DataIntegrityViolationException exception) {
            throw badRequest("Unknown employee or project");
        }
        return labourEntryRepository.findById(entry.getLabourEntryId());
    }

    public LabourEntry update(UUID labourEntryId, LabourEntry entry) {
        LabourEntry existing = labourEntryRepository.findById(labourEntryId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Labour entry not found");
        }
        validateProjectAndHours(entry);
        validateDailyTotal(existing.getEmployeeCode(), existing.getWorkDate(), entry.getHours(), labourEntryId);

        try {
            labourEntryRepository.update(labourEntryId, entry);
        } catch (DataIntegrityViolationException exception) {
            throw badRequest("Unknown project");
        }
        return labourEntryRepository.findById(labourEntryId);
    }

    public List<LabourEntry> search(LocalDate from, LocalDate to, String projectCode, String employeeCode) {
        return labourEntryRepository.search(from, to, projectCode, employeeCode);
    }

    public boolean delete(UUID labourEntryId) {
        return labourEntryRepository.delete(labourEntryId) > 0;
    }

    private void validateProjectAndHours(LabourEntry entry) {
        if (entry.getProjectCode() == null || entry.getProjectCode().isBlank()) {
            throw badRequest("Project is required");
        }
        if (entry.getHours() == null || entry.getHours().signum() <= 0) {
            throw badRequest("Hours must be greater than zero");
        }
        if (entry.getHours().compareTo(MAX_HOURS_PER_DAY) > 0) {
            throw badRequest("Hours for one entry cannot exceed 24");
        }
    }

    // One person cannot work more than 24 hours across all the places they are recorded in a day.
    private void validateDailyTotal(String employeeCode, LocalDate workDate, BigDecimal hours, UUID excludeId) {
        BigDecimal booked = labourEntryRepository.sumHoursForEmployeeOnDate(employeeCode, workDate, excludeId);
        if (booked.add(hours).compareTo(MAX_HOURS_PER_DAY) > 0) {
            throw badRequest("This employee already has " + booked.stripTrailingZeros().toPlainString()
                    + " hour(s) recorded on " + workDate + "; the day total cannot exceed 24 hours");
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
