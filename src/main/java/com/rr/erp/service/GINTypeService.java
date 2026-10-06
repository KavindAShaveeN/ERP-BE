package com.rr.erp.service;

import com.rr.erp.entity.GINType;
import com.rr.erp.repository.GINTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GINTypeService {

    private final GINTypeRepository ginTypeRepository;

    public GINTypeService(GINTypeRepository ginTypeRepository) {
        this.ginTypeRepository = ginTypeRepository;
    }

    public List<GINType> getAllGinTypes() {
        return ginTypeRepository.getAllGinTypes();
    }
}