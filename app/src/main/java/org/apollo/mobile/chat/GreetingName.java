package org.apollo.mobile.chat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GreetingName {
    private static final Pattern LOCAL_PART = Pattern.compile("^([\\p{L}]{2,})(?:[._-][\\p{L}]{2,})+$");

    private GreetingName() {
    }

    public static String firstName(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return null;
        }
        Matcher matcher = LOCAL_PART.matcher(email.substring(0, at).trim());
        if (!matcher.matches()) {
            return null;
        }
        String name = matcher.group(1).toLowerCase(Locale.ROOT);
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }
}
