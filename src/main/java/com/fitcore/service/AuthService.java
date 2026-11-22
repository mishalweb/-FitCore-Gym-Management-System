package com.fitcore.service;

import com.fitcore.config.Database;
import com.fitcore.security.PasswordUtil;
import java.sql.*;

public final class AuthService {
    public String authenticate(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null) return null;
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "SELECT full_name, password_hash FROM admins WHERE email=? AND is_active=TRUE")) {
            s.setString(1, email.strip().toLowerCase());
            try (ResultSet r = s.executeQuery()) {
                if (r.next() && PasswordUtil.verify(password, r.getString("password_hash"))) {
                    return r.getString("full_name");
                }
            }
        }
        return null;
    }
}
