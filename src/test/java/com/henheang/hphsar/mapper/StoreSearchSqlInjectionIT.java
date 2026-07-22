package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.model.store.StoreBuyer;
import com.henheang.hphsar.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.henheang.hphsar.support.TestDataFactory.insertCategory;
import static com.henheang.hphsar.support.TestDataFactory.insertProduct;
import static com.henheang.hphsar.support.TestDataFactory.insertStore;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static com.henheang.hphsar.support.TestDataFactory.linkStoreCategory;
import static com.henheang.hphsar.support.TestDataFactory.linkStoreProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the StoreMapper.xml category/product search
 * queries that used to interpolate the search term via MyBatis {@code ${}}
 * (real SQL injection) and now bind it via {@code #{}}.
 */
class StoreSearchSqlInjectionIT extends AbstractIntegrationTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private int khmerFoodSupplyStoreId;
    private int seoulMarketStoreId;

    @BeforeEach
    void seedTwoStoresWithDistinctCategoriesAndProducts() {
        int supplier1 = insertSupplierAccount(jdbc, "supplier1@example.test");
        int supplier2 = insertSupplierAccount(jdbc, "supplier2@example.test");

        khmerFoodSupplyStoreId = insertStore(jdbc, supplier1, "Khmer Food Supply");
        seoulMarketStoreId = insertStore(jdbc, supplier2, "Seoul Market");

        int freshFoodCategory = insertCategory(jdbc, "Fresh Food");
        int importedGoodsCategory = insertCategory(jdbc, "Imported Goods");
        linkStoreCategory(jdbc, khmerFoodSupplyStoreId, freshFoodCategory);
        linkStoreCategory(jdbc, seoulMarketStoreId, importedGoodsCategory);

        int jasmineRice = insertProduct(jdbc, "Jasmine Rice");
        int kimchi = insertProduct(jdbc, "Kimchi");
        linkStoreProduct(jdbc, khmerFoodSupplyStoreId, jasmineRice);
        linkStoreProduct(jdbc, seoulMarketStoreId, kimchi);
    }

    @Test
    void categorySearch_partialMatch_returnsOnlyMatchingStore() {
        List<StoreBuyer> result = storeRepository.getStoresByCategorySearchASC("Food", "name");

        assertEquals(1, result.size());
        assertEquals(khmerFoodSupplyStoreId, result.get(0).getId());
    }

    @Test
    void categorySearch_injectionPayload_isTreatedAsLiteralNotAsSql() {
        List<StoreBuyer> result = storeRepository.getStoresByCategorySearchASC("' OR '1'='1", "name");

        assertTrue(result.isEmpty(),
                "A payload that isn't a real category substring must match nothing — " +
                        "getting every store back would mean the quote broke out of the string literal.");
    }

    @Test
    void productSearch_partialMatch_returnsOnlyMatchingStoreId() {
        List<Integer> result = storeRepository.getStoreIdByProductSearchASC("Rice", "name");

        assertEquals(1, result.size());
        assertEquals(khmerFoodSupplyStoreId, result.get(0));
    }

    @Test
    void productSearch_injectionPayload_isTreatedAsLiteralNotAsSql() {
        List<Integer> result = storeRepository.getStoreIdByProductSearchASC("' OR '1'='1", "name");

        assertTrue(result.isEmpty(),
                "A payload that isn't a real product substring must match nothing, not every store.");
    }
}
