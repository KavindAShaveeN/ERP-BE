package com.rr.erp.service;

import com.rr.erp.entity.JobStatusType;
import com.rr.erp.repository.JobStatusTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobStatusTypeService {

    private final JobStatusTypeRepository jobStatusTypeRepository;

    public List<JobStatusType> getAllJobStatusTypes() {

        return jobStatusTypeRepository.getAllJobStatusTypes();
    }
}