package com.henheang.hphsar.service;

import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductEditRequest;
import com.henheang.hphsar.model.product.ProductImport;
import com.henheang.hphsar.model.product.ProductRequest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public interface ProductDistributorService {
    List<Product> insertNewProduct(Integer currentUserId, ArrayList<ProductRequest> productRequests) throws ParseException;

    Product getProductById(Integer id) throws ParseException;

    String deleteProductById(Integer productId);

//    List<Product> getAllProductByQty(Integer currentUserId);
//
    List<Product> getAllProductByName(Integer currentUserId, String name) throws ParseException;
//
//    List<Product> getAllProductByUnitPrice(Integer currentUserId);
//
//    List<Product> importProduct(Integer currentUserId);

    Product editProduct(Integer id, ProductEditRequest productRequest) throws ParseException;

    String unPublishProduct(Integer id);

    String publishProduct(Integer id);
    List<Product> getAllProductBySorting(String sort, String by, Integer pageNumber, Integer pageSize) throws ParseException;
//
    Integer getTotalPage(Integer pageSize);

    List<Product> importProduct(List<ProductImport> productsImport) throws ParseException;

}
