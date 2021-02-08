package com.finki.bnks.project.server.service;

import com.finki.bnks.project.server.domain.AuthenticationFlow;
import com.finki.bnks.project.server.domain.model.AppUser;
import com.finki.bnks.project.server.repository.AppUserRepository;
import com.finki.bnks.project.server.security.AppUserAuthentication;
import com.finki.bnks.project.server.security.AppUserDetail;
import com.finki.bnks.project.server.security.custom_totp.CustomTotp;
import com.finki.bnks.project.server.security.custom_totp.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final static String USER_AUTHENTICATION_OBJECT = "USER_AUTHENTICATION_OBJECT";
    private final static String USER_NOTFOUND_ENCODED_PASSWORD = "userNotFoundPassword";

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;
    //public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
    //    this.appUserRepository = appUserRepository;
    //    this.passwordEncoder = passwordEncoder;
    //}

    //public AuthService() {
    //
    //}

    public AuthenticationFlow login(String username, String password, HttpSession httpSession){

        AppUser appUser = this.appUserRepository.findByUsername(username).orElse(null);

        if(appUser != null){
            boolean pwMatches = this.passwordEncoder.matches(password, appUser.getPasswordHash());
            if(pwMatches && appUser.getEnabled().booleanValue()){
                AppUserDetail detail = new AppUserDetail(appUser);
                AppUserAuthentication userAuthentication = new AppUserAuthentication(detail);
                if(isNotBlank(appUser.getSecret())){
                    httpSession.setAttribute(USER_AUTHENTICATION_OBJECT, userAuthentication);
                    
                    if(isUserInAdditionalSecurityMode(detail.getAppUserId())){
                        return AuthenticationFlow.TOTP_ADDITIONAL_SECURITY;
                    }

                    return AuthenticationFlow.TOTP;
                }

                SecurityContextHolder.getContext().setAuthentication(userAuthentication);
                return AuthenticationFlow.AUTHENTICATED;
            }
        }
        else{
            this.passwordEncoder.matches(password, this.USER_NOTFOUND_ENCODED_PASSWORD);
        }

        return AuthenticationFlow.NOT_AUTHENTICATED;
    }

    public AuthenticationFlow totp(String code, HttpSession httpSession) {
        AppUserAuthentication userAuthentication = (AppUserAuthentication) httpSession.getAttribute(USER_AUTHENTICATION_OBJECT);

        if(userAuthentication == null){
            return AuthenticationFlow.NOT_AUTHENTICATED;
        }

        AppUserDetail detail = (AppUserDetail) userAuthentication.getPrincipal();
        if(isUserInAdditionalSecurityMode(detail.getAppUserId())){
            return AuthenticationFlow.TOTP_ADDITIONAL_SECURITY;
        }

        String secret = ((AppUserDetail) userAuthentication.getPrincipal()).getSecret();
        if(isNotBlank(secret) && isNotBlank(code)){
            //CustomTotp totp = new CustomTotp(secret);
            //if(totp.verify(code, 2, 2).isValid()){
            if(totpService.isTotpValid(secret, code, 2, 2)){
                SecurityContextHolder.getContext().setAuthentication(userAuthentication);
                return AuthenticationFlow.AUTHENTICATED;
            }
            setAdditionalSecurityFlag(detail.getAppUserId());
            return AuthenticationFlow.TOTP_ADDITIONAL_SECURITY;
        }

        return AuthenticationFlow.NOT_AUTHENTICATED;
    }

    public AuthenticationFlow verifyTotpAdditionalSecurity(String code1, String code2, String code3, HttpSession httpSession) {
        AppUserAuthentication userAuthentication = (AppUserAuthentication) httpSession.getAttribute(USER_AUTHENTICATION_OBJECT);
        if(userAuthentication == null){
            return AuthenticationFlow.NOT_AUTHENTICATED;
        }

        if(code1.equals(code2) || code1.equals(code3) || code2.equals(code3)){
            return AuthenticationFlow.NOT_AUTHENTICATED;
        }

        String secret = ((AppUserDetail) userAuthentication.getPrincipal()).getSecret();
        if(isNotBlank(secret) && isNotBlank(code1) && isNotBlank(code2) && isNotBlank(code3)){
            Result result = totpService.isCustomTotpValid(secret, List.of(code1, code2, code3));
            if (result.isValid()) {
                if (result.getShift() > 2 || result.getShift() < -2) {
                    httpSession.setAttribute("totp-shift", result.getShift());
                }

                AppUserDetail detail = (AppUserDetail) userAuthentication.getPrincipal();
                clearAdditionalSecurityFlag(detail.getAppUserId());
                httpSession.removeAttribute(USER_AUTHENTICATION_OBJECT);

                SecurityContextHolder.getContext().setAuthentication(userAuthentication);
                return AuthenticationFlow.AUTHENTICATED;
            }
        }
        return AuthenticationFlow.NOT_AUTHENTICATED;
    }

    public String getTotpShift(HttpSession httpSession) {
        Long shift = (Long) httpSession.getAttribute("totp-shift");
        if (shift == null) {
            return null;
        }
        httpSession.removeAttribute("totp-shift");

        StringBuilder out = new StringBuilder();
        long total30Seconds = (int) Math.abs(shift);
        long hours = total30Seconds / 120;
        total30Seconds = total30Seconds % 120;
        long minutes = total30Seconds / 2;
        boolean seconds = total30Seconds % 2 != 0;

        if (hours == 1) {
            out.append("1 hour ");
        }
        else if (hours > 1) {
            out.append(hours).append(" hours ");
        }

        if (minutes == 1) {
            out.append("1 minute ");
        }
        else if (minutes > 1) {
            out.append(minutes).append(" minutes ");
        }

        if (seconds) {
            out.append("30 seconds ");
        }

        return out.append(shift < 0 ? "behind" : "ahead").toString();
    }

    private boolean isNotBlank(String secret) {
        return secret != null && !secret.isBlank();
    }

    private boolean isUserInAdditionalSecurityMode(Long appUserId) {
        return this.appUserRepository.findById(appUserId).get().getAdditionalSecurity();
    }

    private void setAdditionalSecurityFlag(Long appUserId) {
        AppUser appUser = this.appUserRepository.findById(appUserId).get();
        appUser.setAdditionalSecurity(true);
        this.appUserRepository.save(appUser);
    }

    private void clearAdditionalSecurityFlag(Long appUserId) {
        AppUser appUser = this.appUserRepository.findById(appUserId).get();
        appUser.setAdditionalSecurity(false);
        this.appUserRepository.save(appUser);
    }


}
