package com.nfc.attendance.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DateTime Utility — date/time formatting helpers
 */
public class DateTimeUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static String formatDateTime(LocalDateTime dt) { return dt.format(DATE_TIME_FORMATTER); }
    public static String formatDate(LocalDateTime dt) { return dt.format(DATE_FORMATTER); }
    public static String formatTime(LocalDateTime dt) { return dt.format(TIME_FORMATTER); }
    public static String formatForFileName(LocalDateTime dt) { return dt.format(FILE_NAME_FORMATTER); }
    public static LocalDateTime parseDateTime(String s) { return LocalDateTime.parse(s, DATE_TIME_FORMATTER); }
}

