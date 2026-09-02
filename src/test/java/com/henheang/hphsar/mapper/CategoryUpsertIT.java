package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.henheang.hphsar.support.TestDataFactory.insertCategory;
import static com.henheang.hphsar.support.TestDataFactory.insertStore;
import static com.henheang.hphsar.support.TestDataFactory.insertSupplierAccount;
import static com.henheang.hphsar.support.TestDataFactory.linkStoreCategory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coverage for the two find-or-create queries added to {@code CategoryMapper.xml} so a
 * product can be created against a category name instead of a pre-existing id:
 * {@code upsertCategoryByName} and {@code linkStoreCategoryIfAbsent}.
 *
 * <p>Both replace a read-then-write pair that was not safe to repeat. These tests pin the
 * properties that make them safe: the upsert is idempotent and revives a soft-deleted
 * name rather than colliding with the {@code UNIQUE(name)} row it left behind, and the
 * link insert never produces a second row for the same (store, category).
 *
 * @author henheang
 * @since 2026.09.02
 * @version 1.0
 */
class CategoryUpsertIT extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private int storeId;
    private String tag;

    @BeforeEach
    void seedOneStore() {
        // Unique per-invocation tag — the Testcontainers Postgres is a JVM-wide singleton
        // (see AbstractIntegrationTest), so fixed names collide across test methods.
        this.tag = "Tag" + System.nanoTime();
        int supplier = insertSupplierAccount(jdbc, "upsert-" + tag + "@example.test");
        storeId = insertStore(jdbc, supplier, "Upsert Store " + tag);
    }

    @Test
    void createsTheCategoryWhenTheNameIsNew() {
        String name = "beverages " + tag;

        Integer categoryId = categoryRepository.upsertCategoryByName(name);

        assertEquals(name, categoryNameOf(categoryId),
                "The upsert must return the id of the row it just created.");
    }

    @Test
    void returnsTheSameIdInsteadOfInsertingTwice() {
        String name = "snacks " + tag;

        Integer firstId = categoryRepository.upsertCategoryByName(name);
        Integer secondId = categoryRepository.upsertCategoryByName(name);

        assertEquals(firstId, secondId,
                "A repeated upsert must resolve to the existing row — this is what makes the "
                        + "concurrent 'both suppliers add the same new category' case safe.");
        assertEquals(1, countCategoriesNamed(name),
                "tb_category must still hold exactly one row for the name.");
    }

    @Test
    void revivesASoftDeletedCategoryInsteadOfCollidingWithIt() {
        String name = "seasonal " + tag;
        int deletedId = insertCategory(jdbc, name);
        // Mirrors CategoryMapper.deleteCategory: the row keeps holding the UNIQUE name.
        jdbc.update("UPDATE tb_category SET is_active = false WHERE id = ?", deletedId);

        Integer resolvedId = categoryRepository.upsertCategoryByName(name);

        assertEquals(deletedId, resolvedId,
                "The soft-deleted row must be reused, not duplicated.");
        assertTrue(isActive(deletedId),
                "Re-creating a soft-deleted category must make it usable again — a plain "
                        + "INSERT here used to fail on the UNIQUE(name) constraint instead.");
    }

    @Test
    void linksTheCategoryToTheStoreWhenAbsent() {
        int categoryId = insertCategory(jdbc, "linkable " + tag);

        int affectedRows = categoryRepository.linkStoreCategoryIfAbsent(storeId, categoryId);

        assertEquals(1, affectedRows, "A missing link must be inserted.");
        assertEquals(1, countLinks(categoryId));
    }

    @Test
    void doesNotDuplicateAnExistingStoreLink() {
        int categoryId = insertCategory(jdbc, "already-linked " + tag);
        linkStoreCategory(jdbc, storeId, categoryId);

        int affectedRows = categoryRepository.linkStoreCategoryIfAbsent(storeId, categoryId);

        assertEquals(0, affectedRows,
                "0 affected rows is the 'store already had it' case, which the service treats as success.");
        assertEquals(1, countLinks(categoryId),
                "tb_store_category has no UNIQUE (store_id, category_id), so only the "
                        + "WHERE NOT EXISTS guard prevents a duplicate link row here.");
    }

    private String categoryNameOf(Integer categoryId) {
        return jdbc.queryForObject("SELECT name FROM tb_category WHERE id = ?", String.class, categoryId);
    }

    private boolean isActive(int categoryId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT is_active FROM tb_category WHERE id = ?", Boolean.class, categoryId));
    }

    private int countCategoriesNamed(String name) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tb_category WHERE name = ?", Integer.class, name);
        return count == null ? 0 : count;
    }

    private int countLinks(int categoryId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tb_store_category WHERE store_id = ? AND category_id = ?",
                Integer.class, storeId, categoryId);
        return count == null ? 0 : count;
    }
}
