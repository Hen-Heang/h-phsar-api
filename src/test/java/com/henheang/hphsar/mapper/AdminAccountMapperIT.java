package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.model.appUser.AdminUserSummaryDto;
import com.henheang.hphsar.repository.AdminAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.henheang.hphsar.support.TestDataFactory.insertBuyerAccount;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real-Postgres coverage for AdminAccountMapper.xml: search filtering,
 * pagination math, and the guarded status-update queries. Every fixture name
 * is tagged with a per-run unique suffix and every list/count assertion
 * filters by that tag — the container in AbstractIntegrationTest is a shared
 * static resource (no per-test rollback), so assertions must not assume they
 * see the *only* rows in the table. Service-layer throw-on-zero-rows behavior
 * is covered separately in AdminAccountServiceImplTest (mocked repository).
 */
class AdminAccountMapperIT extends AbstractIntegrationTest {

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private String tag;
    private int khmerSupplierId;
    private int seoulSupplierId;

    @BeforeEach
    void seedTwoSuppliers() {
        tag = "Tag" + System.nanoTime();

        khmerSupplierId = insertSupplierAccount(jdbc, "khmer-" + tag + "@example.test");
        seoulSupplierId = insertSupplierAccount(jdbc, "seoul-" + tag + "@example.test");

        jdbc.update("INSERT INTO tb_supplier_info (supplier_account_id, first_name, last_name, primary_phone_number) " +
                "VALUES (?, ?, 'Foods', '011111111')", khmerSupplierId, "Khmer" + tag);
        jdbc.update("INSERT INTO tb_supplier_info (supplier_account_id, first_name, last_name, primary_phone_number) " +
                "VALUES (?, ?, 'Market', '022222222')", seoulSupplierId, "Seoul" + tag);
    }

    @Test
    void listSuppliers_searchByTag_returnsBothWithProfileFields() {
        List<AdminUserSummaryDto> result = adminAccountRepository.listSuppliers(tag, 1, 10);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> ("Khmer" + tag + " Foods").equals(s.getFullName())));
        assertTrue(result.stream().anyMatch(s -> ("Seoul" + tag + " Market").equals(s.getFullName())));
    }

    @Test
    void listSuppliers_searchByNamePrefix_returnsOnlyMatchingSupplier() {
        List<AdminUserSummaryDto> result = adminAccountRepository.listSuppliers("Khmer" + tag, 1, 10);

        assertEquals(1, result.size());
        assertEquals(khmerSupplierId, result.get(0).getId());
    }

    @Test
    void listSuppliers_searchInjectionPayload_matchesNothing() {
        List<AdminUserSummaryDto> result = adminAccountRepository.listSuppliers("' OR '1'='1", 1, 10);

        assertTrue(result.isEmpty(),
                "A payload that isn't a real name/email substring must match nothing, not every supplier.");
    }

    @Test
    void listSuppliers_pageSizeOne_returnsOnlyOneRowPerPage() {
        List<AdminUserSummaryDto> page1 = adminAccountRepository.listSuppliers(tag, 1, 1);
        List<AdminUserSummaryDto> page2 = adminAccountRepository.listSuppliers(tag, 2, 1);

        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertTrue(!page1.get(0).getId().equals(page2.get(0).getId()),
                "Page 1 and page 2 with pageSize=1 must return different rows.");
    }

    @Test
    void countSuppliers_scopedByTag_matchesSeededRows() {
        assertEquals(2, adminAccountRepository.countSuppliers(tag));
        assertEquals(1, adminAccountRepository.countSuppliers("Khmer" + tag));
    }

    @Test
    void updateSupplierActiveStatus_existingId_updatesAndReturnsOneRowAffected() {
        Integer rowsAffected = adminAccountRepository.updateSupplierActiveStatus(khmerSupplierId, false);

        assertEquals(1, rowsAffected);
        AdminUserSummaryDto updated = adminAccountRepository.findSupplierById(khmerSupplierId);
        assertEquals(false, updated.getIsActive());
    }

    @Test
    void updateSupplierActiveStatus_unknownId_returnsZeroRowsAffected() {
        Integer rowsAffected = adminAccountRepository.updateSupplierActiveStatus(-1, false);

        assertEquals(0, rowsAffected);
    }

    @Test
    void listBuyers_searchByTag_returnsOnlyMatchingBuyer() {
        int buyerId = insertBuyerAccount(jdbc, "buyer-" + tag + "@example.test");

        List<AdminUserSummaryDto> result = adminAccountRepository.listBuyers(tag, 1, 10);

        assertEquals(1, result.size());
        assertEquals(buyerId, result.get(0).getId());
    }
}
