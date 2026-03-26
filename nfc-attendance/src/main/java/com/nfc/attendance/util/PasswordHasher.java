package com.nfc.attendance.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Hashing Utility — SHA-256 with salt
 */
public class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes());

            byte[] combined = new byte[salt.length + hashed.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashed, 0, combined, salt.length, hashed.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    public static boolean verifyPassword(String password, String hash) {
        try {
            byte[] decoded = Base64.getDecoder().decode(hash);
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(decoded, 0, salt, 0, SALT_LENGTH);

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes());

            for (int i = 0; i < hashed.length; i++) {
                if (decoded[SALT_LENGTH + i] != hashed[i]) return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
}

