package com.rr.erp.service;

import com.rr.erp.entity.Supplier;
import com.rr.erp.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier addSupplier(Supplier supplierRequest) {

        if (supplierRepository.existsBySupplierCode( supplierRequest.getSupplierCode() ))
        {
            throw new IllegalArgumentException(
                    "Supplier code already exists: "
                            + supplierRequest.getSupplierCode()
            );
        }

        if (supplierRequest.getStatus() == null) {
            supplierRequest.setStatus(true);
        }

        int affectedRows = supplierRepository.addSupplier(supplierRequest);

        if (affectedRows != 1) {
            throw new IllegalStateException(
                    "Supplier could not be added"
            );
        }

        return supplierRequest;
    }
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.getAllSuppliers();
    }

    public Supplier updateSupplier(
            String supplierCode,
            Supplier supplierRequest
    ) {

        int affectedRows = supplierRepository.updateSupplier(
                supplierCode,
                supplierRequest
        );

        if (affectedRows == 0) {
            throw new IllegalArgumentException(
                    "Supplier not found with code: " + supplierCode
            );
        }

        return supplierRequest;
    }
}