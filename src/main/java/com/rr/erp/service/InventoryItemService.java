package com.rr.erp.service;

import com.rr.erp.entity.InventoryItem;
import com.rr.erp.repository.InventoryItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryItemService(
            InventoryItemRepository inventoryItemRepository
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    // POST
    public InventoryItem createInventoryItem(
            InventoryItem inventoryItem
    ) {

        if (inventoryItem.getIsActive() == null) {
            inventoryItem.setIsActive(true);
        }

        Integer inventoryItemId =
                inventoryItemRepository.createInventoryItem(
                        inventoryItem
                );

        return inventoryItemRepository.getInventoryItemById(
                inventoryItemId
        );
    }

    // GET ALL
    public List<InventoryItem> getAllInventoryItems() {

        return inventoryItemRepository.getAllInventoryItems();
    }

    // GET BY ID
    public InventoryItem getInventoryItemById(
            Integer inventoryItemId
    ) {

        return inventoryItemRepository.getInventoryItemById(
                inventoryItemId
        );
    }

    // PUT
    public InventoryItem updateInventoryItem(
            Integer inventoryItemId,
            InventoryItem inventoryItem
    ) {

        if (inventoryItem.getIsActive() == null) {
            inventoryItem.setIsActive(true);
        }

        int updatedRows =
                inventoryItemRepository.updateInventoryItem(
                        inventoryItemId,
                        inventoryItem
                );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Inventory item not found with ID: "
                            + inventoryItemId
            );
        }

        return inventoryItemRepository.getInventoryItemById(
                inventoryItemId
        );
    }

    // DELETE
    public void deleteInventoryItem(
            Integer inventoryItemId
    ) {

        try {
            int deletedRows =
                    inventoryItemRepository.deleteInventoryItem(
                            inventoryItemId
                    );

            if (deletedRows == 0) {
                throw new RuntimeException(
                        "Inventory item not found with ID: "
                                + inventoryItemId
                );
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory item cannot be deleted because it is referenced by another record", e);
        }
    }
}
