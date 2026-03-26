package com.nfc.attendance.controller;

import com.nfc.attendance.model.Session;
import com.nfc.attendance.service.SessionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Session Controller — handles session management
 */
public class SessionController {

    @FXML private TextField subjectField;
    @FXML private TextField sectionField;
    @FXML private Button startButton;
    @FXML private Button endButton;
    @FXML private Label activeSessionLabel;
    @FXML private Label sessionIdLabel;
    @FXML private TableView<Session> sessionTable;
    @FXML private TableColumn<Session, Number> idColumn;
    @FXML private TableColumn<Session, String> subjectColumn;
    @FXML private TableColumn<Session, String> sectionColumn;
    @FXML private TableColumn<Session, String> startTimeColumn;
    @FXML private TableColumn<Session, String> endTimeColumn;
    @FXML private TableColumn<Session, String> statusColumn;
    @FXML private Label statusLabel;

    private SessionService sessionService;
    private int activeSessionId = -1;
    private ObservableList<Session> sessionList;

    @FXML
    public void initialize() {
        this.sessionService = new SessionService();
        this.sessionList = FXCollections.observableArrayList();
        setupTable();
        loadSessions();
        updateActiveSessionDisplay();
    }

    private void setupTable() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()));
        }
        if (subjectColumn != null) {
            subjectColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject()));
        }
        if (sectionColumn != null) {
            sectionColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().getSection()));
        }
        if (startTimeColumn != null) {
            startTimeColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getStartTime() != null ? data.getValue().getStartTime().toString() : ""));
        }
        if (endTimeColumn != null) {
            endTimeColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getEndTime() != null ? data.getValue().getEndTime().toString() : ""));
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().isActive() ? "Active" : "Ended"));
        }
    }

    private void loadSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        sessionList.setAll(sessions);
        sessionTable.setItems(sessionList);
    }

    private void updateActiveSessionDisplay() {
        Session activeSession = sessionService.getActiveSession();
        if (activeSession != null) {
            activeSessionId = activeSession.getId();
            activeSessionLabel.setText("Active: " + activeSession.getSubject() + " - " + activeSession.getSection());
            sessionIdLabel.setText("Session ID: " + activeSession.getId());
            startButton.setDisable(true);
            endButton.setDisable(false);
        } else {
            activeSessionId = -1;
            activeSessionLabel.setText("No active session");
            sessionIdLabel.setText("");
            startButton.setDisable(false);
            endButton.setDisable(true);
        }
    }

    @FXML
    public void onStartSessionClicked() {
        String subject = subjectField.getText().trim();
        String section = sectionField.getText().trim();

        if (subject.isEmpty() || section.isEmpty()) {
            statusLabel.setText("Please enter subject and section");
            return;
        }

        int sessionId = sessionService.startSession(subject, section);
        if (sessionId > 0) {
            statusLabel.setText("Session started (ID: " + sessionId + ")");
            subjectField.clear();
            sectionField.clear();
            updateActiveSessionDisplay();
            loadSessions();
        } else {
            statusLabel.setText("Failed to start session (end active session first?)");
        }
    }

    @FXML
    public void onEndSessionClicked() {
        if (activeSessionId <= 0) {
            statusLabel.setText("No active session to end");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("End Session");
        alert.setHeaderText("Are you sure you want to end the current session?");
        alert.setContentText("Session ID: " + activeSessionId);

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (sessionService.endSession()) {
                statusLabel.setText("Session ended successfully");
                updateActiveSessionDisplay();
                loadSessions();
            } else {
                statusLabel.setText("Failed to end session");
            }
        }
    }
}

