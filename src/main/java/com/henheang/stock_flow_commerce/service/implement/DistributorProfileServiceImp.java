package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.ConflictException;
import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.distributor.Distributor;
import com.henheang.stock_flow_commerce.model.distributor.DistributorRequest;
import com.henheang.stock_flow_commerce.repository.DistributorProfileRepository;
import com.henheang.stock_flow_commerce.service.DistributorProfileService;
import com.henheang.stock_flow_commerce.utils.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DistributorProfileServiceImp implements DistributorProfileService {

    private final DistributorProfileRepository userProfileRepository;

    public DistributorProfileServiceImp(DistributorProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public Boolean checkUserProfileIfCreated(Integer currentUserId) {
        return userProfileRepository.checkIfUserProfileIsCreated(currentUserId);
    }


    @Override
    public Distributor getUserProfile(Integer currentUserId) throws ParseException {
        // get user profile for current user
        Distributor userProfile = userProfileRepository.getUserProfile(currentUserId);
        if (userProfile == null) {
            throw new NotFoundException("Distributor profile not found");
        }
        userProfile.setCreatedDate(formatter.format(formatter.parse(userProfile.getCreatedDate())));
        userProfile.setUpdatedDate(formatter.format(formatter.parse(userProfile.getUpdatedDate())));
        return userProfile;
    }

    //for insert additionalPhoneNumber into table tb_distributor_phone
//    public void addAdditionalPhoneNumber(String phone){
//
//        Distributor userProfile= userProfileRepository.addAdditionalPhoneNumber(distributorRequest, additionalPhoneNumber);
//    }

    @Override
    public Distributor addUserProfile(Integer currentUserId, DistributorRequest distributorRequest) throws ParseException {
        //check if user profile is already created
        if (checkUserProfileIfCreated(currentUserId)) {
            throw new ConflictException("User profile is already created!");
        }
        // prevent blank
        if (distributorRequest.getFirstName().isEmpty() || distributorRequest.getFirstName().isBlank() ||
                distributorRequest.getLastName().isEmpty() || distributorRequest.getLastName().isBlank() ||
                distributorRequest.getGender().isEmpty() || distributorRequest.getGender().isBlank()) {
            throw new BadRequestException("First name, Last name, or Gender can not be empty.");
        }
        if (!(distributorRequest.getGender().equalsIgnoreCase("male") ||
                distributorRequest.getGender().equalsIgnoreCase("female") ||
                distributorRequest.getGender().equalsIgnoreCase("Other")
        )) {
            throw new BadRequestException("Please input valid gender. Available gender are 'male', 'female', or 'other'.");
        }
        //insert a distributor profile and return distributor info id
        Distributor distributor = userProfileRepository.insertDistributorInfo(currentUserId, distributorRequest);
        if (distributor == null) {
            throw new InternalServerErrorException("Fail to insert user profile");
        }
        distributor.setCreatedDate(formatter.format(formatter.parse(distributor.getCreatedDate())));
        distributor.setUpdatedDate(formatter.format(formatter.parse(distributor.getUpdatedDate())));
        return distributor;
    }

    private boolean checkIfAdditionalPhoneNumberExist(String additionalPhoneNumber) {
        return userProfileRepository.checkIfAdditionalPhoneNumberExist(additionalPhoneNumber);
    }


    @Override
    public Distributor updateUserProfile(Integer currentUserId, DistributorRequest distributorRequest) throws ParseException {

        if (!checkUserProfileIfCreated(currentUserId)) {
            throw new ConflictException("User profile isn't created yet!");
        }

        // update user profile
        Distributor distributor = userProfileRepository.updateUserProfile(currentUserId, distributorRequest);
        if (distributor == null) {
            throw new InternalServerErrorException("Fail to update profile");
        }
        distributor.setCreatedDate(formatter.format(formatter.parse(distributor.getCreatedDate())));
        distributor.setUpdatedDate(formatter.format(formatter.parse(distributor.getUpdatedDate())));

        return distributor;
    }


}

