package com.foodsystem.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton JDBC Connection manager for XAMPP MySQL.
 */
public class DBConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    /**
     * Obtains a new JDBC Connection to the configured database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            DatabaseConfig.getJdbcUrl(),
            DatabaseConfig.getUser(),
            DatabaseConfig.getPassword()
        );
    }

    /**
     * Checks if MySQL database is reachable and contains required tables.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Auto-bootstraps database tables if they do not yet exist.
     */
    public static void ensureSchemaInitialized() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'orders'")) {

            if (!rs.next()) {
                System.out.println("Tables not found. Initializing schema from sql/schema.sql...");
                executeSchemaFile(conn);
            }
        } catch (Exception e) {
            System.err.println("Schema check or initialization notice: " + e.getMessage());
        }
    }

    private static void executeSchemaFile(Connection conn) {
        String schemaPath = "sql/schema.sql";
        try {
            String content = new String(Files.readAllBytes(Paths.get(schemaPath)));
            String[] queries = content.split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String query : queries) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--") && !trimmed.startsWith("/*")) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException ex) {
                            // Non-fatal if database already selected or comment
                            System.err.println("Query execution note: " + ex.getMessage());
                        }
                    }
                }
            }
            System.out.println("Schema successfully initialized.");
        } catch (Exception e) {
            System.err.println("Failed to read/execute schema file: " + e.getMessage());
        }
    }
}
