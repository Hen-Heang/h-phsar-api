package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.supplier.Supplier;
import com.henheang.hphsar.model.supplier.SupplierRequest;
import com.henheang.hphsar.repository.SupplierProfileRepository;
import com.henheang.hphsar.service.SupplierProfileService;
import com.henheang.hphsar.utils.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class SupplierProfileServiceImpl implements SupplierProfileService {

    private final SupplierProfileRepository userProfileRepository;

    public SupplierProfileServiceImpl(SupplierProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public Boolean checkUserProfileIfCreated(Integer currentUserId) {
        return userProfileRepository.checkIfUserProfileIsCreated(currentUserId);
    }


    @Override
    public Supplier getUserProfile(Integer currentUserId) throws ParseException {
        // get user profile for current user
        Supplier userProfile = userProfileRepository.getUserProfile(currentUserId);
        if (userProfile == null) {
            throw new NotFoundException("Supplier profile not found");
        }
        userProfile.setCreatedDate(formatter.format(formatter.parse(userProfile.getCreatedDate())));
        userProfile.setUpdatedDate(formatter.format(formatter.parse(userProfile.getUpdatedDate())));
        return userProfile;
    }

    //for insert additionalPhoneNumber into table tb_supplier_phone
//    public void addAdditionalPhoneNumber(String phone){
//
//        Supplier userProfile= userProfileRepository.addAdditionalPhoneNumber(supplierRequest, additionalPhoneNumber);
//    }

    @Override
    public Supplier addUserProfile(Integer currentUserId, SupplierRequest supplierRequest) throws ParseException {
        //check if user profile is already created
        if (checkUserProfileIfCreated(currentUserId)) {
            throw new ConflictException("User profile is already created!");
        }
        // prevent blank
        if (supplierRequest.getFirstName().isEmpty() || supplierRequest.getFirstName().isBlank() ||
                supplierRequest.getLastName().isEmpty() || supplierRequest.getLastName().isBlank() ||
                supplierRequest.getGender().isEmpty() || supplierRequest.getGender().isBlank()) {
            throw new BadRequestException("First name, Last name, or Gender can not be empty.");
        }
        if (!(supplierRequest.getGender().equalsIgnoreCase("male") ||
                supplierRequest.getGender().equalsIgnoreCase("female") ||
                supplierRequest.getGender().equalsIgnoreCase("Other")
        )) {
            throw new BadRequestException("Please input valid gender. Available gender are 'male', 'female', or 'other'.");
        }
        //insert a supplier profile and return supplier info id
        Supplier supplier = userProfileRepository.insertSupplierInfo(currentUserId, supplierRequest);
        if (supplier == null) {
            throw new InternalServerErrorException("Fail to insert user profile");
        }
        supplier.setCreatedDate(formatter.format(formatter.parse(supplier.getCreatedDate())));
        supplier.setUpdatedDate(formatter.format(formatter.parse(supplier.getUpdatedDate())));
        return supplier;
    }

    private boolean checkIfAdditionalPhoneNumberExist(String additionalPhoneNumber) {
        return userProfileRepository.checkIfAdditionalPhoneNumberExist(additionalPhoneNumber);
    }


    @Override
    public Supplier updateUserProfile(Integer currentUserId, SupplierRequest supplierRequest) throws ParseException {

        if (!checkUserProfileIfCreated(currentUserId)) {
            throw new ConflictException("User profile isn't created yet!");
        }

        // update user profile
        Supplier supplier = userProfileRepository.updateUserProfile(currentUserId, supplierRequest);
        if (supplier == null) {
            throw new InternalServerErrorException("Fail to update profile");
        }
        supplier.setCreatedDate(formatter.format(formatter.parse(supplier.getCreatedDate())));
        supplier.setUpdatedDate(formatter.format(formatter.parse(supplier.getUpdatedDate())));

        return supplier;
    }


}

