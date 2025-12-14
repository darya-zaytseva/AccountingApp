package com.example.accountingapp;
import java.sql.*;
public class BalanceDAO {
    public double getCurrentBalance() {
        String query = "SELECT total_balance FROM balance WHERE user_id = ?";
        System.out.println("[BalanceDAO] Получение баланса для user_id: " + DatabaseConnection.getCurrentUserId());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, DatabaseConnection.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("total_balance");
                System.out.println("[BalanceDAO] Текущий баланс: " + balance);
                return balance;
            } else {
                System.out.println("[BalanceDAO] Запись баланса не найдена, создаем новую");
                String insert = "INSERT INTO balance (total_balance, user_id) VALUES (0.00, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insert);
                insertStmt.setInt(1, DatabaseConnection.getCurrentUserId());
                insertStmt.executeUpdate();
                return 0.0;
            }
        } catch (SQLException e) {
            System.err.println("[BalanceDAO] Ошибка получения баланса: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    public boolean updateBalance(double amount, String type) {
        System.out.println("[BalanceDAO] Обновление баланса: " + type + " на сумму " + amount +
                " для user_id: " + DatabaseConnection.getCurrentUserId());
        double currentBalance = getCurrentBalance();
        System.out.println("[BalanceDAO] Текущий баланс до операции: " + currentBalance);
        String operator = type.equals("income") ? "+" : "-";
        String checkQuery = "SELECT COUNT(*) as count FROM balance WHERE user_id = ?";
        String updateQuery = "UPDATE balance SET total_balance = total_balance " + operator + " ?, last_updated = NOW() WHERE user_id = ?";
        String insertQuery = "INSERT INTO balance (total_balance, user_id, last_updated) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, DatabaseConnection.getCurrentUserId());
            ResultSet rs = checkStmt.executeQuery();
            boolean recordExists = false;
            if (rs.next()) {
                recordExists = rs.getInt("count") > 0;
            }
            PreparedStatement pstmt;
            if (recordExists) {
                pstmt = conn.prepareStatement(updateQuery);
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            } else {
                double initialBalance = type.equals("income") ? amount : -amount;
                pstmt = conn.prepareStatement(insertQuery);
                pstmt.setDouble(1, initialBalance);
                pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            }
            int rowsUpdated = pstmt.executeUpdate();
            boolean result = rowsUpdated > 0;
            if (result) {
                double newBalance = getCurrentBalance();
                System.out.println("[BalanceDAO] Баланс успешно обновлен. Новое значение: " + newBalance);
            } else {
                System.err.println("[BalanceDAO] Не удалось обновить баланс! Строк обновлено: " + rowsUpdated);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("[BalanceDAO] Ошибка updateBalance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean initializeBalance() {
        String query = "INSERT INTO balance (total_balance, user_id, last_updated) VALUES (0.00, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE last_updated = NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, DatabaseConnection.getCurrentUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean resetBalance(double newBalance) {
        String query = "UPDATE balance SET total_balance = ?, last_updated = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            boolean result = pstmt.executeUpdate() > 0;
            System.out.println("[BalanceDAO] Баланс сброшен до: " + newBalance);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void createInitialBalanceForUser(int userId, Connection conn) throws SQLException {
        String query = "INSERT INTO balance (total_balance, user_id, last_updated) VALUES (0.00, ?, NOW())";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        System.out.println("[BalanceDAO] Создан начальный баланс для пользователя ID: " + userId);
    }
}