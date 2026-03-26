package com.nfc.attendance.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * File Utility — file operations for photos and reports
 */
public class FileUtil {

    private static final String DATA_DIR = "data";
    private static final String PHOTOS_DIR = DATA_DIR + File.separator + "photos";
    private static final String REPORTS_DIR = DATA_DIR + File.separator + "reports";

    static {
        createDirectoryIfNotExists(PHOTOS_DIR);
        createDirectoryIfNotExists(REPORTS_DIR);
    }

    public static void createDirectoryIfNotExists(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            System.err.println("Error creating directory: " + path + " - " + e.getMessage());
        }
    }

    public static String generatePhotoPath(int studentId, int sessionId) {
        String fileName = "student_" + studentId + "_session_" + sessionId + "_" +
                DateTimeUtil.formatForFileName(LocalDateTime.now()) + ".png";
        return PHOTOS_DIR + File.separator + fileName;
    }

    public static String generateReportPath(int sessionId) {
        String fileName = "Attendance_Report_session_" + sessionId + "_" +
                DateTimeUtil.formatForFileName(LocalDateTime.now()) + ".pdf";
        return REPORTS_DIR + File.separator + fileName;
    }

    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Error deleting file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }

    public static String getPhotosDir() { return PHOTOS_DIR; }
    public static String getReportsDir() { return REPORTS_DIR; }

    public static String getAbsolutePath(String relativePath) {
        return new File(relativePath).getAbsolutePath();
    }
}

