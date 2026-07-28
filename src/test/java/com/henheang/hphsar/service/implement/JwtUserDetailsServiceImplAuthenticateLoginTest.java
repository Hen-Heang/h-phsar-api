package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.repository.AppUserRepository;
import com.henheang.hphsar.repository.OtpRepository;
import com.henheang.hphsar.service.OtpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit coverage of JwtUserDetailsServiceImpl#authenticateLogin — mocked
 * repositories/AuthenticationManager, no Spring context, no database.
 * <p>
 * Guards against email enumeration: an unregistered email and a registered
 * email with a wrong password must throw the exact same message, so a caller
 * can't tell which emails have accounts from the login response alone.
 */
@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceImplAuthenticateLoginTest {

    private static final String GENERIC_MESSAGE = "Invalid email or password.";

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private OtpRepository otpRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private OtpService otpService;

    private JwtUserDetailsServiceImpl service() {
        return new JwtUserDetailsServiceImpl(
                appUserRepository, otpRepository, passwordEncoder, authenticationManager, otpService);
    }

    @Test
    void authenticateLogin_unknownEmail_throwsGenericMessage() {
        when(appUserRepository.getVerifySupplierEmail("nobody@example.test")).thenReturn(null);
        when(appUserRepository.getVerifyBuyerEmail("nobody@example.test")).thenReturn(null);
        when(appUserRepository.getVerifyAdminEmail("nobody@example.test")).thenReturn(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service().authenticateLogin("nobody@example.test", "whatever"));

        assertEquals(GENERIC_MESSAGE, ex.getMessage());
    }

    @Test
    void authenticateLogin_wrongPassword_throwsTheSameGenericMessageAsUnknownEmail() throws Exception {
        when(appUserRepository.getVerifySupplierEmail("real@example.test")).thenReturn(true);
        doThrow(new BadCredentialsException("bad creds"))
                .when(authenticationManager).authenticate(any());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service().authenticateLogin("real@example.test", "wrong-password"));

        assertEquals(GENERIC_MESSAGE, ex.getMessage());
    }

    @Test
    void authenticateLogin_unverifiedEmail_sendsOtpAndThrowsConflict() {
        when(appUserRepository.getVerifySupplierEmail("unverified@example.test")).thenReturn(false);

        assertThrows(ConflictException.class,
                () -> service().authenticateLogin("unverified@example.test", "whatever"));

        verify(otpService).generateOtp(eq("unverified@example.test"));
    }
}
