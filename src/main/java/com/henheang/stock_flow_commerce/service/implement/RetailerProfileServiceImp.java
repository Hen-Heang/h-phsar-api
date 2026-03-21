package com.henheang.stock_flow_commerce.service.implement;


import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.ConflictException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.retailer.Retailer;
import com.henheang.stock_flow_commerce.model.retailer.RetailerRequest;
import com.henheang.stock_flow_commerce.repository.RetailerProfileRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.RetailerProfileService;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RetailerProfileServiceImp implements RetailerProfileService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final RetailerProfileRepository retailerProfileRepository;
    private final StoreRepository storeRepository;

    public RetailerProfileServiceImp(RetailerProfileRepository retailerProfileRepository, StoreRepository storeRepository) {
        this.retailerProfileRepository = retailerProfileRepository;
        this.storeRepository = storeRepository;
    }


    @Override
    public Retailer createRetailerProfile(Integer currentUserId, RetailerRequest retailerRequest) {

        //check if retailer profile is already created
        if (checkIfRetailerProfileIsAlreadyCreated(currentUserId)) {
            throw new ConflictException("Opps, retailer profile is already created!");
        }


        Set<String> uniqueAdditionalPhoneNumber = new HashSet<String>();
        // check if additional phone number is duplicated
        for (String additionalPhoneNumber : retailerRequest.getAdditionalPhoneNumber()) {
            if (additionalPhoneNumber.isBlank()){
                continue;
            }
            if (!uniqueAdditionalPhoneNumber.add(additionalPhoneNumber)) {
                throw new ConflictException("Opps, additional phone number cannot be duplicated");
            }
        }
        //validation for phone number
        String regex = "^[0-9,]+$"; //allow number and comma (,)
        String regex2 = "^[0-9]+$"; //allow number
        Pattern pattern = Pattern.compile(regex);
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher primaryPhone = pattern2.matcher(retailerRequest.getPrimaryPhoneNumber());
        String additionalPhone = String.join(",", retailerRequest.getAdditionalPhoneNumber());
        Matcher additionalPhoneNumbers = pattern.matcher(additionalPhone);

        if (!primaryPhone.matches()) {
            System.out.println(primaryPhone);
            throw new BadRequestException(("Opps, please input the valid primary phone number start with ( 0 ) or (855)"));
        }
        if (storeRepository.checkDuplicatePhone(retailerRequest.getPrimaryPhoneNumber())){
            throw new ConflictException("Phone number already exist. Please input another phone number.");
        }

        if(!additionalPhone.isEmpty() && !additionalPhone.isBlank())
            if (!additionalPhoneNumbers.matches()) {
                throw new BadRequestException(("Opps, additional phone number cannot contain letters or symbols"));
            }
        if(!(additionalPhone.isBlank() && additionalPhone.isBlank())) {
            if (!(additionalPhone.startsWith("0") || additionalPhone.startsWith("855"))) {
                throw new BadRequestException("Opps, please input the valid additional phone number start with ( 0 ) or (855)");
            }
        }

        if (!(retailerRequest.getGender().equalsIgnoreCase("male") ||
                retailerRequest.getGender().equalsIgnoreCase("female") ||
                retailerRequest.getGender().equalsIgnoreCase("Gay") ||
                retailerRequest.getGender().equalsIgnoreCase("Lesbian") ||
                retailerRequest.getGender().equalsIgnoreCase("Bisexual") ||
                retailerRequest.getGender().equalsIgnoreCase("Pansexual") ||
                retailerRequest.getGender().equalsIgnoreCase("Queer") ||
                retailerRequest.getGender().equalsIgnoreCase("Other")
        )) {
            throw new BadRequestException("Please input valid gender. Available gender are 'male', 'female', 'gay', 'lesbian', 'bisexual', 'pansexual', 'queer', or 'other'.");
        }

        // prevent blank
        if (retailerRequest.getFirstName().isEmpty() || retailerRequest.getFirstName().isBlank() ||
                retailerRequest.getLastName().isEmpty() || retailerRequest.getLastName().isBlank() ||
                retailerRequest.getGender().isEmpty() || retailerRequest.getGender().isBlank() ||
                retailerRequest.getAddress().isEmpty() || retailerRequest.getAddress().isBlank() ||
                retailerRequest.getProfileImage().isEmpty() || retailerRequest.getProfileImage().isBlank()
        ) {
            throw new BadRequestException("Opps, fields cannot be empty or blank!");
        }

//        insert reatiler profile and return retailer info id in real time
        Integer retailerInfoId = retailerProfileRepository.createRetailerProfile(currentUserId, retailerRequest);

        //insert additional phone number to tb_retailer_phone
        for (String additionalPhoneNumber : retailerRequest.getAdditionalPhoneNumber()) {
            retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
//            if (additionalPhoneNumber.startsWith("0")) {
//                additionalPhoneNumber = "855" + additionalPhoneNumber.substring(1);
//                retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
//            } else if (additionalPhoneNumber.isBlank()) {
//                retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
//            } else if (!(additionalPhoneNumber.startsWith("0") || additionalPhoneNumber.startsWith("855"))) {
//                throw new BadRequestException("Opps, please input the valid additional phone number start with ( 0 ) or (855)");
//            } else retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
        }

        return retailerProfileRepository.getRetailerProfile(currentUserId);
    }

    private boolean checkIfRetailerProfileIsAlreadyCreated(Integer currentUserId) {

        return retailerProfileRepository.checkIfRetailerProfileIsAlreadyCreated(currentUserId);
    }


    @Override
    public Retailer getRetailerProfile(Integer currentUserId) throws ParseException {

        Retailer retailer = retailerProfileRepository.getRetailerProfile(currentUserId);
        if(retailer== null){
            throw new NotFoundException("Retailer profile not found!");
        }
        retailer.setUpdatedDate(formatter.format(formatter.parse(retailer.getUpdatedDate())));
        retailer.setCreatedDate(formatter.format(formatter.parse(retailer.getCreatedDate())));
        return retailer;
    }

    @Override
    public Retailer updateRetailerProfile(Integer currentUserId, RetailerRequest retailerRequest) {

        //check if retailer profile is already creatd
        if (!checkIfRetailerProfileIsAlreadyCreated(currentUserId)) {
            throw new ConflictException("Opps, retailer profile isn't created yet!");
        }

        String regex = "^[0-9,]+$"; //allow only number and comma (,)
        String regex2 = "^[0-9]+$"; //allow only number
        Pattern pattern = Pattern.compile(regex);
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher primaryPhone = pattern2.matcher(retailerRequest.getPrimaryPhoneNumber());
        String additionalPhone = String.join(",", retailerRequest.getAdditionalPhoneNumber());
        Matcher additionalPhoneNumbers = pattern.matcher(additionalPhone);

        if (!primaryPhone.matches()) {
            throw new BadRequestException(("Opps, please input the valid primary phone number start with ( 0 ) or (855)"));
        }
        if(!additionalPhone.isEmpty() && !additionalPhone.isBlank())
        if (!additionalPhoneNumbers.matches()) {
            throw new BadRequestException(("Opps, additional phone number cannot contain letters or symbols"));
        }


        Integer retailerInfoId = retailerProfileRepository.getRetailerInfoId(currentUserId);
        //delete from table retailer phone
        retailerProfileRepository.deleteAdditionalPhoneNumber(retailerInfoId);

        // update table retailer info
        retailerProfileRepository.updateRetailerProfile(currentUserId, retailerRequest);

        for (String additionalPhoneNumber : retailerRequest.getAdditionalPhoneNumber()) {
            if (additionalPhoneNumber.startsWith("0")) {
                additionalPhoneNumber = "855" + additionalPhoneNumber.substring(1);
                retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
            } else if (additionalPhoneNumber.isBlank()) {
                retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
            } else if (!(additionalPhoneNumber.startsWith("0") || additionalPhoneNumber.startsWith("855"))) {
                throw new BadRequestException("Opps, please input the valid additional phone number start with ( 0 ) or (855)");
            } else retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId, additionalPhoneNumber);
        }
//        for(String additionalPhoneNumber: retailerRequest.getAdditionalPhoneNumber()) {
//            //insert additional phone number
//            retailerProfileRepository.insertAdditinalPhoneNumber(retailerInfoId,additionalPhoneNumber);
//        }
//        Retailer retailer = retailerProfileRepository.getRetailerProfile(currentUserId);
//        if (retailer.getAdditionalPhoneNumber().isEmpty()){
//            retailer.getAdditionalPhoneNumber().add("No additional phone number");
//        }
        return retailerProfileRepository.getRetailerProfile(currentUserId);
    }


}
