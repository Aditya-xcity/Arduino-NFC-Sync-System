package com.nfc.attendance.controller;

import com.nfc.attendance.model.Attendance;
import com.nfc.attendance.model.Session;
import com.nfc.attendance.model.Student;
import com.nfc.attendance.nfc.ArduinoController;
import com.nfc.attendance.nfc.NFCCardReader;
import com.nfc.attendance.report.PDFReportGenerator;
import com.nfc.attendance.service.AttendanceService;
import com.nfc.attendance.service.SessionService;
import com.nfc.attendance.util.FileUtil;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Attendance Controller — MERGED
 * Combines Draft's database-backed attendance UI with Main project's
 * working NFC scan loop and webcam capture integration.
 */
public class AttendanceController {

    @FXML private Label sessionInfoLabel;
    @FXML private Label presentLabel;
    @FXML private Label absentLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<Attendance> attendanceTable;
    @FXML private TableColumn<Attendance, String> rollNoColumn;
    @FXML private TableColumn<Attendance, String> nameColumn;
    @FXML private TableColumn<Attendance, String> timestampColumn;
    @FXML private TableColumn<Attendance, String> photoColumn;
    @FXML private Label statusLabel;
    @FXML private ImageView webcamView;
    @FXML private Button startScanButton;
    @FXML private Button stopScanButton;
    @FXML private ListView<String> attendanceList;

    private AttendanceService attendanceService;
    private SessionService sessionService;
    private int activeSessionId = -1;
    private ObservableList<Attendance> attendanceData;
    private final AtomicBoolean scanning = new AtomicBoolean(false);

    @FXML
    public void initialize() {
        this.attendanceService = new AttendanceService();
        this.sessionService = new SessionService();
        this.attendanceData = FXCollections.observableArrayList();
        setupTable();
        updateDisplay();
        if (stopScanButton != null) stopScanButton.setDisable(true);
    }

