package com.rr.erp.service;

import com.rr.erp.entity.ReceivedService;
import com.rr.erp.repository.ReceivedServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceivedServiceService {

    private final ReceivedServiceRepository receivedServiceRepository;

    public ReceivedServiceService(ReceivedServiceRepository receivedServiceRepository) {
        this.receivedServiceRepository = receivedServiceRepository;
    }

    public List<ReceivedService> getByProjectCode(String projectCode) {
        return receivedServiceRepository.findByProjectCode(projectCode);
    }

    public List<ReceivedService> getAll() {
        return receivedServiceRepository.findAll();
    }
}
