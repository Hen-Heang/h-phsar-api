package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.retailer.Retailer;
import com.henheang.stock_flow_commerce.model.retailer.RetailerRequest;

import java.text.ParseException;

public interface RetailerProfileService {
    Retailer createRetailerProfile(Integer currentUserId, RetailerRequest retailerRequest);
    Retailer getRetailerProfile(Integer currentUserId) throws ParseException;
    Retailer updateRetailerProfile(Integer currentUserId, RetailerRequest retailerRequest);
}
