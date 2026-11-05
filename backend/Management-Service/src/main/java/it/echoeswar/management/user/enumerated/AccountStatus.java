package it.echoeswar.management.user.enumerated;

/** Stati che determinano se un account puo' ottenere o continuare a usare credenziali. */
public enum AccountStatus {

    PENDING_EMAIL_VERIFICATION,
    ACTIVE,
    DISABLED,
    BANNED
}
