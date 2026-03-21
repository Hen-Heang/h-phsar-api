package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.product.ProductRequest;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProductRepository {
     @Select("""
            SELECT *FROM tb_product
            WHERE  name ILIKE '${name}';
             """)
    Product getProductValueExist(String name);

     @Select("""
            INSERT INTO tb_product(name)
            VALUES(#{product.name})
            RETURNING *
             """)
     
     @Results(id = "MapProduct", value = {
             @Result(property = "createdDate",column = "created_date"),
             @Result(property = "updatedDate",column = "updated_date") ,
             @Result(property = "isActive", column = "is_active")
     })
    Product addNewProduct(@Param("product") ProductRequest productRequest);

     
}
