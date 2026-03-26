package com.nfc.attendance.repository;

import com.nfc.attendance.model.Attendance;
import com.nfc.attendance.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Attendance Repository — database operations for Attendance records
 */
public class AttendanceRepository {

    private Connection conn;

    public AttendanceRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public boolean hasAttendance(int studentId, int sessionId) {
        String query = "SELECT COUNT(*) FROM Attendance WHERE student_id = ? AND session_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sessionId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking attendance: " + e.getMessage());
        }
        return false;
    }

    public Attendance findById(int id) {
        String query = "SELECT * FROM Attendance WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding attendance by id: " + e.getMessage());
        }
        return null;
    }

    public List<Attendance> findBySessionId(int sessionId) {
        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM Attendance WHERE session_id = ? ORDER BY timestamp";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding attendance by session: " + e.getMessage());
        }
        return list;
    }

    public List<Attendance> findByStudentId(int studentId) {
        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM Attendance WHERE student_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding attendance by student: " + e.getMessage());
        }
        return list;
    }

    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM Attendance ORDER BY timestamp DESC";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding all attendance: " + e.getMessage());
        }
        return list;
    }

    public int create(Attendance attendance) {
        String query = "INSERT INTO Attendance (student_id, session_id, timestamp, photo_path) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getSessionId());
            pstmt.setString(3, attendance.getTimestamp().toString());
            pstmt.setString(4, attendance.getPhotoPath());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating attendance: " + e.getMessage());
        }
        return -1;
    }

    public boolean update(Attendance attendance) {
        String query = "UPDATE Attendance SET student_id = ?, session_id = ?, timestamp = ?, photo_path = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, attendance.getStudentId());
            pstmt.setInt(2, attendance.getSessionId());
            pstmt.setString(3, attendance.getTimestamp().toString());
            pstmt.setString(4, attendance.getPhotoPath());
            pstmt.setInt(5, attendance.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating attendance: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "DELETE FROM Attendance WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
        }
        return false;
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setSessionId(rs.getInt("session_id"));
        a.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        a.setPhotoPath(rs.getString("photo_path"));
        return a;
    }
}

