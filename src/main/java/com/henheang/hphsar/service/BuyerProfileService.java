package com.henheang.hphsar.service;

import com.henheang.hphsar.model.buyer.Buyer;
import com.henheang.hphsar.model.buyer.BuyerRequest;

import java.text.ParseException;

public interface BuyerProfileService {
    Buyer createBuyerProfile(Integer currentUserId, BuyerRequest buyerRequest);
    Buyer getBuyerProfile(Integer currentUserId) throws ParseException;
    Buyer updateBuyerProfile(Integer currentUserId, BuyerRequest buyerRequest);
}
