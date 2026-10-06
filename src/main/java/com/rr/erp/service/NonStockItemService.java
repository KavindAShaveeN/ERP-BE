package com.rr.erp.service;

import com.rr.erp.entity.NonStockItem;
import com.rr.erp.repository.NonStockItemRepository;
import com.rr.erp.repository.ItemCodeRepository;
import com.rr.erp.repository.UOMRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NonStockItemService {

    private final NonStockItemRepository nonStockItemRepository;
    private final ItemCodeRepository itemCodeRepository;
    private final UOMRepository uomRepository;

    public NonStockItemService(
            NonStockItemRepository nonStockItemRepository,
            ItemCodeRepository itemCodeRepository,
            UOMRepository uomRepository
    ) {
        this.nonStockItemRepository = nonStockItemRepository;
        this.itemCodeRepository = itemCodeRepository;
        this.uomRepository = uomRepository;
    }

    private void validateForeignKeys(NonStockItem item) {

        if (!itemCodeRepository.existsById(item.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Item code not found with id: " + item.getItemCodeId());
        }
        if (!uomRepository.existsById(item.getUomId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "UOM not found with id: " + item.getUomId());
        }
    }

    public NonStockItem addNonStockItem(NonStockItem item) {

        validateForeignKeys(item);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        if (nonStockItemRepository.existsByItemCodeId(item.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A non-stock item already exists for item code id: " + item.getItemCodeId());
        }

        Long id = nonStockItemRepository.addNonStockItem(item);
        item.setNonStockItemId(id);

        return item;
    }

    public List<NonStockItem> getAllNonStockItems() {
        return nonStockItemRepository.getAllNonStockItems();
    }

    public NonStockItem getNonStockItemById(Long id) {
        return nonStockItemRepository.getNonStockItemById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Non-stock item not found with id: " + id));
    }

    public NonStockItem updateNonStockItem(Long id, NonStockItem item) {

        validateForeignKeys(item);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        if (nonStockItemRepository.existsByItemCodeIdExcludingId(item.getItemCodeId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A non-stock item already exists for item code id: " + item.getItemCodeId());
        }

        int affectedRows = nonStockItemRepository.updateNonStockItem(id, item);

        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Non-stock item not found with id: " + id);
        }

        item.setNonStockItemId(id);

        return item;
    }

    public void deleteNonStockItem(Long id) {

        try {
            int affectedRows = nonStockItemRepository.deleteById(id);

            if (affectedRows == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Non-stock item not found with id: " + id);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Non-stock item cannot be deleted because it is referenced by another record", e);
        }
    }
}
