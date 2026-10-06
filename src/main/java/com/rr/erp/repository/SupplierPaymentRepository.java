package com.rr.erp.repository;

import com.rr.erp.entity.SupplierPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SupplierPaymentRepository {

    private final JdbcTemplate jdbcTemplate;


    public void createPayment(SupplierPayment payment) {

        String sql = """
                INSERT INTO supplier_payment (
                    supplier_payment_id,
                    payment_code,
                    supplier_code,
                    project_code,
                    po_id,
                    po_code,
                    payment_date,
                    payment_amount,
                    payment_method,
                    payment_reference_no,
                    remarks,
                    recorded_by,
                    recorded_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                payment.getSupplierPaymentId(),
                payment.getPaymentCode(),
                payment.getSupplierCode(),
                payment.getProjectCode(),
                payment.getPoId(),
                payment.getPoCode(),
                payment.getPaymentDate(),
                payment.getPaymentAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentReferenceNo(),
                payment.getRemarks(),
                payment.getRecordedBy(),
                payment.getRecordedDate()
        );
    }


    public List<SupplierPayment> getByPoCode(String poCode) {

        String sql = """
                SELECT *
                FROM supplier_payment
                WHERE po_code = ?
                ORDER BY payment_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapPayment(rs),
                poCode
        );
    }


    // Every supplier payment across every PO — used to compute paid/due totals for the
    // Supplier Payments list view in one request instead of one per PO.
    public List<SupplierPayment> getAllPayments() {

        String sql = """
                SELECT *
                FROM supplier_payment
                ORDER BY payment_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapPayment(rs)
        );
    }


    private SupplierPayment mapPayment(ResultSet rs) throws SQLException {

        SupplierPayment payment = new SupplierPayment();

        payment.setSupplierPaymentId(
                rs.getObject("supplier_payment_id", UUID.class)
        );

        payment.setPaymentCode(
                rs.getString("payment_code")
        );

        payment.setSupplierCode(
                rs.getString("supplier_code")
        );

        payment.setProjectCode(
                rs.getString("project_code")
        );

        payment.setPoId(
                rs.getObject("po_id", UUID.class)
        );

        payment.setPoCode(
                rs.getString("po_code")
        );

        payment.setPaymentDate(
                rs.getObject("payment_date", LocalDate.class)
        );

        payment.setPaymentAmount(
                rs.getBigDecimal("payment_amount")
        );

        payment.setPaymentMethod(
                rs.getString("payment_method")
        );

        payment.setPaymentReferenceNo(
                rs.getString("payment_reference_no")
        );

        payment.setRemarks(
                rs.getString("remarks")
        );

        payment.setRecordedBy(
                rs.getString("recorded_by")
        );

        payment.setRecordedDate(
                rs.getObject("recorded_date", LocalDateTime.class)
        );

        return payment;
    }
}
