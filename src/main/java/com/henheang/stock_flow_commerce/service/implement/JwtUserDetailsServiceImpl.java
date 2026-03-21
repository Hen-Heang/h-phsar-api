package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.ConflictException;
import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.exception.NotFoundException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.appUser.AppUserDto;
import com.henheang.stock_flow_commerce.model.appUser.AppUserRequest;
import com.henheang.stock_flow_commerce.model.jwt.JwtChangePasswordRequest;
import com.henheang.stock_flow_commerce.model.otp.Otp;
import com.henheang.stock_flow_commerce.repository.AppUserRepository;
import com.henheang.stock_flow_commerce.repository.OtpRepository;
import com.henheang.stock_flow_commerce.service.JwtUserDetailsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService, JwtUserDetailsService {

    private final AppUserRepository appUserRepository;
    private final OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    public JwtUserDetailsServiceImpl(AppUserRepository appUserRepository, OtpRepository otpRepository) {
        this.appUserRepository = appUserRepository;
        this.otpRepository = otpRepository;
    }

    Boolean checkDuplicatePhone(String phone, Integer roleId){
        Boolean isExistInUserPhone = false;
        Boolean isExistInUserInfo = false;

        if (roleId == 1) {
            isExistInUserPhone = appUserRepository.checkPhoneNumberFromDistributorPhone(phone);
            isExistInUserInfo = appUserRepository.checkPhoneNumberFromDistributorDetail(phone);
        } else {
            isExistInUserPhone = appUserRepository.checkPhoneNumberFromRetailerPhone(phone);
            isExistInUserInfo = appUserRepository.checkPhoneNumberFromRetailerDetail(phone);
        }
        if (isExistInUserPhone || isExistInUserInfo){
            return true;
        }
        return false;
    }

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public boolean validateEmail(final String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails user = appUserRepository.findDistributorUserByEmail(email);
        if (user == null){
            user = appUserRepository.findRetailerUserByEmail(email);
        }
        if (user == null){
            throw new BadRequestException("Invalid email address. Please input valid email address.");
        }
        return user;
    }

    @Override
    public AppUserDto insertUser(AppUserRequest appUserRequest) {
        if (!(appUserRequest.getRoleId().equals(1)|| appUserRequest.getRoleId().equals(2))){
            throw new BadRequestException("Invalid roleId.");
        }
        if (appUserRequest.getEmail().isBlank()){
            throw new BadRequestException("Email can not be null");
        }
        if (!(validateEmail(appUserRequest.getEmail()))){
            throw new BadRequestException("Please follow email format.");
        }
        if (appUserRequest.getPassword().isBlank()){
            throw new BadRequestException("Password can not be null");
        }
        appUserRequest.setPassword(passwordEncoder.encode((appUserRequest.getPassword())));
        AppUser appUser = null;
        AppUser checkDuplicate = appUserRepository.findDistributorUserByEmail(appUserRequest.getEmail());
        AppUser checkDuplicateRetailer = appUserRepository.findRetailerUserByEmail(appUserRequest.getEmail());
        if (checkDuplicate != null || checkDuplicateRetailer != null){
            throw new ConflictException("This email is already taken");
        }
        if (appUserRequest.getRoleId() == 1) {
            if (appUserRequest.getPassword().equals("string") || appUserRequest.getPassword().isBlank()) {
                throw new BadRequestException("Invalid password");
            }
            appUser = appUserRepository.insertDistributorUser(appUserRequest);
        }else if (appUserRequest.getRoleId() == 2){

            if (appUserRequest.getPassword().equals("string") || appUserRequest.getPassword().isBlank()) {
                throw new BadRequestException("Invalid password");
            }
            appUser = appUserRepository.insertRetailerUser(appUserRequest);
        }
        return modelMapper.map(appUser, AppUserDto.class);
    }

    @Override
    public boolean getVerifyEmail(String email) {
        Boolean isVerified = appUserRepository.getVerifyDistributorEmail(email);
        if (isVerified == null){
            isVerified = appUserRepository.getVerifyRetailerEmail(email);
        }
        if (isVerified == null){
            throw new BadRequestException("Email does not exist.");
        }
        return isVerified;
    }

    @Override
    public AppUserDto changePassword(JwtChangePasswordRequest request) {
        boolean isDistributor = true;
        AppUser appUser = appUserRepository.findDistributorUserByEmail(request.getEmail());
        if (appUser == null){
            isDistributor = false;
            appUser = appUserRepository.findRetailerUserByEmail(request.getEmail());
        }
        if (appUser == null){
            throw new NotFoundException("Not found. Invalid email.");
        }
        // verify password with encrypted password
        if (!(passwordEncoder.matches(request.getOldPassword(), appUser.getPassword()))){
            throw new NotFoundException("Old password is incorrect. Please input correct password.");
        }
        // if match encrypt new password and update database password
        request.setNewPassword(passwordEncoder.encode(request.getNewPassword()));
        AppUser newAppUser = new AppUser();
        if (isDistributor){
            newAppUser = appUserRepository.updateDistributorUser(request);
        } else {
            newAppUser = appUserRepository.updateRetailerUser(request);
        }
        return modelMapper.map(newAppUser, AppUserDto.class);
    }

    @Override
    public String forgetPassword(Integer otp, String email, String newPassword) {
        // check if user is exists
        Boolean isDistributor = true;
        Otp otpObj = null;
        AppUser appUser = appUserRepository.findDistributorUserByEmail(email);
        otpObj = otpRepository.getDistributorOtpByEmail(email);
        if (appUser == null || otpObj == null){
            isDistributor = false;
            appUser = appUserRepository.findRetailerUserByEmail(email);
            otpObj = otpRepository.getRetailerOtpByEmail(email);
        }
        if (appUser == null){
            throw new NotFoundException("Not found. Invalid email.");
        }
        if (otpObj == null){
            throw new BadRequestException("This OTP does not exist");
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
        // update new password
        String updatedPassword = "null";
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        if (isDistributor) {
            updatedPassword = appUserRepository.updateForgetDistributorUser(email, encodedNewPassword);
        } else {
            updatedPassword = appUserRepository.updateForgetRetailerUser(email, encodedNewPassword);
        }
        if (Objects.equals(updatedPassword, "null")){
            throw new InternalServerErrorException("Fail to update new password.");
        }
        return "New password updated. Your new password is: "+ newPassword;
    }


    public Integer getRoleIdByMail(String email) {
        Integer roleId = appUserRepository.getRoleIdByMail(email);
        if (roleId == null){
            roleId = appUserRepository.getRoleIdByMailRetailer(email);
        }
        return roleId;
    }
    public Boolean lessThan3MinutesCheck(Date createdDate) {
        Date currentDate = new Date();
        long diffInMillis = Math.abs(currentDate.getTime() - createdDate.getTime());
        long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillis, TimeUnit.MILLISECONDS);
        return diffInMinutes < 3;
    }

    public Integer getUserIdByMail(String email) {
        Integer userId = appUserRepository.getUserIdByMailDistributor(email);
        if (userId == null){
            userId = appUserRepository.getUserIdByMailRetailer(email);
        }
        return userId;
    }
}
