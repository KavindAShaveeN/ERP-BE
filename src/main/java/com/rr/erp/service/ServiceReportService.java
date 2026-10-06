package com.rr.erp.service;

import com.rr.erp.dto.JobCostSummaryRow;
import com.rr.erp.dto.LaborEntryRow;
import com.rr.erp.dto.ThirdPartyServiceReportRow;
import com.rr.erp.repository.ServiceReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceReportService {

    private final ServiceReportRepository serviceReportRepository;

    public List<JobCostSummaryRow> getJobCostSummaries(LocalDate from, LocalDate to) {

        validateRange(from, to);

        return serviceReportRepository.getJobCostSummaries(from, to);
    }

    public List<LaborEntryRow> getLaborEntries(LocalDate from, LocalDate to) {

        validateRange(from, to);

        return serviceReportRepository.getLaborEntries(from, to);
    }

    public List<ThirdPartyServiceReportRow> getThirdPartyServiceEntries(LocalDate from, LocalDate to) {

        validateRange(from, to);

        return serviceReportRepository.getThirdPartyServiceEntries(from, to);
    }

    private void validateRange(LocalDate from, LocalDate to) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' date must not be after 'to' date"
            );
        }
    }
}
