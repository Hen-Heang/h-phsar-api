package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.*;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.otp.Otp;
import com.henheang.stock_flow_commerce.repository.OtpRepository;
import com.henheang.stock_flow_commerce.service.EmailService;
import com.henheang.stock_flow_commerce.service.OtpService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.sql.Time;

@Service
public class OtpServiceImplV1 implements OtpService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public OtpServiceImplV1(OtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    public Boolean lessThan3MinutesCheck(Date createdDate) {
        Date currentDate = new Date();
        long diffInMillis = Math.abs(currentDate.getTime() - createdDate.getTime());
        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS);
        return diffInMinutes < 3;
    }


    Boolean checkIfUserIsActivated(String email) {
        AppUser appUser = otpRepository.checkIfActivatedByDistributorEmail(email);
        if (appUser == null) {
            appUser = otpRepository.checkIfActivatedByRetailerEmail(email);
        }
        Boolean isVerified = true;
        if (appUser != null) {
            return isVerified;
        } else {
            return false;
        }
    }

    AppUser getUserDistributorByEmail(String email) {
        return otpRepository.getUserDistributorByEmail(email);
    }

    AppUser getUserRetailerByEmail(String email) {
        return otpRepository.getUserRetailerByEmail(email);
    }

    @Override
    public String generateOtp(String email) {
        Random rand = new Random();
        Integer otpNumber = rand.nextInt(9000) + 1000;
//        System.out.println(otpNumber);

        AppUser appUser = getUserDistributorByEmail(email);
        if (appUser == null) {
            appUser = getUserRetailerByEmail(email);
        }
        if (appUser == null) {
            throw new BadRequestException("This user does not exist.");
        }
        Integer currentUserId = appUser.getId();
        // need OTP for reset password
//        if (checkIfUserIsActivated(email)) {
//            throw new ConflictException("This User is already verified");
//        }
        long timeInMilli = System.currentTimeMillis();
        Otp otp = null;
        emailService.sendSimpleMail(email, "Here is your verification code: " + otpNumber, otpNumber + " - Warehouse master verification code");
        if (appUser.getRoleId() == 1) {
            java.sql.Timestamp time = new Timestamp(timeInMilli);
            otp = otpRepository.generateDistributorOtp(currentUserId, otpNumber, email, time);
        } else {
            java.sql.Timestamp time = new Timestamp(timeInMilli);
            otp = otpRepository.generateRetailerOtp(currentUserId, otpNumber, email, time);
        }
        // if generate OTP success, send email
        if (otp == null) {
            throw new InternalServerErrorException("Failed to generate OTP...");
        }

        return "We've already sent you the code to " + email;
    }

    @Override
    public String verifyOtp(Integer otp, String email) {
        // Check if user is already verified
        if (checkIfUserIsActivated(email)) {
            throw new ConflictException("This User is already verified");
        }
        // get user by email
        AppUser appUser = new AppUser();
        Otp otpObj = new Otp();
        appUser = getUserDistributorByEmail(email);
        otpObj = otpRepository.getDistributorOtpByEmail(email);
        if (appUser == null || otpObj == null) {
            appUser = getUserRetailerByEmail(email);
            otpObj = otpRepository.getRetailerOtpByEmail(email);
        }

        String returnMsg = "User has been verified";
        if (appUser == null) {
            throw new BadRequestException("This user does not exist.");
        }
        if (otpObj == null) {
            throw new BadRequestException("This OTP code does not exist.");
        }
        // check if request and database of OTP matches
        if (!Objects.equals(appUser.getEmail(), otpObj.getEmail())) {
            throw new BadRequestException("Email not match");
        } else if (!Objects.equals(otpObj.getOtpCode(), otp)) {
            throw new BadRequestException("OTP code not match");
        }
        // check timeout of 3 minutes
        else if (!lessThan3MinutesCheck(otpObj.getCreatedDate())) {
            throw new BadRequestException("OTP Expired");
        }
        // if everything is success, verify that email

        String confirm = otpRepository.verifyDistributor(email);
        if (Objects.equals(confirm, "1")) {
            return returnMsg;
        }

        confirm = otpRepository.verifyRetailer(email);
        if (Objects.equals(confirm, "1")) {
            return returnMsg;
        }
        return "Verifying OTP failed";
    }

}
