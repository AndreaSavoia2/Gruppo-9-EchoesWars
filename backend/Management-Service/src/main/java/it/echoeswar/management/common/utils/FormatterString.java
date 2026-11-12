package it.echoeswar.management.common.utils;

import java.util.Locale;

public class FormatterString {

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * Capitalizes the first character of the given string, leaving the rest unchanged.
     * <p>
     * If the string is {@code null} or empty, it is returned as is.
     *
     * @param str the string to capitalize, may be {@code null}
     * @return the string with its first character converted to uppercase,
     *         or the original value if it is {@code null} or empty
     */
    public static String capitalizeFirstLetter(String str){
        if (str == null || str.isEmpty()){
            return str;
        }
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }
}
