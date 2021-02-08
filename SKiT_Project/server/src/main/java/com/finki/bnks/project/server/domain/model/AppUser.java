package com.finki.bnks.project.server.domain.model;

import javax.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(name = "password_hash")
    private String passwordHash;
    private String secret;
    private Boolean enabled;
    @Column(name = "additional_security")
    private Boolean additionalSecurity;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, String secret, Boolean enabled, Boolean additionalSecurity) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.secret = secret;
        this.enabled = enabled;
        this.additionalSecurity = additionalSecurity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAdditionalSecurity() {
        return additionalSecurity;
    }

    public void setAdditionalSecurity(Boolean additionalSecurity) {
        this.additionalSecurity = additionalSecurity;
    }
}
