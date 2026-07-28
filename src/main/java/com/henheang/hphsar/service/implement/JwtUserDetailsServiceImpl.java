package com.henheang.hphsar.service.implement;
import com.henheang.hphsar.common.ExceptionMessages;

import com.henheang.hphsar.utils.EmailValidatorUtils;
import com.henheang.hphsar.utils.OtpUtils;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.appUser.AppUserDto;
import com.henheang.hphsar.model.appUser.AppUserRequest;
import com.henheang.hphsar.model.jwt.JwtChangePasswordRequest;
import com.henheang.hphsar.model.otp.Otp;
import com.henheang.hphsar.repository.AppUserRepository;
import com.henheang.hphsar.repository.OtpRepository;
import com.henheang.hphsar.service.JwtUserDetailsService;
import com.henheang.hphsar.service.OtpService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * JwtUserDetailsServiceImpl — Core User & Auth Business Logic
 * <p>
 * Implements two interfaces:
 *   - UserDetailsService (Spring Security) → loadUserByUsername() used during login
 *   - JwtUserDetailsService (our own) → register, change password, forget password
 * <p>
 * PROCESSES:
 *   insertUser()      → register new account with validation + Bcrypt hashing
 *   loadUserByUsername() → load user from DB for Spring Security authentication
 *   getVerifyEmail()  → check if email is verified before allowing login
 *   changePassword()  → verify old password then update to new Bcrypt hash
 *   forgetPassword()  → reset password using OTP verification
 */
