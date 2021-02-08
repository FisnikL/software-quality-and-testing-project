package com.finki.bnks.project.server.domain;

public enum AuthenticationFlow {
    NOT_AUTHENTICATED, AUTHENTICATED, TOTP, TOTP_ADDITIONAL_SECURITY
}
