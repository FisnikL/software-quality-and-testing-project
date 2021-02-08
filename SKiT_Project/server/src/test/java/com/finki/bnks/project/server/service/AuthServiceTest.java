package com.finki.bnks.project.server.service;

import com.finki.bnks.project.server.domain.AuthenticationFlow;
import com.finki.bnks.project.server.domain.model.AppUser;
import com.finki.bnks.project.server.repository.AppUserRepository;
import com.finki.bnks.project.server.security.AppUserAuthentication;
import com.finki.bnks.project.server.security.AppUserDetail;
import com.finki.bnks.project.server.security.custom_totp.Result;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @TestConfiguration
    static class AuthServiceImplTestContextConfiguration{
        @Bean
        public AuthService authService() {
            return new AuthService();
        }
    }

    @Autowired
    private AuthService authService;

    @MockBean
    private AppUserRepository mockAppUserRepository;

    @MockBean
    private PasswordEncoder mockPasswordEncoder;

    @MockBean
    private TotpService totpService;


    private final static String USER_NOTFOUND_ENCODED_PASSWORD = "userNotFoundPassword";
    private final static String USER_AUTHENTICATION_OBJECT = "USER_AUTHENTICATION_OBJECT";


    @Test
    public void testUserDoesntExist() throws Exception {

        AuthenticationFlow authenticationFlow = authService.login("username", "password", null);

        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldLoginWithTotpAdditionalSecurity() throws Exception {
         //given
        AppUser appUser = new AppUser("username", "passwordHash", "secret", true, true);
        given(mockAppUserRepository.findByUsername(anyString())).willReturn(Optional.of(appUser));
        given(mockPasswordEncoder.matches(eq("passwordHash"), eq("passwordHash"))).willReturn(true);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "", true, true)));

        // when
        AuthenticationFlow authenticationFlow = authService.login("username", "passwordHash", new MockHttpSession());

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.TOTP_ADDITIONAL_SECURITY);
    }

    @Test
    public void shouldLoginWithTotp() throws Exception {
        //given
        AppUser appUser = new AppUser("username", "passwordHash", "secret", true, false);
        given(mockAppUserRepository.findByUsername(anyString())).willReturn(Optional.of(appUser));
        given(mockPasswordEncoder.matches(eq("passwordHash"), eq("passwordHash"))).willReturn(true);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "", true, false)));

        // when
        AuthenticationFlow authenticationFlow = authService.login("username", "passwordHash", new MockHttpSession());

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.TOTP);
    }

    @Test
    public void shouldLoginWithoutTotp() throws Exception {
        //given
        AppUser appUser = new AppUser("username", "passwordHash", "", true, false);
        given(mockAppUserRepository.findByUsername(anyString())).willReturn(Optional.of(appUser));
        given(mockPasswordEncoder.matches(eq("passwordHash"), eq("passwordHash"))).willReturn(true);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "", true, false)));

        // when
        AuthenticationFlow authenticationFlow = authService.login("username", "passwordHash", new MockHttpSession());

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
    }


    @Test
    public void shouldReturnNotAuthenticated_HttpSessionDoesntContainTheUser() throws Exception {

        // when
        AuthenticationFlow authenticationFlow = authService.totp("code", new MockHttpSession());

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldReturntotpAdditionalSecurity_AdditionalSecurityFlagIsTrue() throws Exception {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "", true, true)));

        // when
        AuthenticationFlow authenticationFlow = authService.totp("code", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.TOTP_ADDITIONAL_SECURITY);
    }

    @Test
    public void shouldReturnTotpAuthenticated_SecretIsBlank() throws Exception {
        // given
        AppUser appUser = new AppUser("username", "password", "", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "", true, false)));

        // when
        AuthenticationFlow authenticationFlow = authService.totp("code", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldReturnAuthenticated_TotpIsValid() throws Exception {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, false)));
        given(totpService.isTotpValid(anyString(), anyString(), anyInt(), anyInt()))
            .willReturn(true);

        // when
        AuthenticationFlow authenticationFlow = authService.totp("code", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
    }

    @Test
    public void shouldReturnNotAuthenticated_TotpIsNotValid() throws Exception {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, false)));
        given(totpService.isTotpValid(anyString(), anyString(), anyInt(), anyInt()))
            .willReturn(false);

        // when
        AuthenticationFlow authenticationFlow = authService.totp("code", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.TOTP_ADDITIONAL_SECURITY);
    }

    @Test
    public void shouldVerifyTotpAdditionalSecurity_NoUserInTheSession() {
        // given
        MockHttpSession mockHttpSession = new MockHttpSession();

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldReturnTotpAdditionalSecurity_Code1EqualsCode2() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code1", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }


    @Test
    public void shouldReturnTotpAdditionalSecurity_Code2EqualsCode3() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code2", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldReturnTotpAdditionalSecurity_Code1EqualsCode3() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code1", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldVerifyTotpAdditionalSecurity_SecretIsBlank() {
        // given
        AppUser appUser = new AppUser("username", "password", "", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldVerifyTotpAdditionalSecurity_Code1IsBlank() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldVerifyTotpAdditionalSecurity_Code2IsBlank() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldVerifyTotpAdditionalSecurity_Code3IsBlank() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.NOT_AUTHENTICATED);
    }

    @Test
    public void shouldVerifyTotpAdditionalSecuritySuccessfully_returnAuthenticated() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, true)));
        given(mockAppUserRepository.save(any(AppUser.class)))
            .willReturn(new AppUser("SAVED", "SAVED", "SAVED", true, false));
        given(totpService.isCustomTotpValid(anyString(), anyList()))
            .willReturn(new Result(true, 0));
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
    }

    @Test
    public void shouldReturnPlusTotpShiftPlural() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, true)));
        given(mockAppUserRepository.save(any(AppUser.class)))
            .willReturn(new AppUser("SAVED", "SAVED", "SAVED", true, false));
        given(totpService.isCustomTotpValid(anyString(), anyList()))
            .willReturn(new Result(true, 5));
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
        assertThat(authService.getTotpShift(mockHttpSession)).isEqualTo("2 minutes 30 seconds ahead");
    }

    @Test
    public void shouldReturnPlusTotpShiftSingular() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);

        // when
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, true)));
        given(mockAppUserRepository.save(any(AppUser.class)))
            .willReturn(new AppUser("SAVED", "SAVED", "SAVED", true, false));
        given(totpService.isCustomTotpValid(anyString(), anyList()))
            .willReturn(new Result(true, 3));
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
        assertThat(authService.getTotpShift(mockHttpSession)).isEqualTo("1 minute 30 seconds ahead");
    }

    @Test
    public void shouldReturnMinusTotpShiftPlural() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);
        // when
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, true)));
        given(mockAppUserRepository.save(any(AppUser.class)))
            .willReturn(new AppUser("SAVED", "SAVED", "SAVED", true, false));
        given(totpService.isCustomTotpValid(anyString(), anyList()))
            .willReturn(new Result(true, -5));
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
        assertThat(authService.getTotpShift(mockHttpSession)).isEqualTo("2 minutes 30 seconds behind");
    }


    @Test
    public void shouldReturnMinusTotpShiftSingular() {
        // given
        AppUser appUser = new AppUser("username", "password", "secret", true, true);
        AppUserDetail appUserDetail = new AppUserDetail(appUser);
        AppUserAuthentication appUserAuthentication = new AppUserAuthentication(appUserDetail);
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(USER_AUTHENTICATION_OBJECT, appUserAuthentication);
        // when
        given(mockAppUserRepository.findById(any()))
            .willReturn(Optional.of(new AppUser("", "", "secret", true, true)));
        given(mockAppUserRepository.save(any(AppUser.class)))
            .willReturn(new AppUser("SAVED", "SAVED", "SAVED", true, false));
        given(totpService.isCustomTotpValid(anyString(), anyList()))
            .willReturn(new Result(true, -3));
        AuthenticationFlow authenticationFlow = authService.verifyTotpAdditionalSecurity("code1", "code2", "code3", mockHttpSession);

        // then
        assertThat(authenticationFlow).isEqualTo(AuthenticationFlow.AUTHENTICATED);
        assertThat(authService.getTotpShift(mockHttpSession)).isEqualTo("1 minute 30 seconds behind");
    }
}
