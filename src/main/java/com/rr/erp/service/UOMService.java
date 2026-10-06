package com.rr.erp.service;

import com.rr.erp.entity.UOM;
import com.rr.erp.repository.UOMRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UOMService {

    private final UOMRepository uomRepository;

    public UOMService(UOMRepository uomRepository) {
        this.uomRepository = uomRepository;
    }

    public List<UOM> getAllUom() {
        return uomRepository.getAllUom();
    }
}