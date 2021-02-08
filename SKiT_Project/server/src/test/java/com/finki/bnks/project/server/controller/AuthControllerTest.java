package com.finki.bnks.project.server.controller;

import com.finki.bnks.project.server.controller.context.Context;
import com.finki.bnks.project.server.domain.AuthenticationFlow;
import com.finki.bnks.project.server.security.AppUserAuthentication;
import com.finki.bnks.project.server.service.AuthService;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private Context context;


    @Test
    public void shouldReturnWelcome() throws Exception {
        // given

        // when
        MockHttpServletResponse response = mvc.perform(get("/welcome")
            .accept(MediaType.APPLICATION_JSON))
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("Welcome");
    }


    @Test
    public void shouldReturnAuthenticated() throws Exception {
        // given
        given(context.getAuthentication())
            .willReturn(new AppUserAuthentication(null));

        // when
        MockHttpServletResponse response = mvc.perform(get("/authenticate")
        .accept(MediaType.APPLICATION_JSON))
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.AUTHENTICATED + "\"");
    }

    @Test
    public void shouldReturnNotAuthenticated() throws Exception {
        // given
        given(context.getAuthentication())
            .willReturn(null);

        // when
        MockHttpServletResponse response = mvc.perform(get("/authenticate")
            .accept(MediaType.APPLICATION_JSON))
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.NOT_AUTHENTICATED + "\"");
    }

    @Test
    public void shouldLoginSuccessfully() throws Exception {
        // given
        given(authService.login(eq("authenticated"), eq("authenticated"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.AUTHENTICATED);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signin");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "authenticated");
        request.param("password", "authenticated");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.AUTHENTICATED + "\"");
    }

    @Test
    public void shouldLoginUnsuccessfully() throws Exception {
        // given
        given(authService.login(eq("notAuthenticated"), eq("notAuthenticated"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.NOT_AUTHENTICATED);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signin");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "notAuthenticated");
        request.param("password", "notAuthenticated");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.NOT_AUTHENTICATED + "\"");
    }

    @Test
    public void shouldLoginAndThenRequireTOTP() throws Exception {
        // given
        given(authService.login(eq("totp"), eq("totp"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.TOTP);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signin");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "totp");
        request.param("password", "totp");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.TOTP + "\"");
    }

    @Test
    public void shouldLoginAndRequireAdditionalTOTPSecurity() throws Exception {
        // given
        given(authService.login(eq("totpAdditional"), eq("totpAdditional"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.TOTP_ADDITIONAL_SECURITY);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signin");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "totpAdditional");
        request.param("password", "totpAdditional");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.TOTP_ADDITIONAL_SECURITY + "\"");
    }

    // verify-totp endpoint
    @Test
    public void shouldPassTOTPSuccessfully() throws Exception {
        // given
        given(authService.totp(eq("verifyTotpAuthenticated"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.AUTHENTICATED);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/verify-totp");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("code", "verifyTotpAuthenticated");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.AUTHENTICATED + "\"");
    }

    @Test
    public void shouldNotPassTOTPSuccessfully() throws Exception {
        // given
        given(authService.totp(eq("verifyTotpNotAuthenticated"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.NOT_AUTHENTICATED);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/verify-totp");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("code", "verifyTotpNotAuthenticated");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.NOT_AUTHENTICATED + "\"");
    }

    @Test
    public void shouldRequireTOTPAdditionalSecurity_InsertedTOTPIsWrong() throws Exception {
        // given
        given(authService.totp(eq("verifyTotpAdditional"), any(HttpSession.class)))
            .willReturn(AuthenticationFlow.TOTP_ADDITIONAL_SECURITY);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/verify-totp");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("code", "verifyTotpAdditional");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.TOTP_ADDITIONAL_SECURITY + "\"");
    }


    // verify-totp-additional-security
    @Test
    public void shouldPassAdditionalTOTPSecuritySuccessfully() throws Exception {
        // given
        given(authService.verifyTotpAdditionalSecurity(
            eq("verifyAdditionalTotpAuthenticated"),
            eq("verifyAdditionalTotpAuthenticated"),
            eq("verifyAdditionalTotpAuthenticated"),
            any(HttpSession.class))
        ).willReturn(AuthenticationFlow.AUTHENTICATED);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/verify-totp-additional-security");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("code1", "verifyAdditionalTotpAuthenticated");
        request.param("code2", "verifyAdditionalTotpAuthenticated");
        request.param("code3", "verifyAdditionalTotpAuthenticated");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.AUTHENTICATED + "\"");
    }

    @Test
    public void shouldNotPassAdditionalTOTPSecuritySuccessfully() throws Exception {
        // given
        given(authService.verifyTotpAdditionalSecurity(
            eq("verifyAdditionalTotpNotAuthenticated"),
            eq("verifyAdditionalTotpNotAuthenticated"),
            eq("verifyAdditionalTotpNotAuthenticated"),
            any(HttpSession.class))
        ).willReturn(AuthenticationFlow.NOT_AUTHENTICATED);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/verify-totp-additional-security");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("code1", "verifyAdditionalTotpNotAuthenticated");
        request.param("code2", "verifyAdditionalTotpNotAuthenticated");
        request.param("code3", "verifyAdditionalTotpNotAuthenticated");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("\"" + AuthenticationFlow.NOT_AUTHENTICATED + "\"");
    }

    // totp-shift
    @Test
    public void shouldReturnTOTP_SHIFT() throws Exception {
        // given
        given(authService.getTotpShift(any(HttpSession.class))).willReturn("TOTP_SHIFT");

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/totp-shift").accept(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("TOTP_SHIFT");
    }
}
