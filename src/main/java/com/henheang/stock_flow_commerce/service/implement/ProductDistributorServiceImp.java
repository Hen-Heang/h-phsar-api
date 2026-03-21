package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.ConflictException;
import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.product.Product;
import com.henheang.stock_flow_commerce.model.product.ProductEditRequest;
import com.henheang.stock_flow_commerce.model.product.ProductImport;
import com.henheang.stock_flow_commerce.model.product.ProductRequest;
import com.henheang.stock_flow_commerce.repository.CategoryRepository;
import com.henheang.stock_flow_commerce.repository.ProductDistributorRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.ProductDistributorService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import javax.naming.ConfigurationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.henheang.stock_flow_commerce.service.implement.RetailerStoreServiceImplV1.getProducts;

@Service
public class ProductDistributorServiceImp implements ProductDistributorService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final ProductDistributorRepository productDistributorRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    public ProductDistributorServiceImp(ProductDistributorRepository productDIstributorRepository, StoreRepository storeRepository, CategoryRepository categoryRepository) {
        this.productDistributorRepository = productDIstributorRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
    }

//    @Override
//    public List<Product> insertNewProduct(Integer currentUserId, ArrayList<ProductRequest> productRequests) throws ParseException {
//        List<Product> productResponse = new ArrayList<Product>();
//        for (ProductRequest productRequest : productRequests) {
//            System.out.println(productRequest);
//            //         insert into tb_product and return product id
//            List<Integer> productIds = productDistributorRepository.insertNewProduct(productRequest);
//
//            for (Integer productId : productIds) {
//                //get store id by distributor info id
//                Integer storeId = productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
//                System.out.println(storeId);
//
//                // insert into tb_store_product_detail
//                productDistributorRepository.insertNewProductDetail(productId, storeId, productRequest);
//
//                Integer categoryId = productRequest.getCategoryId();
//
//                String categoryName = productRequest.getCategoryName();
//                Integer categoryIdFromName = productDistributorRepository.getCategoryIdByName(categoryName);
//
//                //insert in to tb_product_category
//                productDistributorRepository.insertNewProductCategory(productId, categoryIdFromName);
//
//                //insert into tb_product_import and return product import id
//                Integer productImportId = productDistributorRepository.insertNewProductImport(storeId);
//                System.out.println(productImportId);
//
//                //insert into tb_product_import_detail
//                productDistributorRepository.insertNewProductImportDetail(productId, productImportId, productRequest);
//
//                //update tb_store_product_detail (qty and price)
//                productDistributorRepository.updateStoreProductDetail(storeId, productId, productRequest);
//
//                Product product = productDistributorRepository.getProductById(productId);
//                product.setCreatedDate(formatter.format(formatter.parse(product.getCreatedDate())));
//                product.setUpdatedDate(formatter.format(formatter.parse(product.getUpdatedDate())));
//                assert false;
//                productResponse.add(product);
//            }
//        }
//        return productResponse;
//    }


    @Override
    public List<Product> insertNewProduct(Integer currentUserId, ArrayList<ProductRequest> productRequests) throws ParseException {
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException("No store is found. Please create store before insert product");
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // get precision limit to (5, 2)
        for (ProductRequest productRequest : productRequests) {
            if (productRequest.getCategoryId() > 2147483646){
                throw new BadRequestException("Integer value can not exceed 2147483646");
            }
            if (productRequest.getPrice() > 1000) {
                throw new BadRequestException("Maximum price is 999.");
            }
            // check input
            if (productRequest.getName().isBlank() || productRequest.getCategoryId() <= 0) {
                throw new BadRequestException("Invalid input request on product: " + productRequest.getName());
            }
            // check if store already have this product
            Integer productId = productDistributorRepository.getProductIdByName(productRequest.getName().trim()); // this can be better, but I don't want to change old code.
            System.out.println(storeId+" " + productId);
            if (productDistributorRepository.checkStoreHasProduct(storeId, productId)) {
                throw new ConflictException("Store already have product: " + productRequest.getName() + ". If you encounter error while inserting this product previously, please remove this product from your insert table.");
            }
            // category need to exist in store category
            if (!categoryRepository.checkIfStoreCategoryDuplicate(storeId, productRequest.getCategoryId())) {
                throw new NotFoundException("This category id does not exist in your store. Please insert before use. Error on product: " + productRequest.getName());
            }
        }
        Integer importId = productDistributorRepository.createNewImportRecord(storeId);
        List<Product> productResponse = new ArrayList<>();
        outer_loop:
        for (ProductRequest productRequest : productRequests) {
            boolean isAlsoImport = true; // if have qty will insert separately
            // price can not be 0. set import or not
//            if (productRequest.getPrice() == 0) {
////                throw new BadRequestException("Product can not be 0. Error on product: " + productRequest.getName());
//                productRequest.setIsPublish(false);
//            }
            // if qty is 0, default is inactive and no import
            if (productRequest.getQty() == 0) {
                isAlsoImport = false;
                productRequest.setIsPublish(false);
            }

            // if product already exist get id, if not create new then get id
            Integer productId = productDistributorRepository.getProductIdByName(productRequest.getName().trim());
            if (productId == 0) { // 0 mean not exist
                productId = productDistributorRepository.createNewProduct(productRequest.getName());
            }
            if (productDistributorRepository.checkStoreHasProduct(storeId, productId)) { // I already handle it, but I use the same error handling here just in case Internal server error, we can skip the previous input
                continue outer_loop; // prevent input the same product after error. this code will probably never run, but just in case >.0
            }
            // insert
            String storeProductDetailId = productDistributorRepository.insertNewProduct(storeId, productId, productRequest);
            if (storeProductDetailId == null) {
                throw new InternalServerErrorException("Fail to insert new product.");
            }
            // if import true import product and update amount
            if (isAlsoImport) {
                System.out.println(productRequest.getQty());
                // insert import detail
                productDistributorRepository.insertImportDetail(importId, productId, productRequest.getQty(), productRequest.getPrice(), productRequest.getCategoryId());
                // update store product qty and update price according to the last import
                productDistributorRepository.updateStoreProductDetail(storeId, Integer.parseInt(storeProductDetailId), productRequest.getQty(), productRequest.getPrice(), productRequest.getIsPublish());
            } else { // insert just price
                productDistributorRepository.updateStoreProductDetail(storeId, Integer.parseInt(storeProductDetailId), productRequest.getQty(), productRequest.getPrice(), productRequest.getIsPublish());
            }
            // insert to list
            Product thisProduct = productDistributorRepository.getStoreProductByStoreProductId(Integer.parseInt(storeProductDetailId));
            // convert date
            thisProduct.setCreatedDate(formatter.format(formatter.parse(thisProduct.getCreatedDate())));
            thisProduct.setUpdatedDate(formatter.format(formatter.parse(thisProduct.getUpdatedDate())));
            thisProduct.getCategory().setCreatedDate(formatter.format(formatter.parse(thisProduct.getCategory().getCreatedDate())));
            thisProduct.getCategory().setUpdatedDate(formatter.format(formatter.parse(thisProduct.getCategory().getUpdatedDate())));
            productResponse.add(thisProduct);
        }
        return productResponse;
    }

    @Override
    public Product getProductById(Integer id) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product in store
        if (!productDistributorRepository.checkStoreHasProduct(storeId, id)) {
            throw new NotFoundException("Product not found.");
        }
        Product product = productDistributorRepository.getProductById(storeId, id);
        if (product == null) {
            throw new InternalServerErrorException("Fail to fetch product.");
        }
        product.setCreatedDate(formatter.format(formatter.parse(product.getCreatedDate())));
        product.setUpdatedDate(formatter.format(formatter.parse(product.getUpdatedDate())));
        product.getCategory().setCreatedDate(formatter.format(formatter.parse(product.getCategory().getCreatedDate())));
        product.getCategory().setUpdatedDate(formatter.format(formatter.parse(product.getCategory().getUpdatedDate())));
        return product;
    }

    @Override
    public String deleteProductById(Integer productId) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        // check if product is in order
        if  (productDistributorRepository.checkForProductInOrder(currentUserId, productId)){
            throw new ConflictException("Can not delete product because product object is being used.");
        }
        //get store id by distributor info id
        Integer storeId = productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
        if (productDistributorRepository.deleteProductDetailById(productId, storeId) == null) {
            throw new BadRequestException("Product not found.");
        }
        return "Product " + productId + " deleted successfully.";
    }

