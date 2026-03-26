package com.henheang.hphsar.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseInitializer — Auto Schema Setup on Startup
 * <p>
 * Runs SQL statements once when the application starts to ensure the
 * database schema is ready before any request is handled.
 * <p>
 * Why use this instead of Flyway/Liquibase?
 *   - Simple approach for small, safe schema additions using IF NOT EXISTS
 *   - Safe to run multiple times (no side effects)
 *   - Good for adding columns or tables that may be missing in older deployments
 * <p>
 * When does it run?
 *   @PostConstruct → runs automatically after Spring injects all dependencies,
 *   but before the app starts handling HTTP requests.
 * <p>
 * What it sets up:
 *   - tb_distributor_otp  : stores OTP codes sent to distributors (for password reset / verification)
 *   - tb_retailer_otp     : stores OTP codes sent to retailers
 *   - tb_store.is_active  : adds soft-delete column if missing
 *   - tb_store.phone      : adds phone column if missing
 */
@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeDatabaseSchema() {

        // OTP table for distributors — used to verify email or reset password
        // Links to tb_distributor_account and deletes OTP when account is deleted
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_distributor_otp (
                    id                     SERIAL PRIMARY KEY,
                    distributor_account_id INTEGER NOT NULL REFERENCES tb_distributor_account (id) ON DELETE CASCADE,
                    otp_code               INTEGER NOT NULL,
                    distributor_email      VARCHAR(255) NOT NULL,
                    created_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // OTP table for retailers — same purpose as distributor OTP
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_retailer_otp (
                    id                   SERIAL PRIMARY KEY,
                    retailer_account_id  INTEGER NOT NULL REFERENCES tb_retailer_account (id) ON DELETE CASCADE,
                    otp_code             INTEGER NOT NULL,
                    retailer_email       VARCHAR(255) NOT NULL,
                    created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """);

        // Add is_active column to tb_store (soft delete — FALSE means store is deactivated, not deleted)
        jdbcTemplate.execute("""
                ALTER TABLE tb_store
                ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
                """);

        // Add phone column to tb_store for store contact information
        jdbcTemplate.execute("""
                ALTER TABLE tb_store
                ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
                """);
    }
}
