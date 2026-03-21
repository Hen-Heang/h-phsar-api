package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.otp.Otp;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OtpRepository {

    @Select("""
            SELECT ta.id as id, email, password, tr.name as role, role_id as roleId, is_verified as isVerified, is_active as isActive FROM tb_distributor_account as ta
            JOIN tb_role tr ON ta.role_id = tr.id
            WHERE email = #{email}
            AND is_verified = TRUE;
            """)
    @Result(property = "userId", column = "user_id")
    AppUser checkIfActivatedByDistributorEmail(String email);

    @Select("""
            SELECT ta.id as id, email, password, tr.name as role, role_id as roleId, is_verified as isVerified, is_active as isActive FROM tb_retailer_account as ta
            JOIN tb_role tr ON ta.role_id = tr.id
            WHERE email = #{email}
            AND is_verified = TRUE;
            """)
    @Result(property = "userId", column = "user_id")
    AppUser checkIfActivatedByRetailerEmail(String email);


//    @Select("""
//            INSERT INTO tb_distributor_otp
//            VALUES (DEFAULT, #{currentUserId}, #{otpNumber}, #{email}, DEFAULT)
//            RETURNING *;
//            """)
//    Otp generateDistributorOtp(Integer currentUserId, Integer otpNumber, String email);

    @Select("""
            INSERT INTO tb_distributor_otp
            VALUES (DEFAULT, #{currentUserId}, #{otpNumber}, #{email}, #{time})
            RETURNING *;
            """)
    Otp generateDistributorOtp(Integer currentUserId, Integer otpNumber, String email, java.sql.Timestamp time);

    @Select("""
            INSERT INTO tb_retailer_otp
            VALUES (DEFAULT, #{currentUserId}, #{otpNumber}, #{email}, #{time})
            RETURNING *;
            """)
    Otp generateRetailerOtp(Integer currentUserId, Integer otpNumber, String email, java.sql.Timestamp time);

    @Select("""
            SELECT ta.id as id, email, password, tr.name as role, role_id as roleId, is_verified as isVerified, is_active as isActive FROM tb_distributor_account as ta
            JOIN tb_role tr ON ta.role_id = tr.id
            WHERE email = #{email};
            """)
    @Result(property = "userId", column = "user_id")
    AppUser getUserDistributorByEmail(String email);

    @Select("""
            SELECT ta.id as id, email, password, tr.name as role, role_id as roleId, is_verified as isVerified, is_active as isActive FROM tb_retailer_account as ta
            JOIN tb_role tr ON ta.role_id = tr.id
            WHERE email = #{email};
            """)
    @Result(property = "userId", column = "user_id")
    AppUser getUserRetailerByEmail(String email);

    @Select("""
            SELECT id, distributor_account_id as distributorAccountId, otp_code as otpCode, distributor_email as email, created_date AS createdDate
            FROM tb_distributor_otp
            WHERE distributor_email = #{email}
            ORDER BY created_date DESC
            LIMIT 1;
            """)
    Otp getDistributorOtpByEmail(String email);

    @Select("""
            SELECT id, retailer_account_id as distributorAccountId, otp_code as otpCode, retailer_email as email, created_date AS createdDate
            FROM tb_retailer_otp
            WHERE retailer_email = #{email}
            ORDER BY created_date DESC
            LIMIT 1;
            """)
    Otp getRetailerOtpByEmail(String email);

    @Select("""
            UPDATE tb_distributor_account
            SET is_verified = true
            WHERE email = #{email}
            RETURNING '1';
            """)
    String verifyDistributor(String email);

    @Select("""
            UPDATE tb_retailer_account
            SET is_verified = true
            WHERE email = #{email}
            RETURNING '1';
            """)
    String verifyRetailer(String email);
}
