package com.henheang.hphsar.controller.distributor.category;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;


@RestController
@Tag(name = "Distributor Category Controller")
@RequestMapping("${base.distributor.v1}/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController extends BaseController {
    private final CategoryService categoryService;


    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @Operation(summary = "Fetch all category from store")
    @GetMapping("")
    public ResponseEntity<?> getAllCategory(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
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
        if (id > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        return ok(
                "Fetched category detail.",
                categoryService.getCategoryById(id));
    }

    @Operation(summary = "Edit category")
    @PutMapping("/{id}")
    public ResponseEntity<?> editCategory(@PathVariable Integer id, @RequestParam String name) throws ParseException {
        if (id > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        return ok(
                "Category updated.",
                categoryService.editCategory(name, id));
    }

    @Operation(summary = "Delete category from store")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        if (id > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
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
        if (pageNumber > 2147483646 || pageSize > 2147483646) {
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        return okPage(
                "Fetched category detail.",
                categoryService.searchCategoryByName(name, pageNumber, pageSize),
                pageNumber,
                pageSize,
                categoryService.findTotalPage(pageSize)
        );
    }

}