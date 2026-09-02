package com.henheang.hphsar.mapper;

import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static com.henheang.hphsar.support.TestDataFactory.insertCategory;
import static com.henheang.hphsar.support.TestDataFactory.insertStore;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static com.henheang.hphsar.support.TestDataFactory.linkStoreCategory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coverage for {@code CategoryMapper.xml}'s {@code getCategoryByCurrentUserId} —
 * the un-paginated "every category of my store" query behind
 * {@code GET /api/v1/suppliers/categories/all}.
 *
 * <p>The query existed before but was unreachable over HTTP, so its store-ownership
 * filter was never exercised by a test. Now that the product-creation screen calls it
 * to fill the category dropdown, these tests pin the three properties that endpoint
 * depends on: it is scoped to one store, it hides soft-deleted categories, and it is
 * genuinely un-paginated (the reason it replaced a {@code pageSize=100} call).
 *
 * @author henheang
 * @since 2026.09.02
 * @version 1.0
 */
class CategoryStoreListingIT extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private int ownStoreId;
    private int otherStoreId;
    private String tag;

    @BeforeEach
    void seedTwoStoresEachWithItsOwnCategory() {
        // Unique per-invocation tag — @BeforeEach runs once per test method against the
        // same JVM-wide singleton Testcontainers Postgres (see AbstractIntegrationTest),
        // so fixed emails/names would hit a duplicate-key violation on the 2nd test.
        this.tag = "Tag" + System.nanoTime();

        int ownSupplier = insertSupplierAccount(jdbc, "own-" + tag + "@example.test");
        int otherSupplier = insertSupplierAccount(jdbc, "other-" + tag + "@example.test");

        ownStoreId = insertStore(jdbc, ownSupplier, "Own Store " + tag);
        otherStoreId = insertStore(jdbc, otherSupplier, "Other Store " + tag);
    }

    @Test
    void returnsOnlyTheCategoriesLinkedToTheGivenStore() {
        int ownCategory = insertCategory(jdbc, "Beverages " + tag);
        int otherCategory = insertCategory(jdbc, "Stationery " + tag);
        linkStoreCategory(jdbc, ownStoreId, ownCategory);
        linkStoreCategory(jdbc, otherStoreId, otherCategory);

        List<Category> result = categoryRepository.getCategoryByCurrentUserId(ownStoreId);

        assertTrue(containsId(result, ownCategory),
                "The store's own category must be listed.");
        assertFalse(containsId(result, otherCategory),
                "Another supplier's category must never appear — the dropdown is scoped to one store.");
    }

    @Test
    void excludesSoftDeletedCategories() {
        int liveCategory = insertCategory(jdbc, "Live " + tag);
        int deletedCategory = insertCategory(jdbc, "Deleted " + tag);
        linkStoreCategory(jdbc, ownStoreId, liveCategory);
        linkStoreCategory(jdbc, ownStoreId, deletedCategory);
        // Mirrors CategoryMapper.deleteCategory, which soft-deletes via is_active.
        jdbc.update("UPDATE tb_category SET is_active = false WHERE id = ?", deletedCategory);

        List<Category> result = categoryRepository.getCategoryByCurrentUserId(ownStoreId);

        assertTrue(containsId(result, liveCategory),
                "An active category must still be listed.");
        assertFalse(containsId(result, deletedCategory),
                "A soft-deleted category must not be selectable when creating a product.");
    }

    @Test
    void returnsEveryCategoryWithoutAPageSizeCap() {
        // 120 > the pageSize=100 the product screen used to send. Before this endpoint
        // existed the last 20 were silently dropped from the dropdown, so a supplier
        // could not pick them when creating a product.
        int categoryCount = 120;
        for (int index = 0; index < categoryCount; index++) {
            linkStoreCategory(jdbc, ownStoreId, insertCategory(jdbc, "Bulk " + index + " " + tag));
        }

        List<Category> result = categoryRepository.getCategoryByCurrentUserId(ownStoreId);

        assertEquals(categoryCount, result.size(),
                "Every category of the store must come back in one call — this query is un-paginated by design.");
    }

    private boolean containsId(List<Category> categories, int categoryId) {
        return categories.stream().anyMatch(category -> category.getId() == categoryId);
    }
}
