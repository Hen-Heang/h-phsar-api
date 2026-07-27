package com.henheang.hphsar.service.implement;
import com.henheang.hphsar.utils.ValidationUtils;
import com.henheang.hphsar.common.ExceptionMessages;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.product.Product;
import com.henheang.hphsar.model.product.ProductEditRequest;
import com.henheang.hphsar.model.product.ProductImport;
import com.henheang.hphsar.model.product.ProductRequest;
import com.henheang.hphsar.repository.CategoryRepository;
import com.henheang.hphsar.repository.SupplierProductRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.SupplierProductService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import com.henheang.hphsar.utils.DateTimeUtil;
import com.henheang.hphsar.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.henheang.hphsar.service.implement.BuyerStoreServiceImpl.getProducts;

@Service
@RequiredArgsConstructor
public class SupplierProductServiceImpl implements SupplierProductService {

    private final SupplierProductRepository supplierProductRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

//    @Override
//    public List<Product> insertNewProduct(Integer currentUserId, ArrayList<ProductRequest> productRequests) throws ParseException {
//        List<Product> productResponse = new ArrayList<Product>();
//        for (ProductRequest productRequest : productRequests) {
//            System.out.println(productRequest);
//            //         insert into tb_product and return product id
//            List<Integer> productIds = supplierProductRepository.insertNewProduct(productRequest);
//
//            for (Integer productId : productIds) {
//                //get store id by supplier info id
//                Integer storeId = supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
//                System.out.println(storeId);
//
//                // insert into tb_store_product_detail
//                supplierProductRepository.insertNewProductDetail(productId, storeId, productRequest);
//
//                Integer categoryId = productRequest.getCategoryId();
//
//                String categoryName = productRequest.getCategoryName();
//                Integer categoryIdFromName = supplierProductRepository.getCategoryIdByName(categoryName);
//
//                //insert in to tb_product_category
//                supplierProductRepository.insertNewProductCategory(productId, categoryIdFromName);
//
//                //insert into tb_product_import and return product import id
//                Integer productImportId = supplierProductRepository.insertNewProductImport(storeId);
//                System.out.println(productImportId);
//
//                //insert into tb_product_import_detail
//                supplierProductRepository.insertNewProductImportDetail(productId, productImportId, productRequest);
//
//                //update tb_store_product_detail (qty and price)
//                supplierProductRepository.updateStoreProductDetail(storeId, productId, productRequest);
//
//                Product product = supplierProductRepository.getProductById(productId);
//                product.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCreatedDate())));
//                product.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getUpdatedDate())));
//                assert false;
//                productResponse.add(product);
//            }
//        }
//        return productResponse;
//    }


