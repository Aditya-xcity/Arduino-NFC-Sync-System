package com.nfc.attendance.controller;

import com.nfc.attendance.model.Admin;
import com.nfc.attendance.service.AdminService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Login Controller — handles admin authentication
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AdminService adminService;
    private LoginCallback loginCallback;

    @FXML
    public void initialize() {
        this.adminService = new AdminService();
    }

    @FXML
    public void onLoginClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        Admin admin = adminService.authenticate(username, password);
        if (admin != null) {
            if (loginCallback != null) loginCallback.onLoginSuccess(admin);
        } else {
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }

    @FXML
    public void onRegisterClicked() {
        if (loginCallback != null) loginCallback.onRegisterClicked();
    }

    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }

    public interface LoginCallback {
        void onLoginSuccess(Admin admin);
        void onRegisterClicked();
    }
}

