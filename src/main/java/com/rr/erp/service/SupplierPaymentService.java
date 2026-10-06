package com.rr.erp.service;

import com.rr.erp.entity.SupplierPayment;
import com.rr.erp.repository.SupplierPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierPaymentService {

    private final SupplierPaymentRepository repository;


    // POST — the due/paid/status figures shown to the user are computed on the frontend
    // from PO value, approved supplier-return value, and every payment row for the PO, so
    // this only needs to validate and record the payment itself.
    public SupplierPayment createPayment(
            SupplierPayment payment
    ) {

        if (payment.getPoCode() == null || payment.getPoCode().isBlank()) {
            throw new RuntimeException("A purchase order is required for a supplier payment.");
        }

        if (payment.getSupplierCode() == null || payment.getSupplierCode().isBlank()) {
            throw new RuntimeException("A supplier is required for a supplier payment.");
        }

        if (payment.getPaymentAmount() == null || payment.getPaymentAmount().signum() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero.");
        }

        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().isBlank()) {
            throw new RuntimeException("Payment method is required.");
        }

        payment.setSupplierPaymentId(UUID.randomUUID());

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }

        payment.setRecordedDate(LocalDateTime.now());

        repository.createPayment(payment);

        return payment;
    }


    public List<SupplierPayment> getByPoCode(String poCode) {

        return repository.getByPoCode(poCode);
    }


    // Every payment across every PO — used by the HQ-wide Supplier Payments list view.
    public List<SupplierPayment> getAllPayments() {

        return repository.getAllPayments();
    }
}
