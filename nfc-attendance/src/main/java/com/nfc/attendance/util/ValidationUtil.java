package com.nfc.attendance.util;

import java.util.regex.Pattern;

/**
 * Validation Utility — input validation methods
 */
public class ValidationUtil {

    private static final Pattern NFC_UID_PATTERN = Pattern.compile("^[A-Fa-f0-9]{8,32}$");
    private static final Pattern ROLL_NO_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,20}$");

    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return !isEmpty(username) && username.length() >= 3 && username.length() <= 20 &&
                username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return !isEmpty(name) && name.matches("^[a-zA-Z\\s]{2,100}$");
    }

    public static boolean isValidRollNo(String rollNo) {
        return !isEmpty(rollNo) && ROLL_NO_PATTERN.matcher(rollNo).matches();
    }

    public static boolean isValidSection(String section) {
        return !isEmpty(section) && section.matches("^[a-zA-Z0-9\\-]{1,50}$");
    }

    public static boolean isValidSubject(String subject) {
        return !isEmpty(subject) && subject.matches("^[a-zA-Z\\s]{2,100}$");
    }

    public static boolean isValidNfcUid(String nfcUid) {
        return !isEmpty(nfcUid) && NFC_UID_PATTERN.matcher(nfcUid).matches();
    }

    public static boolean isValidFilePath(String filePath) {
        return !isEmpty(filePath) && filePath.matches("^[a-zA-Z0-9\\-_.\\\\/:]+$");
    }
}

