package com.finki.bnks.project.server.controller;

import com.finki.bnks.project.server.controller.context.Context;
import com.finki.bnks.project.server.domain.AuthenticationFlow;
import com.finki.bnks.project.server.security.AppUserAuthentication;
import com.finki.bnks.project.server.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class AuthController {

    private final AuthService authService;
    private final Context context;

    public AuthController(AuthService authService, Context context) {
        this.authService = authService;
        this.context = context;
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome(){
        return ResponseEntity.ok("Welcome");
    }

    @GetMapping("/authenticate")
    public AuthenticationFlow authenticate(HttpServletRequest request){
        Authentication auth = context.getAuthentication();
        if(auth instanceof AppUserAuthentication){
            return AuthenticationFlow.AUTHENTICATED;
        }

        HttpSession httpSession = request.getSession(false);
        if(httpSession != null){
            httpSession.invalidate();
        }

        return AuthenticationFlow.NOT_AUTHENTICATED;
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthenticationFlow> login(@RequestParam String username, @RequestParam String password, HttpSession httpSession){
        return ResponseEntity.ok(authService.login(username, password, httpSession));
    }

    @PostMapping("/verify-totp")
    public ResponseEntity<AuthenticationFlow> totp(@RequestParam String code, HttpSession httpSession){
        return ResponseEntity.ok(authService.totp(code, httpSession));
    }

    @PostMapping("/verify-totp-additional-security")
    public ResponseEntity<AuthenticationFlow> verifyTotpAdditionalSecurity(
            @RequestParam String code1,
            @RequestParam String code2,
            @RequestParam String code3,
            HttpSession httpSession
    ){
        return ResponseEntity.ok(authService.verifyTotpAdditionalSecurity(code1, code2, code3, httpSession));
    }

    @GetMapping("/totp-shift")
    public ResponseEntity<String> getTotpShift(HttpSession httpSession){
        return ResponseEntity.ok(authService.getTotpShift(httpSession));
    }
}
