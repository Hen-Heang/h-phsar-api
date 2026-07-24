package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AdminUserSummaryDto;
import com.henheang.hphsar.repository.AdminAccountRepository;
import com.henheang.hphsar.service.AdminAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    private final AdminAccountRepository adminAccountRepository;

    public AdminAccountServiceImpl(AdminAccountRepository adminAccountRepository) {
        this.adminAccountRepository = adminAccountRepository;
    }

    @Override
    public List<AdminUserSummaryDto> listSuppliers(String search, Integer pageNumber, Integer pageSize) {
        validatePaging(pageNumber, pageSize);
        return adminAccountRepository.listSuppliers(search, pageNumber, pageSize);
    }

    @Override
    public long countSuppliers(String search) {
        return adminAccountRepository.countSuppliers(search);
    }

    @Override
    public AdminUserSummaryDto getSupplierById(Integer id) {
        AdminUserSummaryDto supplier = adminAccountRepository.findSupplierById(id);
        if (supplier == null) {
            throw new NotFoundException("Supplier not found.");
        }
        return supplier;
    }

    @Override
    @Transactional
    public AdminUserSummaryDto updateSupplierActiveStatus(Integer id, Boolean isActive) {
        if (isActive == null) {
            throw new BadRequestException("isActive is required.");
        }
        Integer rowsAffected = adminAccountRepository.updateSupplierActiveStatus(id, isActive);
        if (rowsAffected == null || rowsAffected == 0) {
            throw new NotFoundException("Supplier not found.");
        }
        return getSupplierById(id);
    }

    @Override
    public List<AdminUserSummaryDto> listBuyers(String search, Integer pageNumber, Integer pageSize) {
        validatePaging(pageNumber, pageSize);
        return adminAccountRepository.listBuyers(search, pageNumber, pageSize);
    }

    @Override
    public long countBuyers(String search) {
        return adminAccountRepository.countBuyers(search);
    }

    @Override
    public AdminUserSummaryDto getBuyerById(Integer id) {
        AdminUserSummaryDto buyer = adminAccountRepository.findBuyerById(id);
        if (buyer == null) {
            throw new NotFoundException("Buyer not found.");
        }
        return buyer;
    }

    @Override
    @Transactional
    public AdminUserSummaryDto updateBuyerActiveStatus(Integer id, Boolean isActive) {
        if (isActive == null) {
            throw new BadRequestException("isActive is required.");
        }
        Integer rowsAffected = adminAccountRepository.updateBuyerActiveStatus(id, isActive);
        if (rowsAffected == null || rowsAffected == 0) {
            throw new NotFoundException("Buyer not found.");
        }
        return getBuyerById(id);
    }

    private void validatePaging(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1 || pageSize == null || pageSize < 1) {
            throw new BadRequestException("pageNumber and pageSize must be positive integers.");
        }
    }
}
