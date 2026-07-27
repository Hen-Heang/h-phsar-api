package com.henheang.hphsar.service.implement;
import com.henheang.hphsar.common.ExceptionMessages;

import com.henheang.hphsar.exception.*;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.store.Store;
import com.henheang.hphsar.model.store.StoreRequest;
import com.henheang.hphsar.repository.AppUserRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.SupplierStoreService;
import com.henheang.hphsar.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SupplierStoreServiceImpl implements SupplierStoreService {
    Date date;
    private final StoreRepository storeRepository;
    private final AppUserRepository appUserRepository;

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
            throw new BadRequestException(ExceptionMessages.ONE_OF_THE_FIELDS_INSIDE_THE_STOREREQUEST_OBJECT);
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
        AppUser appUser = appUserRepository.findSupplierUserById(currentUserId);
        int words = storeRequest.getDescription().split(" ").length;
        if (words > 100) {
            throw new BadRequestException(ExceptionMessages.DESCRIPTION_WORD_COUNT_CAN_NOT_EXCEED_100);
        }
        Store store;
        // check phone number
        if (!isValid(storeRequest.getPrimaryPhone())) {
            throw new BadRequestException(ExceptionMessages.PRIMARY_PHONE_FORMAT_INVALID);
        }
        for (String phone : storeRequest.getAdditionalPhone()){
            if (!isValid(phone) && !phone.isBlank()){
                throw new BadRequestException(ExceptionMessages.ADDITIONAL_PHONE_FORMAT_INVALID);
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
            throw new ForbiddenException("User is not verified. Unable to create store. Please verify email/account.");
        }
        Store newStore = storeRepository.getUserStore(currentUserId);
        newStore.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
        newStore.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
        return newStore;
    }

    @Override
    public Store getUserStore(Integer currentUserId) throws ParseException {
        // get store for current user
        Store store = storeRepository.getUserStore(currentUserId);
        // if not exist throw exception
        if (store == null) {
            throw new NotFoundException(ExceptionMessages.STORE_NOT_FOUND);
        }
        store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
        store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
        return store;
    }

    @Override
    public Store editAllFieldUserStore(Integer currentUserId, StoreRequest storeRequest) throws ParseException {
        if (storeRequest.getName() == null || storeRequest.getDescription() == null || storeRequest.getBannerImage() == null || storeRequest.getIsPublish() == null) {
            throw new BadRequestException(ExceptionMessages.ONE_OF_THE_FIELDS_INSIDE_THE_STOREREQUEST_OBJECT);
        }
        int words = storeRequest.getDescription().split(" ").length;
        if (words > 100) {
            throw new BadRequestException(ExceptionMessages.DESCRIPTION_WORD_COUNT_CAN_NOT_EXCEED_100);
        }

        Integer storeId = storeRepository.getStoreIdByUserId(currentUserId);
        storeRequest.setPrimaryPhone(storeRequest.getPrimaryPhone().trim());
        if (!isValid(storeRequest.getPrimaryPhone())) {
            throw new BadRequestException(ExceptionMessages.PRIMARY_PHONE_FORMAT_INVALID);
        }
        for (String phone : storeRequest.getAdditionalPhone()){
            if (!isValid(phone) && !phone.isBlank()){
                throw new BadRequestException(ExceptionMessages.ADDITIONAL_PHONE_FORMAT_INVALID);
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
        store.setCreatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getCreatedDate())));
        store.setUpdatedDate(DateTimeUtil.format(DateTimeUtil.parse(store.getUpdatedDate())));
        return store;
    }

    @Override
    public String deleteUserStore(Integer currentUserId) {
        // check if store exist for this user
        if (!checkStoreIfCreated(currentUserId)) {
            throw new NotFoundException(ExceptionMessages.STORE_NOT_FOUND);
        }
        String store = storeRepository.deleteUserStore(currentUserId);
        if (store == null) {
            throw new InternalServerErrorException(ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DELETING);
        }
        return "Store was deleted from account permanently. All data will be deleted along side the store.";
    }

    @Override
    public String disableStore(Integer currentUserId) {
        if (!checkStoreIfCreated(currentUserId)) {
            throw new NotFoundException(ExceptionMessages.STORE_NOT_FOUND);
        }
        // check if store already disable
        if (!storeRepository.checkIfStoreIsDisable(currentUserId)) {
            throw new ConflictException("Store is already disabled.");
        }
        String store = storeRepository.disableStore(currentUserId);
        if (store == null) {
            throw new InternalServerErrorException(ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DELETING);
        }
        return "Store disabled.";
    }

    @Override
    public String enableStore(Integer currentUserId) {
        if (!checkStoreIfCreated(currentUserId)) {
            throw new NotFoundException(ExceptionMessages.STORE_NOT_FOUND);
        }
        // check if store already enabled
        if (storeRepository.checkIfStoreIsDisable(currentUserId)) {
            throw new ConflictException("Store is already enabled.");
        }
        String store = storeRepository.enableStore(currentUserId);
        if (store == null) {
            throw new InternalServerErrorException(ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DELETING);
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
