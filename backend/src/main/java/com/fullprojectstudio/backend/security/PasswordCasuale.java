package com.fullprojectstudio.backend.security;

import java.security.SecureRandom;

/** Password casuali facili da ricopiare: niente caratteri che si confondono (0/O, 1/l/I). */
public final class PasswordCasuale {

    private static final String CARATTERI = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordCasuale() {
    }

    public static String genera(int lunghezza) {
        StringBuilder sb = new StringBuilder(lunghezza);
        for (int i = 0; i < lunghezza; i++) {
            sb.append(CARATTERI.charAt(RANDOM.nextInt(CARATTERI.length())));
        }
        return sb.toString();
    }
}
