package com.rr.erp.service;

import com.rr.erp.dto.CutReturnResultLine;
import com.rr.erp.dto.IntraProjectIssueItemSource;
import com.rr.erp.entity.IntraProjectIssueReturn;
import com.rr.erp.entity.IntraProjectIssueReturnItem;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.IntraProjectIssueRepository;
import com.rr.erp.repository.IntraProjectIssueReturnRepository;
import com.rr.erp.util.DimensionalItems;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IntraProjectIssueReturnService {

    private final IntraProjectIssueReturnRepository repository;
    private final IntraProjectIssueRepository issueRepository;
    private final ProjectStoreService projectStoreService;
    private final StockBatchService stockBatchService;
    private final AssetRepository assetRepository;

    public IntraProjectIssueReturnService(
            IntraProjectIssueReturnRepository repository,
            IntraProjectIssueRepository issueRepository,
            ProjectStoreService projectStoreService,
            StockBatchService stockBatchService,
            AssetRepository assetRepository
    ) {
        this.repository = repository;
        this.issueRepository = issueRepository;
        this.projectStoreService = projectStoreService;
        this.stockBatchService = stockBatchService;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public IntraProjectIssueReturn createReturn(IntraProjectIssueReturn stockReturn) {

        UUID intraProjectIssueReturnId = UUID.randomUUID();
        stockReturn.setIntraProjectIssueReturnId(intraProjectIssueReturnId);

        if (stockReturn.getIsApproved() == null) {
            stockReturn.setIsApproved(false);
        }

        normalizeFromAssetCode(stockReturn);

        List<IntraProjectIssueItemSource> sources = validateReturnQuantities(stockReturn, null);

        repository.createReturn(stockReturn);

        if (stockReturn.getItems() != null) {

            // Stock only moves once a return is Approved — a newly created return is
            // always Pending (see the return form), so this normally does nothing at
            // create time; it stays a real check in case a return is ever created
            // pre-approved by another caller.
            boolean applyStock = Boolean.TRUE.equals(stockReturn.getIsApproved());

            Map<UUID, List<CutLineWithSource>> cutLinesBySourceItem = new LinkedHashMap<>();

            for (int i = 0; i < stockReturn.getItems().size(); i++) {

                IntraProjectIssueReturnItem item = stockReturn.getItems().get(i);
                item.setIntraProjectIssueReturnItemId(UUID.randomUUID());

                if (applyStock) {
                    applyToStockOrCollectCut(stockReturn, item, sources.get(i), cutLinesBySourceItem);
                }

                repository.createReturnItem(intraProjectIssueReturnId, item);
            }

            if (applyStock) {
                processCutReturns(stockReturn, intraProjectIssueReturnId, cutLinesBySourceItem);
            }
        }

        return stockReturn;
    }

    @Transactional
    public IntraProjectIssueReturn updateReturn(UUID intraProjectIssueReturnId, IntraProjectIssueReturn stockReturn) {

        IntraProjectIssueReturn existing = repository.findById(intraProjectIssueReturnId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Intra project issue return not found: " + intraProjectIssueReturnId
                ));

        // Once approved, a return's stock has already been credited back and it is no
        // longer editable — enforced here too so nothing can re-apply stock for it twice.
        if (Boolean.TRUE.equals(existing.getIsApproved())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Intra project issue return " + intraProjectIssueReturnId + " has already been approved and can no longer be edited."
            );
        }

        stockReturn.setIntraProjectIssueId(existing.getIntraProjectIssueId());
        stockReturn.setIssuedProjectCode(existing.getIssuedProjectCode());
        stockReturn.setReturnType(existing.getReturnType());
        stockReturn.setSubcontractorId(existing.getSubcontractorId());
        stockReturn.setEmployeeCode(existing.getEmployeeCode());
        stockReturn.setJobCardId(existing.getJobCardId());
        normalizeFromAssetCode(stockReturn);

        List<IntraProjectIssueItemSource> sources = stockReturn.getItems() != null
                ? validateReturnQuantities(stockReturn, intraProjectIssueReturnId)
                : null;

        int updated = repository.updateReturn(intraProjectIssueReturnId, stockReturn);

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Intra project issue return not found: " + intraProjectIssueReturnId
            );
        }

        if (stockReturn.getItems() != null) {

            repository.deleteItems(intraProjectIssueReturnId);

            // existing was confirmed not-yet-approved above, so isApproved being true here
            // means this call is exactly the Pending -> Approved transition — the one
            // moment stock should move. A plain edit (still Pending) must not touch
            // project_store at all.
            boolean applyStock = Boolean.TRUE.equals(stockReturn.getIsApproved());

            Map<UUID, List<CutLineWithSource>> cutLinesBySourceItem = new LinkedHashMap<>();

            for (int i = 0; i < stockReturn.getItems().size(); i++) {

                IntraProjectIssueReturnItem item = stockReturn.getItems().get(i);
                item.setIntraProjectIssueReturnItemId(UUID.randomUUID());

                if (applyStock) {
                    applyToStockOrCollectCut(stockReturn, item, sources.get(i), cutLinesBySourceItem);
                }

                repository.createReturnItem(intraProjectIssueReturnId, item);
            }

            if (applyStock) {
                processCutReturns(stockReturn, intraProjectIssueReturnId, cutLinesBySourceItem);
            }
        }

        stockReturn.setIntraProjectIssueReturnId(intraProjectIssueReturnId);

        return stockReturn;
    }

    // The asset the returned items were issued for is optional; a blank value is stored as
    // NULL, and a given one must be a registered asset.
    private void normalizeFromAssetCode(IntraProjectIssueReturn stockReturn) {

        if (stockReturn.getFromAssetCode() == null || stockReturn.getFromAssetCode().isBlank()) {
            stockReturn.setFromAssetCode(null);
            return;
        }

        String fromAssetCode = stockReturn.getFromAssetCode().trim();
        if (!assetRepository.existsByAssetCode(fromAssetCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + fromAssetCode);
        }
        stockReturn.setFromAssetCode(fromAssetCode);
    }

    public List<IntraProjectIssueReturn> getReturnsByProject(String issuedProjectCode) {

        List<IntraProjectIssueReturn> returns = repository.getReturnsByProject(issuedProjectCode);
        addItems(returns);
        return returns;
    }

    public List<IntraProjectIssueReturn> getReturnsByIssue(UUID intraProjectIssueId) {

        List<IntraProjectIssueReturn> returns = repository.getReturnsByIssue(intraProjectIssueId);
        addItems(returns);
        return returns;
    }

    public List<IntraProjectIssueReturn> getAllReturns() {

        List<IntraProjectIssueReturn> returns = repository.getAllReturns();
        addItems(returns);
        return returns;
    }

    public List<IntraProjectIssueReturn> getReturnsByJobCard(UUID jobCardId) {

        List<IntraProjectIssueReturn> returns = repository.getReturnsByJobCard(jobCardId);
        addItems(returns);
        return returns;
    }

    private void addItems(List<IntraProjectIssueReturn> returns) {
        for (IntraProjectIssueReturn stockReturn : returns) {
            stockReturn.setItems(repository.getItems(stockReturn.getIntraProjectIssueReturnId()));
        }
    }

    /**
     * Each return line normally carries its own intraProjectIssueItemId (items on the same
     * return may come from different source issues — a subcontractor or employee can hold
     * items issued across several intra project issues). A line can never exceed what was
     * actually issued on that source line, minus whatever has already been returned
     * against it (excluding the return being edited, for the update path). Returns the
     * resolved source for each item, in the same order, for applyToStock to reuse without
     * looking it up twice.
     *
     * JOB_CARD returns are the one exception: a line may omit intraProjectIssueItemId
     * entirely, for a spare part that was removed from the job card's asset and is being
     * returned to stock without ever having been issued via any intra project issue. Such a
     * line has no outstanding quantity to check against — see resolveUnsourcedJobCardItem.
     */
    private List<IntraProjectIssueItemSource> validateReturnQuantities(
            IntraProjectIssueReturn stockReturn,
            UUID excludingReturnId
    ) {

        if (stockReturn.getItems() == null) {
            return List.of();
        }

        boolean allowUnsourcedItems = "JOB_CARD".equals(stockReturn.getReturnType());

        return stockReturn.getItems().stream().map(item -> {

            if (item.getIntraProjectIssueItemId() == null) {

                if (!allowUnsourcedItems) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item " + item.getItemCode() + " is missing its source issue line.");
                }

                return resolveUnsourcedJobCardItem(stockReturn, item);
            }

            IntraProjectIssueItemSource source = issueRepository.findItemSource(item.getIntraProjectIssueItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Source issue line not found for item " + item.getItemCode() + "."
                    ));

            BigDecimal issuedQty = source.getQuantity() != null ? source.getQuantity() : BigDecimal.ZERO;
            BigDecimal alreadyReturnedQty = repository.getReturnedQuantityForItem(item.getIntraProjectIssueItemId(), excludingReturnId);
            BigDecimal outstandingQty = issuedQty.subtract(alreadyReturnedQty);

            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return quantity must be greater than zero for item " + item.getItemCode() + ".");
            }

            // A cut-return line's quantity is a piece count of a NEW size born from cutting
            // the issued bars — not directly comparable to the outstanding piece count of
            // the ORIGINAL issued size, so the usual "quantity <= outstanding" check would
            // reject perfectly valid cuts (e.g. 4 one-metre offcuts from 2 six-metre bars).
            // Cut lines are instead bounded length-wise in StockBatchService#recordCutReturn
            // (against the original issue's real stock_action_allocation history), which is
            // the "parallel/adjusted path" this line needs.
            if (!isCutReturn(item, source) && item.getQuantity().compareTo(outstandingQty) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot return " + item.getQuantity() + " of " + item.getItemCode()
                                + " — only " + outstandingQty + " is outstanding on that issue."
                );
            }

            // An asset line carries the asset code of the issued line it returns.
            if (source.getAssetCode() != null && !source.getAssetCode().isBlank()) {
                item.setAssetCode(source.getAssetCode());
                item.setItemCode(null);
            }

            return source;
        }).toList();
    }

    /**
     * Builds a synthetic source for a JOB_CARD return line with no intraProjectIssueItemId —
     * a spare part removed from the job card's asset and returned straight to stock, never
     * issued via any intra project issue. There is no outstanding quantity to check it
     * against, only that it names a real item and a positive quantity. The synthetic
     * source's lengthM/widthM are copied from the line itself (rather than left null) so
     * isCutReturn compares them as equal and treats the line as an ordinary return, not a
     * cut — there is no original size for it to have been cut from.
     */
    private IntraProjectIssueItemSource resolveUnsourcedJobCardItem(
            IntraProjectIssueReturn stockReturn,
            IntraProjectIssueReturnItem item
    ) {

        if (item.getItemCode() == null || item.getItemCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item code is required for a return line with no source issue.");
        }

        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return quantity must be greater than zero for item " + item.getItemCode() + ".");
        }

        IntraProjectIssueItemSource source = new IntraProjectIssueItemSource();
        source.setIssuedProjectCode(stockReturn.getIssuedProjectCode());
        source.setItemCode(item.getItemCode());
        source.setLengthM(item.getLengthM());
        source.setWidthM(item.getWidthM());

        return source;
    }

    /**
     * A dimensional-item return line (see com.rr.erp.util.DimensionalItems) whose size
     * differs from the size it was originally issued at — i.e. the worker cut the issued
     * bars and is returning offcuts of a new size, rather than unused stock of the same
     * size. Compared null-safely since a length-only item leaves widthM null on both sides.
     */
    private boolean isCutReturn(IntraProjectIssueReturnItem item, IntraProjectIssueItemSource source) {

        if (!DimensionalItems.isDimensionalItem(item.getItemCode()) || item.getLengthM() == null) {
            return false;
        }

        return !bigDecimalsEqual(item.getLengthM(), source.getLengthM())
                || !bigDecimalsEqual(item.getWidthM(), source.getWidthM());
    }

    private boolean bigDecimalsEqual(BigDecimal a, BigDecimal b) {

        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    /**
     * Applies one return line to stock: an ordinary line (non-dimensional, or a
     * dimensional line returning the same size it was issued at) is credited back
     * immediately via receiveNewBatch, same as before. A cut-return line (see
     * {@link #isCutReturn}) is instead collected into {@code cutLinesBySourceItem},
     * grouped by its source issue item, so {@link #processCutReturns} can hand the
     * whole group — potentially several differently-sized result lines cut from the
     * same issued bars — to StockBatchService#recordCutReturn in one call, which is
     * what lets wastage be computed across the group rather than per line.
     */
    private void applyToStockOrCollectCut(
            IntraProjectIssueReturn stockReturn,
            IntraProjectIssueReturnItem item,
            IntraProjectIssueItemSource source,
            Map<UUID, List<CutLineWithSource>> cutLinesBySourceItem
    ) {

        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // An asset line was never deducted from the quantity stock when it was issued, so
        // returning it must not credit stock either - the return itself is the record.
        if (source.getAssetCode() != null && !source.getAssetCode().isBlank()) {
            return;
        }

        if (isCutReturn(item, source)) {
            cutLinesBySourceItem
                    .computeIfAbsent(item.getIntraProjectIssueItemId(), key -> new ArrayList<>())
                    .add(new CutLineWithSource(item, source));
            return;
        }

        boolean dimensional = DimensionalItems.isDimensionalItem(item.getItemCode());

        // Each item is credited back to the project that actually issued it — not the
        // return's own issuedProjectCode — since a subcontractor/employee return can
        // combine items originally issued from different projects.
        if (dimensional) {
            // quantity is the piece/bar count of the (unchanged) issued size, same
            // convention as GRN/GIN/intra-project-issue for these items.
            projectStoreService.receiveNewBatch(
                    source.getIssuedProjectCode(),
                    item.getItemCode(),
                    item.getQuantity(),
                    stockReturn.getReturnDate(),
                    item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO,
                    StockBatchService.SOURCE_INTRA_PROJECT_ISSUE_RETURN,
                    stockReturn.getIntraProjectIssueReturnId(),
                    stockReturn.getIntraProjectIssueReturnCode(),
                    item.getLengthM(),
                    item.getWidthM(),
                    item.getQuantity().intValue(),
                    item.getDescription()
            );
        } else {
            projectStoreService.receiveNewBatch(
                    source.getIssuedProjectCode(),
                    item.getItemCode(),
                    item.getQuantity(),
                    stockReturn.getReturnDate(),
                    item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO,
                    StockBatchService.SOURCE_INTRA_PROJECT_ISSUE_RETURN,
                    stockReturn.getIntraProjectIssueReturnId(),
                    stockReturn.getIntraProjectIssueReturnCode(),
                    null, null, null,
                    item.getDescription()
            );
        }
    }

    /**
     * Hands each group of cut-return lines (grouped by the source issue item they were
     * cut from) to StockBatchService#recordCutReturn. Wastage per group is computed here,
     * across this submission only (issued length minus this submission's cut results) —
     * it does not account for same-size returns of the same source item made in a
     * different, earlier submission, since intra_project_issue_return_item's quantity
     * mixes units across ordinary and cut lines in a way that can't be summed reliably
     * once cutting is involved (see the comment in validateReturnQuantities). This is a
     * simplification: recordCutReturn re-validates the group against the source issue's
     * real stock_action_allocation history (a reliable, unit-consistent length total) and
     * throws if the group's results are impossibly large, which is the case that actually
     * matters for data integrity.
     */
    private void processCutReturns(
            IntraProjectIssueReturn stockReturn,
            UUID intraProjectIssueReturnId,
            Map<UUID, List<CutLineWithSource>> cutLinesBySourceItem
    ) {

        for (Map.Entry<UUID, List<CutLineWithSource>> entry : cutLinesBySourceItem.entrySet()) {

            List<CutLineWithSource> group = entry.getValue();
            IntraProjectIssueItemSource source = group.get(0).source;

            BigDecimal issuedLength = lineQty(source.getQuantity(), source.getLengthM(), source.getWidthM());

            List<CutReturnResultLine> resultLines = new ArrayList<>();
            BigDecimal resultTotal = BigDecimal.ZERO;

            for (CutLineWithSource cutLine : group) {

                IntraProjectIssueReturnItem item = cutLine.item;

                resultLines.add(new CutReturnResultLine(
                        item.getLengthM(),
                        item.getWidthM(),
                        item.getQuantity().intValue(),
                        item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO
                ));

                resultTotal = resultTotal.add(lineQty(item.getQuantity(), item.getLengthM(), item.getWidthM()));
            }

            BigDecimal wastage = issuedLength.subtract(resultTotal);

            stockBatchService.recordCutReturn(
                    source.getIssuedProjectCode(),
                    source.getItemCode(),
                    source.getIntraProjectIssueId(),
                    intraProjectIssueReturnId,
                    stockReturn.getIntraProjectIssueReturnCode(),
                    stockReturn.getReturnDate(),
                    resultLines,
                    wastage
            );

            // recordCutReturn only opens the resulting batch(es) — project_store's own
            // quantity_on_hand ledger (what issuing validates against, and what the Stock
            // page shows) is a separate table and needs crediting here, by the total pieces
            // actually produced (wastage never reaches project_store — it left the batch
            // system entirely).
            BigDecimal totalPiecesReturned = resultLines.stream()
                    .map(line -> BigDecimal.valueOf(line.getPieceCount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            projectStoreService.creditQuantityOnly(
                    source.getIssuedProjectCode(),
                    source.getItemCode(),
                    totalPiecesReturned,
                    stockReturn.getReturnDate()
            );
        }
    }

    private BigDecimal lineQty(BigDecimal pieceCount, BigDecimal lengthM, BigDecimal widthM) {

        BigDecimal perPiece = widthM != null ? lengthM.multiply(widthM) : lengthM;

        return perPiece.multiply(pieceCount);
    }

    /** Pairs a cut-return line with its already-resolved source, for grouping by source issue item. */
    private static final class CutLineWithSource {

        private final IntraProjectIssueReturnItem item;
        private final IntraProjectIssueItemSource source;

        private CutLineWithSource(IntraProjectIssueReturnItem item, IntraProjectIssueItemSource source) {
            this.item = item;
            this.source = source;
        }
    }
}
