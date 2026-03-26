package com.nfc.attendance.service;

import com.nfc.attendance.model.Session;
import com.nfc.attendance.model.Student;
import com.nfc.attendance.model.Attendance;
import com.nfc.attendance.repository.SessionRepository;
import com.nfc.attendance.repository.AttendanceRepository;
import com.nfc.attendance.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Attendance Service — attendance marking and verification
 */
public class AttendanceService {

    private AttendanceRepository attendanceRepository;
    private StudentRepository studentRepository;
    private SessionRepository sessionRepository;

    public AttendanceService() {
        this.attendanceRepository = new AttendanceRepository();
        this.studentRepository = new StudentRepository();
        this.sessionRepository = new SessionRepository();
    }

    /**
     * Marks attendance for a student by student ID
     */
    public boolean markAttendance(int studentId, int sessionId, String photoPath) {
        Student student = studentRepository.findById(studentId);
        if (student == null) {
            System.out.println("Error: Student not found");
            return false;
        }
        Session session = sessionRepository.findById(sessionId);
        if (session == null || !session.isActive()) {
            System.out.println("Error: Session not found or not active");
            return false;
        }
        if (attendanceRepository.hasAttendance(studentId, sessionId)) {
            System.out.println("Warning: Duplicate attendance for: " + student.getName());
            return false;
        }
        Attendance attendance = new Attendance(studentId, sessionId, LocalDateTime.now());
        attendance.setPhotoPath(photoPath);
        int recordId = attendanceRepository.create(attendance);
        if (recordId > 0) {
            System.out.println("Attendance marked for: " + student.getName() + " (" + student.getRollNo() + ")");
            return true;
        }
        return false;
    }

    /**
     * Marks attendance by NFC UID — the main scan-loop entry point
     */
    public Student markAttendanceByNfcUid(String nfcUid, int sessionId, String photoPath) {
        Student student = studentRepository.findByNfcUid(nfcUid);
        if (student == null) {
            System.out.println("Error: Student not found for NFC UID: " + nfcUid);
            return null;
        }
        if (markAttendance(student.getId(), sessionId, photoPath)) {
            return student;
        }
        return null;
    }

    public List<Attendance> getSessionAttendance(int sessionId) {
        return attendanceRepository.findBySessionId(sessionId);
    }

    public List<Student> getPresentStudents(int sessionId) {
        List<Student> presentStudents = new ArrayList<>();
        List<Attendance> records = attendanceRepository.findBySessionId(sessionId);
        Set<Integer> ids = new HashSet<>();
        for (Attendance a : records) ids.add(a.getStudentId());
        for (Integer id : ids) {
            Student s = studentRepository.findById(id);
            if (s != null) presentStudents.add(s);
        }
        return presentStudents;
    }

    public List<Student> getAbsentStudents(int sessionId) {
        Session session = sessionRepository.findById(sessionId);
        if (session == null) return new ArrayList<>();

        List<Student> allStudents = studentRepository.findBySection(session.getSection());
        List<Student> present = getPresentStudents(sessionId);
        Set<Integer> presentIds = new HashSet<>();
        for (Student s : present) presentIds.add(s.getId());

        List<Student> absent = new ArrayList<>();
        for (Student s : allStudents) {
            if (!presentIds.contains(s.getId())) absent.add(s);
        }
        return absent;
    }

    /**
     * Returns [presentCount, absentCount, totalCount]
     */
    public int[] getAttendanceStats(int sessionId) {
        List<Student> present = getPresentStudents(sessionId);
        List<Student> absent = getAbsentStudents(sessionId);
        return new int[]{present.size(), absent.size(), present.size() + absent.size()};
    }

    public List<Attendance> getStudentAttendance(int studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }
}

