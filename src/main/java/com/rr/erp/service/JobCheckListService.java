package com.rr.erp.service;

import com.rr.erp.entity.JobCheckList;
import com.rr.erp.repository.JobCheckListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobCheckListService {

    private final JobCheckListRepository jobCheckListRepository;


    // =========================================================
    // INSERT OR UPDATE
    // =========================================================
    public void upsertJobCheckList(
            UUID jobCardId,
            JobCheckList jobCheckList) {

        jobCheckListRepository.upsertJobCheckList(
                jobCardId,
                jobCheckList
        );
    }


    // =========================================================
    // GET
    // =========================================================
    public Optional<JobCheckList> getJobCheckList(UUID jobCardId) {

        return jobCheckListRepository
                .getJobCheckListByJobCardId(jobCardId);
    }
}