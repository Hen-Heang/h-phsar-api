package com.henheang.hphsar.controller.supplier.category;
import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Tag(name = "Supplier Category Controller")
@RequestMapping("${base.supplier.v1}/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController extends BaseController {
    private final CategoryService categoryService;


    @Operation(summary = "Fetch all category from store")
    @GetMapping("")
    public ResponseEntity<?> getAllCategory(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<Category> categories = categoryService.getAllCategory(pageNumber, pageSize);
        return okPage(
                "Fetched all categories.",
                categories,
                pageNumber,
                pageSize,
                categoryService.findTotalPage(pageSize)
        );
    }


    @Operation(summary = "Get category by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(
                "Fetched category detail.",
                categoryService.getCategoryById(id));
    }

    @Operation(summary = "Edit category")
    @PutMapping("/{id}")
    public ResponseEntity<?> editCategory(@PathVariable Integer id, @RequestParam String name) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(
                "Category updated.",
                categoryService.editCategory(name, id));
    }

    @Operation(summary = "Delete category from store")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(
                "Category deleted.",
                categoryService.deleteCategory(id));
    }


    @Operation(summary = "Create new category")
    @PostMapping("")
    public ResponseEntity<?> createCategoryStore(@RequestParam String name) throws ParseException {
        return created(
                "New category created.",
                categoryService.createCategoryStore(name));
    }

    @Operation(summary = "Search category by name")
    @GetMapping("/search")
    public ResponseEntity<?> searchCategoryByName(@RequestParam String name, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        return okPage(
                "Fetched category detail.",
                categoryService.searchCategoryByName(name, pageNumber, pageSize),
                pageNumber,
                pageSize,
                categoryService.findTotalPage(pageSize)
        );
    }

}