package com.foodsystem.config;

/**
 * Database configuration settings for XAMPP MySQL.
 */
public class DatabaseConfig {
    private static String host = "127.0.0.1";
    private static int port = 3306;
    private static String database = "food_ordering_db";
    private static String user = "root";
    private static String password = "";

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
               "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public static String getHost() { return host; }
    public static void setHost(String newHost) { host = newHost; }

    public static int getPort() { return port; }
    public static void setPort(int newPort) { port = newPort; }

    public static String getDatabase() { return database; }
    public static void setDatabase(String newDatabase) { database = newDatabase; }

    public static String getUser() { return user; }
    public static void setUser(String newUser) { user = newUser; }

    public static String getPassword() { return password; }
    public static void setPassword(String newPassword) { password = newPassword; }
}
