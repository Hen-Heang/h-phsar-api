package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.NoContentException;
import com.henheang.stock_flow_commerce.exception.AlreadyExistException;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.product.ProductRequest;
import com.henheang.stock_flow_commerce.repository.ProductRepository;
import com.henheang.stock_flow_commerce.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImp implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImp(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product addNewProduct(ProductRequest productRequest)  {
        if (productRequest.getName().equals("string") || productRequest.getName().isBlank()){
            throw new BadRequestException("Can not use default value. Please input value! ");
        }
        Product checkForDuplicate = productRepository.getProductValueExist(productRequest.getName());
        if (checkForDuplicate!=null){
            throw new AlreadyExistException("This product is already exist ");
        }
        return productRepository.addNewProduct(productRequest);
    }
}
