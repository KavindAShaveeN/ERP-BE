package com.rr.erp.service;

import com.rr.erp.entity.SubcontractorProjectAssignment;
import com.rr.erp.repository.SubcontractorProjectRepository;
import com.rr.erp.repository.SubcontractorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SubcontractorProjectService {

    private final SubcontractorProjectRepository subcontractorProjectRepository;
    private final SubcontractorRepository subcontractorRepository;

    public SubcontractorProjectService(
            SubcontractorProjectRepository subcontractorProjectRepository,
            SubcontractorRepository subcontractorRepository
    ) {
        this.subcontractorProjectRepository = subcontractorProjectRepository;
        this.subcontractorRepository = subcontractorRepository;
    }

    public List<SubcontractorProjectAssignment> getAssignmentsByProjectCode(String projectCode) {
        return subcontractorProjectRepository.getAssignmentsByProjectCode(projectCode);
    }

    public void assign(Integer subcontractorId, String projectCode, String contractLink) {

        if (!subcontractorRepository.existsById(subcontractorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcontractor not found");
        }

        subcontractorProjectRepository.assign(subcontractorId, projectCode, contractLink);
    }

    public boolean unassign(Integer subcontractorId, String projectCode) {

        if (!subcontractorProjectRepository.existsAssignment(subcontractorId, projectCode)) {
            return false;
        }

        return subcontractorProjectRepository.unassign(subcontractorId, projectCode) > 0;
    }
}