    @Override
    public List<Product> insertNewProduct(Integer currentUserId, ArrayList<ProductRequest> productRequests) throws ParseException {
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // get precision limit to (5, 2)
        for (ProductRequest productRequest : productRequests) {
            ValidationUtils.rejectIfExceedsIntLimit(productRequest.getCategoryId());
            if (productRequest.getPrice() > 1000) {
                throw new BadRequestException("Maximum price is 999.");
            }
            // check input
            if (productRequest.getName().isBlank() || productRequest.getCategoryId() <= 0) {
                throw new BadRequestException("Invalid input request on product: " + productRequest.getName());
            }
            // initial stock can be 0 (unpublished, no import) but never negative
            if (productRequest.getQty() == null || productRequest.getQty() < 0) {
                throw new BadRequestException("Quantity can not be negative. Error on product: " + productRequest.getName());
            }
            // check if store already have this product
            Integer productId = supplierProductRepository.getProductIdByName(productRequest.getName().trim()); // this can be better, but I don't want to change old code.
            System.out.println(storeId+" " + productId);
            if (supplierProductRepository.checkStoreHasProduct(storeId, productId)) {
                throw new ConflictException("Store already have product: " + productRequest.getName() + ". If you encounter error while inserting this product previously, please remove this product from your insert table.");
            }
            // category need to exist in store category
            if (!categoryRepository.checkIfStoreCategoryDuplicate(storeId, productRequest.getCategoryId())) {
                throw new NotFoundException("This category id does not exist in your store. Please insert before use. Error on product: " + productRequest.getName());
            }
        }
        Integer importId = supplierProductRepository.createNewImportRecord(storeId);
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
            Integer productId = supplierProductRepository.getProductIdByName(productRequest.getName().trim());
            if (productId == 0) { // 0 mean not exist
                productId = supplierProductRepository.createNewProduct(productRequest.getName());
            }
            if (supplierProductRepository.checkStoreHasProduct(storeId, productId)) { // I already handle it, but I use the same error handling here just in case Internal server error, we can skip the previous input
                continue outer_loop; // prevent input the same product after error. this code will probably never run, but just in case >.0
            }
            // insert
            String storeProductDetailId = supplierProductRepository.insertNewProduct(storeId, productId, productRequest);
            if (storeProductDetailId == null) {
                throw new InternalServerErrorException("Fail to insert new product.");
            }
            // if import true import product and update amount
            if (isAlsoImport) {
                System.out.println(productRequest.getQty());
                // insert import detail
                supplierProductRepository.insertImportDetail(importId, productId, productRequest.getQty(), productRequest.getPrice(), productRequest.getCategoryId());
                // update store product qty and update price according to the last import
                supplierProductRepository.updateStoreProductDetail(storeId, Integer.parseInt(storeProductDetailId), productRequest.getQty(), productRequest.getPrice(), productRequest.getIsPublish());
            } else { // insert just price
                supplierProductRepository.updateStoreProductDetail(storeId, Integer.parseInt(storeProductDetailId), productRequest.getQty(), productRequest.getPrice(), productRequest.getIsPublish());
            }
            // insert to list
            Product thisProduct = supplierProductRepository.getStoreProductByStoreProductId(Integer.parseInt(storeProductDetailId));
            // convert date
            thisProduct.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getCreatedDate())));
            thisProduct.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getUpdatedDate())));
            thisProduct.getCategory().setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getCategory().getCreatedDate())));
            thisProduct.getCategory().setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getCategory().getUpdatedDate())));
            productResponse.add(thisProduct);
        }
        return productResponse;
    }

    @Override
    public Product getProductById(Integer id) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        Integer storeId = supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product in store
        if (!supplierProductRepository.checkStoreHasProduct(storeId, id)) {
            throw new NotFoundException("Product not found.");
        }
        Product product = supplierProductRepository.getProductById(storeId, id);
        if (product == null) {
            throw new InternalServerErrorException("Fail to fetch product.");
        }
        product.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCreatedDate())));
        product.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getUpdatedDate())));
        product.getCategory().setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCategory().getCreatedDate())));
        product.getCategory().setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCategory().getUpdatedDate())));
        return product;
    }

    @Override
    public String deleteProductById(Integer productId) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        // check if product is in order
        if  (supplierProductRepository.checkForProductInOrder(currentUserId, productId)){
            throw new ConflictException("Can not delete product because product object is being used.");
        }
        //get store id by supplier info id
        Integer storeId = supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
        if (supplierProductRepository.deleteProductDetailById(productId, storeId) == null) {
            throw new BadRequestException("Product not found.");
        }
        return "Product " + productId + " deleted successfully.";
    }

//    @Override
//    public List<Product> getAllProductByQty(Integer currentUserId) {
//
//        //get store id by currentUserId
//        Integer storeId = supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
////        List<Product> products = supplierProductRepository.getAllProductByQty(storeId);
//
//
//        return null;
//    }

    @Override
    public List<Product> getAllProductByName(Integer currentUserId, String name) throws ParseException {
        Integer storeId = supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product exist
        if (!supplierProductRepository.checkStoreHasAnyProduct(storeId)){
            throw new NotFoundException("Product not found. You don't have any product.");
        }
        List<Product> products = supplierProductRepository.getAllProductByName(name, storeId);
        if (products.isEmpty()){
            throw new NotFoundException("Can not find product: "+ name);
        }
        for (Product product : products){
            product.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getCreatedDate())));
            product.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(product.getUpdatedDate())));
        }
        System.out.println(storeId);
        return products;
    }

