package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.NotFoundException;
import com.henheang.hphsar.model.appUser.AdminUserSummaryDto;
import com.henheang.hphsar.repository.AdminAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Pure unit coverage of AdminAccountServiceImpl with a mocked repository —
 * no Spring context, no database. Mapper/pagination SQL correctness belongs
 * in a Testcontainers IT instead (see AdminAccountMapper.xml).
 */
@ExtendWith(MockitoExtension.class)
class AdminAccountServiceImplTest {

    @Mock
    private AdminAccountRepository adminAccountRepository;

    @InjectMocks
    private AdminAccountServiceImpl adminAccountService;

    @Test
    void updateSupplierActiveStatus_throwsNotFound_whenNoRowsAffected() {
        when(adminAccountRepository.updateSupplierActiveStatus(1, true)).thenReturn(0);

        assertThrows(NotFoundException.class,
                () -> adminAccountService.updateSupplierActiveStatus(1, true));
    }

    @Test
    void updateSupplierActiveStatus_throwsBadRequest_whenIsActiveNull() {
        assertThrows(BadRequestException.class,
                () -> adminAccountService.updateSupplierActiveStatus(1, null));

        verify(adminAccountRepository, never()).updateSupplierActiveStatus(any(), anyBoolean());
    }

    @Test
    void updateSupplierActiveStatus_returnsUpdatedRow_whenRowAffected() {
        AdminUserSummaryDto expected = new AdminUserSummaryDto();
        expected.setId(1);
        when(adminAccountRepository.updateSupplierActiveStatus(1, false)).thenReturn(1);
        when(adminAccountRepository.findSupplierById(1)).thenReturn(expected);

        AdminUserSummaryDto result = adminAccountService.updateSupplierActiveStatus(1, false);

        assertSame(expected, result);
    }

    @Test
    void getSupplierById_throwsNotFound_whenMissing() {
        when(adminAccountRepository.findSupplierById(99)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> adminAccountService.getSupplierById(99));
    }

    @Test
    void listSuppliers_throwsBadRequest_whenPageNumberInvalid() {
        assertThrows(BadRequestException.class,
                () -> adminAccountService.listSuppliers(null, 0, 10));

        verifyNoInteractions(adminAccountRepository);
    }

    @Test
    void updateBuyerActiveStatus_throwsNotFound_whenNoRowsAffected() {
        when(adminAccountRepository.updateBuyerActiveStatus(2, true)).thenReturn(0);

        assertThrows(NotFoundException.class,
                () -> adminAccountService.updateBuyerActiveStatus(2, true));
    }
}