//    @Override
//    public List<Product> getAllProductByQty(Integer currentUserId) {
//
//        //get store id by currentUserId
//        Integer storeId = productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
////        List<Product> products = productDistributorRepository.getAllProductByQty(storeId);
//
//
//        return null;
//    }

    @Override
    public List<Product> getAllProductByName(Integer currentUserId, String name) throws ParseException {
        Integer storeId = productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product exist
        if (!productDistributorRepository.checkStoreHasAnyProduct(storeId)){
            throw new NotFoundException("Product not found. You don't have any product.");
        }
        List<Product> products = productDistributorRepository.getAllProductByName(name, storeId);
        if (products.isEmpty()){
            throw new NotFoundException("Can not find product: "+ name);
        }
        for (Product product : products){
            product.setCreatedDate(formatter.format(formatter.parse(product.getCreatedDate())));
            product.setUpdatedDate(formatter.format(formatter.parse(product.getUpdatedDate())));
        }
        System.out.println(storeId);
        return products;
    }

//    @Override
//    public List<Product> getAllProductByUnitPrice(Integer currentUserId) {
//
//        Integer storeId = productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
//
//        List<Product> products = productDistributorRepository.getAllProductByUnitPrice(storeId);
//
//        return products;
//    }

//    @Override
//    public List<Product> importProduct(Integer currentUserId) {
//
////        Integer storeId= productDistributorRepository.getStoreIdByCurrentUserId(currentUserId);
////
////        productDistributorRepository.getProductImportId(storeId);
////
//        return null;
//    }

    @Override
    public Product editProduct(Integer id, ProductEditRequest productRequest) throws ParseException {
        if (productRequest.getCategoryId() > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // check create store yet
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException("No store is found. Please create store.");
        }
        if (!productDistributorRepository.checkStoreHasProduct(storeId, id)){
            throw new NotFoundException("This product id is not exist in your store.");
        }
        if (productRequest.getName().isBlank() || productRequest.getName().isEmpty() || productRequest.getCategoryId() == 0) {
            throw new BadRequestException("Invalid request Name or categoryId on product: " + productRequest.getName());
        }

        productRequest.setName(productRequest.getName().trim());
        productRequest.setIsPublish(true);
        Integer productId = productDistributorRepository.getProductIdByName(productRequest.getName());

        // check if product name does not exist, create new one
        if (productId == 0) {
            productId = productDistributorRepository.createNewProduct(productRequest.getName());
        }
        // check category in store or not
        if (!categoryRepository.checkIfStoreCategoryDuplicate(storeId, productRequest.getCategoryId())) {
            throw new NotFoundException("Category not found. Please create category before insert.");
        }
        if (productRequest.getPrice() == 0){
            throw new BadRequestException("Could not set price to 0. Please try again.");
        }
        // update
        String storeProductDetailId = productDistributorRepository.changeStoreProductDetail(storeId, productRequest, productId, id, productRequest.getIsPublish());
        if (storeProductDetailId == null) {
            throw new InternalServerErrorException("Update product failed.");
        }
        Product thisProduct = productDistributorRepository.getStoreProductByStoreProductId(Integer.parseInt(storeProductDetailId));
        thisProduct.setCreatedDate(formatter.format(formatter.parse(thisProduct.getCreatedDate())));
        thisProduct.setUpdatedDate(formatter.format(formatter.parse(thisProduct.getUpdatedDate())));
        thisProduct.getCategory().setCreatedDate(formatter.format(formatter.parse(thisProduct.getCategory().getCreatedDate())));
        thisProduct.getCategory().setUpdatedDate(formatter.format(formatter.parse(thisProduct.getCategory().getUpdatedDate())));
        return thisProduct;
    }
