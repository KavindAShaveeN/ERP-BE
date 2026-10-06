package com.rr.erp.service;

import com.rr.erp.entity.ProjectType;
import com.rr.erp.repository.ProjectTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectTypeService {

    private final ProjectTypeRepository projectTypeRepository;

    public List<ProjectType> getAllProjectTypes() {
        return projectTypeRepository.findAll();
    }
}