package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.*;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.store.Store;
import com.henheang.stock_flow_commerce.model.store.StoreRequest;
import com.henheang.stock_flow_commerce.repository.AppUserRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.DistributorStoreService;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

@Service
public class DistributorStoreServiceImplV1 implements DistributorStoreService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;
    private final StoreRepository storeRepository;
    private final AppUserRepository appUserRepository;

    public DistributorStoreServiceImplV1(StoreRepository storeRepository, AppUserRepository appUserRepository) {
        this.storeRepository = storeRepository;
        this.appUserRepository = appUserRepository;
    }

    public Boolean checkStoreIfCreated(Integer currentUserId) {
        int check = 2;
        check = storeRepository.checkStoreIfCreated(currentUserId);
        return check == 1;
    }

    Boolean checkDuplicateStoreName(String name) {
        return storeRepository.checkDuplicateStoreName(name);
    }

    @Override
    public Store createNewStore(StoreRequest storeRequest, Integer currentUserId) throws ParseException {
        if (storeRequest.getName() == null || storeRequest.getDescription() == null || storeRequest.getBannerImage() == null) {
            throw new BadRequestException("One of the fields inside the StoreRequest object is null.");
        }
        if (storeRequest.getPrimaryPhone().isEmpty() || storeRequest.getPrimaryPhone().isBlank()) {
            throw new BadRequestException("Primary phone number is null. Must input primary phone number.");
        }
        if (storeRequest.getAddress() == null) {
            throw new BadRequestException("Address is required. Please input address");
        }
        // check if store is already created
        if (checkStoreIfCreated(currentUserId)) {
            throw new ConflictException("Store already created.");
        }
        // check store name duplicate
        if (checkDuplicateStoreName(storeRequest.getName().trim())) {
            throw new ConflictException("Store name is taken.");
        }

        // prevent blank
        if (storeRequest.getName().isBlank() || storeRequest.getDescription().isBlank()) {
            throw new BadRequestException("Request payload invalid. Payload can not be empty or blank.");
        }

        // Get store to check if account is validated
        AppUser appUser = appUserRepository.findDistributorUserById(currentUserId);
        int words = storeRequest.getDescription().split(" ").length;
        if (words > 100) {
            throw new BadRequestException("Description word count can not exceed 100.");
        }
        Store store;
        // check phone number
        if (!isValid(storeRequest.getPrimaryPhone())) {
            throw new BadRequestException("Incorrect primary phone number format. Phone number should start with '0', 8 or 9 index, and can not be '0' on second index. Example: 0XXXXXXXX(X).");
        }
        for (String phone : storeRequest.getAdditionalPhone()){
            if (!isValid(phone) && !phone.isBlank()){
                throw new BadRequestException("Incorrect additional phone number format. Phone number should start with '0', 8 or 9 index, and can not be '0' on second index. Example: 0XXXXXXXX(X).");
            }
        }
        // trim space before and after
        storeRequest.setName(storeRequest.getName().trim());
        if (appUser.getIsVerified()) {
            // get insert store. return with rating
            store = storeRepository.createNewStore(storeRequest, currentUserId);
            for (String phone : storeRequest.getAdditionalPhone()) {
                if (phone.isBlank()){
                    continue;
                }
                storeRepository.addAdditionalPhone(phone, store.getId());
            }
            if (store == null) {
                throw new InternalServerErrorException("Insert failed.");
            }
        } else {
            throw new UnauthorizedException("User is not verified. Unable to create store. Please verify email/account.");
        }
        Store newStore = storeRepository.getUserStore(currentUserId);
        newStore.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
        newStore.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
        return newStore;
    }

    @Override
    public Store getUserStore(Integer currentUserId) throws ParseException {
        // get store for current user
        Store store = storeRepository.getUserStore(currentUserId);
        // if not exist throw exception
        if (store == null) {
            throw new NotFoundException("Store not found.");
        }
        store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
        store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
        return store;
    }

    @Override
    public Store editAllFieldUserStore(Integer currentUserId, StoreRequest storeRequest) throws ParseException {
        if (storeRequest.getName() == null || storeRequest.getDescription() == null || storeRequest.getBannerImage() == null || storeRequest.getIsPublish() == null) {
            throw new BadRequestException("One of the fields inside the StoreRequest object is null.");
        }
        int words = storeRequest.getDescription().split(" ").length;
        if (words > 100) {
            throw new BadRequestException("Description word count can not exceed 100.");
        }

        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        storeRequest.setPrimaryPhone(storeRequest.getPrimaryPhone().trim());
        if (!isValid(storeRequest.getPrimaryPhone())) {
            throw new BadRequestException("Incorrect primary phone number format. Phone number should start with '0', 8 or 9 index, and can not be '0' on second index. Example: 0XXXXXXXX(X).");
        }
        for (String phone : storeRequest.getAdditionalPhone()){
            if (!isValid(phone) && !phone.isBlank()){
                throw new BadRequestException("Incorrect additional phone number format. Phone number should start with '0', 8 or 9 index, and can not be '0' on second index. Example: 0XXXXXXXX(X).");
            }
        }
        // check if store exist
        if (!checkStoreIfCreated(currentUserId)) {
            throw new ConflictException("Store not found. Please setup your store.");
        }
        // check if blank throw exception
        if (storeRequest.getName().isBlank()
                || storeRequest.getName().isEmpty()
                || storeRequest.getDescription().isEmpty()
                || storeRequest.getDescription().isBlank()
                || storeRequest.getBannerImage().isBlank()
                || storeRequest.getBannerImage().isEmpty()) {
            throw new BadRequestException("Payload can not be empty or blank.");
        }
        // update and check if return null throw exception
        Store store = storeRepository.editAllFieldUserStore(storeId, storeRequest);
        // delete additional phone
        storeRepository.deleteAdditionalPhone(storeId);
        for (String phone : storeRequest.getAdditionalPhone()) {
            if (phone.isBlank()){
                continue;
            }
            storeRepository.addAdditionalPhone(phone, store.getId());
        }
        if (store == null) {
            throw new InternalServerErrorException("Update failed.");
        }
        store.setCreatedDate(formatter.format(formatter.parse(store.getCreatedDate())));
        store.setUpdatedDate(formatter.format(formatter.parse(store.getUpdatedDate())));
        return store;
    }

    @Override
    public String deleteUserStore(Integer currentUserId) {
        // check if store exist for this user
        if (!checkStoreIfCreated(currentUserId)) {
            throw new ConflictException("Store not found.");
        }
        String store = storeRepository.deleteUserStore(currentUserId);
        if (store == null) {
            throw new InternalServerErrorException("Something went wrong while deleting");
        }
        return "Store was deleted from account permanently. All data will be deleted along side the store.";
    }

    @Override
    public String disableStore(Integer currentUserId) {
        if (!checkStoreIfCreated(currentUserId)) {
            throw new ConflictException("Store not found.");
        }
        // check if store already disable
        if (!storeRepository.checkIfStoreIsDisable(currentUserId)) {
            throw new ConflictException("Store is already disabled.");
        }
        String store = storeRepository.disableStore(currentUserId);
        if (store == null) {
            throw new InternalServerErrorException("Something went wrong while deleting");
        }
        return "Store disabled.";
    }

    @Override
    public String enableStore(Integer currentUserId) {
        if (!checkStoreIfCreated(currentUserId)) {
            throw new ConflictException("Store not found.");
        }
        // check if store already enabled
        if (storeRepository.checkIfStoreIsDisable(currentUserId)) {
            throw new ConflictException("Store is already enabled.");
        }
        String store = storeRepository.enableStore(currentUserId);
        if (store == null) {
            throw new InternalServerErrorException("Something went wrong while deleting");
        }
        return "Store enabled.";
    }

    private static boolean containsLetter(String phone) {
        for (char c : phone.toCharArray()) {
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(String input) {
        Pattern pattern = Pattern.compile("^0[1-9][0-9]{7,8}$");
        return pattern.matcher(input).matches();
    }
}
