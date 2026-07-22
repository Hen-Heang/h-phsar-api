package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.category.Category;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductEditRequest;
import com.henheang.hphsar.model.product.ProductRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface SupplierProductRepository {


    Integer getStoreIdByCurrentUserId(Integer currentUserId);

    Integer insertNewProductImport(Integer storeId);

    void insertNewProductImportDetail(Integer productId, Integer productImportId, @Param("proRequest") ProductRequest productRequest);

    Integer getCategoryIdByName(String categoryName);

    void insertNewProductCategory(Integer productId, Integer categoryIdFromName);

    void updateStoreProductDetail(Integer storeId, Integer productId, Integer qty, Double price, Boolean isPublish);

    Product getProductById(Integer storeId,Integer productId);

    String getCategories(Integer categoryId);

    void deleteProductById(Integer productId, Integer storeId);

    String deleteProductDetailById(Integer productId, Integer storeId);

    List<Product> getAllProductByName(String name, Integer storeId);

    List<Product> getAllProductByUnitPrice(Integer storeId);

    Integer getProductIdByName(String name);

    boolean checkStoreHasProduct(Integer storeId, Integer productId);

    Integer createNewProduct(String name);

    String insertNewProduct(Integer storeId, Integer productId, @Param("prod") ProductRequest productRequest);

    Integer createNewImportRecord(Integer storeId);

    void insertImportDetail(Integer importId, Integer productId, Integer qty, Double price, Integer categoryId);

    Product getStoreProductByStoreProductId(Integer storeProductDetailId);

    Category getCategoryByCategoryId(Integer id);

    String changeStoreProductDetail(Integer storeId, @Param("prod") ProductEditRequest productRequest, Integer productId, Integer id, Boolean isPublish);

    Integer unPublishProduct(Integer storeId, Integer id);

    Integer publishProduct(Integer storeId, Integer id);

    List<Product> getAllProductByQtyASC(String by, Integer storeId, Integer pageSize, Integer pageNumber);

    List<Product> getAllProductByQtyDESC(String by, Integer storeId, Integer pageSize, Integer pageNumber);

    Integer getAllProduct(Integer storeId);

    boolean checkProductPublish(Integer storeId, Integer id);

    Integer getProductIdInStoreProduct(Integer storeId, Integer id);

    boolean checkStoreHasAnyProduct(Integer storeId);

    boolean checkForProductInOrder(Integer currentUserId, Integer productId);


}