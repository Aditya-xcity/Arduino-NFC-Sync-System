package com.nfc.attendance.service;

import com.nfc.attendance.model.Admin;
import com.nfc.attendance.repository.AdminRepository;

/**
 * Admin Service — authentication and admin management
 */
public class AdminService {

    private AdminRepository adminRepository;

    public AdminService() {
        this.adminRepository = new AdminRepository();
    }

    public Admin authenticate(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        Admin admin = adminRepository.findByUsernameAndPassword(username, password);
        if (admin != null) {
            System.out.println("Admin authenticated: " + username);
        } else {
            System.out.println("Authentication failed for: " + username);
        }
        return admin;
    }

    public boolean register(String username, String password, String confirmPassword) {
        if (username == null || username.length() < 3) {
            System.out.println("Error: Username must be at least 3 characters");
            return false;
        }
        if (password == null || password.length() < 6) {
            System.out.println("Error: Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            System.out.println("Error: Passwords do not match");
            return false;
        }
        if (adminRepository.findByUsername(username) != null) {
            System.out.println("Error: Username already exists");
            return false;
        }
        boolean success = adminRepository.create(new Admin(username, password));
        if (success) System.out.println("Admin registered: " + username);
        return success;
    }

    public boolean changePassword(int adminId, String oldPassword, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("Error: New password must be at least 6 characters");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Error: Passwords do not match");
            return false;
        }
        Admin admin = new Admin(adminId, "", newPassword);
        return adminRepository.update(admin);
    }
}

