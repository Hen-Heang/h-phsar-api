package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.supplier.Supplier;
import com.henheang.hphsar.model.supplier.SupplierRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SupplierProfileRepository {


    //for getting addition phone number by supplier info id
    List<String> getAdditionalPhoneNumberBySupplierInfoId(Integer id);

    Supplier getUserProfile(Integer currentUserId);

    //create supplier info profile and return supplier info id in real time
    Supplier insertSupplierInfo(Integer currentUserId, @Param("dis") SupplierRequest supplierRequest);

    Supplier updateUserProfile(Integer currentUserId, @Param("dis") SupplierRequest supplierRequest);

    void addAdditionalPhoneNumber(Integer infoId, String additionalPhoneNumber);

    boolean checkIfUserProfileIsCreated(Integer currentUserId);

    void deleteAdditioanlPhoneNumber(Integer infoId);

    Integer getSupplierInfoId(Integer currentUserId);

    boolean checkIfAdditionalPhoneNumberExist(String additionalPhoneNumber);

    Integer getSupplierIdByStoreId(Integer storeId);
}