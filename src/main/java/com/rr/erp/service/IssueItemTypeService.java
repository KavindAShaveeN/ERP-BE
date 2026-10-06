package com.rr.erp.service;

import com.rr.erp.entity.IssueItemType;
import com.rr.erp.repository.IssueItemTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssueItemTypeService {

    private final IssueItemTypeRepository issueItemTypeRepository;

    public IssueItemTypeService(IssueItemTypeRepository issueItemTypeRepository) {
        this.issueItemTypeRepository = issueItemTypeRepository;
    }

    public List<IssueItemType> getAllIssueItemTypes() {
        return issueItemTypeRepository.getAllIssueItemTypes();
    }
}