package com.henheang.stock_flow_commerce.service;

import com.henheang.stock_flow_commerce.model.distributor.Distributor;
import com.henheang.stock_flow_commerce.model.distributor.DistributorRequest;
import java.text.ParseException;

public interface DistributorProfileService {

       Distributor getUserProfile(Integer currentUserId) throws ParseException;

       Distributor addUserProfile(Integer currentUserId, DistributorRequest distributorRequest) throws ParseException;

       Distributor updateUserProfile(Integer currentUserId, DistributorRequest distributorRequest) throws ParseException;
}
