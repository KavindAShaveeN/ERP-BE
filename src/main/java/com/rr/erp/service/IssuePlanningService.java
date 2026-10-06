package com.rr.erp.service;

import com.rr.erp.dto.DispatchLineResponse;
import com.rr.erp.dto.ForwardPlanAgreeRequest;
import com.rr.erp.dto.IssuePlanConfirmRequest;
import com.rr.erp.dto.IssuePlanGridResponse;
import com.rr.erp.dto.IssuePlanResult;
import com.rr.erp.dto.PlannedAllocationResponse;
import com.rr.erp.entity.GIN;
import com.rr.erp.entity.GINItem;
import com.rr.erp.entity.MR;
import com.rr.erp.entity.MRItem;
import com.rr.erp.repository.IssuePlanningRepository;
import com.rr.erp.repository.IssuePlanningRepository.PendingLine;
import com.rr.erp.repository.IssuePlanningRepository.StockPosition;
import com.rr.erp.repository.MRRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main-store issue planning. Builds the item x MR grid (stock position + requested quantities),
 * saves reservations when the storekeeper confirms a plan, and records the shortfall plan the
 * user explicitly agrees (nothing is ever forwarded to Purchasing automatically).
 *
 * Available = on hand - reserved. Stock is still deducted only when a GIN is authorized (see
 * GINService); at that moment {@link #onGinAuthorized} converts the reservation to issued and
 * refreshes the MR line status.
 */
@Service
public class IssuePlanningService {

    /** The Purchasing Department is represented in this system by the Head Quarters project. */
    public static final String PURCHASING_PROJECT_CODE = "HQ-COLOMBO";

    private static final String ACTION_FORWARD = "FORWARD";
    private static final String ACTION_BACK_ORDER = "BACK_ORDER";

    private final IssuePlanningRepository planningRepository;
    private final MRRepository mrRepository;

    public IssuePlanningService(IssuePlanningRepository planningRepository, MRRepository mrRepository) {
        this.planningRepository = planningRepository;
        this.mrRepository = mrRepository;
    }

    // ------------------------------------------------------------------ grid

    @Transactional(readOnly = true)
    public IssuePlanGridResponse getGrid(String storeCode) {

        requireText(storeCode, "Store code is required");

        List<PendingLine> lines = planningRepository.findOpenLines(storeCode).stream()
                .filter(line -> plannable(line).signum() > 0 || line.allocatedQty().signum() > 0)
                .toList();

        Map<String, StockPosition> positions = planningRepository.findStockPositions(storeCode);
        Map<String, BigDecimal> reserved = planningRepository.findReservedByItem(storeCode);
        Map<String, BigDecimal> inTransit = planningRepository.findInTransitByItem(storeCode);
        Map<String, BigDecimal> incomingPo = planningRepository.findIncomingPoByItem(storeCode);

        IssuePlanGridResponse response = new IssuePlanGridResponse();
        response.setStoreCode(storeCode);

        // One column per MR, in the order the requests came in.
        Map<UUID, IssuePlanGridResponse.MrColumn> columns = new LinkedHashMap<>();
        for (PendingLine line : lines) {
            IssuePlanGridResponse.MrColumn column = columns.computeIfAbsent(line.mrId(), id -> {
                IssuePlanGridResponse.MrColumn c = new IssuePlanGridResponse.MrColumn();
                c.setMrId(line.mrId());
                c.setMrCode(line.mrCode());
                c.setRequestingProjectCode(line.requestingProjectCode());
                c.setRequestingProjectName(line.requestingProjectName());
                c.setRequestedDate(line.requestedDate());
                c.setPriority(line.priority());
                return c;
            });
            column.setRequiredDate(earliest(column.getRequiredDate(), line.requiredDate()));
            if (isUrgent(line.priority())) {
                column.setPriority(line.priority());
            }
        }
        response.setMrs(new ArrayList<>(columns.values()));

        // One row per item, one cell per pending MR line of that item.
        Map<String, IssuePlanGridResponse.ItemRow> rows = new LinkedHashMap<>();
        for (PendingLine line : lines) {
            IssuePlanGridResponse.ItemRow row = rows.computeIfAbsent(line.itemCode(), code -> {
                IssuePlanGridResponse.ItemRow r = new IssuePlanGridResponse.ItemRow();
                r.setItemCode(code);
                r.setDescription(line.description());
                r.setUomId(line.uomId());
                r.setUomName(line.uomName());
                StockPosition position = positions.get(code);
                BigDecimal onHand = position == null ? BigDecimal.ZERO : position.onHand();
                BigDecimal held = reserved.getOrDefault(code, BigDecimal.ZERO);
                r.setOnHand(onHand);
                r.setReserved(held);
                r.setAvailable(onHand.subtract(held));
                r.setReorderLevel(position == null ? BigDecimal.ZERO : position.reorderLevel());
                r.setInTransit(inTransit.getOrDefault(code, BigDecimal.ZERO));
                r.setIncomingPo(incomingPo.getOrDefault(code, BigDecimal.ZERO));
                return r;
            });
            row.getCells().add(toCell(line));
        }
        response.setItems(new ArrayList<>(rows.values()));

        return response;
    }

    // --------------------------------------------------------------- confirm

    /**
     * Saves the storekeeper's allocation for the given MR lines, replacing each line's existing
     * reservation. Rejects a plan that would reserve more than the store has available.
     */
    @Transactional
    public IssuePlanResult confirmPlan(IssuePlanConfirmRequest request) {

        requireText(request.getStoreCode(), "Store code is required");
        requireText(request.getConfirmedBy(), "Confirmed by is required");
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw badRequest("The plan has no lines.");
        }

        String store = request.getStoreCode();
        Map<UUID, BigDecimal> qtyByLine = new LinkedHashMap<>();
        for (IssuePlanConfirmRequest.Line line : request.getLines()) {
            if (line.getMrItemId() == null || line.getQty() == null || line.getQty().signum() < 0) {
                throw badRequest("Every plan line needs an MR line and a quantity of zero or more.");
            }
            qtyByLine.merge(line.getMrItemId(), line.getQty(), BigDecimal::add);
        }

        List<UUID> lineIds = new ArrayList<>(qtyByLine.keySet());
        Map<UUID, PendingLine> lines = planningRepository.findLines(store, lineIds).stream()
                .collect(Collectors.toMap(PendingLine::mrItemId, l -> l));
        for (UUID id : lineIds) {
            if (!lines.containsKey(id)) {
                throw badRequest("MR line " + id + " is not an approved request addressed to " + store + ".");
            }
        }

        // Per line: cannot allocate more than is still owed from this store.
        for (UUID id : lineIds) {
            PendingLine line = lines.get(id);
            if (qtyByLine.get(id).compareTo(plannable(line)) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        line.mrCode() + " / " + line.itemCode() + ": allocation " + qtyByLine.get(id).stripTrailingZeros().toPlainString()
                                + " is more than the " + plannable(line).stripTrailingZeros().toPlainString() + " still owed."
                );
            }
        }

        // Per item: lock the stock row, then new allocations + everyone else's reservations
        // must fit within on-hand.
        Map<String, List<UUID>> lineIdsByItem = lineIds.stream()
                .collect(Collectors.groupingBy(id -> lines.get(id).itemCode(), LinkedHashMap::new, Collectors.toList()));
        Map<String, BigDecimal> onHandByItem =
                planningRepository.lockOnHand(store, new ArrayList<>(lineIdsByItem.keySet()));

        for (Map.Entry<String, List<UUID>> entry : lineIdsByItem.entrySet()) {
            String itemCode = entry.getKey();
            BigDecimal requested = entry.getValue().stream()
                    .map(qtyByLine::get)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal heldByOthers = planningRepository.sumActiveExcluding(store, itemCode, entry.getValue());
            BigDecimal available = onHandByItem.get(itemCode).subtract(heldByOthers);
            if (requested.compareTo(available) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        itemCode + ": plan allocates " + requested.stripTrailingZeros().toPlainString()
                                + " but only " + available.max(BigDecimal.ZERO).stripTrailingZeros().toPlainString()
                                + " is available in " + store + "."
                );
            }
        }

        UUID batchId = UUID.randomUUID();
        int saved = 0;
        Set<UUID> touchedMrs = new java.util.LinkedHashSet<>();
        for (UUID id : lineIds) {
            PendingLine line = lines.get(id);
            BigDecimal qty = qtyByLine.get(id);
            touchedMrs.add(line.mrId());
            if (qty.compareTo(line.allocatedQty()) == 0) {
                continue;
            }
            planningRepository.releaseActiveForLine(id, request.getConfirmedBy(), "Re-planned");
            if (qty.signum() > 0) {
                planningRepository.insertAllocation(batchId, line.mrId(), id, line.itemCode(), store, qty, request.getConfirmedBy());
            }
            saved++;
        }
        touchedMrs.forEach(planningRepository::refreshLineStatus);

        return new IssuePlanResult(batchId, saved, List.of());
    }

    // ---------------------------------------------------------- forward plan

    /**
     * Records the shortfall plan the user agreed. FORWARD rows become one FWD- MR per source MR
     * addressed to Purchasing; BACK_ORDER rows stay open at this store, marked BACK_ORDERED.
     */
    @Transactional
    public IssuePlanResult agreeForwardPlan(ForwardPlanAgreeRequest request) {

        requireText(request.getStoreCode(), "Store code is required");
        requireText(request.getAgreedBy(), "Agreed by is required");
        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw badRequest("The forward plan has no rows.");
        }

        String store = request.getStoreCode();
        List<UUID> lineIds = request.getRows().stream().map(ForwardPlanAgreeRequest.Row::getMrItemId).toList();
        Map<UUID, PendingLine> lines = planningRepository.findLines(store, lineIds).stream()
                .collect(Collectors.toMap(PendingLine::mrItemId, l -> l));

        for (ForwardPlanAgreeRequest.Row row : request.getRows()) {
            PendingLine line = lines.get(row.getMrItemId());
            if (line == null) {
                throw badRequest("MR line " + row.getMrItemId() + " is not an approved request addressed to " + store + ".");
            }
            if (row.getQty() == null || row.getQty().signum() <= 0) {
                throw badRequest("Forward quantities must be greater than zero.");
            }
            if (!ACTION_FORWARD.equals(row.getAction()) && !ACTION_BACK_ORDER.equals(row.getAction())) {
                throw badRequest("Action must be FORWARD or BACK_ORDER.");
            }
            BigDecimal unallocated = plannable(line).subtract(line.allocatedQty());
            if (row.getQty().compareTo(unallocated) > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        line.mrCode() + " / " + line.itemCode() + ": shortfall is only "
                                + unallocated.max(BigDecimal.ZERO).stripTrailingZeros().toPlainString()
                                + " after the confirmed allocation."
                );
            }
        }

        UUID batchId = UUID.randomUUID();
        List<String> forwardedCodes = new ArrayList<>();

        // One FWD- MR per source MR, like the issue form's own forward button.
        Map<UUID, List<ForwardPlanAgreeRequest.Row>> forwardBySourceMr = request.getRows().stream()
                .filter(row -> ACTION_FORWARD.equals(row.getAction()))
                .collect(Collectors.groupingBy(row -> lines.get(row.getMrItemId()).mrId(), LinkedHashMap::new, Collectors.toList()));

        Map<UUID, UUID> forwardedMrIdBySource = new LinkedHashMap<>();
        for (Map.Entry<UUID, List<ForwardPlanAgreeRequest.Row>> entry : forwardBySourceMr.entrySet()) {
            List<ForwardPlanAgreeRequest.Row> rows = entry.getValue();
            PendingLine first = lines.get(rows.get(0).getMrItemId());

            MR forwarded = new MR();
            forwarded.setMrCode(planningRepository.nextForwardMrCode());
            forwarded.setRequestingProjectCode(store);
            forwarded.setDestinationProjectCode(PURCHASING_PROJECT_CODE);
            forwarded.setRequestedDate(LocalDateTime.now());
            forwarded.setRequestedBy(request.getAgreedBy());
            forwarded.setRemark("Forwarded shortfall from " + first.mrCode());
            forwarded.setIsApproved(false);

            List<MRItem> items = new ArrayList<>();
            for (ForwardPlanAgreeRequest.Row row : rows) {
                PendingLine line = lines.get(row.getMrItemId());
                MRItem item = new MRItem();
                item.setItemCode(line.itemCode());
                item.setDescription(line.description());
                item.setSize(line.size());
                item.setUomId(line.uomId());
                item.setQuantity(row.getQty());
                item.setPriority(line.priority() == null ? "Normal" : line.priority());
                item.setRequiredDate(line.requiredDate() == null ? LocalDate.now() : line.requiredDate());
                items.add(item);
            }
            forwarded.setItems(items);

            MR created = mrRepository.insertMR(forwarded);
            forwardedMrIdBySource.put(entry.getKey(), created.getMrId());
            forwardedCodes.add(created.getMrCode());
        }

        Set<UUID> touchedMrs = new java.util.LinkedHashSet<>();
        for (ForwardPlanAgreeRequest.Row row : request.getRows()) {
            PendingLine line = lines.get(row.getMrItemId());
            boolean forward = ACTION_FORWARD.equals(row.getAction());
            planningRepository.insertForwardPlanRow(
                    batchId, line.mrId(), line.mrItemId(), line.itemCode(), row.getQty(),
                    row.getAction(),
                    forward ? "FORWARDED" : "AGREED",
                    forward ? forwardedMrIdBySource.get(line.mrId()) : null,
                    request.getAgreedBy()
            );
            touchedMrs.add(line.mrId());
        }
        touchedMrs.forEach(planningRepository::refreshLineStatus);
        for (ForwardPlanAgreeRequest.Row row : request.getRows()) {
            if (ACTION_BACK_ORDER.equals(row.getAction())) {
                planningRepository.markBackOrdered(row.getMrItemId());
            }
        }

        return new IssuePlanResult(batchId, request.getRows().size(), forwardedCodes);
    }

    // ----------------------------------------------------- GIN / MR lifecycle

    /** The reservations still held for one MR at the store — pre-fills a GIN from a confirmed plan. */
    @Transactional(readOnly = true)
    public List<PlannedAllocationResponse> getPlannedAllocations(UUID mrId, String storeCode) {
        return planningRepository.findActiveByMr(mrId, storeCode);
    }

    /**
     * Called when a GIN is authorized (its stock was just deducted): converts the MR's
     * reservation to issued for what this GIN actually issued, then refreshes line status.
     * A GIN with no MR, or against an MR with no reservation, only gets the status refresh.
     */
    public void onGinAuthorized(GIN gin, UUID ginId) {

        Set<UUID> touchedMrs = new java.util.LinkedHashSet<>();
        if (gin.getItems() != null && gin.getIssuedProjectCode() != null) {
            for (GINItem item : gin.getItems()) {
                if (item.getItemCode() == null || item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                    continue;
                }
                if (item.getMrItemId() != null) {
                    // A line that names its MR line (one GIN covering several MRs for a site).
                    planningRepository.consumeForMrItem(
                            item.getMrItemId(), gin.getIssuedProjectCode(), item.getQuantity(), ginId
                    );
                    UUID mrId = planningRepository.findMrIdByMrItemId(item.getMrItemId());
                    if (mrId != null) {
                        touchedMrs.add(mrId);
                    }
                } else if (gin.getMrId() != null) {
                    planningRepository.consumeForGin(
                            gin.getMrId(), gin.getIssuedProjectCode(), item.getItemCode(), item.getQuantity(), ginId
                    );
                    touchedMrs.add(gin.getMrId());
                }
            }
        }
        if (gin.getMrId() != null) {
            touchedMrs.add(gin.getMrId());
        }
        touchedMrs.forEach(planningRepository::refreshLineStatus);
    }

    /**
     * A GIN line that names an MR line must belong to an approved MR addressed to the issuing
     * store, requested by the site the GIN is going to, for the same item. This keeps one
     * per-site GIN from carrying another site's request.
     */
    public void validateMrLines(GIN gin) {
        if (gin.getItems() == null) {
            return;
        }
        List<GINItem> linked = gin.getItems().stream().filter(item -> item.getMrItemId() != null).toList();
        if (linked.isEmpty()) {
            return;
        }
        requireText(gin.getIssuedProjectCode(), "Issuing project is required for MR-linked lines");
        Map<UUID, PendingLine> lines = planningRepository.findLines(
                        gin.getIssuedProjectCode(), linked.stream().map(GINItem::getMrItemId).toList())
                .stream()
                .collect(Collectors.toMap(PendingLine::mrItemId, l -> l));
        for (GINItem item : linked) {
            PendingLine line = lines.get(item.getMrItemId());
            if (line == null) {
                throw badRequest("A GIN line refers to an MR line that is not an approved request to "
                        + gin.getIssuedProjectCode() + ".");
            }
            if (!line.requestingProjectCode().equals(gin.getReceivedProjectCode())) {
                throw badRequest(line.mrCode() + " was requested by " + line.requestingProjectCode()
                        + ", not by " + gin.getReceivedProjectCode() + " - it cannot go on this GIN.");
            }
            if (item.getItemCode() == null || !item.getItemCode().equals(line.itemCode())) {
                throw badRequest("A GIN line does not match the item requested on " + line.mrCode() + ".");
            }
        }
    }

    /** Reserved MR lines at the store across all sites - the source for a per-site dispatch GIN. */
    @Transactional(readOnly = true)
    public List<DispatchLineResponse> getDispatchLines(String storeCode) {
        requireText(storeCode, "Store code is required");
        return planningRepository.findDispatchLines(storeCode);
    }

    /** An MR was rejected or un-approved: its reservations are no longer wanted. */
    public void onMrRejected(UUID mrId, String by) {
        planningRepository.releaseActiveForMr(mrId, by == null ? "system" : by, "MR rejected");
    }

    // --------------------------------------------------------------- helpers

    private static BigDecimal plannable(PendingLine line) {
        return line.requestedQty().subtract(line.issuedQty()).subtract(line.forwardedQty()).max(BigDecimal.ZERO);
    }

    private static IssuePlanGridResponse.Cell toCell(PendingLine line) {
        IssuePlanGridResponse.Cell cell = new IssuePlanGridResponse.Cell();
        cell.setMrId(line.mrId());
        cell.setMrItemId(line.mrItemId());
        cell.setSize(line.size());
        cell.setRequestedQty(line.requestedQty());
        cell.setIssuedQty(line.issuedQty());
        cell.setRemainingQty(line.requestedQty().subtract(line.issuedQty()).max(BigDecimal.ZERO));
        cell.setForwardedQty(line.forwardedQty());
        cell.setPlannableQty(plannable(line));
        cell.setAllocatedQty(line.allocatedQty());
        cell.setSiteBalance(line.siteBalance());
        cell.setSiteInTransit(line.siteInTransit());
        cell.setPriority(line.priority());
        cell.setRequiredDate(line.requiredDate());
        cell.setLineStatus(line.lineStatus());
        return cell;
    }

    private static boolean isUrgent(String priority) {
        return priority != null && priority.equalsIgnoreCase("Urgent");
    }

    private static LocalDate earliest(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw badRequest(message);
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
