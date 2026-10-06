package com.rr.erp.service;

import com.rr.erp.dto.CutReturnResultLine;
import com.rr.erp.dto.StockBatchView;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.entity.StockBatch;
import com.rr.erp.entity.StockBatchSplit;
import com.rr.erp.repository.StockBatchRepository;
import com.rr.erp.repository.StockBatchSplitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The FIFO engine behind project_store. A batch's identity (see {@link StockBatch})
 * is opened once — by a supplier GRN or a positive stock adjustment — and never
 * recreated; internal transfers and stock returns move quantity between that same
 * batch's per-project location rows (the stock_batch_location table) rather than
 * spawning a new batch, so a batch's name and cost survive moving between project
 * stores unchanged. Every stock-reducing action is walked oldest-batch-first until
 * it's covered, recording exactly which batches (and at what cost) it drew from.
 *
 * Called from {@link ProjectStoreService#receiveNewBatch}, {@link ProjectStoreService#creditExistingBatches}
 * and {@link ProjectStoreService#issueGoods} so project_store.quantity_on_hand and the
 * sum of batch-location quantities can never drift apart.
 */
@Service
public class StockBatchService {

    public static final String SOURCE_SUPPLIER_GRN = "SUPPLIER_GRN";
    public static final String SOURCE_INTERNAL_GRN = "INTERNAL_GRN";
    public static final String SOURCE_STOCK_RETURN = "STOCK_RETURN";
    public static final String SOURCE_STOCK_ADJUSTMENT = "STOCK_ADJUSTMENT";
    public static final String SOURCE_INTRA_PROJECT_ISSUE_RETURN = "INTRA_PROJECT_ISSUE_RETURN";
    public static final String SOURCE_PLANT_PRODUCTION = "PLANT_PRODUCTION";
    public static final String SOURCE_JOB_CARD_ITEM_RETURN = "JOB_CARD_ITEM_RETURN";
    /** Pieces produced by cutting an issued dimensional-item batch and returning the offcuts. */
    public static final String SOURCE_CUT_RETURN = "CUT_RETURN";

    /** Tolerance for rounding noise when checking that returned + cut material doesn't exceed what was issued. */
    private static final BigDecimal WASTAGE_TOLERANCE = new BigDecimal("0.001");

    public static final String ACTION_GIN = "GIN";
    public static final String ACTION_INTRA_PROJECT_ISSUE = "INTRA_PROJECT_ISSUE";
    public static final String ACTION_STOCK_ADJUSTMENT = "STOCK_ADJUSTMENT";
    public static final String ACTION_STOCK_RETURN = "STOCK_RETURN";
    public static final String ACTION_PLANT_PRODUCTION = "PLANT_PRODUCTION";
    public static final String ACTION_JOB_CARD_ISSUE = "JOB_CARD_ISSUE";
    public static final String ACTION_FUEL_ISSUE = "FUEL_ISSUE";

    private static final DateTimeFormatter BATCH_CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StockBatchRepository repository;
    private final StockBatchSplitRepository splitRepository;

    public StockBatchService(StockBatchRepository repository, StockBatchSplitRepository splitRepository) {
        this.repository = repository;
        this.splitRepository = splitRepository;
    }

    /**
     * Opens a brand-new batch identity — only for stock that genuinely has no
     * existing lot to attribute it to (a supplier delivery, or a stock count
     * correction). The batch is placed at {@code projectCode} with its full
     * quantity as its first (and initially only) location.
     */
    @Transactional
    public StockBatch openNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            BigDecimal unitCost,
            LocalDate originDate,
            String sourceType,
            UUID sourceId,
            String sourceReference
    ) {
        return openNewBatch(
                projectCode, itemCode, quantity, unitCost, originDate,
                sourceType, sourceId, sourceReference,
                null, null, null, null
        );
    }

    /**
     * Dimensional-item overload (see com.rr.erp.util.DimensionalItems): also records the
     * size/piece-count this batch was opened with — the supplier-received size at GRN time,
     * or a new size born from cutting at cut-return time. {@code lengthValue}/{@code widthValue}/
     * {@code pieceCount} are null for non-dimensional items, in which case this behaves
     * exactly like the plain overload.
     */
    @Transactional
    public StockBatch openNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            BigDecimal unitCost,
            LocalDate originDate,
            String sourceType,
            UUID sourceId,
            String sourceReference,
            BigDecimal lengthValue,
            BigDecimal widthValue,
            Integer pieceCount
    ) {
        return openNewBatch(
                projectCode, itemCode, quantity, unitCost, originDate,
                sourceType, sourceId, sourceReference,
                lengthValue, widthValue, pieceCount, null
        );
    }

    /**
     * Description-aware overload: also records the GRN line's own description at the
     * moment this batch was opened (see {@link StockBatch#getDescription()}), so several
     * different physical items sharing one item_code — mainly non-stock items — can be
     * told apart once they're in stock and picked explicitly at issue time. {@code
     * description} is optional; for stock/consumable/inventory items it's harmless to pass
     * (purely descriptive, never used to scope a FIFO draw).
     */
    @Transactional
    public StockBatch openNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            BigDecimal unitCost,
            LocalDate originDate,
            String sourceType,
            UUID sourceId,
            String sourceReference,
            BigDecimal lengthValue,
            BigDecimal widthValue,
            Integer pieceCount,
            String description
    ) {
        return openNewBatch(
                projectCode, itemCode, quantity, unitCost, originDate,
                sourceType, sourceId, sourceReference,
                lengthValue, widthValue, pieceCount, description, null
        );
    }

    /**
     * Expiry-aware overload: also records the expiry date of special items that expire (see
     * {@link StockBatch#getExpiryDate()}) — taken from the GRN line at receipt, or inherited
     * from the source batch at cut-return time. {@code expiryDate} is optional; null for items
     * that don't expire.
     */
    @Transactional
    public StockBatch openNewBatch(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            BigDecimal unitCost,
            LocalDate originDate,
            String sourceType,
            UUID sourceId,
            String sourceReference,
            BigDecimal lengthValue,
            BigDecimal widthValue,
            Integer pieceCount,
            String description,
            LocalDate expiryDate
    ) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        UUID batchId = UUID.randomUUID();

        StockBatch batch = new StockBatch(
                batchId,
                buildBatchCode(itemCode, originDate),
                itemCode,
                originDate,
                sourceType,
                sourceId,
                sourceReference,
                quantity,
                unitCost != null ? unitCost : BigDecimal.ZERO,
                lengthValue,
                widthValue,
                pieceCount,
                description,
                expiryDate
        );

        repository.insertBatch(batch);
        repository.creditLocation(batchId, projectCode, quantity, pieceCount);

        return batch;
    }

    /**
     * Credits {@code credits} — batches (and quantities) that were just
     * debited elsewhere by a FIFO consumption — onto {@code projectCode}'s
     * location rows for those exact same batches. Used by internal transfers
     * and stock returns so the receiving project ends up holding the same
     * batch identities the sending project just gave up, not a new one.
     */
    @Transactional
    public void creditBatches(String projectCode, List<StockActionAllocation> credits) {

        for (StockActionAllocation credit : credits) {
            repository.creditLocation(credit.getStockBatchId(), projectCode, credit.getQtyTaken());
        }
    }

    /**
     * Consumes {@code quantity} from the oldest remaining batches for this
     * project/item first, recording one allocation per batch touched.
     * Throws if the batches on hand can't cover the full quantity.
     */
    @Transactional
    public List<StockActionAllocation> issueFifo(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            String actionType,
            UUID actionId,
            UUID actionItemId
    ) {
        return issueFifo(projectCode, itemCode, quantity, actionType, actionId, actionItemId, null, null);
    }

    /**
     * Size-scoped variant, for dimensional items (see com.rr.erp.util.DimensionalItems):
     * when {@code lengthValue} is non-null, the FIFO walk only draws from batches of that
     * exact size, and — for a batch whose own piece_count is non-null — also decrements a
     * proportional piece count from that batch's location. Dimensional issuing is assumed
     * to always move whole pieces, so {@code quantity} is assumed to be an exact multiple
     * of the batch's own length within any single batch touched (no fractional-piece
     * semantics are attempted here). {@code lengthValue} null behaves exactly like the
     * unscoped overload.
     */
    @Transactional
    public List<StockActionAllocation> issueFifo(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            String actionType,
            UUID actionId,
            UUID actionItemId,
            BigDecimal lengthValue,
            BigDecimal widthValue
    ) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        List<StockBatchView> batches = repository.lockFifoBatchLocations(projectCode, itemCode, lengthValue, widthValue);

        List<StockActionAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = quantity;

        for (StockBatchView batch : batches) {

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal takenFromBatch = batch.getQtyRemaining().min(remaining);

            if (takenFromBatch.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Integer piecesTaken = null;
            if (batch.getPieceCount() != null && batch.getLengthM() != null
                    && batch.getLengthM().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal[] divideAndRemainder = takenFromBatch.divideAndRemainder(batch.getLengthM());

                // Dimensional issuing always moves whole pieces (see method doc) — a non-zero
                // remainder means the caller asked for a quantity that isn't a whole multiple
                // of this batch's own piece length, which shouldn't happen for these items.
                piecesTaken = divideAndRemainder[0].intValueExact();
            }

            repository.decrementLocation(batch.getStockBatchId(), projectCode, takenFromBatch, piecesTaken);

            StockActionAllocation allocation = new StockActionAllocation(
                    UUID.randomUUID(),
                    batch.getStockBatchId(),
                    actionType,
                    actionId,
                    actionItemId,
                    itemCode,
                    takenFromBatch,
                    batch.getUnitCost()
            );
            repository.insertAllocation(allocation);
            allocations.add(allocation);

            remaining = remaining.subtract(takenFromBatch);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal availableQuantity = quantity.subtract(remaining);

            throw new RuntimeException(
                    "Insufficient batch stock for item "
                            + itemCode
                            + " in project "
                            + projectCode
                            + ". Available quantity: "
                            + availableQuantity
                            + ", requested quantity: "
                            + quantity
            );
        }

        return allocations;
    }

    /**
     * Explicit-batch variant, for non-stock items: draws {@code quantity} from ONE
     * specific batch the caller (the user, via a picker in the UI) chose — e.g. "NS-001 —
     * office chair, blue" vs "NS-001 — office chair, red" — rather than walking FIFO across
     * every batch of the item code oldest-first. Non-stock items aren't meaningfully FIFO
     * (there's no "older" or "newer" chair, just different ones), and blind FIFO can't
     * distinguish them at all, so the caller must know exactly which physical item it wants.
     * Throws if the batch doesn't have enough remaining at {@code projectCode}.
     */
    @Transactional
    public StockActionAllocation issueFromBatch(
            UUID stockBatchId,
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            String actionType,
            UUID actionId,
            UUID actionItemId
    ) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        StockBatch batch = repository.findById(stockBatchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found: " + stockBatchId));

        BigDecimal remaining = repository.getLocationQtyRemaining(stockBatchId, projectCode).orElse(BigDecimal.ZERO);
        if (remaining.compareTo(quantity) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only " + remaining + " remaining in the selected batch (" + batch.getBatchCode()
                            + ") for item " + itemCode + " at project " + projectCode
            );
        }

        repository.decrementLocation(stockBatchId, projectCode, quantity, null);

        StockActionAllocation allocation = new StockActionAllocation(
                UUID.randomUUID(),
                stockBatchId,
                actionType,
                actionId,
                actionItemId,
                itemCode,
                quantity,
                batch.getUnitCost()
        );
        repository.insertAllocation(allocation);

        return allocation;
    }

    /**
     * Records a cut-return: a worker cut some issued dimensional-item bars/pieces on site
     * and is returning offcuts of one or more NEW sizes (see com.rr.erp.util.DimensionalItems
     * and stock_batch_dimensional_tracking.sql). For each result size, opens a brand-new
     * batch (SOURCE_CUT_RETURN) at {@code issuedProjectCode} — the project that originally
     * issued the bars — and records a stock_batch_split row linking it back to the source
     * batch(es) the original intra-project issue drew from, so the full cut transformation
     * can be reassembled for audit.
     * <p>
     * Consumption is apportioned across the original issue's source batches oldest-first
     * (the same FIFO order {@link #issueFifo} drew them in). {@code wastageQty} — computed
     * by the caller across the whole return submission, since a source issue item's material
     * can be split across several return lines and even several source batches — is attached
     * to a trailing split row with no resulting batch of its own, per stock_batch_split's
     * design (one row per (source batch, result) pair, wastage riding along on the last one).
     */
    @Transactional
    public List<StockBatch> recordCutReturn(
            String issuedProjectCode,
            String itemCode,
            UUID intraProjectIssueId,
            UUID intraProjectIssueReturnId,
            String intraProjectIssueReturnCode,
            LocalDate returnDate,
            List<CutReturnResultLine> resultLines,
            BigDecimal wastageQty
    ) {

        List<StockActionAllocation> sourceAllocations =
                getAllocationsForActionAndItem(ACTION_INTRA_PROJECT_ISSUE, intraProjectIssueId, itemCode);

        if (sourceAllocations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No source batches found for the original issue of item " + itemCode
            );
        }

        // A source allocation's qtyTaken is in the issued item's own tracked unit — PIECES for
        // a dimensional item (see issueFifo) — but cutting/returning is naturally accounted in
        // LENGTH (or area): the piece count changes across a cut, the material doesn't. Convert
        // each allocation's piece count to the length/area it represents via its own batch's
        // size up front, and work in that unit for the rest of this method (matching
        // resultLineQty/wastageQty below, which are already length-based).
        List<StockBatch> sourceBatches = new ArrayList<>();
        List<BigDecimal> sourceRemaining = new ArrayList<>();
        BigDecimal totalIssuedQty = BigDecimal.ZERO;
        for (StockActionAllocation allocation : sourceAllocations) {
            StockBatch sourceBatch = repository.findById(allocation.getStockBatchId()).orElse(null);
            sourceBatches.add(sourceBatch);
            BigDecimal perPiece = perPieceSize(sourceBatch);
            BigDecimal lengthQty = perPiece != null ? allocation.getQtyTaken().multiply(perPiece) : allocation.getQtyTaken();
            sourceRemaining.add(lengthQty);
            totalIssuedQty = totalIssuedQty.add(lengthQty);
        }

        BigDecimal totalResultQty = BigDecimal.ZERO;
        for (CutReturnResultLine line : resultLines) {
            totalResultQty = totalResultQty.add(resultLineQty(line));
        }

        BigDecimal safeWastage = wastageQty == null ? BigDecimal.ZERO : wastageQty;
        // Clamp tiny negative wastage (rounding noise) to zero rather than blocking the return.
        if (safeWastage.compareTo(BigDecimal.ZERO) < 0 && safeWastage.negate().compareTo(WASTAGE_TOLERANCE) <= 0) {
            safeWastage = BigDecimal.ZERO;
        }

        BigDecimal totalAccountedFor = totalResultQty.add(safeWastage);
        if (totalAccountedFor.subtract(totalIssuedQty).compareTo(WASTAGE_TOLERANCE) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Returned and cut material (" + totalAccountedFor + ") exceeds what was issued ("
                            + totalIssuedQty + ") for item " + itemCode
            );
        }

        // sourceRemaining (built above, in length units) is the mutable running-remaining view
        // of each source allocation, consumed oldest-first across every result line — several
        // result lines can draw from the same source batch.
        List<StockBatch> resultBatches = new ArrayList<>();

        // Cut pieces are the same physical material, so they keep its expiry. If the cut drew
        // from several source batches, the earliest expiry among them is the safe one to carry.
        LocalDate inheritedExpiryDate = sourceBatches.stream()
                .map(StockBatch::getExpiryDate)
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);

        for (int lineIndex = 0; lineIndex < resultLines.size(); lineIndex++) {

            CutReturnResultLine line = resultLines.get(lineIndex);
            BigDecimal resultQty = resultLineQty(line);

            // A batch's own quantity/qty_remaining is tracked in PIECES for a dimensional
            // item (the same convention GRN/issue/every other batch-opening path uses) — NOT
            // the length resultQty computes, which exists only to reconcile this cut against
            // what was issued (see the totalIssuedQty/totalResultQty check above).
            StockBatch resultBatch = openNewBatch(
                    issuedProjectCode,
                    itemCode,
                    BigDecimal.valueOf(line.getPieceCount()),
                    line.getUnitCost(),
                    returnDate,
                    SOURCE_CUT_RETURN,
                    intraProjectIssueReturnId,
                    intraProjectIssueReturnCode,
                    line.getLengthM(),
                    line.getWidthM(),
                    line.getPieceCount(),
                    null,
                    inheritedExpiryDate
            );
            resultBatches.add(resultBatch);

            BigDecimal remainingForThisLine = resultQty;

            for (int i = 0; i < sourceAllocations.size() && remainingForThisLine.compareTo(BigDecimal.ZERO) > 0; i++) {

                BigDecimal available = sourceRemaining.get(i);
                if (available.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal qtyFromThisSource = available.min(remainingForThisLine);
                StockActionAllocation sourceAllocation = sourceAllocations.get(i);

                StockBatch sourceBatch = sourceBatches.get(i);
                Integer piecesConsumed = piecesFor(qtyFromThisSource, sourceBatch);

                splitRepository.insert(new StockBatchSplit(
                        UUID.randomUUID(),
                        intraProjectIssueReturnId,
                        sourceAllocation.getStockBatchId(),
                        piecesConsumed != null ? piecesConsumed : 1,
                        qtyFromThisSource,
                        resultBatch.getStockBatchId(),
                        qtyFromThisSource,
                        BigDecimal.ZERO
                ));

                sourceRemaining.set(i, available.subtract(qtyFromThisSource));
                remainingForThisLine = remainingForThisLine.subtract(qtyFromThisSource);
            }
        }

        // Wastage is always recorded as its own trailing split row (no resulting batch of
        // its own), against the last source batch touched — simpler and less error-prone
        // than trying to detect "the last split row" while apportioning multiple result
        // lines across multiple source batches, at the cost of always attributing wastage
        // to the newest source batch rather than whichever one it physically came from
        // (stock_batch_split's own doc comment leaves this apportionment to judgement).
        if (safeWastage.compareTo(BigDecimal.ZERO) > 0) {

            int lastIndex = sourceAllocations.size() - 1;
            StockActionAllocation lastAllocation = sourceAllocations.get(lastIndex);
            StockBatch lastSourceBatch = sourceBatches.get(lastIndex);
            Integer piecesConsumed = piecesFor(safeWastage, lastSourceBatch);

            splitRepository.insert(new StockBatchSplit(
                    UUID.randomUUID(),
                    intraProjectIssueReturnId,
                    lastAllocation.getStockBatchId(),
                    piecesConsumed != null ? piecesConsumed : 1,
                    safeWastage,
                    null,
                    BigDecimal.ZERO,
                    safeWastage
            ));
        }

        return resultBatches;
    }

    private BigDecimal resultLineQty(CutReturnResultLine line) {

        BigDecimal pieceCount = BigDecimal.valueOf(line.getPieceCount());
        BigDecimal perPiece = line.getWidthM() != null
                ? line.getLengthM().multiply(line.getWidthM())
                : line.getLengthM();

        return perPiece.multiply(pieceCount);
    }

    private Integer piecesFor(BigDecimal qty, StockBatch sourceBatch) {

        BigDecimal perPiece = perPieceSize(sourceBatch);
        if (perPiece == null || sourceBatch.getPieceCount() == null) {
            return null;
        }

        BigDecimal pieces = qty.divide(perPiece, 0, RoundingMode.CEILING);
        int result = pieces.intValueExact();

        return Math.max(result, 1);
    }

    /** A dimensional batch's length (or, if it also has a width, area) per piece — null for a
     * non-dimensional batch, or one with no positive length recorded. */
    private BigDecimal perPieceSize(StockBatch batch) {

        if (batch == null || batch.getLengthValue() == null
                || batch.getLengthValue().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return batch.getWidthValue() != null
                ? batch.getLengthValue().multiply(batch.getWidthValue())
                : batch.getLengthValue();
    }

    /** Every allocation a specific action drew for a specific item — used to replay a GIN's exact batches onto an internal transfer's destination. */
    public List<StockActionAllocation> getAllocationsForActionAndItem(String actionType, UUID actionId, String itemCode) {
        return repository.findAllocationsForActionAndItem(actionType, actionId, itemCode);
    }

    /** Every batch a specific source action opened — used to find a plant production's own finished-goods batches when reversing it. */
    public List<StockBatch> getBatchesBySource(String sourceType, UUID sourceId) {
        return repository.findBatchesBySource(sourceType, sourceId);
    }

    /**
     * Voids a batch this same source action opened — used only to reverse a plant
     * production's finished-goods output, never a general-purpose debit. Unlike
     * {@link #issueFifo}, this targets one specific batch (the one this action
     * itself created) rather than walking FIFO across every batch of the item,
     * because a FIFO walk could easily debit a different, unrelated batch instead.
     * Refuses (throws) if any of the batch's quantity has already left this
     * project — e.g. via a GIN issued after production was approved — since
     * reversing it then would understate what's actually gone.
     */
    @Transactional
    public void reverseOwnBatch(StockBatch batch, String projectCode) {

        BigDecimal remaining = repository
                .getLocationQtyRemaining(batch.getStockBatchId(), projectCode)
                .orElse(BigDecimal.ZERO);

        if (remaining.compareTo(batch.getOriginalQty()) != 0) {
            throw new RuntimeException(
                    "Cannot reverse production: item " + batch.getItemCode()
                            + " from batch " + batch.getBatchCode()
                            + " has already been partially or fully issued out of the plant"
            );
        }

        repository.decrementLocation(batch.getStockBatchId(), projectCode, remaining);
    }

    /**
     * Collapses a FIFO consumption that may span several batches at
     * different costs into one number — needed only where a caller has a
     * single scalar cost field to fill (e.g. a stock adjustment line's
     * unitPrice) and can't show a per-batch breakdown.
     */
    public BigDecimal weightedAverageCost(List<StockActionAllocation> allocations) {

        if (allocations == null || allocations.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (StockActionAllocation allocation : allocations) {
            totalQty = totalQty.add(allocation.getQtyTaken());
            totalCost = totalCost.add(allocation.getQtyTaken().multiply(allocation.getUnitCost()));
        }

        if (totalQty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalCost.divide(totalQty, 4, RoundingMode.HALF_UP);
    }

    /** Fallback cost when there's no batch to exactly replay (e.g. an internal GRN with no traceable source GIN). */
    public BigDecimal mostRecentUnitCost(String itemCode) {
        return repository.findMostRecentBatch(itemCode)
                .map(StockBatch::getUnitCost)
                .orElse(BigDecimal.ZERO);
    }

    public List<StockBatchView> getBatchViewsForItem(String projectCode, String itemCode) {
        return repository.findBatchViewsForItem(projectCode, itemCode);
    }

    /** Per-size stock breakdown for one project/item — see StockBatchRepository#findSizeBreakdownForItem. */
    public List<com.rr.erp.dto.StockSizeBreakdown> getSizeBreakdownForItem(String projectCode, String itemCode) {
        return repository.findSizeBreakdownForItem(projectCode, itemCode);
    }

    public List<StockActionAllocation> getAllocationsForAction(String actionType, UUID actionId) {
        return repository.findAllocationsForAction(actionType, actionId);
    }

    /** {itemCode}-{yyyyMMdd}-{seq}, e.g. CEM001-20260904-01 — the item and the date it genuinely entered the system, never touched again. */
    private String buildBatchCode(String itemCode, LocalDate originDate) {

        int seq = repository.nextSequence(itemCode, originDate);

        return itemCode + "-" + originDate.format(BATCH_CODE_DATE) + "-" + String.format("%02d", seq);
    }
}
