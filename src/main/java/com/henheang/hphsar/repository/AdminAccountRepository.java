package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.appUser.AdminUserSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AdminAccountRepository — read/manage supplier & buyer accounts as an ADMIN.
 * <p>
 * Separate from AppUserRepository (which backs registration/login) because
 * these queries join the profile tables (tb_supplier_info/tb_buyer_info) for
 * an admin-facing summary, and admin has no ownership scoping (sees all rows).
 */
@Mapper
public interface AdminAccountRepository {

    List<AdminUserSummaryDto> listSuppliers(@Param("search") String search,
                                             @Param("pageNumber") Integer pageNumber,
                                             @Param("pageSize") Integer pageSize);
    long countSuppliers(@Param("search") String search);
    AdminUserSummaryDto findSupplierById(@Param("id") Integer id);
    Integer updateSupplierActiveStatus(@Param("id") Integer id, @Param("isActive") boolean isActive);

    List<AdminUserSummaryDto> listBuyers(@Param("search") String search,
                                          @Param("pageNumber") Integer pageNumber,
                                          @Param("pageSize") Integer pageSize);
    long countBuyers(@Param("search") String search);
    AdminUserSummaryDto findBuyerById(@Param("id") Integer id);
    Integer updateBuyerActiveStatus(@Param("id") Integer id, @Param("isActive") boolean isActive);
}
