package com.fitcore.tools;

import com.fitcore.config.Database;
import com.fitcore.security.PasswordUtil;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public final class AdminSetup {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            Console console = System.console();
            System.out.print("Admin full name: ");
            String name = scanner.nextLine().strip();
            System.out.print("Admin email: ");
            String email = scanner.nextLine().strip().toLowerCase();
            String password;
            if (console != null) {
                char[] raw = console.readPassword("Admin password (8+ characters): ");
                password = new String(raw);
                java.util.Arrays.fill(raw, '\0');
            } else {
                System.out.println("Warning: your password input may be visible in this terminal.");
                System.out.print("Admin password (8+ characters): ");
                password = scanner.nextLine();
            }
            if (name.isBlank() || !email.contains("@") || password.length() < 8)
                throw new IllegalArgumentException("Provide a name, valid email and password with 8+ characters.");
            String hash = PasswordUtil.hash(password);
            try (Connection c = Database.connect();
                 PreparedStatement s = c.prepareStatement(
                         "INSERT INTO admins(full_name, email, password_hash) VALUES(?,?,?)")) {
                s.setString(1, name);
                s.setString(2, email);
                s.setString(3, hash);
                s.executeUpdate();
            }
            System.out.println("Admin account created. Run mvn javafx:run to log in.");
        } catch (Exception e) {
            System.err.println("Cannot create admin: " + e.getMessage());
            System.exit(1);
        }
    }
}