    private void setupTable() {
        if (rollNoColumn != null) {
            rollNoColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getStudentRollNo() != null ? data.getValue().getStudentRollNo() : ""));
        }
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getStudentName() != null ? data.getValue().getStudentName() : ""));
        }
        if (timestampColumn != null) {
            timestampColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getTimestamp() != null ? data.getValue().getTimestamp().toString() : ""));
        }
        if (photoColumn != null) {
            photoColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getPhotoPath() != null ? "Yes" : "No"));
        }
    }

    private void updateDisplay() {
        Session activeSession = sessionService.getActiveSession();
        if (activeSession != null) {
            activeSessionId = activeSession.getId();
            sessionInfoLabel.setText("Session: " + activeSession.getSubject() + " - " + activeSession.getSection());
            updateStats();
            loadAttendance();
        } else {
            activeSessionId = -1;
            sessionInfoLabel.setText("No active session — start one from Sessions tab");
            presentLabel.setText("0");
            absentLabel.setText("0");
            totalLabel.setText("0");
            attendanceData.clear();
        }
    }

    private void updateStats() {
        if (activeSessionId <= 0) return;
        int[] stats = attendanceService.getAttendanceStats(activeSessionId);
        presentLabel.setText(String.valueOf(stats[0]));
        absentLabel.setText(String.valueOf(stats[1]));
        totalLabel.setText(String.valueOf(stats[2]));
    }

    private void loadAttendance() {
        if (activeSessionId <= 0) return;
        List<Attendance> records = attendanceService.getSessionAttendance(activeSessionId);

        // Enrich with student details for table display
        for (Attendance a : records) {
            Student s = new com.nfc.attendance.service.StudentService().getStudentById(a.getStudentId());
            if (s != null) {
                a.setStudentName(s.getName());
                a.setStudentRollNo(s.getRollNo());
            }
        }

        attendanceData.setAll(records);
        attendanceTable.setItems(attendanceData);
    }

    // ======================== NFC SCAN LOOP (from Main project) ========================

    /**
     * Starts the NFC scan loop in a background thread.
     * Merged from NfcAttendanceSystem.startFxSession().
     */
    @FXML
    public void onStartScanClicked() {
        if (activeSessionId <= 0) {
            statusLabel.setText("Error: Start a session first (Sessions tab)");
            return;
        }

        if (scanning.get()) {
            statusLabel.setText("Scan already running");
            return;
        }

        scanning.set(true);
        if (startScanButton != null) startScanButton.setDisable(true);
        if (stopScanButton != null) stopScanButton.setDisable(false);
        statusLabel.setText("Scanning for NFC cards...");

        new Thread(() -> {
            NFCCardReader reader = NFCCardReader.getInstance();

            if (!reader.isReaderAvailable()) {
                Platform.runLater(() -> statusLabel.setText("Error: No NFC reader found"));
                scanning.set(false);
                Platform.runLater(() -> {
                    if (startScanButton != null) startScanButton.setDisable(false);
                    if (stopScanButton != null) stopScanButton.setDisable(true);
                });
                return;
            }

            while (scanning.get()) {
                String uid = reader.waitAndReadCardUID(1000);

                if (uid != null && !uid.isEmpty()) {
                    uid = uid.toUpperCase();

                    // Capture photo from webcam (from Main project)
                    String photoPath = capturePhoto(uid);

                    // Mark attendance via service (from Draft)
                    Student student = attendanceService.markAttendanceByNfcUid(uid, activeSessionId, photoPath);

                    if (student != null) {
                        // Signal Arduino: blink red LED + buzzer beep
                        ArduinoController.getInstance().triggerScan();

                        final String displayName = student.getName();
                        Platform.runLater(() -> {
                            if (attendanceList != null) {
                                attendanceList.getItems().add(displayName);
                            }
                            statusLabel.setText("Marked: " + displayName);
                            updateStats();
                            loadAttendance();
                        });
                    } else {
                        final String finalUid = uid;
                        Platform.runLater(() ->
                                statusLabel.setText("Card scanned: " + finalUid + " (already marked or unknown)"));
                    }

                    // Wait for card to be removed before scanning again
                    reader.waitForCardAbsent(2000);
                }
            }

            Platform.runLater(() -> statusLabel.setText("Scan stopped"));
        }, "NFC-Scan-Thread").start();
    }

    @FXML
    public void onStopScanClicked() {
        scanning.set(false);
        if (startScanButton != null) startScanButton.setDisable(false);
        if (stopScanButton != null) stopScanButton.setDisable(true);
        statusLabel.setText("Scan stopped");
    }

    /**
     * Captures a webcam photo — merged from NfcAttendanceSystem.capturePhoto()
     */
    private String capturePhoto(String uid) {
        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) return null;

            if (!webcam.isOpen()) webcam.open();
            BufferedImage image = webcam.getImage();
            if (image == null) return null;

            // Update webcam preview on JavaFX thread
            if (webcamView != null) {
                Platform.runLater(() ->
                        webcamView.setImage(SwingFXUtils.toFXImage(image, null)));
            }

            // Save photo
            String photoPath = FileUtil.generatePhotoPath(0, activeSessionId);
            File photoFile = new File(photoPath);
            photoFile.getParentFile().mkdirs();
            ImageIO.write(image, "PNG", photoFile);

            webcam.close();
            return photoPath;

        } catch (Exception e) {
            System.out.println("Camera Error: " + e.getMessage());
            return null;
        }
    }

    // ======================== REPORT ========================

    @FXML
    public void onGenerateReportClicked() {
        if (activeSessionId <= 0) {
            statusLabel.setText("Error: No active session");
            return;
        }

        PDFReportGenerator generator = new PDFReportGenerator();
        String reportPath = generator.generateAttendanceReport(activeSessionId);

        Alert alert = new Alert(reportPath != null ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle("Generate Report");
        if (reportPath != null) {
            alert.setHeaderText("Report generated successfully");
            alert.setContentText(reportPath);
            statusLabel.setText("Report: " + reportPath);
        } else {
            alert.setHeaderText("Failed to generate report");
            statusLabel.setText("Report generation failed");
        }
        alert.showAndWait();
    }

    @FXML
    public void onRefreshClicked() {
        updateDisplay();
        statusLabel.setText("Refreshed");
    }
}