//
    @Override
    public String unPublishProduct(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException("No store is found. Please create store.");
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product is in store
        if (!productDistributorRepository.checkStoreHasProduct(storeId, id)) {
            throw new NotFoundException("This product is not found in this store.");
        }
        // check if already un-list
        if (!productDistributorRepository.checkProductPublish(storeId, id)){
            throw new ConflictException("Product already disabled.");
        }
        // un list
        Integer storeProductId = productDistributorRepository.unPublishProduct(storeId, id);
        if (!Objects.equals(storeProductId, id)) {
            throw new InternalServerErrorException("Unpublished wrong product.");
        }
        return "Product can no longer be viewed from store by customer.";
    }

    @Override
    public String publishProduct(Integer id) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException("No store is found. Please create store.");
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product is in store
        if (!productDistributorRepository.checkStoreHasProduct(storeId, id)) {
            throw new NotFoundException("This product is not found in this store.");
        }
        // check if already un-list
        if (productDistributorRepository.checkProductPublish(storeId, id)){
            throw new ConflictException("Product already enabled.");
        }
        // un list
        Integer storeProductId = productDistributorRepository.publishProduct(storeId, id);
        if (!Objects.equals(storeProductId, id)) {
            throw new InternalServerErrorException("Published wrong product.");
        }
        return "Product can be viewed from store by customer.";
    }

    @Override
    public List<Product> getAllProductBySorting(String sort, String by, Integer pageNumber, Integer pageSize) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException("No store is found. Please create store.");
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        by = by.toLowerCase().trim();
        if (!(by.equals("createddate") || by.equals("qty") || by.equals("name") || by.equals("price") || by.equals("product_id"))){
            throw new BadRequestException("Invalid input. Available sorting are 'createdDate' - 'qty' - 'name' - 'price' - 'product_id'. Case sensitive not needed.");
        }
        // get all product
        List<Product> products;
        if (Objects.equals(sort.toLowerCase().trim(), "asc")) {
            products = productDistributorRepository.getAllProductByQtyASC(by, storeId, pageSize, pageNumber);
        } else if (Objects.equals(sort.toLowerCase().trim(), "desc")) {
            products = productDistributorRepository.getAllProductByQtyDESC(by, storeId, pageSize, pageNumber);
        } else {
            throw new BadRequestException("Sort can only be 'ASC' or 'DESC'. Case sensitive not needed.");
        }
        if (products.isEmpty()) {
            throw new BadRequestException("Product not found.");
        }
        return getProducts(products, formatter);
    }

    @Override
    public Integer getTotalPage(Integer pageSize) {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) {
            throw new NotFoundException("User have not created store.");
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        Integer totalProduct = productDistributorRepository.getAllProduct(storeId);
        int totalPage;
        if (totalProduct % pageSize == 0) {
            totalPage = totalProduct / pageSize;
        } else {
            totalPage = (totalProduct / pageSize) + 1;
        }
        return totalPage;
    }

    @Override
    public List<Product> importProduct(List<ProductImport> productsImport) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) {
            throw new NotFoundException("User have not created store.");
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // create an import
        Integer importId = productDistributorRepository.createNewImportRecord(storeId);
        // track fail on index
        int count = 1;
        List<Product> products = new ArrayList<>();
        for (ProductImport productImport : productsImport){
            if (productImport.getId() > 2147483646){
                throw new BadRequestException("Integer value can not exceed 2147483646. Fail on count " + count);
            }
            Integer productId = productDistributorRepository.getProductIdInStoreProduct(storeId, productImport.getId());
            // check product exist in store
            if (!productDistributorRepository.checkStoreHasProduct(storeId, productImport.getId())){
                throw new NotFoundException("This product does not exist in this store. Please confirm your product id. Fail on count " + count);
            }
            Integer categoryId = categoryRepository.getCategoryIdByProductId(productImport.getId());
            // price can't be 0
            if (productImport.getPrice() == 0){
                throw new BadRequestException("Price can't be 0. Recommend un-list this product. Fail on count " + count);
            }
            // insert product to import detail
            productDistributorRepository.insertImportDetail(importId, productId, productImport.getQty(), productImport.getPrice(), categoryId);
            // update store product
            productDistributorRepository.updateStoreProductDetail(storeId, productImport.getId(), productImport.getQty(), productImport.getPrice(), true);
            Product product = productDistributorRepository.getProductById(storeId, productImport.getId());
            products.add(product);
            count ++;
        }
        return getProducts(products, formatter);
    }


}
