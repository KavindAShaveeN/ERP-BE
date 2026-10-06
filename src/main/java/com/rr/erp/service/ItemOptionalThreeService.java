package com.rr.erp.service;

import com.rr.erp.entity.ItemOptionalThree;
import com.rr.erp.repository.ItemOptionalThreeRepository;
import com.rr.erp.repository.ItemOptionalTwoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemOptionalThreeService {

    private final ItemOptionalThreeRepository optionalThreeRepository;
    private final ItemOptionalTwoRepository optionalTwoRepository;

    public ItemOptionalThreeService(
            ItemOptionalThreeRepository optionalThreeRepository,
            ItemOptionalTwoRepository optionalTwoRepository
    ) {
        this.optionalThreeRepository = optionalThreeRepository;
        this.optionalTwoRepository = optionalTwoRepository;
    }

    private void validateParent(ItemOptionalThree optionalThree) {
        if (!optionalTwoRepository.existsById(optionalThree.getItemOptionalTwoId())) {
            throw new IllegalArgumentException("Optional 2 not found with id: " + optionalThree.getItemOptionalTwoId());
        }
    }

    public ItemOptionalThree addOptionalThree(ItemOptionalThree optionalThree) {

        validateParent(optionalThree);

        if (optionalThreeRepository.existsByParentIdAndCode(optionalThree.getItemOptionalTwoId(), optionalThree.getItemOptionalThreeCode())) {
            throw new IllegalArgumentException(
                    "Optional 3 code already exists under this optional 2: " + optionalThree.getItemOptionalThreeCode()
            );
        }

        Long id = optionalThreeRepository.addOptionalThree(optionalThree);
        optionalThree.setItemOptionalThreeId(id);

        return optionalThree;
    }

    public List<ItemOptionalThree> getAllOptionalThrees() {
        return optionalThreeRepository.getAllOptionalThrees();
    }

    public ItemOptionalThree updateOptionalThree(Long id, ItemOptionalThree optionalThree) {

        validateParent(optionalThree);

        if (optionalThreeRepository.existsByParentIdAndCodeExcludingId(optionalThree.getItemOptionalTwoId(), optionalThree.getItemOptionalThreeCode(), id)) {
            throw new IllegalArgumentException(
                    "Optional 3 code already exists under this optional 2: " + optionalThree.getItemOptionalThreeCode()
            );
        }

        int affectedRows = optionalThreeRepository.updateOptionalThree(id, optionalThree);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Optional 3 not found with id: " + id);
        }

        optionalThree.setItemOptionalThreeId(id);

        return optionalThree;
    }
}
