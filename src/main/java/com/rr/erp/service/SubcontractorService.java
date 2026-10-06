package com.rr.erp.service;

import com.rr.erp.entity.Subcontractor;
import com.rr.erp.repository.SubcontractorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubcontractorService {

    private final SubcontractorRepository subcontractorRepository;

    public SubcontractorService(SubcontractorRepository subcontractorRepository) {
        this.subcontractorRepository = subcontractorRepository;
    }

    @Transactional
    public Integer createSubcontractor(Subcontractor subcontractor) {

        if (subcontractor.getIsActive() == null) {
            subcontractor.setIsActive(true);
        }

        Integer subcontractorId = subcontractorRepository.createSubcontractor(subcontractor);

        if (subcontractor.getProjectCodes() != null) {
            for (String projectCode : subcontractor.getProjectCodes()) {
                subcontractorRepository.addProjectAssignment(subcontractorId, projectCode);
            }
        }

        return subcontractorId;
    }

    public List<Subcontractor> getAllSubcontractors() {
        return subcontractorRepository.getAllSubcontractors();
    }

    public List<Subcontractor> getSubcontractorsByProjectCode(String projectCode) {
        return subcontractorRepository.getSubcontractorsByProjectCode(projectCode);
    }

    public Optional<Subcontractor> getSubcontractorById(Integer subcontractorId) {
        return subcontractorRepository.getSubcontractorById(subcontractorId);
    }

    @Transactional
    public boolean updateSubcontractor(Integer subcontractorId, Subcontractor subcontractor) {

        if (!subcontractorRepository.existsById(subcontractorId)) {
            return false;
        }

        if (subcontractor.getIsActive() == null) {
            subcontractor.setIsActive(true);
        }

        boolean updated = subcontractorRepository.updateSubcontractor(
                subcontractorId,
                subcontractor
        ) > 0;

        // Replace the full set of project assignments with the new list
        subcontractorRepository.deleteProjectAssignments(subcontractorId);

        if (subcontractor.getProjectCodes() != null) {
            for (String projectCode : subcontractor.getProjectCodes()) {
                subcontractorRepository.addProjectAssignment(subcontractorId, projectCode);
            }
        }

        return updated;
    }

    public boolean deleteSubcontractor(Integer subcontractorId) {

        if (!subcontractorRepository.existsById(subcontractorId)) {
            return false;
        }

        return subcontractorRepository.deleteSubcontractor(subcontractorId) > 0;
    }
}
