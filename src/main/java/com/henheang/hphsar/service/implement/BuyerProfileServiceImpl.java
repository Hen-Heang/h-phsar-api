package com.henheang.hphsar.service.implement;


import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.buyer.Buyer;
import com.henheang.hphsar.model.buyer.BuyerRequest;
import com.henheang.hphsar.repository.BuyerProfileRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.BuyerProfileService;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BuyerProfileServiceImpl implements BuyerProfileService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final BuyerProfileRepository buyerProfileRepository;
    private final StoreRepository storeRepository;

    public BuyerProfileServiceImpl(BuyerProfileRepository buyerProfileRepository, StoreRepository storeRepository) {
        this.buyerProfileRepository = buyerProfileRepository;
        this.storeRepository = storeRepository;
    }


    @Override
    public Buyer createBuyerProfile(Integer currentUserId, BuyerRequest buyerRequest) {

        //check if buyer profile is already created
        if (checkIfBuyerProfileIsAlreadyCreated(currentUserId)) {
            throw new ConflictException("Opps, buyer profile is already created!");
        }


        Set<String> uniqueAdditionalPhoneNumber = new HashSet<String>();
        // check if additional phone number is duplicated
        for (String additionalPhoneNumber : buyerRequest.getAdditionalPhoneNumber()) {
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
        Matcher primaryPhone = pattern2.matcher(buyerRequest.getPrimaryPhoneNumber());
        String additionalPhone = String.join(",", buyerRequest.getAdditionalPhoneNumber());
        Matcher additionalPhoneNumbers = pattern.matcher(additionalPhone);

        if (!primaryPhone.matches()) {
            System.out.println(primaryPhone);
            throw new BadRequestException(("Opps, please input the valid primary phone number start with ( 0 ) or (855)"));
        }
        if (storeRepository.checkDuplicatePhone(buyerRequest.getPrimaryPhoneNumber())){
            throw new ConflictException("Phone number already exist. Please input another phone number.");
        }

        if(!additionalPhone.isEmpty() && !additionalPhone.isBlank())
            if (!additionalPhoneNumbers.matches()) {
                throw new BadRequestException(("Opps, additional phone number cannot contain letters or symbols"));
            }
        if(!(additionalPhone.isEmpty() && additionalPhone.isBlank())) {
            if (!(additionalPhone.startsWith("0") || additionalPhone.startsWith("855"))) {
                throw new BadRequestException("Opps, please input the valid additional phone number start with ( 0 ) or (855)");
            }
        }

        if (!(buyerRequest.getGender().equalsIgnoreCase("male") ||
                buyerRequest.getGender().equalsIgnoreCase("female") ||
                buyerRequest.getGender().equalsIgnoreCase("Gay") ||
                buyerRequest.getGender().equalsIgnoreCase("Lesbian") ||
                buyerRequest.getGender().equalsIgnoreCase("Bisexual") ||
                buyerRequest.getGender().equalsIgnoreCase("Pansexual") ||
                buyerRequest.getGender().equalsIgnoreCase("Queer") ||
                buyerRequest.getGender().equalsIgnoreCase("Other")
        )) {
            throw new BadRequestException("Please input valid gender. Available gender are 'male', 'female', 'gay', 'lesbian', 'bisexual', 'pansexual', 'queer', or 'other'.");
        }

        // prevent null or blank
        if (buyerRequest.getFirstName() == null || buyerRequest.getFirstName().isBlank() ||
                buyerRequest.getLastName() == null || buyerRequest.getLastName().isBlank() ||
                buyerRequest.getGender() == null || buyerRequest.getGender().isBlank() ||
                buyerRequest.getAddress() == null || buyerRequest.getAddress().isBlank() ||
                buyerRequest.getProfileImage() == null || buyerRequest.getProfileImage().isBlank()
        ) {
            throw new BadRequestException("Opps, fields cannot be empty or blank!");
        }

//        insert buyer profile and return buyer info id in real time
        Integer buyerInfoId = buyerProfileRepository.createBuyerProfile(currentUserId, buyerRequest);

        //insert additional phone number to tb_buyer_phone
        for (String additionalPhoneNumber : buyerRequest.getAdditionalPhoneNumber()) {
            buyerProfileRepository.insertAdditinalPhoneNumber(buyerInfoId, additionalPhoneNumber);
        }

        return buyerProfileRepository.getBuyerProfile(currentUserId);
    }

    private boolean checkIfBuyerProfileIsAlreadyCreated(Integer currentUserId) {

        return buyerProfileRepository.checkIfBuyerProfileIsAlreadyCreated(currentUserId);
    }


    @Override
    public Buyer getBuyerProfile(Integer currentUserId) throws ParseException {

        Buyer buyer = buyerProfileRepository.getBuyerProfile(currentUserId);
        if(buyer== null){
            throw new NotFoundException("Buyer profile not found!");
        }
        buyer.setUpdatedDate(formatter.format(formatter.parse(buyer.getUpdatedDate())));
        buyer.setCreatedDate(formatter.format(formatter.parse(buyer.getCreatedDate())));
        return buyer;
    }

    @Override
    public Buyer updateBuyerProfile(Integer currentUserId, BuyerRequest buyerRequest) {

        //check if buyer profile is already creatd
        if (!checkIfBuyerProfileIsAlreadyCreated(currentUserId)) {
            throw new ConflictException("Opps, buyer profile isn't created yet!");
        }

        String regex = "^[0-9,]+$"; //allow only number and comma (,)
        String regex2 = "^[0-9]+$"; //allow only number
        Pattern pattern = Pattern.compile(regex);
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher primaryPhone = pattern2.matcher(buyerRequest.getPrimaryPhoneNumber());
        String additionalPhone = String.join(",", buyerRequest.getAdditionalPhoneNumber());
        Matcher additionalPhoneNumbers = pattern.matcher(additionalPhone);

        if (!primaryPhone.matches()) {
            throw new BadRequestException(("Opps, please input the valid primary phone number start with ( 0 ) or (855)"));
        }
        if(!additionalPhone.isEmpty() && !additionalPhone.isBlank())
        if (!additionalPhoneNumbers.matches()) {
            throw new BadRequestException(("Opps, additional phone number cannot contain letters or symbols"));
        }


        Integer buyerInfoId = buyerProfileRepository.getBuyerInfoId(currentUserId);
        //delete from table buyer phone
        buyerProfileRepository.deleteAdditionalPhoneNumber(buyerInfoId);

        // update table buyer info
        buyerProfileRepository.updateBuyerProfile(currentUserId, buyerRequest);

        for (String additionalPhoneNumber : buyerRequest.getAdditionalPhoneNumber()) {
            if (additionalPhoneNumber.startsWith("0")) {
                additionalPhoneNumber = "855" + additionalPhoneNumber.substring(1);
                buyerProfileRepository.insertAdditinalPhoneNumber(buyerInfoId, additionalPhoneNumber);
            } else if (additionalPhoneNumber.isBlank()) {
                buyerProfileRepository.insertAdditinalPhoneNumber(buyerInfoId, additionalPhoneNumber);
            } else if (!(additionalPhoneNumber.startsWith("0") || additionalPhoneNumber.startsWith("855"))) {
                throw new BadRequestException("Opps, please input the valid additional phone number start with ( 0 ) or (855)");
            } else buyerProfileRepository.insertAdditinalPhoneNumber(buyerInfoId, additionalPhoneNumber);
        }
        return buyerProfileRepository.getBuyerProfile(currentUserId);
    }


}
