package com.fitcore.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/fitcore_gym_db?serverTimezone=UTC";
    private Database() { }

    public static Connection connect() throws SQLException {
        String username = System.getenv("FITCORE_DB_USER");
        String password = System.getenv("FITCORE_DB_PASSWORD");
        String url = System.getenv("FITCORE_DB_URL");
        if (username == null || username.isBlank() || password == null) {
            throw new SQLException("Set FITCORE_DB_USER and FITCORE_DB_PASSWORD in your terminal first.");
        }
        return DriverManager.getConnection(url == null || url.isBlank() ? DEFAULT_URL : url,
                username, password);
    }
}
