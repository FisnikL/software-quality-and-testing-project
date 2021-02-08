package com.finki.bnks.project.server.controller;

import com.finki.bnks.project.server.domain.signup.SignupResponse;
import com.finki.bnks.project.server.domain.signup.SignupStatus;
import com.finki.bnks.project.server.service.SignupService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class SignupControllerTest { ;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SignupService signupService;

    @Test
    public void shouldReturnUsernameTaken() throws Exception {
        // given
        given(signupService.signup(eq("username_taken"), eq("password"), eq(false)))
            .willReturn(new SignupResponse(SignupStatus.USERNAME_TAKEN));

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signup");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "username_taken");
        request.param("password", "password");
        request.param("totp", "false");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(new SignupResponse(SignupStatus.USERNAME_TAKEN).toString());
    }

    @Test
    public void shouldReturnWeakPassword() throws Exception {
        // given
        given(signupService.signup(eq("username"), eq("weak_password"), eq(false)))
            .willReturn(new SignupResponse(SignupStatus.WEAK_PASSWORD));

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signup");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "username");
        request.param("password", "weak_password");
        request.param("totp", "false");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(new SignupResponse(SignupStatus.WEAK_PASSWORD).toString());
    }

    @Test
    public void shouldSignUpSuccessfullyWithoutTotp() throws Exception {
        // given
        given(signupService.signup(eq("username"), eq("password"), eq(false)))
            .willReturn(new SignupResponse(SignupStatus.OK));

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signup");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "username");
        request.param("password", "password");
        request.param("totp", "false");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(new SignupResponse(SignupStatus.OK).toString());
    }

    @Test
    public void shouldSignupSuccessfullyWithTotp() throws Exception {
        // given
        given(signupService.signup(eq("username"), eq("password"), eq(true)))
            .willReturn(new SignupResponse(SignupStatus.OK, "username", "secret"));

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signup");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "username");
        request.param("password", "password");
        request.param("totp", "true");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(new SignupResponse(SignupStatus.OK, "username", "secret").toString());
    }

    @Test
    public void shouldConfirmSecretAfterSignUpSuccessfully() throws Exception {
        // given
        given(signupService.signupConfirmSecret(eq("username"), eq("true_code")))
            .willReturn(true);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signup-confirm-secret");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "username");
        request.param("code", "true_code");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("true");
    }


    @Test
    public void shouldConfirmSecretAfterSignUpUnSuccessfully() throws Exception {
        // given
        given(signupService.signupConfirmSecret(eq("username"), eq("false_code")))
            .willReturn(false);

        // when
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/signup-confirm-secret");
        request.contentType(MediaType.APPLICATION_JSON);
        request.param("username", "username");
        request.param("code", "false_code");

        MockHttpServletResponse response = mvc.perform(request)
            .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("false");
    }
}
