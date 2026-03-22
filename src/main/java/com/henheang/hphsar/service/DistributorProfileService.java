package com.henheang.hphsar.service;

import com.henheang.hphsar.model.distributor.Distributor;
import com.henheang.hphsar.model.distributor.DistributorRequest;
import java.text.ParseException;

public interface DistributorProfileService {

       Distributor getUserProfile(Integer currentUserId) throws ParseException;

       Distributor addUserProfile(Integer currentUserId, DistributorRequest distributorRequest) throws ParseException;

       Distributor updateUserProfile(Integer currentUserId, DistributorRequest distributorRequest) throws ParseException;
}
