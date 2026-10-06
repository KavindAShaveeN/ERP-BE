package com.rr.erp.service;

import com.rr.erp.entity.JobWorker;
import com.rr.erp.repository.JobWorkerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobWorkerService {

    private final JobWorkerRepository jobWorkerRepository;

    public JobWorkerService(
            JobWorkerRepository jobWorkerRepository
    ) {
        this.jobWorkerRepository = jobWorkerRepository;
    }

    // =========================================================
    // CREATE
    // =========================================================

    public JobWorker createJobWorker(JobWorker jobWorker) {

        jobWorkerRepository.createJobWorker(jobWorker);

        return jobWorker;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void updateJobWorker(
            UUID jobWorkerId,
            JobWorker jobWorker
    ) {

        int updatedRows =
                jobWorkerRepository.updateJobWorker(
                        jobWorkerId,
                        jobWorker
                );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Job worker not found with ID: "
                            + jobWorkerId
            );
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deleteJobWorker(UUID jobWorkerId) {

        int deletedRows =
                jobWorkerRepository.deleteJobWorker(jobWorkerId);

        if (deletedRows == 0) {
            throw new RuntimeException(
                    "Job worker not found with ID: "
                            + jobWorkerId
            );
        }
    }
}