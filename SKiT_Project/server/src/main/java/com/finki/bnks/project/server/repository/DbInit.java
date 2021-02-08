package com.finki.bnks.project.server.repository;

import com.finki.bnks.project.server.domain.model.AppUser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DbInit implements CommandLineRunner {

    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;

    public DbInit(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // remove data
        this.appUserRepository.deleteAll();

        // Add users
        AppUser admin = new AppUser("admin", passwordEncoder.encode("admin"), "W4AU5VIXXCPZ3S6T", true, false);
        AppUser user = new AppUser("user", passwordEncoder.encode("user"), "LRVLAZ4WVFOU3JBF", true, false);
        AppUser lazy = new AppUser("lazy", passwordEncoder.encode("lazy"), null, true, false);

        this.appUserRepository.save(admin);
        this.appUserRepository.save(user);
        this.appUserRepository.save(lazy);
    }
}
