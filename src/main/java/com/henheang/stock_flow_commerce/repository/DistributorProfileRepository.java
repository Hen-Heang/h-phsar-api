package com.henheang.stock_flow_commerce.repository;

import com.henheang.stock_flow_commerce.model.distributor.Distributor;
import com.henheang.stock_flow_commerce.model.distributor.DistributorRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DistributorProfileRepository {


    //for getting addition phone number by distributor info id
    @Select("""
            select phone_number as additionalPhoneNUmber
            from tb_distributor_phone where distributor_info_id= #{id};
            """)
    List<String> getAdditionalPhoneNumberByDistributorInfoId(Integer id);

    @Select("""
            select id as id, distributor_account_id As distributorAccountId, first_name As firstName, last_name as lastName,
            profile_image as profileImage, created_date as createdDate, updated_date as updatedDate ,gender from tb_distributor_info
            where distributor_account_id = #{currentUserId};
            """)
    @Result(property = "id", column = "id")
    Distributor getUserProfile(Integer currentUserId);

    //create distributor info profile and return distributor info id in real time
    @Select("""
            INSERT INTO tb_distributor_info(id, distributor_account_id, first_name, last_name, gender, profile_image, created_date, updated_date)
            VALUES (default, #{currentUserId}, #{dis.firstName}, #{dis.lastName}, #{dis.gender}, #{dis.profileImage}, default, default)
            RETURNING id, distributor_account_id AS distributorAccountId, first_name AS firstName, last_name AS lastName, gender, profile_image AS profileImage, created_date AS createdDate, updated_date AS updatedDate;
            """)
    Distributor insertDistributorInfo(Integer currentUserId, @Param("dis") DistributorRequest distributorRequest);


    @Select("""
            UPDATE tb_distributor_info
            SET first_name=#{dis.firstName}, last_name=#{dis.lastName}, gender=#{dis.gender},
            profile_image= #{dis.profileImage}, updated_date = now()
            WHERE distributor_account_id= #{currentUserId}
            RETURNING id, distributor_account_id AS distributorAccountId, first_name AS firstName, last_name AS lastName, gender, profile_image AS profileImage, created_date AS createdDate, updated_date AS updatedDate;
            """)
    Distributor updateUserProfile(Integer currentUserId, @Param("dis") DistributorRequest distributorRequest);


    @Insert("""
            insert into tb_distributor_phone
            values (default,#{infoId},#{additionalPhoneNumber})
            """)
    void addAdditionalPhoneNumber(Integer infoId, String additionalPhoneNumber);

    @Select("""
            select exists(SELECT * FROM tb_distributor_info where distributor_account_id = #{currentUserId})
            """)
    boolean checkIfUserProfileIsCreated(Integer currentUserId);

    @Select("""
            delete from tb_distributor_phone where distributor_info_id=#{infoId}
                                                  """)
    void deleteAdditioanlPhoneNumber(Integer infoId);


    @Select("""
            select id from tb_distributor_info where distributor_account_id=#{currentUserId}
                      
            """)
    Integer getDistributorInfoId(Integer currentUserId);

    @Select("""
            select exists(select * from tb_distributor_phone where phone_number = #{additionalPhoneNumber} )
            """)
    boolean checkIfAdditionalPhoneNumberExist(String additionalPhoneNumber);

    @Select("""
            SELECT distributor_account_id FROM tb_store
            WHERE id = #{storeId};
            """)
    Integer getDistributorIdByStoreId(Integer storeId);
}
