package com.rr.erp.service;

import com.rr.erp.entity.Defect;
import com.rr.erp.repository.DefectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DefectService {

    private final DefectRepository defectRepository;
    private final FaultWorkflowService faultWorkflow;

    public DefectService(DefectRepository defectRepository, FaultWorkflowService faultWorkflow) {
        this.faultWorkflow = faultWorkflow;
        this.defectRepository = defectRepository;
    }


    // =========================================================
    // CREATE MULTIPLE DEFECTS
    // =========================================================

    @Transactional
    public void createDefects(List<Defect> defects) {

        if (defects == null || defects.isEmpty()) {
            throw new RuntimeException("Defect list cannot be empty");
        }

        defectRepository.createDefects(defects);
    }


    // =========================================================
    // UPDATE DEFECT
    // =========================================================

    public void updateDefect(UUID defectId, Defect defect) {
        faultWorkflow.protectDefect(defectId);

        int updatedRows =
                defectRepository.updateDefect(defectId, defect);

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Defect not found with ID: " + defectId
            );
        }
    }


    // =========================================================
    // DELETE DEFECT
    // =========================================================

    public void deleteDefect(UUID defectId) {
        faultWorkflow.protectDefect(defectId);

        int deletedRows =
                defectRepository.deleteDefect(defectId);

        if (deletedRows == 0) {
            throw new RuntimeException(
                    "Defect not found with ID: " + defectId
            );
        }
    }
}