@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService, JwtUserDetailsService {

    private final AppUserRepository appUserRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    // AuthenticationManager is backed by DaoAuthenticationProvider, which in turn depends
    // on this class as its UserDetailsService — an eager reference here would recreate the
    // same bean-creation cycle. @Lazy defers real resolution until authenticateLogin()
    // actually calls it, after the context has finished starting. @RequiredArgsConstructor
    // does not propagate field annotations to the generated constructor, so this
    // constructor is written explicitly.
    public JwtUserDetailsServiceImpl(AppUserRepository appUserRepository,
                                      OtpRepository otpRepository,
                                      PasswordEncoder passwordEncoder,
                                      @Lazy AuthenticationManager authenticationManager,
                                      OtpService otpService) {
        this.appUserRepository = appUserRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
    }

    // ─── Load User For Spring Security ─────────────────────────────────────────

    /**
     * Called by Spring Security during authentication (login).
     * Searches supplier table first, then buyer table.
     * Returns AppUser which implements UserDetails.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try supplier account first
        UserDetails user = appUserRepository.findSupplierUserByEmail(email);
        if (user == null) {
            // If not found, try buyer account
            user = appUserRepository.findBuyerUserByEmail(email);
        }
        if (user == null) {
            // If not found, try admin account
            user = appUserRepository.findAdminUserByEmail(email);
        }
        if (user == null) {
            throw new BadRequestException("Invalid email address. Please input valid email address.");
        }
        return user;
    }

    // ─── Register New User ──────────────────────────────────────────────────────

    /**
     * PROCESS — insertUser():
     *   1. Validate roleId (must be 1 or 2)
     *   2. Validate email is not blank + matches email format
     *   3. FIX 2: Validate password BEFORE encoding (was after, so check never worked)
     *   4. Check for duplicate email
     *   5. Encode password with Bcrypt
     *   6. Insert into correct DB table based on role
     */
    @Override
    public AppUserDto insertUser(AppUserRequest appUserRequest) {

        // Step 1: Validate role — only 1 (SUPPLIER) or 2 (BUYER) allowed
        if (!(appUserRequest.getRoleId().equals(1) || appUserRequest.getRoleId().equals(2))) {
            throw new BadRequestException("Invalid roleId. Use 1 for SUPPLIER or 2 for BUYER.");
        }

        // Step 2: Validate email
        if (appUserRequest.getEmail().isBlank()) {
            throw new BadRequestException("Email cannot be empty.");
        }
        if (!EmailValidatorUtils.isValid(appUserRequest.getEmail())) {
            throw new BadRequestException("Please follow email format (e.g. user@domain.com).");
        }

        // Step 3 (FIX 2): Validate password BEFORE encoding
        // WHY: After Bcrypt encoding, the password becomes a hash like "$2a$10$..."
        //      Checking .equals("string") on a hash will ALWAYS be false — the check never works.
        //      We must validate the raw password BEFORE encoding it.
        if (appUserRequest.getPassword().isBlank() || appUserRequest.getPassword().equals("string")) {
            throw new BadRequestException("Invalid password. Please provide a real password.");
        }
        if (!appUserRequest.getPassword().equals(appUserRequest.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match.");
        }

        // Step 4: Check if email is already registered (in either table)
        AppUser checkDuplicate = appUserRepository.findSupplierUserByEmail(appUserRequest.getEmail());
        AppUser checkDuplicateBuyer = appUserRepository.findBuyerUserByEmail(appUserRequest.getEmail());
        if (checkDuplicate != null || checkDuplicateBuyer != null) {
            throw new ConflictException("This email is already taken.");
        }

        // Step 5: Encode password with Bcrypt AFTER all validations pass
        // Bcrypt adds a random salt and hashes the password — never stores plain text
        appUserRequest.setPassword(passwordEncoder.encode(appUserRequest.getPassword()));

        // Step 6: Insert into the correct table based on role
        AppUser appUser;
        if (appUserRequest.getRoleId() == 1) {
            appUser = appUserRepository.insertSupplierUser(appUserRequest);
        } else {
            appUser = appUserRepository.insertBuyerUser(appUserRequest);
        }

        return toDto(appUser);
    }

    // ─── Check Email Verification Status ───────────────────────────────────────

    /**
     * Called during login to check if user has verified their email via OTP.
     * Returns true if verified, false if not yet verified.
     */
    @Override
    public boolean getVerifyEmail(String email) {
        // Try supplier first, then buyer
        Boolean isVerified = appUserRepository.getVerifySupplierEmail(email);
        if (isVerified == null) {
            isVerified = appUserRepository.getVerifyBuyerEmail(email);
        }
        if (isVerified == null) {
            isVerified = appUserRepository.getVerifyAdminEmail(email);
        }
        if (isVerified == null) {
            throw new BadRequestException("Email does not exist.");
        }
        return isVerified;
    }

    // ─── Change Password (logged-in user) ──────────────────────────────────────

    /**
     * PROCESS — changePassword():
     *   1. Find user by email
     *   2. Verify old password matches stored Bcrypt hash
     *   3. Encode new password with Bcrypt
     *   4. Update DB with new hashed password
     */
    @Override
    public AppUserDto changePassword(JwtChangePasswordRequest request) {
        // Step 1: Find user — try supplier first, then buyer
        boolean isSupplier = true;
        AppUser appUser = appUserRepository.findSupplierUserByEmail(request.getEmail());
        if (appUser == null) {
            isSupplier = false;
            appUser = appUserRepository.findBuyerUserByEmail(request.getEmail());
        }
        if (appUser == null) {
            throw new NotFoundException(ExceptionMessages.USER_NOT_FOUND_INVALID_EMAIL);
        }

        // Step 2: Verify old password matches the stored Bcrypt hash
        // BCrypt.matches() compares raw password against hash safely
        if (!passwordEncoder.matches(request.getOldPassword(), appUser.getPassword())) {
            throw new NotFoundException("Old password is incorrect.");
        }

        // Step 3: Encode the new password before saving
        request.setNewPassword(passwordEncoder.encode(request.getNewPassword()));

        // Step 4: Update password in DB
        AppUser updatedUser;
        if (isSupplier) {
            updatedUser = appUserRepository.updateSupplierUser(request);
        } else {
            updatedUser = appUserRepository.updateBuyerUser(request);
        }

        return toDto(updatedUser);
    }

    // ─── Forget Password (uses OTP) ─────────────────────────────────────────────

    /**
     * PROCESS — forgetPassword():
     *   1. Find user + their latest OTP from DB
     *   2. Validate OTP: email matches, code matches, not expired
     *   3. Encode new password and update DB
     *   4. FIX 3: Delete OTP after use so it cannot be reused
     *   5. FIX 1: Return success message only (no plain text password in response)
     */
    @Override
    @Transactional
    public String forgetPassword(Integer otp, String email, String newPassword) {

        // Step 1: Find user and their latest OTP
        boolean isSupplier = true;
        AppUser appUser = appUserRepository.findSupplierUserByEmail(email);
        Otp otpObj = otpRepository.getSupplierOtpByEmail(email);

        if (appUser == null || otpObj == null) {
            isSupplier = false;
            appUser = appUserRepository.findBuyerUserByEmail(email);
            otpObj = otpRepository.getBuyerOtpByEmail(email);
        }

        if (appUser == null) {
            throw new NotFoundException(ExceptionMessages.USER_NOT_FOUND_INVALID_EMAIL);
        }
        if (otpObj == null) {
            throw new BadRequestException("No OTP found. Please request a new OTP first.");
        }

        // Step 2: Validate the OTP
        if (!Objects.equals(appUser.getEmail(), otpObj.getEmail())) {
            throw new BadRequestException("Email does not match OTP record.");
        }
        if (!Objects.equals(otpObj.getOtpCode(), otp)) {
            throw new BadRequestException("OTP code is incorrect.");
        }
        // FIX 4: Using shared OtpUtils instead of duplicate local method
        if (!OtpUtils.isNotExpired(otpObj.getCreatedDate())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Step 3: Encode and save the new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        String updatedPassword;
        if (isSupplier) {
            updatedPassword = appUserRepository.updateForgetSupplierUser(email, encodedNewPassword);
        } else {
            updatedPassword = appUserRepository.updateForgetBuyerUser(email, encodedNewPassword);
        }

        if (Objects.equals(updatedPassword, "null") || updatedPassword == null) {
            throw new InternalServerErrorException("Failed to update password. Please try again.");
        }

        // Step 4 (FIX 3): Delete OTP after use — one-time use only
        // WHY: If we don't delete, the same OTP could be reused to change the
        //      password again without the user knowing.
        otpRepository.deleteSupplierOtp(email);
        otpRepository.deleteBuyerOtp(email);

        // Step 5 (FIX 1): Return only a success message — NEVER return the plain text password
        // WHY: If the response is intercepted (man-in-the-middle), the attacker
        //      would get the user's new password in plain text.
        return "Password updated successfully. Please log in with your new password.";
    }

    // ─── Login Authentication ───────────────────────────────────────────────────

    /**
     * PROCESS — authenticateLogin():
     *   1. Block login if email not yet verified via OTP — auto-send a new OTP
     *   2. Verify email + password combination via Spring Security
     *      (internally: load user from DB + BCrypt.matches(raw, hash))
     * <p>
     * An unregistered email and a registered email with a wrong password both
     * end up throwing the same "Invalid email or password." message below —
     * getVerifyEmail()'s "email does not exist" case is caught and normalized
     * to that message instead of being allowed to reach the client as-is.
     * Without this, the distinct wording let a caller enumerate which emails
     * have accounts just from the login response, with no password needed.
     */
    @Override
    public void authenticateLogin(String email, String password) throws Exception {
        boolean isVerified;
        try {
            isVerified = getVerifyEmail(email);
        } catch (BadRequestException emailNotFound) {
            throw new BadRequestException("Invalid email or password.");
        }

        if (!isVerified) {
            otpService.generateOtp(email);
            throw new ConflictException("Email is not verified. We just sent you a verification code.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password.");
        }
    }

    // ─── Mapping Helper ─────────────────────────────────────────────────────────

    private AppUserDto toDto(AppUser user) {
        AppUserDto dto = new AppUserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRoleId(user.getRoleId());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsActive(user.getIsActive());
        return dto;
    }

    // ─── Role & User ID Helpers ─────────────────────────────────────────────────

    // Gets the role ID for a given email — checks supplier first, then buyer, then admin
    public Integer getRoleIdByMail(String email) {
        Integer roleId = appUserRepository.getRoleIdByMail(email);
        if (roleId == null) {
            roleId = appUserRepository.getRoleIdByMailBuyer(email);
        }
        if (roleId == null) {
            roleId = appUserRepository.getRoleIdByMailAdmin(email);
        }
        return roleId;
    }

    // Gets the user ID for a given email — checks supplier first, then buyer, then admin
    public Integer getUserIdByMail(String email) {
        Integer userId = appUserRepository.getUserIdByMailSupplier(email);
        if (userId == null) {
            userId = appUserRepository.getUserIdByMailBuyer(email);
        }
        if (userId == null) {
            userId = appUserRepository.getUserIdByMailAdmin(email);
        }
        return userId;
    }
}