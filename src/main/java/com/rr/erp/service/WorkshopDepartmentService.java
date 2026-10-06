package com.rr.erp.service;

import com.rr.erp.entity.WorkshopDepartment;
import com.rr.erp.repository.WorkshopDepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopDepartmentService {

    private final WorkshopDepartmentRepository workshopDepartmentRepository;

    public List<WorkshopDepartment> getAllWorkshopDepartments() {

        return workshopDepartmentRepository.getAllWorkshopDepartments();
    }
}
