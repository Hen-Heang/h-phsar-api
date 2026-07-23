package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.otp.Otp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * OtpRepository — OTP Database Operations
 *
 * All SQL is now in: resources/mapper/OtpMapper.xml
 *
 * This interface only declares method signatures.
 * MyBatis matches each method here to a SQL block in OtpMapper.xml
 * using the method name as the id.
 * <p>
 * Example:
 *   Java:  Otp getSupplierOtpByEmail(String email)
 *   XML:   <select id="getSupplierOtpByEmail" ...>SELECT ...</select>
 */
@Mapper
public interface OtpRepository {

    // ─── CHECK IF ACCOUNT IS VERIFIED ──────────────────────────────────────────
    // Returns user only if is_verified = TRUE — null means not yet verified
    AppUser checkIfActivatedBySupplierEmail(String email);
    AppUser checkIfActivatedByBuyerEmail(String email);

    // ─── GET USER BY EMAIL ─────────────────────────────────────────────────────
    // Used to find which account the email belongs to before generating OTP
    AppUser getUserSupplierByEmail(String email);
    AppUser getUserBuyerByEmail(String email);

    // ─── GENERATE OTP ──────────────────────────────────────────────────────────
    // @Param is required because the method has multiple parameters
    // Without @Param, MyBatis cannot tell which value belongs to which #{placeholder}
    Otp generateSupplierOtp(@Param("currentUserId") Integer currentUserId,
                                @Param("otpNumber") Integer otpNumber,
                                @Param("email") String email,
                                @Param("time") java.sql.Timestamp time);

    Otp generateBuyerOtp(@Param("currentUserId") Integer currentUserId,
                             @Param("otpNumber") Integer otpNumber,
                             @Param("email") String email,
                             @Param("time") java.sql.Timestamp time);

    // ─── GET LATEST OTP ────────────────────────────────────────────────────────
    // Fetches the most recently generated OTP for a given email
    Otp getSupplierOtpByEmail(String email);
    Otp getBuyerOtpByEmail(String email);

    // ─── VERIFY ACCOUNT ────────────────────────────────────────────────────────
    // Sets is_verified = true, returns '1' on success
    String verifySupplier(String email);
    String verifyBuyer(String email);

    // ─── DELETE OTP AFTER USE ──────────────────────────────────────────────────
    // Deletes OTP record after it has been used — prevents reuse
    void deleteSupplierOtp(String email);
    void deleteBuyerOtp(String email);
}