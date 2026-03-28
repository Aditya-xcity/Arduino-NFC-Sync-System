package com.nfc.attendance;

import com.nfc.attendance.controller.DashboardController;
import com.nfc.attendance.controller.LoginController;
import com.nfc.attendance.database.DatabaseConnection;
import com.nfc.attendance.database.DatabaseInitializer;
import com.nfc.attendance.model.Admin;
import com.nfc.attendance.nfc.ArduinoController;
import com.nfc.attendance.nfc.NFCCardReader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * NFC Attendance Pro — Main Application Entry Point
 * Merged: Draft's FXML dashboard navigation + Main's dark theme
 */
public class NFCAttendanceApp extends Application {

    private Stage primaryStage;
    private LoginController loginController;
    private DashboardController dashboardController;
    private Admin currentAdmin;

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;

            // Always use COM11 for Arduino
            com.nfc.attendance.nfc.ArduinoController.getInstance().setPortName("COM11");

            configureSerialNativeRuntime();

            // Isolate jSerialComm native extraction for this app to avoid stale/locked DLL conflicts.
            System.setProperty("fazecast.jSerialComm.appid", "nfc-attendance-pro");

            // Initialize database and seed data
            System.out.println("Initializing database...");
            DatabaseInitializer.initializeDatabase();
            DatabaseInitializer.insertDefaultAdmin();
            DatabaseInitializer.seedStudentsFromJson();

            // Show login screen
            showLoginScreen();

            primaryStage.setTitle("NFC Attendance Pro");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(700);
            primaryStage.setOnCloseRequest(event -> onApplicationExit());
            primaryStage.show();

            runArduinoStartupSelfTest();

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            loginController = loader.getController();
            loginController.setLoginCallback(new LoginController.LoginCallback() {
                @Override
                public void onLoginSuccess(Admin admin) {
                    currentAdmin = admin;
                    showDashboard();
                }

                @Override
                public void onRegisterClicked() {
                    showRegistrationDialog();
                }
            });

            Scene scene = new Scene(root, 800, 600);
            applyDarkTheme(scene);
            primaryStage.setScene(scene);

        } catch (Exception e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            dashboardController = loader.getController();
            dashboardController.setCurrentAdmin(currentAdmin);
            dashboardController.setDashboardCallback(new DashboardController.DashboardCallback() {
                @Override public void showDashboard() { loadDashboardHome(); }
                @Override public void showStudentManagement() { loadView("/fxml/students.fxml"); }
                @Override public void showSessionManagement() { loadView("/fxml/sessions.fxml"); }
                @Override public void showAttendance() { loadView("/fxml/attendance.fxml"); }
                @Override public void showReports() { loadDashboardHome(); /* placeholder */ }
                @Override public void showSettings() { loadDashboardHome(); /* placeholder */ }
                @Override public void onLogout() { logout(); }
            });

            Scene scene = new Scene(root, 1200, 700);
            applyDarkTheme(scene);
            primaryStage.setScene(scene);

            loadDashboardHome();

        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboardHome() {
        if (dashboardController == null || dashboardController.getContentPane() == null) return;
        VBox welcome = new VBox(15);
        welcome.setStyle("-fx-padding: 30;");
        welcome.getChildren().addAll(
                new Label("Welcome, " + (currentAdmin != null ? currentAdmin.getUsername() : "Admin")) {{
                    setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
                }},
                new Label("NFC Attendance Pro — Desktop Application") {{
                    setStyle("-fx-font-size: 14;");
                }},
                new Label("Select an option from the left menu to get started.")
        );
        dashboardController.getContentPane().getChildren().clear();
        dashboardController.getContentPane().getChildren().add(welcome);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            dashboardController.getContentPane().getChildren().clear();
            dashboardController.getContentPane().getChildren().add(content);
        } catch (Exception e) {
            System.err.println("Error loading view " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showRegistrationDialog() {
        System.out.println("Registration dialog — placeholder");
    }

    private void logout() {
        currentAdmin = null;
        showLoginScreen();
    }

    private void applyDarkTheme(Scene scene) {
        String css = getClass().getResource("/css/dark.css") != null
                ? getClass().getResource("/css/dark.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }
    }

    /**
     * Align architecture metadata on Windows x64 before native serial libraries initialize.
     * This avoids ARM/native mismatch on some JDK/runtime combinations.
     */
    private void configureSerialNativeRuntime() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        String processorArch = System.getenv("PROCESSOR_ARCHITECTURE");

        if (osName.contains("win") && processorArch != null && processorArch.equalsIgnoreCase("AMD64")) {
            System.setProperty("os.arch", "amd64");
        }
    }

    /**
     * Sends a startup beep command to Arduino in the background.
     * This gives immediate feedback that serial communication is working.
     */
    private void runArduinoStartupSelfTest() {
        Thread startupSignalThread = new Thread(() -> {
            try {
                // Give the UI a moment to stabilize before serial initialization.
                Thread.sleep(500);
                ArduinoController.getInstance().triggerScan();
                System.out.println("[Startup] Arduino buzzer self-test signal sent.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Startup] Arduino self-test interrupted.");
            } catch (RuntimeException e) {
                System.err.println("[Startup] Arduino self-test failed: " + e.getMessage());
            }
        }, "Arduino-Startup-SelfTest");

        startupSignalThread.setDaemon(true);
        startupSignalThread.start();
    }

    private void onApplicationExit() {
        System.out.println("Closing application...");
        try { NFCCardReader.getInstance().closeReader(); } catch (Exception ignored) {}
        try { ArduinoController.getInstance().disconnect(); } catch (Exception ignored) {}
        DatabaseConnection.getInstance().closeConnection();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

