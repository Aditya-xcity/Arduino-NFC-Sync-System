package com.nfc.attendance.report;

import com.nfc.attendance.model.Student;
import com.nfc.attendance.model.Session;
import com.nfc.attendance.model.Attendance;
import com.nfc.attendance.service.AttendanceService;
import com.nfc.attendance.service.SessionService;
import com.nfc.attendance.util.DateTimeUtil;
import com.nfc.attendance.util.FileUtil;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;

import java.io.File;
import java.util.List;

/**
 * PDF Report Generator
 * Merged: iText 7 engine from Main + Draft's structured report layout
 * (session details, attendance stats, present/absent lists with roll numbers, photo embedding)
 */
public class PDFReportGenerator {

    private AttendanceService attendanceService;
    private SessionService sessionService;

    public PDFReportGenerator() {
        this.attendanceService = new AttendanceService();
        this.sessionService = new SessionService();
    }

    /**
     * Generates an attendance report PDF for a given session
     *
     * @param sessionId The session ID
     * @return The path to the generated PDF, or null on failure
     */
    public String generateAttendanceReport(int sessionId) {
        try {
            Session session = sessionService.getSessionById(sessionId);
            if (session == null) {
                System.out.println("Error: Session not found");
                return null;
            }

            List<Student> presentStudents = attendanceService.getPresentStudents(sessionId);
            List<Student> absentStudents = attendanceService.getAbsentStudents(sessionId);
            int[] stats = attendanceService.getAttendanceStats(sessionId);
            List<Attendance> records = attendanceService.getSessionAttendance(sessionId);

            String filePath = FileUtil.generateReportPath(sessionId);

            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            // --- Title ---
            doc.add(new Paragraph("NFC Attendance Report")
                    .setFontSize(22).setBold()
                    .setFontColor(ColorConstants.DARK_GRAY));

            doc.add(new LineSeparator(new SolidLine()));

            // --- Session Details ---
            doc.add(new Paragraph("\nSession Details").setFontSize(14).setBold());
            doc.add(new Paragraph("Subject: " + session.getSubject()));
            doc.add(new Paragraph("Section: " + session.getSection()));
            doc.add(new Paragraph("Date: " + DateTimeUtil.formatDate(session.getStartTime())));
            doc.add(new Paragraph("Time: " + DateTimeUtil.formatTime(session.getStartTime())
                    + " - " + (session.getEndTime() != null ? DateTimeUtil.formatTime(session.getEndTime()) : "Ongoing")));

            // --- Statistics ---
            doc.add(new Paragraph("\nAttendance Summary").setFontSize(14).setBold());
            doc.add(new Paragraph("Total Students: " + stats[2]));
            doc.add(new Paragraph("Present: " + stats[0])
                    .setFontColor(ColorConstants.GREEN));
            doc.add(new Paragraph("Absent: " + stats[1])
                    .setFontColor(ColorConstants.RED));

            double percentage = stats[2] > 0 ? (stats[0] * 100.0 / stats[2]) : 0;
            doc.add(new Paragraph("Attendance %: " + String.format("%.2f%%", percentage)));

            doc.add(new LineSeparator(new SolidLine()));

            // --- Present Students ---
            doc.add(new Paragraph("\nPresent Students (" + stats[0] + ")")
                    .setFontSize(14).setBold()
                    .setFontColor(ColorConstants.GREEN));

            for (Student student : presentStudents) {
                doc.add(new Paragraph("• " + student.getRollNo() + " - " + student.getName()));

                // Embed photo if available
                String photoPath = findPhotoForStudent(records, student.getId());
                if (photoPath != null && new File(photoPath).exists()) {
                    try {
                        Image img = new Image(ImageDataFactory.create(photoPath));
                        img.scaleToFit(120, 120);
                        doc.add(img);
                    } catch (Exception e) {
                        // Skip photo if it can't be loaded
                    }
                }
            }

            doc.add(new LineSeparator(new SolidLine()));

            // --- Absent Students ---
            doc.add(new Paragraph("\nAbsent Students (" + stats[1] + ")")
                    .setFontSize(14).setBold()
                    .setFontColor(ColorConstants.RED));

            for (Student student : absentStudents) {
                doc.add(new Paragraph("• " + student.getRollNo() + " - " + student.getName()));
            }

            // --- Footer ---
            doc.add(new Paragraph("\n\nGenerated: " +
                    DateTimeUtil.formatDateTime(java.time.LocalDateTime.now()))
                    .setFontSize(9).setFontColor(ColorConstants.GRAY));

            doc.close();
            System.out.println("PDF Report generated: " + filePath);
            return filePath;

        } catch (Exception e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds the photo path for a student from their attendance record
     */
    private String findPhotoForStudent(List<Attendance> records, int studentId) {
        for (Attendance a : records) {
            if (a.getStudentId() == studentId && a.getPhotoPath() != null) {
                return a.getPhotoPath();
            }
        }
        return null;
    }
}

