package com.finki.bnks.project.server.controller;

import com.finki.bnks.project.server.domain.signup.SignupResponse;
import com.finki.bnks.project.server.service.SignupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

@RestController
public class SignupController {
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @RequestParam("username") @NotEmpty String username,
            @RequestParam("password") @NotEmpty String password,
            @RequestParam("totp") boolean totp) {
        return ResponseEntity.ok(signupService.signup(username, password, totp));
    }

    @PostMapping("/signup-confirm-secret")
    public ResponseEntity<Boolean> signupConfirmSecret(
            @RequestParam("username") String username,
            @RequestParam("code") @NotEmpty String code){

        return ResponseEntity.ok(signupService.signupConfirmSecret(username, code));
    }
}
