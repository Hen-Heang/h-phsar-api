package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.appUser.AppUserRequest;
import com.henheang.hphsar.model.jwt.JwtChangePasswordRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AppUserRepository — User Database Operations
 * <p>
 * HOW MYBATIS XML WORKS (for beginners):
 * ─────────────────────────────────────────
 * Before (Annotation style):
 *   The SQL query was written directly on the method using @Select("SELECT ...")
 *   The column-to-field mapping was done using @Result(property="...", column="...")
 * <p>
 * After (XML style):
 *   1. This interface only declares the method name and parameters
 *   2. The actual SQL is written in a separate XML file: AppUserMapper.xml
 *   3. MyBatis connects them by matching:
 *        - The @Mapper interface full class name  →  namespace in XML
 *        - The method name here                  →  id="..." in XML
 * <p>
 * Example connection:
 *   Java:  AppUser findSupplierUserByEmail(String email)
 *   XML:   <select id="findSupplierUserByEmail" ...>SELECT ...</select>
 * <p>
 * WHY THIS IS BETTER:
 *   - SQL is in its own file — easier to read and format
 *   - No more messy @Result annotations on every method
 *   - ResultMap is defined once in XML and reused everywhere
 *   - Supports dynamic SQL (<if>, <foreach>) which @Select cannot do
 */

@Mapper
public interface AppUserRepository {

    // ─── INSERT ────────────────────────────────────────────────────────────────
    // @Param("user") tells MyBatis: the parameter named "user" in XML = appUserRequest here
    // In XML you access it as #{user.email}, #{user.password}, #{user.roleId}
    AppUser insertSupplierUser(@Param("user") AppUserRequest appUserRequest);
    AppUser insertBuyerUser(@Param("user") AppUserRequest appUserRequest);

    // ─── SELECT BY EMAIL ───────────────────────────────────────────────────────
    // MyBatis matches the parameter #{email} in XML to the String email argument here
    AppUser findSupplierUserByEmail(String email);
    AppUser findSupplierUserById(Integer id);
    AppUser findBuyerUserByEmail(String email);
    AppUser findAdminUserByEmail(String email);

    // ─── CHECK DUPLICATE PHONE ─────────────────────────────────────────────────
    Boolean checkPhoneNumberFromSupplierPhone(String phone);
    Boolean checkPhoneNumberFromSupplierDetail(String phone);
    Boolean checkPhoneNumberFromBuyerPhone(String phone);
    Boolean checkPhoneNumberFromBuyerDetail(String phone);

    // ─── GET ROLE ID ───────────────────────────────────────────────────────────
    Integer getRoleIdByMail(String email);
    Integer getRoleIdByMailBuyer(String email);
    Integer getRoleIdByMailAdmin(String email);

    // ─── GET VERIFICATION STATUS ───────────────────────────────────────────────
    Boolean getVerifySupplierEmail(String email);
    Boolean getVerifyBuyerEmail(String email);
    Boolean getVerifyAdminEmail(String email);

    // ─── UPDATE PASSWORD ───────────────────────────────────────────────────────
    // @Param is needed here because the method has multiple parameters
    // Without @Param, MyBatis cannot tell which is #{email} and which is #{newPassword}
    AppUser updateSupplierUser(JwtChangePasswordRequest request);
    AppUser updateBuyerUser(JwtChangePasswordRequest request);
    String updateForgetSupplierUser(@Param("email") String email, @Param("newPassword") String newPassword);
    String updateForgetBuyerUser(@Param("email") String email, @Param("newPassword") String newPassword);

    // ─── GET USER ID ───────────────────────────────────────────────────────────
    Integer getUserIdByMailSupplier(String email);
    Integer getUserIdByMailBuyer(String email);
    Integer getUserIdByMailAdmin(String email);
}