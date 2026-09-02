package com.henheang.hphsar.controller.supplier.category;

import com.henheang.hphsar.utils.ValidationUtils;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.common.api.PagedResponse;
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
    public ResponseEntity<PagedResponse<Category>> getAllCategory(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        List<Category> categories = categoryService.getAllCategory(pageNumber, pageSize);
        return okPage(
                Code.FETCHED,
                categories,
                pageNumber,
                pageSize,
                categoryService.findTotalPage(pageSize)
        );
    }


    @Operation(summary = "Get category by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Integer id) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(
                Code.FETCHED,
                categoryService.getCategoryById(id));
    }

    @Operation(summary = "Edit category")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> editCategory(@PathVariable Integer id, @RequestParam String name) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(
                "Category updated.",
                categoryService.editCategory(name, id));
    }

    @Operation(summary = "Delete category from store")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Integer id) {
        ValidationUtils.rejectIfExceedsIntLimit(id);
        return ok(
                "Category deleted.",
                categoryService.deleteCategory(id));
    }


    @Operation(summary = "Create new category")
    @PostMapping("")
    public ResponseEntity<ApiResponse<Category>> createCategoryStore(@RequestParam String name) throws ParseException {
        return created(
                "New category created.",
                categoryService.createCategoryStore(name));
    }

    /**
     * 현재 로그인한 공급자 스토어의 전체 카테고리를 페이징 없이 조회한다.
     *
     * <p>상품 등록 화면의 카테고리 선택 목록처럼 스토어의 모든 카테고리가 한 번에 필요한
     * 경우에 사용한다. 페이징 목록({@code GET /categories})을 큰 pageSize 로 호출해
     * 대신하면 그 상한을 넘는 카테고리가 조용히 누락되므로 별도 엔드포인트로 제공한다.
     *
     * @return 스토어의 활성 카테고리 전체 {@link Category}
     */
    @Operation(summary = "Fetch every category of the current store (no paging)")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategoryOfCurrentStore() {
        return ok(
                Code.FETCHED,
                categoryService.getCategoryByCurrentUserId());
    }

    @Operation(summary = "Search category by name")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<Category>> searchCategoryByName(@RequestParam String name, @RequestParam Integer pageNumber, @RequestParam Integer pageSize) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(pageNumber, pageSize);
        return okPage(
                Code.FETCHED,
                categoryService.searchCategoryByName(name, pageNumber, pageSize),
                pageNumber,
                pageSize,
                categoryService.findTotalPage(pageSize)
        );
    }

}