package com.rr.erp.service;

import com.rr.erp.entity.SPIssue;
import com.rr.erp.repository.SPIssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SPIssueService {

    private final SPIssueRepository spIssueRepository;

    public SPIssueService(SPIssueRepository spIssueRepository) {
        this.spIssueRepository = spIssueRepository;
    }

    public SPIssue createSPIssue(SPIssue spIssue) {

        spIssue.setSpIssueId(UUID.randomUUID());

        if (spIssue.getIsApproved() == null) {
            spIssue.setIsApproved(false);
        }

        spIssueRepository.createSPIssue(spIssue);

        return spIssue;
    }

    public List<SPIssue> getAllSPIssues() {
        return spIssueRepository.getAllSPIssues();
    }

    public SPIssue updateSPIssue(
            UUID spIssueId,
            SPIssue spIssue
    ) {

        int updatedRows =
                spIssueRepository.updateSPIssue(
                        spIssueId,
                        spIssue
                );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "SP Issue not found: " + spIssueId
            );
        }

        return spIssueRepository.getSPIssueById(spIssueId);
    }
}