package com.rr.erp.service;

import com.rr.erp.entity.ItemOptionalOne;
import com.rr.erp.repository.ItemModelRepository;
import com.rr.erp.repository.ItemOptionalOneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemOptionalOneService {

    private final ItemOptionalOneRepository optionalOneRepository;
    private final ItemModelRepository modelRepository;

    public ItemOptionalOneService(
            ItemOptionalOneRepository optionalOneRepository,
            ItemModelRepository modelRepository
    ) {
        this.optionalOneRepository = optionalOneRepository;
        this.modelRepository = modelRepository;
    }

    private void validateParent(ItemOptionalOne optionalOne) {
        if (!modelRepository.existsById(optionalOne.getItemModelId())) {
            throw new IllegalArgumentException("Model not found with id: " + optionalOne.getItemModelId());
        }
    }

    public ItemOptionalOne addOptionalOne(ItemOptionalOne optionalOne) {

        validateParent(optionalOne);

        if (optionalOneRepository.existsByParentIdAndCode(optionalOne.getItemModelId(), optionalOne.getItemOptionalOneCode())) {
            throw new IllegalArgumentException(
                    "Optional 1 code already exists under this model: " + optionalOne.getItemOptionalOneCode()
            );
        }

        Long id = optionalOneRepository.addOptionalOne(optionalOne);
        optionalOne.setItemOptionalOneId(id);

        return optionalOne;
    }

    public List<ItemOptionalOne> getAllOptionalOnes() {
        return optionalOneRepository.getAllOptionalOnes();
    }

    public ItemOptionalOne updateOptionalOne(Long id, ItemOptionalOne optionalOne) {

        validateParent(optionalOne);

        if (optionalOneRepository.existsByParentIdAndCodeExcludingId(optionalOne.getItemModelId(), optionalOne.getItemOptionalOneCode(), id)) {
            throw new IllegalArgumentException(
                    "Optional 1 code already exists under this model: " + optionalOne.getItemOptionalOneCode()
            );
        }

        int affectedRows = optionalOneRepository.updateOptionalOne(id, optionalOne);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Optional 1 not found with id: " + id);
        }

        optionalOne.setItemOptionalOneId(id);

        return optionalOne;
    }
}
