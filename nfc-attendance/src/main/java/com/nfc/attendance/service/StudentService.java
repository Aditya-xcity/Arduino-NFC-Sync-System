package com.nfc.attendance.service;

import com.nfc.attendance.model.Student;
import com.nfc.attendance.repository.StudentRepository;

import java.util.List;

/**
 * Student Service — student management business logic
 */
public class StudentService {

    private StudentRepository studentRepository;

    public StudentService() {
        this.studentRepository = new StudentRepository();
    }

    public boolean addStudent(String name, String rollNo, String section, String subject, String nfcUid) {
        if (isEmpty(name) || isEmpty(rollNo) || isEmpty(section) || isEmpty(subject) || isEmpty(nfcUid)) {
            System.out.println("Error: All fields are required");
            return false;
        }
        if (studentRepository.findByRollNo(rollNo) != null) {
            System.out.println("Error: Roll number already exists");
            return false;
        }
        if (studentRepository.findByNfcUid(nfcUid) != null) {
            System.out.println("Error: NFC UID already registered");
            return false;
        }
        boolean success = studentRepository.create(new Student(name, rollNo, section, subject, nfcUid));
        if (success) System.out.println("Student added: " + name + " (" + rollNo + ")");
        return success;
    }

    public Student getStudentByNfcUid(String nfcUid) {
        return studentRepository.findByNfcUid(nfcUid);
    }

    public Student getStudentById(int id) {
        return studentRepository.findById(id);
    }

    public Student getStudentByRollNo(String rollNo) {
        return studentRepository.findByRollNo(rollNo);
    }

    public List<Student> getStudentsBySection(String section) {
        return studentRepository.findBySection(section);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public boolean updateStudent(int id, String name, String rollNo, String section, String subject, String nfcUid) {
        if (isEmpty(name) || isEmpty(rollNo) || isEmpty(section) || isEmpty(subject) || isEmpty(nfcUid)) {
            System.out.println("Error: All fields are required");
            return false;
        }
        return studentRepository.update(new Student(id, name, rollNo, section, subject, nfcUid));
    }

    public boolean deleteStudent(int id) {
        Student student = studentRepository.findById(id);
        if (student == null) {
            System.out.println("Error: Student not found");
            return false;
        }
        return studentRepository.deleteById(id);
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}

