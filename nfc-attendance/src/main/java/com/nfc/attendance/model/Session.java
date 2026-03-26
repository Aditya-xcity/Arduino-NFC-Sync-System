package com.nfc.attendance.model;

import java.time.LocalDateTime;

/**
 * Session entity model
 * Represents an attendance session
 */
public class Session {
    private int id;
    private String subject;
    private String section;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Session() {}

    public Session(String subject, String section, LocalDateTime startTime) {
        this.subject = subject;
        this.section = section;
        this.startTime = startTime;
    }

    public Session(int id, String subject, String section, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.subject = subject;
        this.section = section;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isActive() { return endTime == null; }

    @Override
    public String toString() {
        return "Session{id=" + id + ", subject='" + subject + "', section='" + section +
               "', startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}

