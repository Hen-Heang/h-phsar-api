package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.retailer.Retailer;
import com.henheang.stock_flow_commerce.model.retailer.RetailerRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RetailerProfileRepository {


    //get retailer info id in real time
    @Select("""  
            insert into tb_retailer_info
            values (default, #{currentUserId},#{re.firstName},#{re.lastName},#{re.gender},#{re.address},#{re.primaryPhoneNumber},#{re.profileImage},default,default)
            returning id
            """)
    Integer createRetailerProfile(Integer currentUserId, @Param("re") RetailerRequest retailerRequest);

    @Select("""
            insert into tb_retailer_phone
            values(default, #{retailerInfoId},#{additionalPhoneNumber})
            """)
    void insertAdditinalPhoneNumber(Integer retailerInfoId, String additionalPhoneNumber);

    @Select("""
            select id, retailer_account_id as retailerAccountId, first_name as firstName,last_name as lastName,
            gender,address,primary_phone_number as primaryPhoneNumber,profile_image as profileImage,created_date as createdDate,updated_date as updatedDate
            from tb_retailer_info where retailer_account_id= #{currentUserId}
            """)
    @Result(property = "additionalPhoneNumber", column = "id",
            many = @Many(select = "getAdditionalPhoneNumberByRetailerInfoId"))
    @Result(property = "id", column = "id")
    Retailer getRetailerProfile(Integer currentUserId);

    @Select("""
             select phone_number
             from tb_retailer_phone where retailer_info_id =#{id}
            """)
    List<String> getAdditionalPhoneNumberByRetailerInfoId(Integer id);


    @Select("""
            update tb_retailer_info
            set first_name=#{re.firstName},last_name=#{re.lastName}, gender=#{re.gender},address=#{re.address},
            primary_phone_number=#{re.primaryPhoneNumber},profile_image=#{re.profileImage}
            where retailer_account_id= #{currentUserId};
            """)
    void updateRetailerProfile(Integer currentUserId, @Param("re") RetailerRequest retailerRequest);

    @Select("""
            select id from tb_retailer_info where retailer_account_id= #{currentUserId}
            """)
    Integer getRetailerInfoId(Integer currentUserId);


    @Select("""
            delete from tb_retailer_phone where retailer_info_id = #{retailerInfoId}
                                                                                  """)
    void deleteAdditionalPhoneNumber(Integer retailerInfoId);

    @Select("""
            select exists( select * from tb_retailer_info where retailer_account_id = #{currentUserId})
            """)
    boolean checkIfRetailerProfileIsAlreadyCreated(Integer currentUserId);

    @Select("""
            SELECT concat(first_name, ' ', last_name) FROM tb_retailer_info
            WHERE retailer_account_id = #{retailerId};
            """)
    String getRetailerNameById(Integer retailerId);

    @Select("""
            SELECT profile_image
            FROM tb_retailer_info
            WHERE retailer_account_id = #{retailerId};
            """)
    String getRetailerImageById(Integer retailerId);

    @Select("""
            SELECT address FROM tb_retailer_info
            WHERE retailer_account_id = #{retailerId};
            """)
    String getRetailerAddressById(Integer retailerId);

    @Select("""
            SELECT primary_phone_number
            FROM tb_retailer_info
            WHERE retailer_account_id = #{retailerId};
            """)
    String getRetailerPhoneById(Integer retailerId);


    @Select("""
            SELECT email FROM tb_retailer_account
            WHERE id = #{id};
            """)
    String getRetailerEmailById(Integer id);
}