package com.henheang.hphsar.service;

import com.henheang.hphsar.model.appUser.AdminUserSummaryDto;

import java.util.List;

public interface AdminAccountService {
    List<AdminUserSummaryDto> listSuppliers(String search, Integer pageNumber, Integer pageSize);
    long countSuppliers(String search);
    AdminUserSummaryDto getSupplierById(Integer id);
    AdminUserSummaryDto updateSupplierActiveStatus(Integer id, Boolean isActive);

    List<AdminUserSummaryDto> listBuyers(String search, Integer pageNumber, Integer pageSize);
    long countBuyers(String search);
    AdminUserSummaryDto getBuyerById(Integer id);
    AdminUserSummaryDto updateBuyerActiveStatus(Integer id, Boolean isActive);
}
