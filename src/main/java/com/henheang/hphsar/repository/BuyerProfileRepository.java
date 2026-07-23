package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.buyer.Buyer;
import com.henheang.hphsar.model.buyer.BuyerRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuyerProfileRepository {


    //get buyer info id in real time
    Integer createBuyerProfile(Integer currentUserId, @Param("re") BuyerRequest buyerRequest);

    void insertAdditionalPhoneNumber(Integer buyerInfoId, String additionalPhoneNumber);

    Buyer getBuyerProfile(Integer currentUserId);

    List<String> getAdditionalPhoneNumberByBuyerInfoId(Integer id);

    void updateBuyerProfile(Integer currentUserId, @Param("re") BuyerRequest buyerRequest);

    Integer getBuyerInfoId(Integer currentUserId);

    void deleteAdditionalPhoneNumber(Integer buyerInfoId);

    boolean checkIfBuyerProfileIsAlreadyCreated(Integer currentUserId);

    String getBuyerNameById(Integer buyerId);

    String getBuyerImageById(Integer buyerId);

    String getBuyerAddressById(Integer buyerId);

    String getBuyerPhoneById(Integer buyerId);

    String getBuyerEmailById(Integer id);
}