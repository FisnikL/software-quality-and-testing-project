package com.finki.bnks.project.server.config;

import com.codahale.passpol.BreachDatabase;
import com.codahale.passpol.PasswordPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception{
        return authentication -> {
            throw new AuthenticationServiceException("Cannot authenticate " + authentication);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new Argon2PasswordEncoder(16, 32, 8, 1 << 16, 4);
    }

    @Bean
    public PasswordPolicy passwordPolicy(){
        return new PasswordPolicy(BreachDatabase.top100K(), 8, 256);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable())
                .authorizeRequests(
                customizer -> {
                    customizer
                            .antMatchers(
                                    "/authenticate", "/signin", "/verify-totp", "/totp-shift",
                                    "/verify-totp-additional-security", "/signup", "/signup-confirm-secret",
                                    "/welcome"
                                    ,"/h2-console/**"
                            )
                            .permitAll()
                            .anyRequest()
                            .authenticated();
                }).logout(customizer -> customizer.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));
    }

    @Override
    public void configure(WebSecurity web){
        web.ignoring().antMatchers("/", "/assets/**/*", "/svg/**/*", "/*.br", "/*.gz",
                "/*.html", "/*.js", "/*.css", "/*.woff2", "/*.ttf", "/*.eot",
                "/*.svg", "/*.woff", "/*.ico");
    }
}
