package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.repository.SupplierProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Locale;

import static com.henheang.hphsar.support.TestDataFactory.insertProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Regression coverage for SupplierProductMapper.xml#getProductIdByName,
 * which used to interpolate the product name via {@code ${name}} into
 * {@code ILIKE '${name}'} and now binds it via {@code #{name}}. The method
 * is used as an exact (case-insensitive) lookup — see
 * SupplierProductServiceImpl's "check if store already have this
 * product"/"if product already exist get id" call sites — so no wildcards
 * are added here, only parameter binding.
 */
class SupplierProductLookupSqlInjectionIT extends AbstractIntegrationTest {

    @Autowired
    private SupplierProductRepository supplierProductRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private int jasmineRiceProductId;
    private String productName;

    @BeforeEach
    void seedOneProduct() {
        // tb_product.name is UNIQUE and the Testcontainers database is shared by every
        // *IT class with no truncation between tests, so a fixed literal collides on
        // the second @BeforeEach in this class. Tag the name per invocation and derive
        // the case variants from it.
        productName = "Jasmine Rice " + System.nanoTime();
        jasmineRiceProductId = insertProduct(jdbc, productName);
    }

    @Test
    void exactMatch_findsExistingProductCaseInsensitively() {
        assertEquals(jasmineRiceProductId, supplierProductRepository.getProductIdByName(productName));
        assertEquals(jasmineRiceProductId,
                supplierProductRepository.getProductIdByName(productName.toLowerCase(Locale.ROOT)));
        assertEquals(jasmineRiceProductId,
                supplierProductRepository.getProductIdByName(productName.toUpperCase(Locale.ROOT)));
    }

    @Test
    void partialNameDoesNotMatch_confirmingThisIsAnExactLookupNotASearch() {
        assertEquals(0, supplierProductRepository.getProductIdByName("Rice"));
        assertEquals(0, supplierProductRepository.getProductIdByName("Jasmine"));
    }

    @Test
    void injectionPayload_isTreatedAsLiteralAndReturnsNoMatch() {
        Integer result = supplierProductRepository.getProductIdByName("' OR '1'='1");

        assertEquals(0, result,
                "A payload that isn't a real product name must resolve to the COALESCE default (0), " +
                        "not to an arbitrary/first row — that would mean the quote broke out of the SQL literal.");
        assertNotEquals(jasmineRiceProductId, result);
    }
}
