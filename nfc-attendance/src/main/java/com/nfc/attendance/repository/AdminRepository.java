package com.nfc.attendance.repository;

import com.nfc.attendance.model.Admin;
import com.nfc.attendance.database.DatabaseConnection;

import java.sql.*;

/**
 * Admin Repository — database operations for Admin entities
 */
public class AdminRepository {

    private Connection conn;

    public AdminRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public Admin findByUsernameAndPassword(String username, String password) {
        String query = "SELECT * FROM Admins WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("Error finding admin: " + e.getMessage());
        }
        return null;
    }

    public Admin findByUsername(String username) {
        String query = "SELECT * FROM Admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("Error finding admin: " + e.getMessage());
        }
        return null;
    }

    public boolean create(Admin admin) {
        String query = "INSERT INTO Admins (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating admin: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Admin admin) {
        String query = "UPDATE Admins SET username = ?, password = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setInt(3, admin.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating admin: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "DELETE FROM Admins WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting admin: " + e.getMessage());
        }
        return false;
    }
}

