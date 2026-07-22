package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.common.utils.OtpUtils;
import com.henheang.hphsar.exception.*;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.otp.Otp;
import com.henheang.hphsar.repository.OtpRepository;
import com.henheang.hphsar.service.EmailService;
import com.henheang.hphsar.service.OtpService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Random;

/**
 * OtpServiceImplV1 — OTP business logic
 * <p>
 * PROCESS — generateOtp():
 *   1. Find the user by email (supplier or buyer)
 *   2. FIX 8: Check if a valid OTP was already sent recently (rate limiting)
 *      → Prevent email spam and DB flooding
 *   3. Generate a random 4-digit number (1000–9999)
 *   4. Save OTP to DB with current timestamp
 *   5. Send OTP to user's email
 * <p>
 * PROCESS — verifyOtp():
 *   1. Block if user is already verified
 *   2. Load user + latest OTP from DB
 *   3. Validate: email matches, OTP code matches, not expired (< 3 min)
 *   4. Mark account as verified in DB (is_verified = true)
 *   5. FIX 3: Delete OTP from DB after use (one-time use only)
 */
@Service
public class OtpServiceImplV1 implements OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public OtpServiceImplV1(OtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    // ─── Helper: Check If User Account Is Already Verified ─────────────────────

    // Returns true if the user's email is already marked as verified in the DB
    // Used to prevent verifying an already-active account
    private boolean checkIfUserIsActivated(String email) {
        AppUser appUser = otpRepository.checkIfActivatedBySupplierEmail(email);
        if (appUser == null) {
            appUser = otpRepository.checkIfActivatedByBuyerEmail(email);
        }
        return appUser != null;
    }

    // ─── Helper: Get User By Email ──────────────────────────────────────────────

    // Try supplier first, then buyer — the email can belong to either table
    private AppUser getUserByEmail(String email) {
        AppUser appUser = otpRepository.getUserSupplierByEmail(email);
        if (appUser == null) {
            appUser = otpRepository.getUserBuyerByEmail(email);
        }
        return appUser;
    }

    // ─── Helper: Get Latest OTP By Email ───────────────────────────────────────

    // Try supplier OTP table first, then buyer
    private Otp getLatestOtpByEmail(String email) {
        Otp otp = otpRepository.getSupplierOtpByEmail(email);
        if (otp == null) {
            otp = otpRepository.getBuyerOtpByEmail(email);
        }
        return otp;
    }

    // ─── Generate OTP ───────────────────────────────────────────────────────────

    @Override
    public String generateOtp(String email) {

        // Step 1: Find the user — must exist before generating OTP
        AppUser appUser = getUserByEmail(email);
        if (appUser == null) {
            throw new BadRequestException("This user does not exist.");
        }

        // Step 2 (FIX 8 — Rate Limiting):
        // Check if user already has a valid OTP that hasn't expired yet.
        // WHY: Without this check, users could spam the endpoint and flood
        //      their inbox or fill the OTP table with thousands of records.
        Otp existingOtp = getLatestOtpByEmail(email);
        if (existingOtp != null && OtpUtils.isNotExpired(existingOtp.getCreatedDate())) {
            throw new ConflictException(
                "An OTP was already sent. Please wait " + OtpUtils.OTP_EXPIRY_MINUTES + " minutes before requesting a new one."
            );
        }

        // Step 3: Generate a random 4-digit OTP code (1000–9999)
        // nextInt(9000) gives 0–8999, adding 1000 shifts range to 1000–9999
        Integer otpNumber = new Random().nextInt(9000) + 1000;

        // Step 4: Save the OTP to the correct table based on user role
        // roleId 1 = SUPPLIER → tb_supplier_otp
        // roleId 2 = BUYER       → tb_buyer_otp
        Otp otp;
        java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
        if (appUser.getRoleId() == 1) {
            otp = otpRepository.generateSupplierOtp(appUser.getId(), otpNumber, email, time);
        } else {
            otp = otpRepository.generateBuyerOtp(appUser.getId(), otpNumber, email, time);
        }

        if (otp == null) {
            throw new InternalServerErrorException("Failed to generate OTP.");
        }

        // Step 5: Send OTP code to the user's email
        emailService.sendSimpleMail(
            email,
            "Here is your verification code: " + otpNumber,
            otpNumber + " - H-Phsar verification code"
        );

        return "We've already sent you the code to " + email;
    }

    // ─── Verify OTP ─────────────────────────────────────────────────────────────

    @Override
    public String verifyOtp(Integer otp, String email) {

        // Step 1: Block re-verification — account is already active
        if (checkIfUserIsActivated(email)) {
            throw new ConflictException("This user is already verified.");
        }

        // Step 2: Load user and their latest OTP from DB
        AppUser appUser = getUserByEmail(email);
        Otp otpObj = getLatestOtpByEmail(email);

        if (appUser == null) {
            throw new BadRequestException("This user does not exist.");
        }
        if (otpObj == null) {
            throw new BadRequestException("No OTP found. Please request a new one.");
        }

        // Step 3: Validate the OTP
        // Check 1 — email in OTP record must match the request email
        if (!Objects.equals(appUser.getEmail(), otpObj.getEmail())) {
            throw new BadRequestException("Email not match.");
        }
        // Check 2 — OTP code must match what was saved in DB
        if (!Objects.equals(otpObj.getOtpCode(), otp)) {
            throw new BadRequestException("OTP code not match.");
        }
        // Check 3 — OTP must be within expiry window (FIX 4: using shared OtpUtils)
        // WHY: Without this, an OTP from last week could still be used to verify
        if (!OtpUtils.isNotExpired(otpObj.getCreatedDate())) {
            throw new BadRequestException("OTP expired. Please request a new one.");
        }

        // Step 4: Mark account as verified in DB (is_verified = true)
        // Try supplier first — if '1' returned, update was successful
        String confirm = otpRepository.verifySupplier(email);
        if (!Objects.equals(confirm, "1")) {
            // If not a supplier, try buyer
            confirm = otpRepository.verifyBuyer(email);
        }
        if (!Objects.equals(confirm, "1")) {
            throw new InternalServerErrorException("Verification failed. Please try again.");
        }

        // Step 5 (FIX 3 — Delete OTP after use):
        // WHY: OTP is now consumed. Delete it so it cannot be reused.
        //      Both deletes are called — only the matching one will affect rows.
        otpRepository.deleteSupplierOtp(email);
        otpRepository.deleteBuyerOtp(email);

        return "Account has been verified successfully.";
    }
}