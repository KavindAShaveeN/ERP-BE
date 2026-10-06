package com.rr.erp.service;

import com.rr.erp.entity.ItemOptionalTwo;
import com.rr.erp.repository.ItemOptionalOneRepository;
import com.rr.erp.repository.ItemOptionalTwoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemOptionalTwoService {

    private final ItemOptionalTwoRepository optionalTwoRepository;
    private final ItemOptionalOneRepository optionalOneRepository;

    public ItemOptionalTwoService(
            ItemOptionalTwoRepository optionalTwoRepository,
            ItemOptionalOneRepository optionalOneRepository
    ) {
        this.optionalTwoRepository = optionalTwoRepository;
        this.optionalOneRepository = optionalOneRepository;
    }

    private void validateParent(ItemOptionalTwo optionalTwo) {
        if (!optionalOneRepository.existsById(optionalTwo.getItemOptionalOneId())) {
            throw new IllegalArgumentException("Optional 1 not found with id: " + optionalTwo.getItemOptionalOneId());
        }
    }

    public ItemOptionalTwo addOptionalTwo(ItemOptionalTwo optionalTwo) {

        validateParent(optionalTwo);

        if (optionalTwoRepository.existsByParentIdAndCode(optionalTwo.getItemOptionalOneId(), optionalTwo.getItemOptionalTwoCode())) {
            throw new IllegalArgumentException(
                    "Optional 2 code already exists under this optional 1: " + optionalTwo.getItemOptionalTwoCode()
            );
        }

        Long id = optionalTwoRepository.addOptionalTwo(optionalTwo);
        optionalTwo.setItemOptionalTwoId(id);

        return optionalTwo;
    }

    public List<ItemOptionalTwo> getAllOptionalTwos() {
        return optionalTwoRepository.getAllOptionalTwos();
    }

    public ItemOptionalTwo updateOptionalTwo(Long id, ItemOptionalTwo optionalTwo) {

        validateParent(optionalTwo);

        if (optionalTwoRepository.existsByParentIdAndCodeExcludingId(optionalTwo.getItemOptionalOneId(), optionalTwo.getItemOptionalTwoCode(), id)) {
            throw new IllegalArgumentException(
                    "Optional 2 code already exists under this optional 1: " + optionalTwo.getItemOptionalTwoCode()
            );
        }

        int affectedRows = optionalTwoRepository.updateOptionalTwo(id, optionalTwo);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Optional 2 not found with id: " + id);
        }

        optionalTwo.setItemOptionalTwoId(id);

        return optionalTwo;
    }
}
