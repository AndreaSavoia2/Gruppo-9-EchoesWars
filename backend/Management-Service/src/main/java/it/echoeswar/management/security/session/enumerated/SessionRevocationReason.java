package it.echoeswar.management.security.session.enumerated;

/**
 * Identifies why an authentication session was revoked.
 */
public enum SessionRevocationReason {

    LOGOUT,
    LOGOUT_ALL,
    TOKEN_REUSE,
    PASSWORD_CHANGE,
    ADMIN_ACTION

}
