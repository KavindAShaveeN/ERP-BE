package com.rr.erp.service;

import com.rr.erp.entity.ProjectPhase;
import com.rr.erp.repository.ProjectPhaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectPhaseService {

    private final ProjectPhaseRepository projectPhaseRepository;

    public ProjectPhaseService(ProjectPhaseRepository projectPhaseRepository) {
        this.projectPhaseRepository = projectPhaseRepository;
    }

    public Integer createProjectPhase(ProjectPhase projectPhase) {
        validateParent(projectPhase);
        return projectPhaseRepository.createProjectPhase(projectPhase);
    }

    public List<ProjectPhase> getProjectPhasesByProjectId(Integer projectId) {
        return projectPhaseRepository.getProjectPhasesByProjectId(projectId);
    }

    public List<ProjectPhase> getAllProjectPhases() {
        return projectPhaseRepository.getAllProjectPhases();
    }

    public boolean updateProjectPhase(
            Integer projectPhaseId,
            ProjectPhase projectPhase
    ) {

        if (!projectPhaseRepository.existsById(projectPhaseId)) {
            return false;
        }

        projectPhase.setProjectPhaseId(projectPhaseId);
        validateParent(projectPhase);

        return projectPhaseRepository.updateProjectPhase(
                projectPhaseId,
                projectPhase
        ) > 0;
    }

    /** A phase's parent must be another phase of the same project, and nesting it
     * there must not create a cycle (a phase can't become its own ancestor). */
    private void validateParent(ProjectPhase projectPhase) {
        Integer parentId = projectPhase.getParentProjectPhaseId();
        if (parentId == null) {
            return;
        }

        if (parentId.equals(projectPhase.getProjectPhaseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A phase cannot be its own parent");
        }

        List<ProjectPhase> projectPhases = projectPhaseRepository.getProjectPhasesByProjectId(projectPhase.getProjectId());
        Map<Integer, Integer> parentById = new HashMap<>();
        for (ProjectPhase existing : projectPhases) {
            parentById.put(existing.getProjectPhaseId(), existing.getParentProjectPhaseId());
        }

        if (!parentById.containsKey(parentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent phase not found in this project");
        }

        Integer current = parentId;
        while (current != null) {
            if (current.equals(projectPhase.getProjectPhaseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This would create a circular phase hierarchy");
            }
            current = parentById.get(current);
        }
    }

    public boolean deleteProjectPhase(Integer projectPhaseId) {

        if (!projectPhaseRepository.existsById(projectPhaseId)) {
            return false;
        }

        return projectPhaseRepository.deleteProjectPhase(projectPhaseId) > 0;
    }
}