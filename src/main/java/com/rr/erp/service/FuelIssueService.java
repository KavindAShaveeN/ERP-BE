package com.rr.erp.service;


import com.rr.erp.dto.FuelAssetSummary;
import com.rr.erp.dto.FuelConsumptionReport;
import com.rr.erp.entity.FuelIssue;
import com.rr.erp.repository.FuelIssueRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FuelIssueService {

    private final FuelIssueRepository repository;
    private final ProjectStoreService projectStoreService;

    public FuelIssueService(FuelIssueRepository repository, ProjectStoreService projectStoreService){
        this.repository = repository;
        this.projectStoreService = projectStoreService;
    }

    // POST
    // Does NOT touch project store stock — a fuel issue only draws from the real fuel
    // stock once the receiver approves it (see receiveFuel below), mirroring how a GIN's
    // stock is deducted on authorization, not on creation.
    @Transactional
    public FuelIssue createFuelIssue(
            FuelIssue fuelIssue
    ) {

        fuelIssue.setFuelIssueId(
                UUID.randomUUID()
        );

        // issuedDate is the system-entered timestamp — always set here, never trusted from
        // the client. fuelIssueDate is the user-editable actual issue date; default it to
        // now if the caller didn't supply one.
        fuelIssue.setIssuedDate(LocalDateTime.now());
        if (fuelIssue.getFuelIssueDate() == null) {
            fuelIssue.setFuelIssueDate(fuelIssue.getIssuedDate());
        }

        if (fuelIssue.getIsIssued() == null) {
            fuelIssue.setIsIssued(true);
        }

        if (fuelIssue.getIsReceived() == null) {
            fuelIssue.setIsReceived(false);
        }

        if (fuelIssue.getIsActive() == null) {
            fuelIssue.setIsActive(true);
        }

        if (fuelIssue.getIsFilled() == null) {
            fuelIssue.setIsFilled(false);
        }

        if (fuelIssue.getProjectCode() == null || fuelIssue.getFuelType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Project code and fuel item are required to issue fuel from stock."
            );
        }

        // The client-supplied code is only a display preview computed from the currently
        // *active* fuel issues it can see — a soft-deleted issue still holds its code under
        // the unique constraint, so re-derive the real next code from every row (active or
        // not) here, and retry once if a concurrent request grabbed the same number first.
        for (int attempt = 0; attempt < 3; attempt++) {
            fuelIssue.setFuelIssueCode(nextFuelIssueCode(fuelIssue.getProjectCode()));
            try {
                repository.createFuelIssue(fuelIssue);
                return fuelIssue;
            } catch (DuplicateKeyException e) {
                if (attempt == 2) {
                    throw e;
                }
            }
        }

        return fuelIssue;
    }

    private static final Pattern FUEL_ISSUE_CODE_PATTERN = Pattern.compile("^FI-.+-(\\d+)$");

    private String nextFuelIssueCode(String projectCode) {
        String prefix = "FI-" + projectCode;
        int maxSequence = 0;
        for (String code : repository.getFuelIssueCodesForProject(projectCode)) {
            Matcher matcher = FUEL_ISSUE_CODE_PATTERN.matcher(code);
            if (matcher.matches()) {
                maxSequence = Math.max(maxSequence, Integer.parseInt(matcher.group(1)));
            }
        }
        return String.format("%s-%04d", prefix, maxSequence + 1);
    }


    // GET
    public List<FuelIssue> getIssuedFuel(
            String projectCode
    ) {

        return repository.getIssuedFuel(
                projectCode
        );
    }


    // RECEIVE
    // The moment the receiver confirms delivery — this is when fuel actually leaves the
    // project's stock, not when the issue note was written up.
    @Transactional
    public void receiveFuel(
            UUID fuelIssueId
    ) {

        FuelIssue fuelIssue = repository.findById(fuelIssueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fuel issue not found: " + fuelIssueId
                ));

        int updated = repository.receiveFuel(
                fuelIssueId
        );

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Fuel issue " + fuelIssueId + " was not found, inactive, or already received."
            );
        }

        projectStoreService.issueGoods(
                fuelIssue.getProjectCode(),
                fuelIssue.getFuelType(),
                fuelIssue.getQuantity(),
                fuelIssue.getFuelIssueDate().toLocalDate(),
                StockBatchService.ACTION_FUEL_ISSUE,
                fuelIssue.getFuelIssueId(),
                null
        );
    }


    // DELETE
    public void deleteFuelIssue(
            UUID fuelIssueId
    ) {

        int updated =
                repository.deleteFuelIssue(
                        fuelIssueId
                );

        if (updated == 0) {
            throw new RuntimeException(
                    "Fuel issue not found: "
                            + fuelIssueId
            );
        }
    }
    public List<FuelIssue> getReceivedFuelIssues(String receivedBy) {

        return repository.getReceivedFuelIssues(receivedBy);
    }

    public List<FuelAssetSummary> getFuelAssetSummary() {
        return repository.getFuelAssetSummary();
    }
    public List<FuelIssue> getFuelIssuesByAssetCode(String assetCode) {
        return repository.getFuelIssuesByAssetCode(assetCode);
    }

    public List<FuelConsumptionReport> getFuelConsumptionReport(String projectCode) {
        return repository.getFuelConsumptionReport(projectCode);
    }
}