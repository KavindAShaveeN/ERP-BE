package com.rr.erp.dto;

import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.Defect;
import com.rr.erp.entity.JobCostEntry;
import com.rr.erp.entity.JobIssueItemReturn;
import com.rr.erp.entity.ThreePService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class JobCardResponse {
    private JobCard initialDetails;
    private List<Defect> defectList;
    private List<JobWorkerResponse> employeeList;
    private List<JobIssueItemResponse> issueItemList;
    private List<ThreePService> threePServiceList;
    private List<JobCostEntry> jobCostEntryList;
    private List<JobIssueItemReturn> issueItemReturnList;
    private List<JobCard> subJobCards;
}
