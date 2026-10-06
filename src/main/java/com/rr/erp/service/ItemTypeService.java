package com.rr.erp.service;

import com.rr.erp.entity.ItemType;
import com.rr.erp.repository.ItemTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemTypeService {

    private final ItemTypeRepository itemTypeRepository;

    public ItemTypeService(ItemTypeRepository itemTypeRepository) {
        this.itemTypeRepository = itemTypeRepository;
    }

    public List<ItemType> getAllItemTypes() {
        return itemTypeRepository.getAllItemTypes();
    }
}
