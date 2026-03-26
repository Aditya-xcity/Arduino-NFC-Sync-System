package com.nfc.attendance.service;

import com.nfc.attendance.model.Session;
import com.nfc.attendance.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Session Service — session management business logic
 */
public class SessionService {

    private SessionRepository sessionRepository;

    public SessionService() {
        this.sessionRepository = new SessionRepository();
    }

    public int startSession(String subject, String section) {
        if (isEmpty(subject) || isEmpty(section)) {
            System.out.println("Error: Subject and section are required");
            return -1;
        }
        Session activeSession = sessionRepository.findActiveSession();
        if (activeSession != null) {
            System.out.println("Warning: An active session already exists. Please end it first.");
            return -1;
        }
        Session session = new Session(subject, section, LocalDateTime.now());
        int sessionId = sessionRepository.create(session);
        if (sessionId > 0) {
            System.out.println("Session started: " + subject + " - " + section + " (ID: " + sessionId + ")");
        }
        return sessionId;
    }

    public boolean endSession() {
        Session activeSession = sessionRepository.findActiveSession();
        if (activeSession == null) {
            System.out.println("Error: No active session found");
            return false;
        }
        boolean success = sessionRepository.endSession(activeSession.getId());
        if (success) {
            System.out.println("Session ended: " + activeSession.getSubject() + " - " + activeSession.getSection());
        }
        return success;
    }

    public Session getActiveSession() {
        return sessionRepository.findActiveSession();
    }

    public Session getSessionById(int id) {
        return sessionRepository.findById(id);
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public List<Session> getSessionsBySection(String section) {
        return sessionRepository.findBySection(section);
    }

    public boolean deleteSession(int id) {
        Session session = sessionRepository.findById(id);
        if (session == null) {
            System.out.println("Error: Session not found");
            return false;
        }
        return sessionRepository.deleteById(id);
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}

