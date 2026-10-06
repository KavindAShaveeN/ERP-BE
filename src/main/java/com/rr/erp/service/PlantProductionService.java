package com.rr.erp.service;

import com.rr.erp.entity.PlantProduction;
import com.rr.erp.entity.PlantProductionExpense;
import com.rr.erp.entity.PlantProductionInput;
import com.rr.erp.entity.PlantProductionOutput;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.repository.PlantProductionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Modeled directly on {@link com.rr.erp.service.GRNService}/{@link com.rr.erp.service.GINService}:
 * a production record's stock is only ever moved on the exact SUBMITTED -&gt; APPROVED
 * transition, and it is rejected as immutable (409) once APPROVED or REVERSED.
 * Unlike GRN/GIN, an approved production can be undone via {@link #reverseProduction},
 * since production is the one workflow in this system with a genuine multi-step
 * lifecycle (Draft/Submitted/Approved/Rejected/Reversed) rather than a single
 * approval gate.
 */
@Service
public class PlantProductionService {

    private static final Logger log = LoggerFactory.getLogger(PlantProductionService.class);

    private static final Set<String> VALID_STATUSES =
            Set.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "REVERSED");

    private final PlantProductionRepository productionRepository;
    private final ProjectStoreService projectStoreService;
    private final StockBatchService stockBatchService;

    public PlantProductionService(
            PlantProductionRepository productionRepository,
            ProjectStoreService projectStoreService,
            StockBatchService stockBatchService
    ) {
        this.productionRepository = productionRepository;
        this.projectStoreService = projectStoreService;
        this.stockBatchService = stockBatchService;
    }

    @Transactional
    public PlantProduction createProduction(PlantProduction production) {

        validateProduction(production);

        // A production is normally created as Draft (see PlantProductionFormPage.tsx);
        // this stays a real check — not dropped — in case one is ever created
        // pre-approved by another caller, mirroring GRNService#createGRN.
        if ("APPROVED".equals(production.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A production record cannot be created already approved; submit then approve it instead."
            );
        }

        production.setProductionId(UUID.randomUUID());
        applyUomConversions(production);

        try {
            return productionRepository.insertProduction(production);
        } catch (DataIntegrityViolationException exception) {
            log.error(
                    "Failed to create plant production {}: {}",
                    production.getProductionCode(),
                    exception.getMostSpecificCause().getMessage(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to create production record. Check project, recipe, item code and UOM values.",
                    exception
            );
        }
    }

    @Transactional
    public PlantProduction updateProduction(UUID productionId, PlantProduction incoming) {

        PlantProduction existing = productionRepository.findById(productionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Production not found: " + productionId));

        if ("APPROVED".equals(existing.getStatus()) || "REVERSED".equals(existing.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Production " + productionId + " is " + existing.getStatus() + " and can no longer be edited."
            );
        }

        validateProduction(incoming);
        applyUomConversions(incoming);

        boolean approving = "APPROVED".equals(incoming.getStatus());

        if (approving && !"SUBMITTED".equals(existing.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Production must be Submitted before it can be Approved."
            );
        }

        if (approving) {
            incoming.setApprovedDate(LocalDateTime.now());
        }

        try {
            int updatedRows = productionRepository.updateProduction(productionId, incoming);

            if (updatedRows == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Production not found: " + productionId);
            }

            productionRepository.deleteInputsByProductionId(productionId);
            productionRepository.deleteOutputsByProductionId(productionId);
            productionRepository.deleteExpensesByProductionId(productionId);
            productionRepository.insertInputs(productionId, incoming.getInputs());
            productionRepository.insertOutputs(productionId, incoming.getOutputs());
            productionRepository.insertExpenses(productionId, incoming.getExpenses());

            // existing was confirmed still-editable above, so `approving` here means
            // this call is exactly the Submitted -> Approved transition — the one
            // moment stock should move. A plain edit, submit, or reject must not
            // touch project_store at all (mirrors GRNService#updateGRN).
            if (approving) {
                applyStockMovements(productionId, incoming);
            }

            return productionRepository.findById(productionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Production not found after update"));

        } catch (DataIntegrityViolationException exception) {
            log.error(
                    "Failed to update plant production {}: {}",
                    productionId,
                    exception.getMostSpecificCause().getMessage(),
                    exception
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to update production record. Check the supplied values.",
                    exception
            );
        }
    }

    /**
     * Undoes an APPROVED production's stock movements: raw materials are
     * credited back onto the exact batches they were drawn from, and each
     * non-waste finished-goods batch this production opened is voided —
     * refusing if any of it has already been issued out of the plant (see
     * StockBatchService#reverseOwnBatch).
     */
    @Transactional
    public PlantProduction reverseProduction(UUID productionId, String reversedBy, String reason) {

        PlantProduction existing = productionRepository.findById(productionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Production not found: " + productionId));

        if (!"APPROVED".equals(existing.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only an Approved production record can be reversed (current status: " + existing.getStatus() + ")."
            );
        }

        String projectCode = existing.getProjectCode();

        for (PlantProductionInput input : existing.getInputs()) {

            List<StockActionAllocation> allocations = stockBatchService.getAllocationsForActionAndItem(
                    StockBatchService.ACTION_PLANT_PRODUCTION, productionId, input.getItemCode()
            );

            if (!allocations.isEmpty()) {
                projectStoreService.creditExistingBatches(
                        projectCode, input.getItemCode(), existing.getProductionDate(), allocations
                );
            }
        }

        for (PlantProductionOutput output : existing.getOutputs()) {

            if (Boolean.TRUE.equals(output.getIsWaste())) {
                continue;
            }

            projectStoreService.reverseProducedBatch(projectCode, output.getItemCode(), productionId);
        }

        existing.setStatus("REVERSED");
        existing.setReversedBy(reversedBy);
        existing.setReversedDate(LocalDateTime.now());
        existing.setReversalReason(reason);

        productionRepository.updateProduction(productionId, existing);

        return productionRepository.findById(productionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Production not found after reversal"));
    }

    public List<PlantProduction> getAllProductions() {
        return productionRepository.findAll();
    }

    public List<PlantProduction> getProductionsByProjectCode(String projectCode) {
        return productionRepository.findByProjectCode(projectCode);
    }

    public PlantProduction getProductionById(UUID productionId) {
        return productionRepository.findById(productionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Production not found: " + productionId));
    }

    /**
     * Runs inside the caller's transaction (updateProduction is @Transactional),
     * so an insufficient-stock RuntimeException thrown partway through rolls
     * back every debit/credit already applied for this production — no manual
     * compensation needed, same as GRN/GIN rely on.
     */
    private void applyStockMovements(UUID productionId, PlantProduction production) {

        String projectCode = production.getProjectCode();

        BigDecimal totalRawMaterialCost = BigDecimal.ZERO;

        for (PlantProductionInput input : production.getInputs()) {

            BigDecimal stockQuantity = input.getStockEquivalentQty() != null
                    ? input.getStockEquivalentQty() : input.getConsumedQuantity();

            List<StockActionAllocation> allocations = projectStoreService.issueGoods(
                    projectCode,
                    input.getItemCode(),
                    stockQuantity,
                    production.getProductionDate(),
                    StockBatchService.ACTION_PLANT_PRODUCTION,
                    productionId,
                    input.getProductionInputId()
            );

            for (StockActionAllocation allocation : allocations) {
                totalRawMaterialCost = totalRawMaterialCost.add(
                        allocation.getQtyTaken().multiply(allocation.getUnitCost())
                );
            }
        }

        // Other expenses (labour, power, transport, ...) are part of the production
        // cost alongside raw materials, so they are loaded onto the finished goods.
        BigDecimal totalOtherExpenses = production.getExpenses() == null
                ? BigDecimal.ZERO
                : production.getExpenses().stream()
                        .map(PlantProductionExpense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProductionCost = totalRawMaterialCost.add(totalOtherExpenses);

        BigDecimal totalOutputQuantity = production.getOutputs().stream()
                .filter(output -> !Boolean.TRUE.equals(output.getIsWaste()))
                .map(output -> output.getStockEquivalentQty() != null ? output.getStockEquivalentQty() : output.getProducedQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cost per unit of output, apportioned pro-rata by quantity across every
        // non-waste output line — a simplifying average rather than a per-item
        // costing model, since raw materials and other expenses are consumed jointly
        // to yield however many finished products a batch produces.
        BigDecimal unitCost = totalOutputQuantity.compareTo(BigDecimal.ZERO) > 0
                ? totalProductionCost.divide(totalOutputQuantity, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        for (PlantProductionOutput output : production.getOutputs()) {

            if (Boolean.TRUE.equals(output.getIsWaste())) {
                continue;
            }

            BigDecimal stockQuantity = output.getStockEquivalentQty() != null
                    ? output.getStockEquivalentQty() : output.getProducedQuantity();

            projectStoreService.receiveNewBatch(
                    projectCode,
                    output.getItemCode(),
                    stockQuantity,
                    production.getProductionDate(),
                    unitCost,
                    StockBatchService.SOURCE_PLANT_PRODUCTION,
                    productionId,
                    production.getProductionCode()
            );
        }
    }

    private void validateProduction(PlantProduction production) {

        if (production.getStatus() == null || !VALID_STATUSES.contains(production.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid production status");
        }

        if (production.getInputs() == null || production.getInputs().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one raw material input is required");
        }

        if (production.getOutputs() == null || production.getOutputs().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one finished-good output is required");
        }

        boolean hasUsableOutput = production.getOutputs().stream()
                .anyMatch(output -> !Boolean.TRUE.equals(output.getIsWaste()));

        if (!hasUsableOutput) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "At least one non-waste finished-good output is required"
            );
        }
    }

    /**
     * See PlantRecipeService#applyUomConversions -- a production line's conversionFactor
     * (recipe-seeded, but editable per batch since actual weight/density can vary) converts
     * its consumed/produced quantity, entered in the plant's working unit, back into the
     * item's stock UOM. stockEquivalentQty is recomputed here from whatever the client sent
     * rather than trusted as-is, so it always reflects the quantity actually being saved.
     */
    private void applyUomConversions(PlantProduction production) {

        for (PlantProductionInput input : production.getInputs()) {
            if (input.getConversionFactor() == null || input.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                input.setConversionFactor(null);
                input.setStockEquivalentQty(input.getConsumedQuantity());
            } else {
                input.setStockEquivalentQty(
                        input.getConsumedQuantity().divide(input.getConversionFactor(), 6, RoundingMode.HALF_UP)
                );
            }
        }

        for (PlantProductionOutput output : production.getOutputs()) {
            if (output.getConversionFactor() == null || output.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                output.setConversionFactor(null);
                output.setStockEquivalentQty(output.getProducedQuantity());
            } else {
                output.setStockEquivalentQty(
                        output.getProducedQuantity().divide(output.getConversionFactor(), 6, RoundingMode.HALF_UP)
                );
            }
        }
    }
}
