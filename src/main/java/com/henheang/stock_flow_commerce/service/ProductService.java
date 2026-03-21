package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.product.ProductRequest;

import java.rmi.AlreadyBoundException;

public interface ProductService {

    Product addNewProduct(ProductRequest productRequest) throws AlreadyBoundException;
}
