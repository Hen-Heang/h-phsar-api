package com.henheang.hphsar.service;

import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.category.CategoryRequest;

import java.text.ParseException;
import java.util.List;

public interface CategoryService {

    Category insertCategory(CategoryRequest categoryRequest);

    List<Category> getAllCategory(Integer pageNumber, Integer pageSize) throws ParseException;

    Category getCategoryById(Integer id) throws ParseException;

    String deleteCategory(Integer id);

    Category editCategory(String name, Integer id) throws ParseException;

    Category createCategoryStore(String name) throws ParseException;

    List<Category> getCategoryByCurrentUserId();

    /**
     * 상품 등록 시 카테고리 이름을 현재 스토어에서 바로 쓸 수 있는 카테고리 id 로 바꾼다.
     * 카테고리가 없으면 만들고, 스토어에 연결돼 있지 않으면 연결한다. 이미 있으면 그대로 쓴다.
     *
     * @param name 카테고리 이름
     * @return 현재 스토어에 연결된 카테고리 id
     */
    Integer resolveCategoryIdForStore(String name);

    Integer findTotalPage(Integer pageSize);

    List<Category> searchCategoryByName(String name, Integer pageNumber, Integer pageSize) throws ParseException;
}
