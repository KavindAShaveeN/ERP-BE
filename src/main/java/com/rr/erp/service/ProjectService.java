package com.rr.erp.service;

import com.rr.erp.dto.ProjectResponseDTO;
import com.rr.erp.entity.Project;
import com.rr.erp.repository.ProjectLocationRepository;
import com.rr.erp.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectLocationRepository projectLocationRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectLocationRepository projectLocationRepository){
        this.projectRepository = projectRepository;
        this.projectLocationRepository = projectLocationRepository;
    }

    @Transactional
    public Project createProject(Project project){

        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        Integer projectId = projectRepository.insertProject(project);
        project.setProjectId(projectId);

        projectLocationRepository.insertLocations(projectId, project.getLocations());

        return project;
    }
    public List<ProjectResponseDTO> getAllProjects() {
        List<ProjectResponseDTO> projects = projectRepository.getAllProjects();

        Map<Integer, List<String>> locationsByProjectId = projectLocationRepository.getLocationsGroupedByProjectId();
        for (ProjectResponseDTO project : projects) {
            project.setLocations(locationsByProjectId.getOrDefault(project.getProjectId(), List.of()));
        }

        return projects;
    }

    @Transactional
    public Project updateProject(
            Integer projectId,
            Project projectRequest
    ) {

        projectRequest.setUpdatedAt(LocalDateTime.now());

        int affectedRows = projectRepository.updateProject(
                projectId,
                projectRequest
        );

        if (affectedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project not found with ID: " + projectId
            );
        }

        projectRequest.setProjectId(projectId);

        projectLocationRepository.deleteLocationsByProjectId(projectId);
        projectLocationRepository.insertLocations(projectId, projectRequest.getLocations());

        return projectRequest;
    }


}
