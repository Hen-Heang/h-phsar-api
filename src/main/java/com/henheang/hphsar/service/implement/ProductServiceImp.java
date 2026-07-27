package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.common.ExceptionMessages;
import com.henheang.hphsar.exception.AlreadyExistException;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductRequest;
import com.henheang.hphsar.repository.ProductRepository;
import com.henheang.hphsar.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImp implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product addNewProduct(ProductRequest productRequest)  {
        if (productRequest.getName().equals("string") || productRequest.getName().isBlank()){
            throw new BadRequestException(ExceptionMessages.CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_VALUE);
        }
        Product checkForDuplicate = productRepository.getProductValueExist(productRequest.getName());
        if (checkForDuplicate!=null){
            throw new AlreadyExistException("This product is already exist ");
        }
        return productRepository.addNewProduct(productRequest);
    }
}
