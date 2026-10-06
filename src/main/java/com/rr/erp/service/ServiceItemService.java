package com.rr.erp.service;

import com.rr.erp.entity.ServiceItem;
import com.rr.erp.repository.ServiceItemRepository;
import com.rr.erp.repository.ItemCodeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;
    private final ItemCodeRepository itemCodeRepository;

    public ServiceItemService(
            ServiceItemRepository serviceItemRepository,
            ItemCodeRepository itemCodeRepository
    ) {
        this.serviceItemRepository = serviceItemRepository;
        this.itemCodeRepository = itemCodeRepository;
    }

    private void validateForeignKeys(ServiceItem item) {

        if (!itemCodeRepository.existsById(item.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Item code not found with id: " + item.getItemCodeId());
        }
    }

    public ServiceItem addServiceItem(ServiceItem item) {

        validateForeignKeys(item);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        if (serviceItemRepository.existsByItemCodeId(item.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A service item already exists for item code id: " + item.getItemCodeId());
        }

        Long id = serviceItemRepository.addServiceItem(item);
        item.setServiceItemId(id);

        return item;
    }

    public List<ServiceItem> getAllServiceItems() {
        return serviceItemRepository.getAllServiceItems();
    }

    public ServiceItem getServiceItemById(Long id) {
        return serviceItemRepository.getServiceItemById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service item not found with id: " + id));
    }

    public ServiceItem updateServiceItem(Long id, ServiceItem item) {

        validateForeignKeys(item);

        if (item.getIsActive() == null) {
            item.setIsActive(true);
        }

        if (serviceItemRepository.existsByItemCodeIdExcludingId(item.getItemCodeId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A service item already exists for item code id: " + item.getItemCodeId());
        }

        int affectedRows = serviceItemRepository.updateServiceItem(id, item);

        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Service item not found with id: " + id);
        }

        item.setServiceItemId(id);

        return item;
    }

    public void deleteServiceItem(Long id) {

        try {
            int affectedRows = serviceItemRepository.deleteById(id);

            if (affectedRows == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service item not found with id: " + id);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Service item cannot be deleted because it is referenced by another record", e);
        }
    }
}
