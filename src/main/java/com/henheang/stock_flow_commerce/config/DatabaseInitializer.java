package com.henheang.stock_flow_commerce.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createOtpTablesIfMissing() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_distributor_otp (
                    id SERIAL PRIMARY KEY,
                    distributor_account_id INTEGER NOT NULL REFERENCES tb_distributor_account (id) ON DELETE CASCADE,
                    otp_code INTEGER NOT NULL,
                    distributor_email VARCHAR(255) NOT NULL,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_retailer_otp (
                    id SERIAL PRIMARY KEY,
                    retailer_account_id INTEGER NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
                    otp_code INTEGER NOT NULL,
                    retailer_email VARCHAR(255) NOT NULL,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // Keep legacy databases compatible with StoreRepository queries.
        jdbcTemplate.execute("""
                ALTER TABLE tb_store
                ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
                """);

        jdbcTemplate.execute("""
                ALTER TABLE tb_store
                ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
                """);
    }
}