//    @Override
//    public List<Product> getAllProductByUnitPrice(Integer currentUserId) {
//
//        Integer storeId = supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
//
//        List<Product> products = supplierProductRepository.getAllProductByUnitPrice(storeId);
//
//        return products;
//    }

//    @Override
//    public List<Product> importProduct(Integer currentUserId) {
//
////        Integer storeId= supplierProductRepository.getStoreIdByCurrentUserId(currentUserId);
////
////        supplierProductRepository.getProductImportId(storeId);
////
//        return null;
//    }

    @Override
    public Product editProduct(Integer id, ProductEditRequest productRequest) throws ParseException {
        ValidationUtils.rejectIfExceedsIntLimit(productRequest.getCategoryId());
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // check create store yet
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        if (!supplierProductRepository.checkStoreHasProduct(storeId, id)){
            throw new NotFoundException("This product id is not exist in your store.");
        }
        if (productRequest.getName().isBlank() || productRequest.getName().isEmpty() || productRequest.getCategoryId() == 0) {
            throw new BadRequestException("Invalid request Name or categoryId on product: " + productRequest.getName());
        }

        productRequest.setName(productRequest.getName().trim());
        productRequest.setIsPublish(true);
        Integer productId = supplierProductRepository.getProductIdByName(productRequest.getName());

        // check if product name does not exist, create new one
        if (productId == 0) {
            productId = supplierProductRepository.createNewProduct(productRequest.getName());
        }
        // check category in store or not
        if (!categoryRepository.checkIfStoreCategoryDuplicate(storeId, productRequest.getCategoryId())) {
            throw new NotFoundException("Category not found. Please create category before insert.");
        }
        if (productRequest.getPrice() == 0){
            throw new BadRequestException("Could not set price to 0. Please try again.");
        }
        // update
        String storeProductDetailId = supplierProductRepository.changeStoreProductDetail(storeId, productRequest, productId, id, productRequest.getIsPublish());
        if (storeProductDetailId == null) {
            throw new InternalServerErrorException("Update product failed.");
        }
        Product thisProduct = supplierProductRepository.getStoreProductByStoreProductId(Integer.parseInt(storeProductDetailId));
        thisProduct.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getCreatedDate())));
        thisProduct.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getUpdatedDate())));
        thisProduct.getCategory().setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getCategory().getCreatedDate())));
        thisProduct.getCategory().setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(thisProduct.getCategory().getUpdatedDate())));
        return thisProduct;
    }
