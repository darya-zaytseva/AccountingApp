package com.example.accountingapp;
import java.sql.*;
public class DatabaseConnection {
    private static Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/home_finance";
    private static final String USER = "root";
    private static final String PASSWORD = "yynao-YAY22";
    private static int currentUserId = 0;
    private static String currentUsername = "";
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DatabaseConnection] Создание нового подключения к БД...");
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DatabaseConnection] Подключение к БД успешно создано");
            }
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DatabaseConnection] Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public static void setCurrentUser(int userId, String username) {
        currentUserId = userId;
        currentUsername = username;
        System.out.println("[DatabaseConnection] Установлен текущий пользователь: " +
                username + " (ID: " + userId + ")");
    }
    public static int getCurrentUserId() {
        System.out.println("[DatabaseConnection] Запрошен user_id: " + currentUserId);
        if (currentUserId == 0) {
            System.err.println("[DatabaseConnection] ВНИМАНИЕ: user_id = 0! Проверьте аутентификацию.");
        }
        return currentUserId;
    }
    public static String getCurrentUsername() {
        return currentUsername;
    }
}