package com.rr.erp.service;

import com.rr.erp.entity.JobCostEntry;
import com.rr.erp.repository.JobCostEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JobCostEntryService {

    private final JobCostEntryRepository repository;

    public JobCostEntryService(JobCostEntryRepository repository) {
        this.repository = repository;
    }

    public JobCostEntry createJobCostEntry(JobCostEntry jobCostEntry) {

        return repository.createJobCostEntry(jobCostEntry);
    }

    public JobCostEntry updateJobCostEntry(
            UUID jobCostEntryId,
            JobCostEntry jobCostEntry
    ) {

        int updatedRows = repository.updateJobCostEntry(
                jobCostEntryId,
                jobCostEntry
        );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Job Cost Entry not found with ID: " + jobCostEntryId
            );
        }

        jobCostEntry.setJobCostEntryId(jobCostEntryId);

        return jobCostEntry;
    }
}