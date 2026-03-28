package com.nfc.attendance.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fazecast.jSerialComm.SerialPort;
import com.nfc.attendance.model.Student;
import com.nfc.attendance.nfc.ArduinoController;
import com.nfc.attendance.nfc.NFCCardReader;
import com.nfc.attendance.service.StudentService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Student Management Controller — handles student CRUD operations
 */
public class StudentManagementController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> rollNoColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> sectionColumn;
    @FXML private TableColumn<Student, String> subjectColumn;
    @FXML private TableColumn<Student, String> nfcUidColumn;
    @FXML private ComboBox<String> sectionComboBox;
    @FXML private Label statusLabel;

    // Arduino Test Dialog
    @FXML
    public void onTestArduinoClicked() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Test Arduino Buzzer/LED");
        dialog.setHeaderText("Test Arduino connection and buzzer/light");

        ComboBox<String> portCombo = new ComboBox<>();
        portCombo.setPrefWidth(180);
        Button refreshButton = new Button("Refresh");
        Button testButton = new Button("Test Buzzer/LED");
        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: #666;");

        Runnable refreshPorts = () -> {
            portCombo.getItems().clear();
            SerialPort[] ports = SerialPort.getCommPorts();
            for (SerialPort p : ports) {
                portCombo.getItems().add(p.getSystemPortName());
            }
            if (!portCombo.getItems().isEmpty()) {
                portCombo.getSelectionModel().select(0);
            }
        };
        refreshPorts.run();
        refreshButton.setOnAction(e -> refreshPorts.run());

        testButton.setOnAction(e -> {
            String port = portCombo.getValue();
            if (port == null || port.isEmpty()) {
                resultLabel.setText("Select a COM port first.");
                return;
            }
            try {
                ArduinoController.getInstance().setPortName(port);
                ArduinoController.getInstance().triggerScan();
                resultLabel.setText("Signal sent to " + port + ". Check buzzer/LED.");
            } catch (Exception ex) {
                resultLabel.setText("Failed: " + ex.getMessage());
            }
        });

        HBox portBox = new HBox(8, new Label("COM Port:"), portCombo, refreshButton, testButton);
        portBox.setStyle("-fx-padding: 8 0 8 0;");

        GridPane content = new GridPane();
        content.setVgap(10);
        content.setHgap(10);
        content.add(portBox, 0, 0);
        content.add(resultLabel, 0, 1);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private StudentService studentService;
    private ObservableList<Student> studentList;

    @FXML
    public void initialize() {
        this.studentService = new StudentService();
        this.studentList = FXCollections.observableArrayList();
        setupTable();
        loadAllStudents();
        loadSections();
    }

    private void setupTable() {
        if (rollNoColumn != null) {
            rollNoColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getRollNo()));
        }
        if (nameColumn != null) {
            nameColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        }
        if (sectionColumn != null) {
            sectionColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getSection()));
        }
        if (subjectColumn != null) {
            subjectColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject()));
        }
        if (nfcUidColumn != null) {
            nfcUidColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getNfcUid()));
        }
    }

    private void loadAllStudents() {
        List<Student> students = studentService.getAllStudents();
        studentList.setAll(students);
        studentTable.setItems(studentList);
        statusLabel.setText("Loaded " + students.size() + " students");
    }

    private void loadSections() {
        List<Student> all = studentService.getAllStudents();
        Set<String> sections = new HashSet<>();
        for (Student s : all) sections.add(s.getSection());
        sectionComboBox.setItems(FXCollections.observableArrayList(sections));
    }

    @FXML
    public void onAddStudentClicked() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student details and scan NFC card");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Student Name");
        TextField rollNoField = new TextField();
        rollNoField.setPromptText("Roll Number");
        TextField sectionField = new TextField();
        sectionField.setPromptText("Section");
        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");
        TextField nfcUidField = new TextField();
        nfcUidField.setPromptText("NFC UID (click Scan to read)");
        nfcUidField.setEditable(false);

        // NFC Scan Button
        Button scanNfcButton = new Button("Scan NFC Card");
        Label scanStatusLabel = new Label("Ready to scan");
        scanStatusLabel.setStyle("-fx-text-fill: #666;");

        AtomicBoolean isScanning = new AtomicBoolean(false);

        scanNfcButton.setOnAction(e -> {
            if (isScanning.get()) {
                scanNfcButton.setText("Scan NFC Card");
                isScanning.set(false);
                return;
            }

            scanNfcButton.setText("Scanning... (Place card)");
            scanNfcButton.setDisable(true);
            isScanning.set(true);

            new Thread(() -> {
                try {
                    NFCCardReader reader = NFCCardReader.getInstance();

                    if (!reader.isReaderAvailable()) {
                        Platform.runLater(() -> {
                            scanStatusLabel.setText("ERROR: No NFC reader found");
                            scanStatusLabel.setStyle("-fx-text-fill: red;");
                            scanNfcButton.setText("Scan NFC Card");
                            scanNfcButton.setDisable(false);
                            isScanning.set(false);
                        });
                        return;
                    }

                    // Wait for card (5 second timeout)
                    String uid = reader.waitAndReadCardUID(5000);

                    Platform.runLater(() -> {
                        if (uid != null && !uid.isEmpty()) {
                            nfcUidField.setText(uid);
                            scanStatusLabel.setText("✓ Card scanned: " + uid);
                            scanStatusLabel.setStyle("-fx-text-fill: green;");
                        } else {
                            scanStatusLabel.setText("No card detected");
                            scanStatusLabel.setStyle("-fx-text-fill: orange;");
                        }
                        scanNfcButton.setText("Scan NFC Card");
                        scanNfcButton.setDisable(false);
                        isScanning.set(false);
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        scanStatusLabel.setText("Error: " + ex.getMessage());
                        scanStatusLabel.setStyle("-fx-text-fill: red;");
                        scanNfcButton.setText("Scan NFC Card");
                        scanNfcButton.setDisable(false);
                        isScanning.set(false);
                    });
                }
            }, "NFC-Scan-Thread").start();
        });

        HBox nfcBox = new HBox(10);
        nfcBox.getChildren().addAll(nfcUidField, scanNfcButton);
        HBox.setHgrow(nfcUidField, javafx.scene.layout.Priority.ALWAYS);

        grid.add(new Label("Name:"), 0, 0);       grid.add(nameField, 1, 0);
        grid.add(new Label("Roll No:"), 0, 1);    grid.add(rollNoField, 1, 1);
        grid.add(new Label("Section:"), 0, 2);    grid.add(sectionField, 1, 2);
        grid.add(new Label("Subject:"), 0, 3);    grid.add(subjectField, 1, 3);
        grid.add(new Label("NFC UID:"), 0, 4);    grid.add(nfcBox, 1, 4);
        grid.add(new Label(""), 0, 5);            grid.add(scanStatusLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (ButtonType.OK == buttonType) {
                if (nameField.getText().isEmpty() || rollNoField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Validation");
                    alert.setHeaderText("Missing Fields");
                    alert.setContentText("Name and Roll No are required");
                    alert.showAndWait();
                    return null;
                }

                boolean success = studentService.addStudent(
                        nameField.getText(), rollNoField.getText(),
                        sectionField.getText(), subjectField.getText(),
                        nfcUidField.getText());
                if (success) {
                    statusLabel.setText("✓ Student added successfully");
                    loadAllStudents();
                    loadSections();
                } else {
                    statusLabel.setText("✗ Failed to add student");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    public void onSearchClicked() {
        String selectedSection = sectionComboBox.getValue();
        if (selectedSection != null && !selectedSection.isEmpty()) {
            List<Student> students = studentService.getStudentsBySection(selectedSection);
            studentList.setAll(students);
            statusLabel.setText("Found " + students.size() + " students in " + selectedSection);
        }
    }

    @FXML
    public void onShowAllClicked() {
        loadAllStudents();
    }
}

