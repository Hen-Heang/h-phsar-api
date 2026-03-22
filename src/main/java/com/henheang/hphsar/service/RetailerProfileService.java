package com.henheang.hphsar.service;

import com.henheang.hphsar.model.retailer.Retailer;
import com.henheang.hphsar.model.retailer.RetailerRequest;

import java.text.ParseException;

public interface RetailerProfileService {
    Retailer createRetailerProfile(Integer currentUserId, RetailerRequest retailerRequest);
    Retailer getRetailerProfile(Integer currentUserId) throws ParseException;
    Retailer updateRetailerProfile(Integer currentUserId, RetailerRequest retailerRequest);
}