//
    @Override
    public String unPublishProduct(Integer id) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product is in store
        if (!supplierProductRepository.checkStoreHasProduct(storeId, id)) {
            throw new NotFoundException(ExceptionMessages.THIS_PRODUCT_IS_NOT_FOUND_IN_THIS_STORE);
        }
        // check if already un-list
        if (!supplierProductRepository.checkProductPublish(storeId, id)){
            throw new ConflictException("Product already disabled.");
        }
        // un list
        Integer storeProductId = supplierProductRepository.unPublishProduct(storeId, id);
        if (!Objects.equals(storeProductId, id)) {
            throw new InternalServerErrorException("Unpublished wrong product.");
        }
        return "Product can no longer be viewed from store by customer.";
    }

    @Override
    public String publishProduct(Integer id) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // check if product is in store
        if (!supplierProductRepository.checkStoreHasProduct(storeId, id)) {
            throw new NotFoundException(ExceptionMessages.THIS_PRODUCT_IS_NOT_FOUND_IN_THIS_STORE);
        }
        // check if already un-list
        if (supplierProductRepository.checkProductPublish(storeId, id)){
            throw new ConflictException("Product already enabled.");
        }
        // un list
        Integer storeProductId = supplierProductRepository.publishProduct(storeId, id);
        if (!Objects.equals(storeProductId, id)) {
            throw new InternalServerErrorException("Published wrong product.");
        }
        return "Product can be viewed from store by customer.";
    }

    @Override
    public List<Product> getAllProductBySorting(String sort, String by, Integer pageNumber, Integer pageSize) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) { // this is old code so it does not output boolean but 1 if true
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        by = by.toLowerCase().trim();
        if (!(by.equals("created_date") || by.equals("qty") || by.equals("name") || by.equals("price") || by.equals("product_id"))){
            throw new BadRequestException("Invalid input. Available sorting are 'created_date' - 'qty' - 'name' - 'price' - 'product_id'. Case sensitive not needed.");
        }
        // created_date exists in both joined tables — qualify with the product table alias to avoid ambiguity
        if (by.equals("created_date")) by = "b.created_date";
        // get all product
        List<Product> products;
        if (Objects.equals(sort.toLowerCase().trim(), "asc")) {
            products = supplierProductRepository.getAllProductByQtyASC(by, storeId, pageSize, pageNumber);
        } else if (Objects.equals(sort.toLowerCase().trim(), "desc")) {
            products = supplierProductRepository.getAllProductByQtyDESC(by, storeId, pageSize, pageNumber);
        } else {
            throw new BadRequestException("Sort can only be 'ASC' or 'DESC'. Case sensitive not needed.");
        }
        if (products.isEmpty()) {
            return new ArrayList<>();
        }
        return getProducts(products);
    }

    @Override
    public Integer getTotalPage(Integer pageSize) {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        Integer totalProduct = supplierProductRepository.getAllProduct(storeId);
        return PaginationUtils.totalPages(totalProduct, pageSize);
    }

    @Override
    public Integer getTotalElements() {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) {
            return 0;
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        return supplierProductRepository.getAllProduct(storeId);
    }

    @Override
    public List<Product> importProduct(List<ProductImport> productsImport) throws ParseException {
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (storeRepository.checkStoreIfCreated(currentUserId) != 1) {
            throw new NotFoundException(ExceptionMessages.USER_HAVE_NOT_CREATED_STORE);
        }
        Integer storeId = categoryRepository.getStoreIdByCurrentUserId(currentUserId);
        // create an import
        Integer importId = supplierProductRepository.createNewImportRecord(storeId);
        // track fail on index
        int count = 1;
        List<Product> products = new ArrayList<>();
        for (ProductImport productImport : productsImport){
            if (productImport.getId() > 2147483646){
                throw new BadRequestException("Integer value can not exceed 2147483646. Fail on count " + count);
            }
            // restock must be a positive increase — a zero/negative "restock" would silently
            // deduct stock through this endpoint, bypassing the order-acceptance stock checks entirely
            if (productImport.getQty() == null || productImport.getQty() <= 0) {
                throw new BadRequestException("Restock quantity must be greater than 0. Fail on count " + count);
            }
            Integer productId = supplierProductRepository.getProductIdInStoreProduct(storeId, productImport.getId());
            // check product exist in store
            if (!supplierProductRepository.checkStoreHasProduct(storeId, productImport.getId())){
                throw new NotFoundException("This product does not exist in this store. Please confirm your product id. Fail on count " + count);
            }
            Integer categoryId = categoryRepository.getCategoryIdByProductId(productImport.getId());
            // if price not provided in request, keep the existing product price
            Double price = productImport.getPrice();
            if (price == null || price == 0) {
                Product existing = supplierProductRepository.getProductById(storeId, productImport.getId());
                price = (existing != null && existing.getPrice() != null) ? existing.getPrice() : null;
                if (price == null || price == 0) {
                    throw new BadRequestException("Price can't be 0. Recommend un-list this product. Fail on count " + count);
                }
            }
            // insert product to import detail
            supplierProductRepository.insertImportDetail(importId, productId, productImport.getQty(), price, categoryId);
            // update store product
            supplierProductRepository.updateStoreProductDetail(storeId, productImport.getId(), productImport.getQty(), price, true);
            Product product = supplierProductRepository.getProductById(storeId, productImport.getId());
            products.add(product);
            count ++;
        }
        return getProducts(products);
    }


}
