package com.nfc.attendance.model;

/**
 * Student entity model
 * Represents a student in the system
 */
public class Student {
    private int id;
    private String name;
    private String rollNo;
    private String section;
    private String subject;
    private String nfcUid;

    public Student() {}

    public Student(String name, String rollNo, String section, String subject, String nfcUid) {
        this.name = name;
        this.rollNo = rollNo;
        this.section = section;
        this.subject = subject;
        this.nfcUid = nfcUid;
    }

    public Student(int id, String name, String rollNo, String section, String subject, String nfcUid) {
        this.id = id;
        this.name = name;
        this.rollNo = rollNo;
        this.section = section;
        this.subject = subject;
        this.nfcUid = nfcUid;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getNfcUid() { return nfcUid; }
    public void setNfcUid(String nfcUid) { this.nfcUid = nfcUid; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', rollNo='" + rollNo +
               "', section='" + section + "', subject='" + subject +
               "', nfcUid='" + nfcUid + "'}";
    }
}

