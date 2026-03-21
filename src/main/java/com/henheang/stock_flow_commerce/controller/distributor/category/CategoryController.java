package com.henheang.stock_flow_commerce.controller.distributor.category;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.model.PaginationApiResponse;
import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
@Tag(name = "Distributor Category Controller")
@RequestMapping("${base.distributor.v1}categories")
@SecurityRequirement(name = "bearer")

public class CategoryController {
    private final CategoryService categoryService;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;


    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @Operation(summary = "Fetch all category from store")
    @GetMapping("")
    public ResponseEntity<?> getAllCategory(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        List<Category> categories = categoryService.getAllCategory(pageNumber, pageSize);
        PaginationApiResponse<List<Category>> categoryApiResponse = PaginationApiResponse.<List<Category>>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch all category successfully")
                .data(categories)
                .totalPage(categoryService.findTotalPage(pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok().body(categoryApiResponse);
    }


    @Operation(summary = "Get category by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .status(HttpStatus.OK.value())
                .message("Fetch category successfully")
                .data(categoryService.getCategoryById(id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Edit category")
    @PutMapping("/{id}")
    public ResponseEntity<?> editCategory(@PathVariable Integer id, @RequestParam String name) throws ParseException {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<Category> categoryApiResponse = ApiResponse.<Category>builder()
                .status(HttpStatus.OK.value())
                .message("Edit category successfully")
                .data(categoryService.editCategory(name, id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok().body(categoryApiResponse);
    }

    @Operation(summary = "Delete category from store")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        if (id > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> categoryApiResponse = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Delete category")
                .data(categoryService.deleteCategory(id))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok().body(categoryApiResponse);
    }


    @Operation(summary = "Create new category")
    @PostMapping("")
    public ResponseEntity<?> createCategoryStore(@RequestParam String name) throws ParseException {
        ApiResponse<Category> response = ApiResponse.<Category>builder()
                .status(HttpStatus.CREATED.value())
                .message("New category created.")
                .data(categoryService.createCategoryStore(name))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Search category by name")
    @GetMapping("/search")
    public ResponseEntity<?> searchCategoryByName(@RequestParam String name, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        if (pageNumber > 2147483646 || pageSize > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        PaginationApiResponse<List<Category>> response = PaginationApiResponse.<List<Category>>builder()
                .status(HttpStatus.OK.value())
                .message("New category created.")
                .data(categoryService.searchCategoryByName(name, pageNumber, pageSize))
                .totalPage(categoryService.findTotalPage(pageSize))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }

}