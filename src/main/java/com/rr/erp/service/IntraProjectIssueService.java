package com.rr.erp.service;

import com.rr.erp.entity.IntraProjectIssue;
import com.rr.erp.entity.IntraProjectIssueItem;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.IntraProjectIssueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class IntraProjectIssueService {

    private static final String TYPE_GENERAL = "GENERAL";
    private static final String TYPE_SUBCONTRACTOR = "SUBCONTRACTOR";
    private static final String TYPE_PERSONAL = "PERSONAL";
    private static final String TYPE_LOAN = "LOAN";
    private static final String TYPE_JOB_CARD = "JOB_CARD";

    private final IntraProjectIssueRepository repository;
    private final ProjectStoreService projectStoreService;
    private final AssetRepository assetRepository;

    public IntraProjectIssueService(IntraProjectIssueRepository repository, ProjectStoreService projectStoreService,
                                    AssetRepository assetRepository) {
        this.repository = repository;
        this.projectStoreService = projectStoreService;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public IntraProjectIssue createIntraProjectIssue(IntraProjectIssue issue) {

        UUID intraProjectIssueId = UUID.randomUUID();
        issue.setIntraProjectIssueId(intraProjectIssueId);

        if (issue.getIssueType() == null || issue.getIssueType().isBlank()) {
            issue.setIssueType(TYPE_GENERAL);
        }

        if (issue.getIsAuthorized() == null) {
            issue.setIsAuthorized(false);
        }

        if (issue.getIsIssued() == null) {
            issue.setIsIssued(true);
        }

        if (issue.getIsReceived() == null) {
            issue.setIsReceived(false);
        }

        validateIssueType(issue);
        normalizeForAssetCode(issue);

        repository.createIntraProjectIssue(issue);

        if (issue.getItems() != null) {

            for (IntraProjectIssueItem item : issue.getItems()) {

                item.setIntraProjectIssueItemId(UUID.randomUUID());
                item.setIntraProjectIssueId(intraProjectIssueId);
                normalizeAssetLine(item);

                repository.createIntraProjectIssueItem(item);
            }

            // GENERAL / PERSONAL / LOAN deduct stock immediately, same as before this issue
            // type ever existed. SUBCONTRACTOR and JOB_CARD mirror GIN's Pending -> Authorized
            // gate: a newly created issue of either type is always Pending (see
            // IntraProjectIssueFormPage.tsx), so this normally does nothing at create time
            // for those types; it stays a real check in case one is ever created
            // pre-authorized by another caller.
            boolean issueStockNow = (!TYPE_SUBCONTRACTOR.equals(issue.getIssueType())
                    && !TYPE_JOB_CARD.equals(issue.getIssueType()))
                    || Boolean.TRUE.equals(issue.getIsAuthorized());

            if (issueStockNow) {
                issueItems(issue, intraProjectIssueId);
            }
        }

        return issue;
    }

    @Transactional
    public IntraProjectIssue updateIntraProjectIssue(UUID intraProjectIssueId, IntraProjectIssue issue) {

        IntraProjectIssue existing = repository.findById(intraProjectIssueId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Intra project issue not found: " + intraProjectIssueId
                ));

        // A subcontractor or job card issue's stock is deducted the moment it is authorized —
        // once that has happened it is no longer editable, so nothing can re-deduct stock for
        // it a second time. Other issue types deduct stock at creation, so this guard only
        // applies to SUBCONTRACTOR and JOB_CARD.
        boolean isGatedType = TYPE_SUBCONTRACTOR.equals(existing.getIssueType()) || TYPE_JOB_CARD.equals(existing.getIssueType());
        if (isGatedType && Boolean.TRUE.equals(existing.getIsAuthorized())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Intra project issue " + intraProjectIssueId + " has already been authorized and can no longer be edited."
            );
        }

        if (issue.getIssueType() == null || issue.getIssueType().isBlank()) {
            issue.setIssueType(existing.getIssueType());
        }

        validateIssueType(issue);
        normalizeForAssetCode(issue);

        int updatedRows = repository.updateIntraProjectIssue(intraProjectIssueId, issue);

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Intra project issue not found: " + intraProjectIssueId
            );
        }

        issue.setIntraProjectIssueId(intraProjectIssueId);

        if (issue.getItems() != null) {

            repository.deleteIntraProjectIssueItems(intraProjectIssueId);

            for (IntraProjectIssueItem item : issue.getItems()) {
                item.setIntraProjectIssueItemId(UUID.randomUUID());
                item.setIntraProjectIssueId(intraProjectIssueId);
                normalizeAssetLine(item);
                repository.createIntraProjectIssueItem(item);
            }

            // existing was confirmed not-yet-authorized above, so for a SUBCONTRACTOR or
            // JOB_CARD issue, isAuthorized being true here means this call is exactly the
            // Pending -> Authorized transition -- the one moment stock should be deducted.
            // A plain edit (still Pending) must not touch project_store at all.
            boolean isGatedTypeNow = TYPE_SUBCONTRACTOR.equals(issue.getIssueType()) || TYPE_JOB_CARD.equals(issue.getIssueType());
            if (isGatedTypeNow && Boolean.TRUE.equals(issue.getIsAuthorized())) {
                issueItems(issue, intraProjectIssueId);
            }
        }

        return issue;
    }

    public List<IntraProjectIssue> getIntraProjectIssues(String issuedProjectCode) {
        return repository.getIntraProjectIssues(issuedProjectCode);
    }

    public List<IntraProjectIssue> getAllIntraProjectIssues() {
        return repository.getAllIntraProjectIssues();
    }

    public List<IntraProjectIssue> getByJobCardId(UUID jobCardId) {
        return repository.getByJobCardId(jobCardId);
    }

    private void issueItems(IntraProjectIssue issue, UUID intraProjectIssueId) {

        for (IntraProjectIssueItem item : issue.getItems()) {

            // A line carrying an asset code hands one specific registered asset to the recipient.
            // It only records the issue - the asset's location is not touched and the quantity
            // stock engine is bypassed - but the same asset cannot be issued again until returned.
            if (item.getAssetCode() != null && !item.getAssetCode().isBlank()) {
                String assetCode = item.getAssetCode().trim();
                if (repository.countOutstandingIssuesForAsset(assetCode, intraProjectIssueId) > 0) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Asset " + assetCode + " is already issued and has not been returned."
                    );
                }
                continue;
            }

            // Non-stock items: the user picked exactly which batch/description to draw from
            // (see IntraProjectIssueItem#stockBatchId) — draw from that one batch instead of
            // blind FIFO.
            if (item.getStockBatchId() != null) {
                projectStoreService.issueFromBatch(
                        item.getStockBatchId(),
                        issue.getIssuedProjectCode(),
                        item.getItemCode(),
                        item.getQuantity(),
                        issue.getIssuedDate().toLocalDate(),
                        StockBatchService.ACTION_INTRA_PROJECT_ISSUE,
                        intraProjectIssueId,
                        item.getIntraProjectIssueItemId()
                );
                continue;
            }

            // Dimensional items (see com.rr.erp.util.DimensionalItems) carry which exact
            // size is being issued — null for non-dimensional items, so this scopes FIFO
            // only when relevant and otherwise behaves exactly as before.
            projectStoreService.issueGoods(
                    issue.getIssuedProjectCode(),
                    item.getItemCode(),
                    item.getQuantity(),
                    issue.getIssuedDate().toLocalDate(),
                    StockBatchService.ACTION_INTRA_PROJECT_ISSUE,
                    intraProjectIssueId,
                    item.getIntraProjectIssueItemId(),
                    item.getLengthM(),
                    item.getWidthM()
            );
        }
    }

    // An asset line is always exactly one asset: no item code, quantity 1, no price.
    private void normalizeAssetLine(IntraProjectIssueItem item) {

        if (item.getAssetCode() == null || item.getAssetCode().isBlank()) {
            return;
        }

        item.setAssetCode(item.getAssetCode().trim());
        item.setItemCode(null);
        item.setQuantity(java.math.BigDecimal.ONE);
        item.setUnitPrice(java.math.BigDecimal.ZERO);
        item.setAmount(java.math.BigDecimal.ZERO);
    }

    // The asset the items are issued for is optional; a blank value is stored as NULL, and a
    // given one must be a registered asset.
    private void normalizeForAssetCode(IntraProjectIssue issue) {

        if (issue.getForAssetCode() == null || issue.getForAssetCode().isBlank()) {
            issue.setForAssetCode(null);
            return;
        }

        String forAssetCode = issue.getForAssetCode().trim();
        if (!assetRepository.existsByAssetCode(forAssetCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + forAssetCode);
        }
        issue.setForAssetCode(forAssetCode);
    }

    /**
     * Each issue type sends goods to a different kind of recipient, so each needs the
     * opposite set of fields required — mirrors StockReturnService.validateReturnType.
     */
    private void validateIssueType(IntraProjectIssue issue) {

        String type = issue.getIssueType();

        if (TYPE_GENERAL.equals(type)) {
            if (issue.getReceivedProjectPhaseId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiving project phase is required for a general issue.");
            }
            if (issue.getReceivedBy() == null || issue.getReceivedBy().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received by is required for a general issue.");
            }
        } else if (TYPE_SUBCONTRACTOR.equals(type)) {
            if (issue.getSubcontractorId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcontractor is required for a subcontractor issue.");
            }
        } else if (TYPE_PERSONAL.equals(type)) {
            if (issue.getReceivedBy() == null || issue.getReceivedBy().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiving employee is required for a personal issue.");
            }
        } else if (TYPE_LOAN.equals(type)) {
            boolean hasEmployee = issue.getReceivedBy() != null && !issue.getReceivedBy().isBlank();
            boolean hasOutsider = issue.getReceiverName() != null && !issue.getReceiverName().isBlank()
                    && issue.getReceiverNic() != null && !issue.getReceiverNic().isBlank();
            if (!hasEmployee && !hasOutsider) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A loan issue needs either a receiving employee or an outside recipient's name and NIC.");
            }
            if (issue.getExpectedReturnDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected return date is required for a loan issue.");
            }
        } else if (TYPE_JOB_CARD.equals(type)) {
            if (issue.getJobCardId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job card is required for a job card issue.");
            }
            if (issue.getReceivedBy() == null || issue.getReceivedBy().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received by is required for a job card issue.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown issue type: " + type);
        }
    }
}
