package com.rr.erp.repository;


import com.rr.erp.entity.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CurrencyRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Currency> getAllCurrencies() {

        String sql = """
                SELECT
                    currency_id,
                    currency_code,
                    currency_name
                FROM currency
                ORDER BY currency_code
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Currency currency = new Currency();

                    currency.setCurrencyId(
                            rs.getInt("currency_id")
                    );
                    currency.setCurrencyCode(
                            rs.getString("currency_code")
                    );
                    currency.setCurrencyName(
                            rs.getString("currency_name")
                    );

                    return currency;
                }
        );
    }
}