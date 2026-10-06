package com.rr.erp.service;

import com.rr.erp.entity.ItemModel;
import com.rr.erp.repository.ItemBrandRepository;
import com.rr.erp.repository.ItemModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemModelService {

    private final ItemModelRepository modelRepository;
    private final ItemBrandRepository brandRepository;

    public ItemModelService(
            ItemModelRepository modelRepository,
            ItemBrandRepository brandRepository
    ) {
        this.modelRepository = modelRepository;
        this.brandRepository = brandRepository;
    }

    private void validateParent(ItemModel model) {
        if (!brandRepository.existsById(model.getItemBrandId())) {
            throw new IllegalArgumentException("Brand not found with id: " + model.getItemBrandId());
        }
    }

    public ItemModel addModel(ItemModel model) {

        validateParent(model);

        if (modelRepository.existsByParentIdAndCode(model.getItemBrandId(), model.getItemModelCode())) {
            throw new IllegalArgumentException(
                    "Model code already exists under this brand: " + model.getItemModelCode()
            );
        }

        Long id = modelRepository.addModel(model);
        model.setItemModelId(id);

        return model;
    }

    public List<ItemModel> getAllModels() {
        return modelRepository.getAllModels();
    }

    public ItemModel updateModel(Long id, ItemModel model) {

        validateParent(model);

        if (modelRepository.existsByParentIdAndCodeExcludingId(model.getItemBrandId(), model.getItemModelCode(), id)) {
            throw new IllegalArgumentException(
                    "Model code already exists under this brand: " + model.getItemModelCode()
            );
        }

        int affectedRows = modelRepository.updateModel(id, model);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Model not found with id: " + id);
        }

        model.setItemModelId(id);

        return model;
    }
}
