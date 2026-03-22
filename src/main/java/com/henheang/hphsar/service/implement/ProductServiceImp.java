package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.NoContentException;
import com.henheang.hphsar.exception.AlreadyExistException;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductRequest;
import com.henheang.hphsar.repository.ProductRepository;
import com.henheang.hphsar.service.ProductService;
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
