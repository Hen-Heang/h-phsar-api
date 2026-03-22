package com.henheang.hphsar.service;

import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductRequest;

import java.rmi.AlreadyBoundException;

public interface ProductService {

    Product addNewProduct(ProductRequest productRequest) throws AlreadyBoundException;
}
