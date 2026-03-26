package com.nfc.attendance.model;

import java.time.LocalDateTime;

/**
 * Attendance entity model
 * Represents an attendance record for a student in a session
 */
public class Attendance {
    private int id;
    private int studentId;
    private int sessionId;
    private LocalDateTime timestamp;
    private String photoPath;

    // Transient fields for display purposes
    private String studentName;
    private String studentRollNo;

    public Attendance() {}

    public Attendance(int studentId, int sessionId, LocalDateTime timestamp) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
    }

    public Attendance(int id, int studentId, int sessionId, LocalDateTime timestamp, String photoPath) {
        this.id = id;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentRollNo() { return studentRollNo; }
    public void setStudentRollNo(String studentRollNo) { this.studentRollNo = studentRollNo; }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", studentId=" + studentId +
               ", sessionId=" + sessionId + ", timestamp=" + timestamp +
               ", photoPath='" + photoPath + "'}";
    }
}

