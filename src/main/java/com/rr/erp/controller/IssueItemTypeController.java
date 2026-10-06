package com.rr.erp.controller;

import com.rr.erp.entity.IssueItemType;
import com.rr.erp.service.IssueItemTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issue_item_type")
@CrossOrigin
public class IssueItemTypeController {

    private final IssueItemTypeService issueItemTypeService;

    public IssueItemTypeController(IssueItemTypeService issueItemTypeService) {
        this.issueItemTypeService = issueItemTypeService;
    }

    @GetMapping("/")
    public ResponseEntity<List<IssueItemType>> getAllIssueItemTypes() {

        return ResponseEntity.ok(
                issueItemTypeService.getAllIssueItemTypes()
        );
    }
}
