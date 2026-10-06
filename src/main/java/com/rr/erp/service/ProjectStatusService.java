package com.rr.erp.service;

import com.rr.erp.entity.ProjectStatus;
import com.rr.erp.repository.ProjectStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectStatusService {

    private final ProjectStatusRepository projectStatusRepository;

    public ProjectStatusService(
            ProjectStatusRepository projectStatusRepository
    ) {
        this.projectStatusRepository = projectStatusRepository;
    }

    public List<ProjectStatus> getAllProjectStatuses() {
        return projectStatusRepository.getAllProjectStatuses();
    }
}
