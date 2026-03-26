package com.nfc.attendance.repository;

import com.nfc.attendance.model.Student;
import com.nfc.attendance.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Student Repository — database operations for Student entities
 */
public class StudentRepository {

    private Connection conn;

    public StudentRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public Student findByNfcUid(String nfcUid) {
        String query = "SELECT * FROM Students WHERE nfc_uid = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nfcUid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding student by NFC UID: " + e.getMessage());
        }
        return null;
    }

    public Student findById(int id) {
        String query = "SELECT * FROM Students WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding student by id: " + e.getMessage());
        }
        return null;
    }

    public Student findByRollNo(String rollNo) {
        String query = "SELECT * FROM Students WHERE roll_no = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error finding student by roll number: " + e.getMessage());
        }
        return null;
    }

    public List<Student> findBySection(String section) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM Students WHERE section = ? ORDER BY roll_no";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, section);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding students by section: " + e.getMessage());
        }
        return students;
    }

    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM Students ORDER BY section, roll_no";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error finding all students: " + e.getMessage());
        }
        return students;
    }

    public boolean create(Student student) {
        String query = "INSERT INTO Students (name, roll_no, section, subject, nfc_uid) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getRollNo());
            pstmt.setString(3, student.getSection());
            pstmt.setString(4, student.getSubject());
            pstmt.setString(5, student.getNfcUid());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating student: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Student student) {
        String query = "UPDATE Students SET name = ?, roll_no = ?, section = ?, subject = ?, nfc_uid = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getRollNo());
            pstmt.setString(3, student.getSection());
            pstmt.setString(4, student.getSubject());
            pstmt.setString(5, student.getNfcUid());
            pstmt.setInt(6, student.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteById(int id) {
        String query = "DELETE FROM Students WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
        }
        return false;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setRollNo(rs.getString("roll_no"));
        s.setSection(rs.getString("section"));
        s.setSubject(rs.getString("subject"));
        s.setNfcUid(rs.getString("nfc_uid"));
        return s;
    }
}

