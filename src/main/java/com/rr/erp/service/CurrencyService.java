package com.rr.erp.service;

import com.rr.erp.entity.Currency;
import com.rr.erp.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository repository;

    public List<Currency> getAllCurrencies() {

        return repository.getAllCurrencies();
    }
}