package it.echoeswar.management.common.utils;

import java.util.Locale;

public class FormatterString {

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
