package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.category.Category;
import com.henheang.stock_flow_commerce.model.category.CategoryRequest;

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

    Integer findTotalPage(Integer pageSize);

    List<Category> searchCategoryByName(String name, Integer pageNumber, Integer pageSize) throws ParseException;
}
