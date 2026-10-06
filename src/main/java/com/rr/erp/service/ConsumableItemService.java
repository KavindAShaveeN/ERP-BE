package com.rr.erp.service;

import com.rr.erp.entity.ConsumableItem;
import com.rr.erp.repository.ConsumableItemRepository;
import com.rr.erp.repository.ItemCodeRepository;
import com.rr.erp.repository.UOMRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ConsumableItemService {

    private final ConsumableItemRepository consumableItemRepository;
    private final ItemCodeRepository itemCodeRepository;
    private final UOMRepository uomRepository;

    public ConsumableItemService(
            ConsumableItemRepository consumableItemRepository,
            ItemCodeRepository itemCodeRepository,
            UOMRepository uomRepository
    ) {
        this.consumableItemRepository = consumableItemRepository;
        this.itemCodeRepository = itemCodeRepository;
        this.uomRepository = uomRepository;
    }

    private void validateForeignKeys(ConsumableItem item) {

        if (!itemCodeRepository.existsById(item.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Item code not found with id: " + item.getItemCodeId());
        }
        if (!uomRepository.existsById(item.getUomId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "UOM not found with id: " + item.getUomId());
        }
    }

    public ConsumableItem addConsumableItem(ConsumableItem item) {

        validateForeignKeys(item);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        if (consumableItemRepository.existsByItemCodeId(item.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A consumable item already exists for item code id: " + item.getItemCodeId());
        }

        Long id = consumableItemRepository.addConsumableItem(item);
        item.setConsumableItemId(id);

        return item;
    }

    public List<ConsumableItem> getAllConsumableItems() {
        return consumableItemRepository.getAllConsumableItems();
    }

    public ConsumableItem getConsumableItemById(Long id) {
        return consumableItemRepository.getConsumableItemById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Consumable item not found with id: " + id));
    }

    public ConsumableItem updateConsumableItem(Long id, ConsumableItem item) {

        validateForeignKeys(item);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        if (consumableItemRepository.existsByItemCodeIdExcludingId(item.getItemCodeId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A consumable item already exists for item code id: " + item.getItemCodeId());
        }

        int affectedRows = consumableItemRepository.updateConsumableItem(id, item);

        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Consumable item not found with id: " + id);
        }

        item.setConsumableItemId(id);

        return item;
    }

    public void deleteConsumableItem(Long id) {

        try {
            int affectedRows = consumableItemRepository.deleteById(id);

            if (affectedRows == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Consumable item not found with id: " + id);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Consumable item cannot be deleted because it is referenced by another record", e);
        }
    }
}
