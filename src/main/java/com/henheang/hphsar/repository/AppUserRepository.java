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
 *   Java:  AppUser findDistributorUserByEmail(String email)
 *   XML:   <select id="findDistributorUserByEmail" ...>SELECT ...</select>
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
    AppUser insertDistributorUser(@Param("user") AppUserRequest appUserRequest);
    AppUser insertRetailerUser(@Param("user") AppUserRequest appUserRequest);

    // ─── SELECT BY EMAIL ───────────────────────────────────────────────────────
    // MyBatis matches the parameter #{email} in XML to the String email argument here
    AppUser findDistributorUserByEmail(String email);
    AppUser findDistributorUserById(Integer id);
    AppUser findRetailerUserByEmail(String email);

    // ─── CHECK DUPLICATE PHONE ─────────────────────────────────────────────────
    Boolean checkPhoneNumberFromDistributorPhone(String phone);
    Boolean checkPhoneNumberFromDistributorDetail(String phone);
    Boolean checkPhoneNumberFromRetailerPhone(String phone);
    Boolean checkPhoneNumberFromRetailerDetail(String phone);

    // ─── GET ROLE ID ───────────────────────────────────────────────────────────
    Integer getRoleIdByMail(String email);
    Integer getRoleIdByMailRetailer(String email);

    // ─── GET VERIFICATION STATUS ───────────────────────────────────────────────
    Boolean getVerifyDistributorEmail(String email);
    Boolean getVerifyRetailerEmail(String email);

    // ─── UPDATE PASSWORD ───────────────────────────────────────────────────────
    // @Param is needed here because the method has multiple parameters
    // Without @Param, MyBatis cannot tell which is #{email} and which is #{newPassword}
    AppUser updateDistributorUser(JwtChangePasswordRequest request);
    AppUser updateRetailerUser(JwtChangePasswordRequest request);
    String updateForgetDistributorUser(@Param("email") String email, @Param("newPassword") String newPassword);
    String updateForgetRetailerUser(@Param("email") String email, @Param("newPassword") String newPassword);

    // ─── GET USER ID ───────────────────────────────────────────────────────────
    Integer getUserIdByMailDistributor(String email);
    Integer getUserIdByMailRetailer(String email);
}