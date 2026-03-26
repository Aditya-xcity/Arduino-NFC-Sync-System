package com.nfc.attendance.repository;

import com.nfc.attendance.model.Session;
import com.nfc.attendance.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Session Repository — database operations for Session entities
 */
public class SessionRepository {

    private Connection conn;

    public SessionRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public Session findActiveSession() {
        String query = "SELECT * FROM Sessions WHERE end_time IS NULL ORDER BY start_time DESC LIMIT 1";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding active session: " + e.getMessage());
        }
        return null;
    }

    public Session findById(int id) {
        String query = "SELECT * FROM Sessions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding session by id: " + e.getMessage());
        }
        return null;
    }

    public List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        String query = "SELECT * FROM Sessions ORDER BY start_time DESC";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) sessions.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding all sessions: " + e.getMessage());
        }
        return sessions;
    }

    public List<Session> findBySection(String section) {
        List<Session> sessions = new ArrayList<>();
        String query = "SELECT * FROM Sessions WHERE section = ? ORDER BY start_time DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, section);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) sessions.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding sessions by section: " + e.getMessage());
        }
        return sessions;
    }

    public int create(Session session) {
        String query = "INSERT INTO Sessions (subject, section, start_time) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, session.getSubject());
            pstmt.setString(2, session.getSection());
            pstmt.setString(3, session.getStartTime().toString());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
        }
        return -1;
    }

    public boolean endSession(int id) {
        String query = "UPDATE Sessions SET end_time = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error ending session: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Session session) {
        String query = "UPDATE Sessions SET subject = ?, section = ?, start_time = ?, end_time = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, session.getSubject());
            pstmt.setString(2, session.getSection());
            pstmt.setString(3, session.getStartTime().toString());
            pstmt.setString(4, session.getEndTime() != null ? session.getEndTime().toString() : null);
            pstmt.setInt(5, session.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating session: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "DELETE FROM Sessions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
        }
        return false;
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setId(rs.getInt("id"));
        s.setSubject(rs.getString("subject"));
        s.setSection(rs.getString("section"));
        s.setStartTime(LocalDateTime.parse(rs.getString("start_time")));
        String endTime = rs.getString("end_time");
        if (endTime != null && !endTime.isEmpty()) {
            s.setEndTime(LocalDateTime.parse(endTime));
        }
        return s;
    }
}

