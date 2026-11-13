package it.echoeswar.management.user.enumerated;

/** States that determine whether an account can obtain or continue using credentials. */
public enum AccountStatus {

    PENDING_EMAIL_VERIFICATION,
    ACTIVE,
    DISABLED,
    BANNED
}
