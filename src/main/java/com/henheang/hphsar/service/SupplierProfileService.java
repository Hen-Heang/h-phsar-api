package com.henheang.hphsar.service;

import com.henheang.hphsar.model.supplier.Supplier;
import com.henheang.hphsar.model.supplier.SupplierRequest;
import java.text.ParseException;

public interface SupplierProfileService {

       Supplier getUserProfile(Integer currentUserId) throws ParseException;

       Supplier addUserProfile(Integer currentUserId, SupplierRequest supplierRequest) throws ParseException;

       Supplier updateUserProfile(Integer currentUserId, SupplierRequest supplierRequest) throws ParseException;
}
