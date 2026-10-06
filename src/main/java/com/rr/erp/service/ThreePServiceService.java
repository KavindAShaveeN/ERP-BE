package com.rr.erp.service;

import com.rr.erp.entity.ThreePService;
import com.rr.erp.repository.ThreePServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ThreePServiceService {

    private final ThreePServiceRepository repository;

    public ThreePServiceService(ThreePServiceRepository repository) {
        this.repository = repository;
    }

    public ThreePService createThreePService(ThreePService service) {

        return repository.createThreePService(service);
    }

    public ThreePService updateThreePService(
            UUID threePServiceId,
            ThreePService service
    ) {

        int updatedRows =
                repository.updateThreePService(
                        threePServiceId,
                        service
                );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "3P Service not found with ID: " + threePServiceId
            );
        }

        service.setThreePServiceId(threePServiceId);

        return service;
    }

    public void deleteThreePService(UUID threePServiceId) {

        int deletedRows =
                repository.deleteThreePService(threePServiceId);

        if (deletedRows == 0) {
            throw new RuntimeException(
                    "3P Service not found with ID: " + threePServiceId
            );
        }
    }
}