package com.rr.erp.service;

import com.rr.erp.entity.JobCard;
import com.rr.erp.entity.ServiceReceiveNote;
import com.rr.erp.repository.JobCardRepository;
import com.rr.erp.repository.ServiceReceiveNoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ServiceReceiveNoteService {

    private static final Logger log = LoggerFactory.getLogger(ServiceReceiveNoteService.class);

    private final ServiceReceiveNoteRepository serviceReceiveNoteRepository;
    private final JobCardRepository jobCardRepository;
    private final FaultWorkflowService faultWorkflow;
    private final AssetLocationService assetLocationService;

    private static final Pattern CODE_PATTERN = Pattern.compile("^SRN-.+-(\\d+)$");

    /**
     * Created by the requesting project itself (Site Store → Services) once it has physically
     * received the asset/item back from the workshop — the confirmation half of the job card's
     * "Delivered" dispatch (see JobCardService#handleJobCardDelivered), mirroring how a GRN
     * confirms a GIN. Records the receipt in asset_location and carries the job card's linked
     * service request forward so the project can see what it asked for against what came back.
     */
    public ServiceReceiveNote createServiceReceiveNote(UUID jobCardId, String receivedBy, String remarks) {

        JobCard jobCard = jobCardRepository.getJobCardById(jobCardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Job card not found"
                ));

        if (!Boolean.TRUE.equals(jobCard.getIsDelivered())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Job card " + jobCard.getJobCardCode() + " has not been marked Delivered yet."
            );
        }

        if (serviceReceiveNoteRepository.getByJobCardId(jobCardId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A Service Receive Note already exists for job card " + jobCard.getJobCardCode() + "."
            );
        }

        ServiceReceiveNote note = new ServiceReceiveNote();
        note.setServiceReceiveNoteId(UUID.randomUUID());
        note.setJobCardId(jobCard.getJobCardId());
        note.setJobCardCode(jobCard.getJobCardCode());
        note.setRequestingProjectCode(jobCard.getRequestingProjectCode());
        note.setServiceRequestId(jobCard.getServiceRequestId());
        note.setAssetCode(jobCard.getAssetCode());
        note.setQuantity(BigDecimal.ONE);
        note.setReceivedDate(LocalDateTime.now());
        note.setReceivedBy(receivedBy);
        note.setRemarks(remarks);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        // Same code-collision handling as FuelIssueService.nextFuelIssueCode: derive the next
        // sequence from every code ever issued for this project and retry once if a concurrent
        // request grabbed the same number first.
        for (int attempt = 0; attempt < 3; attempt++) {
            note.setServiceReceiveNoteCode(nextCode(jobCard.getRequestingProjectCode()));
            try {
                serviceReceiveNoteRepository.createServiceReceiveNote(note);
                break;
            } catch (org.springframework.dao.DuplicateKeyException exception) {
                if (attempt == 2) {
                    throw exception;
                }
            }
        }

        String assetCode = jobCard.getAssetCode();

        if (assetCode != null && !assetCode.isBlank()) {
            try {
                assetLocationService.receiveAssetForServiceReceiveNote(
                        assetCode,
                        jobCard.getRequestingProjectCode(),
                        note.getServiceReceiveNoteId(),
                        note.getServiceReceiveNoteCode(),
                        LocalDate.now(),
                        receivedBy
                );
            } catch (Exception exception) {
                log.error(
                        "Failed to record asset {} as received at {} for Service Receive Note {}",
                        assetCode,
                        jobCard.getRequestingProjectCode(),
                        note.getServiceReceiveNoteCode(),
                        exception
                );
            }
        }

        faultWorkflow.enrichNotes(List.of(note));
        return note;
    }

    private String nextCode(String requestingProjectCode) {

        String prefix = "SRN-" + requestingProjectCode;
        int maxSequence = 0;

        for (String code : serviceReceiveNoteRepository.getCodesForProject(requestingProjectCode)) {
            Matcher matcher = CODE_PATTERN.matcher(code);
            if (matcher.matches()) {
                maxSequence = Math.max(maxSequence, Integer.parseInt(matcher.group(1)));
            }
        }

        return prefix + "-" + String.format("%03d", maxSequence + 1);
    }

    public List<JobCard> getPendingForProject(String requestingProjectCode) {
        return faultWorkflow.enrich(jobCardRepository.getPendingServiceReceiveNoteJobCards(requestingProjectCode));
    }

    public List<ServiceReceiveNote> getAll() {
        return faultWorkflow.enrichNotes(serviceReceiveNoteRepository.getAll());
    }

    public List<ServiceReceiveNote> getByRequestingProjectCode(String requestingProjectCode) {
        return faultWorkflow.enrichNotes(serviceReceiveNoteRepository.getByRequestingProjectCode(requestingProjectCode));
    }

    public ServiceReceiveNote getByJobCardId(UUID jobCardId) {
        return serviceReceiveNoteRepository.getByJobCardId(jobCardId).map(note -> faultWorkflow.enrichNotes(List.of(note)).get(0)).orElse(null);
    }

    public ServiceReceiveNote getById(UUID serviceReceiveNoteId) {
        return serviceReceiveNoteRepository.getById(serviceReceiveNoteId).map(note -> faultWorkflow.enrichNotes(List.of(note)).get(0)).orElse(null);
    }
}
