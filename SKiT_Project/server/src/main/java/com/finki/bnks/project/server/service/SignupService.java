package com.finki.bnks.project.server.service;

import com.codahale.passpol.PasswordPolicy;
import com.codahale.passpol.Status;
import com.finki.bnks.project.server.domain.model.AppUser;
import com.finki.bnks.project.server.domain.signup.SignupResponse;
import com.finki.bnks.project.server.domain.signup.SignupStatus;
import com.finki.bnks.project.server.repository.AppUserRepository;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    private final AppUserRepository appUserRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(SignupService.class);

    public SignupService(AppUserRepository appUserRepository, PasswordPolicy passwordPolicy, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
    }

    public SignupResponse signup(String username, String password, boolean totp) {
        // cancel if the user is already registered
        logger.info("Signup Request: username = [" + username + "], password = [" + password + "], totp = [" + totp + "]");
        AppUser appUser = this.appUserRepository.findByUsername(username).get();
        if(appUser != null){
            return new SignupResponse(SignupStatus.USERNAME_TAKEN);
        }

        Status status = this.passwordPolicy.check(password);
        if(status != Status.OK){
            return new SignupResponse(SignupStatus.WEAK_PASSWORD);
        }

        if(totp){
            String secret = Base32.random();

            AppUser newUser = new AppUser(username, this.passwordEncoder.encode(password), secret, false, false);

            this.appUserRepository.save(newUser);

            return new SignupResponse(SignupStatus.OK, username, secret);
        }

        AppUser newUser = new AppUser(username, this.passwordEncoder.encode(password), null, true, false);
        this.appUserRepository.save(newUser);

        return new SignupResponse(SignupStatus.OK);
    }


    public boolean signupConfirmSecret(String username, String code) {
        AppUser appUser = this.appUserRepository.findByUsername(username).orElse(null);
        if(appUser != null){
            String secret = appUser.getSecret();
            Totp totp = new Totp(secret);
            if(totp.verify(code)){
                appUser.setEnabled(true);
                this.appUserRepository.save(appUser);
                return true;
            }
        }
        return false;
    }
}
