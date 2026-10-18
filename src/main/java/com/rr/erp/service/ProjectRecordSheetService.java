package com.rr.erp.service;

import com.rr.erp.dto.ProjectRecordSheetResponse;
import com.rr.erp.repository.ProjectRecordSheetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ProjectRecordSheetService {

    // Plant is built day by day, so cap the range to keep the query bounded (about 3 years).
    private static final long MAX_DAYS = 1100;

    private final ProjectRecordSheetRepository recordSheetRepository;

    public ProjectRecordSheetService(ProjectRecordSheetRepository recordSheetRepository) {
        this.recordSheetRepository = recordSheetRepository;
    }

    public ProjectRecordSheetResponse build(String projectCode, LocalDate from, LocalDate to) {
        if (projectCode == null || projectCode.isBlank()) {
            throw badRequest("Project is required");
        }
        if (from == null || to == null) {
            throw badRequest("From and to dates are required");
        }
        if (to.isBefore(from)) {
            throw badRequest("The end date must not be before the start date");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_DAYS) {
            throw badRequest("Choose a range of at most " + MAX_DAYS + " days");
        }

        ProjectRecordSheetResponse response = new ProjectRecordSheetResponse();
        response.setProjectCode(projectCode);
        response.setFrom(from);
        response.setTo(to);
        response.setLabour(recordSheetRepository.findLabour(projectCode, from, to));
        response.setPlant(recordSheetRepository.findPlant(projectCode, from, to));
        response.setMaterials(recordSheetRepository.findMaterials(projectCode, from, to));
        return response;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
