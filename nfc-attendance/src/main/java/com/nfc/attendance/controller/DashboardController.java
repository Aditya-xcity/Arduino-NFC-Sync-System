package com.nfc.attendance.controller;

import com.nfc.attendance.model.Admin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Dashboard Controller — main navigation hub
 */
public class DashboardController {

    @FXML private Label userLabel;
    @FXML private StackPane contentPane;

    private Admin currentAdmin;
    private DashboardCallback dashboardCallback;

    @FXML
    public void initialize() {
        System.out.println("Dashboard initialized");
    }

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        if (userLabel != null) {
            userLabel.setText("Welcome, " + admin.getUsername());
        }
    }

    @FXML public void showDashboard() { if (dashboardCallback != null) dashboardCallback.showDashboard(); }
    @FXML public void showStudentManagement() { if (dashboardCallback != null) dashboardCallback.showStudentManagement(); }
    @FXML public void showSessionManagement() { if (dashboardCallback != null) dashboardCallback.showSessionManagement(); }
    @FXML public void showAttendance() { if (dashboardCallback != null) dashboardCallback.showAttendance(); }
    @FXML public void showReports() { if (dashboardCallback != null) dashboardCallback.showReports(); }
    @FXML public void showSettings() { if (dashboardCallback != null) dashboardCallback.showSettings(); }
    @FXML public void onLogoutClicked() { if (dashboardCallback != null) dashboardCallback.onLogout(); }

    public void setDashboardCallback(DashboardCallback callback) {
        this.dashboardCallback = callback;
    }

    public StackPane getContentPane() {
        return contentPane;
    }

    public interface DashboardCallback {
        void showDashboard();
        void showStudentManagement();
        void showSessionManagement();
        void showAttendance();
        void showReports();
        void showSettings();
        void onLogout();
    }
}

