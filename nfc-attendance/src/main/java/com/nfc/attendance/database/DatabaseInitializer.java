package com.nfc.attendance.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;

/**
 * Database Initializer
 * Creates the SQLite schema and seeds student data from JSON
 */
public class DatabaseInitializer {

    /**
     * Simple DTO for JSON deserialization of StudentData.json
     */
    private static class JsonStudent {
        int rollNo;
        String name;
        String uid;
    }

    /**
     * Initializes the database by creating all necessary tables
     */
    public static void initializeDatabase() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS Admins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "roll_no TEXT UNIQUE NOT NULL, " +
                    "section TEXT NOT NULL, " +
                    "subject TEXT NOT NULL, " +
                    "nfc_uid TEXT UNIQUE NOT NULL" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Sessions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "subject TEXT NOT NULL, " +
                    "section TEXT NOT NULL, " +
                    "start_time DATETIME NOT NULL, " +
                    "end_time DATETIME" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "session_id INTEGER NOT NULL, " +
                    "timestamp DATETIME NOT NULL, " +
                    "photo_path TEXT, " +
                    "FOREIGN KEY(student_id) REFERENCES Students(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(session_id) REFERENCES Sessions(id) ON DELETE CASCADE, " +
                    "UNIQUE(student_id, session_id)" +
                    ");");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_students_section ON Students(section);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_students_nfc_uid ON Students(nfc_uid);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_session ON Attendance(session_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_student ON Attendance(student_id);");

            stmt.close();
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts default admin user if none exist
     */
    public static void insertDefaultAdmin() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM Admins");
            rs.next();

            if (rs.getInt(1) == 0) {
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO Admins (username, password) VALUES (?, ?)");
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin123");
                pstmt.executeUpdate();
                pstmt.close();
                System.out.println("Default admin created: username=admin, password=admin123");
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error inserting default admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds the Students table from StudentData.json if the table is empty.
     * This preserves backward compatibility with the original JSON-based approach.
     */
    public static void seedStudentsFromJson() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM Students");
            rs.next();

            if (rs.getInt(1) > 0) {
                rs.close();
                System.out.println("Students table already populated, skipping JSON seed.");
                return;
            }
            rs.close();

            InputStream is = DatabaseInitializer.class.getClassLoader()
                    .getResourceAsStream("StudentData.json");
            if (is == null) {
                System.out.println("StudentData.json not found in resources, skipping seed.");
                return;
            }

            Gson gson = new Gson();
            Type type = new TypeToken<List<JsonStudent>>() {}.getType();
            List<JsonStudent> students = gson.fromJson(new InputStreamReader(is), type);

            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT OR IGNORE INTO Students (name, roll_no, section, subject, nfc_uid) VALUES (?, ?, ?, ?, ?)");

            for (JsonStudent s : students) {
                pstmt.setString(1, s.name);
                pstmt.setString(2, String.valueOf(s.rollNo));
                pstmt.setString(3, "D2");       // default section from original data
                pstmt.setString(4, "General");   // default subject
                pstmt.setString(5, s.uid.toUpperCase());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            pstmt.close();
            System.out.println("Seeded " + students.size() + " students from StudentData.json");

        } catch (Exception e) {
            System.err.println("Error seeding students from JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

