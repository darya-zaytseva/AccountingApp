package com.example.accountingapp;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class TransactionDAO {
    public boolean addTransaction(Transaction transaction) {
        String query = "INSERT INTO transactions (category_id, amount, description, type, transaction_date, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[TransactionDAO] Добавление транзакции: " +
                transaction.getType() + " на " + transaction.getAmount() +
                " для user_id: " + userId);
        if (userId == 0) {
            System.err.println("[TransactionDAO] ОШИБКА: user_id = 0!");
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, transaction.getCategoryId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getType());
            pstmt.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            pstmt.setInt(6, userId);
            boolean result = pstmt.executeUpdate() > 0;
            System.out.println("[TransactionDAO] Транзакция " + (result ? "добавлена" : "не добавлена"));
            return result;
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Ошибка добавления транзакции: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public List<Transaction> getTransactionsByMonth(int year, int month) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT t.*, c.name as category_name FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE YEAR(t.transaction_date) = ? AND MONTH(t.transaction_date) = ? " +
                "AND t.user_id = ? " +
                "ORDER BY t.transaction_date DESC";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[TransactionDAO] Загрузка транзакций за " + year + "-" + month +
                " для user_id: " + userId);
        if (userId == 0) {
            System.err.println("[TransactionDAO] ОШИБКА: user_id = 0!");
            return transactions;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getInt("id"));
                transaction.setCategoryId(rs.getInt("category_id"));
                transaction.setCategoryName(rs.getString("category_name"));
                transaction.setAmount(rs.getDouble("amount"));
                transaction.setDescription(rs.getString("description"));
                transaction.setType(rs.getString("type"));
                transaction.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
                transactions.add(transaction);
                count++;
            }
            System.out.println("[TransactionDAO] Загружено транзакций: " + count);
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Ошибка загрузки транзакций: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }
    public double getTotalIncome(int year, int month) {
        double result = getTotalByType("income", year, month);
        System.out.println("[TransactionDAO] Доходы за " + year + "-" + month + ": " + result +
                " для user_id: " + DatabaseConnection.getCurrentUserId());
        return result;
    }
    public double getTotalExpense(int year, int month) {
        double result = getTotalByType("expense", year, month);
        System.out.println("[TransactionDAO] Расходы за " + year + "-" + month + ": " + result +
                " для user_id: " + DatabaseConnection.getCurrentUserId());
        return result;
    }
    private double getTotalByType(String type, int year, int month) {
        String query = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions " +
                "WHERE type = ? AND YEAR(transaction_date) = ? AND MONTH(transaction_date) = ? " +
                "AND user_id = ?";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[TransactionDAO] Запрос " + type + " за " + year + "-" + month +
                " для user_id: " + userId);
        if (userId == 0) {
            System.err.println("[TransactionDAO] ОШИБКА: user_id = 0!");
            return 0.0;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, type);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);
            pstmt.setInt(4, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.println("[TransactionDAO] Найдено " + type + ": " + total);
                return total;
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Ошибка getTotalByType: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
    public Map<String, Double> getExpensesByCategory(int year, int month) {
        Map<String, Double> expenses = new HashMap<>();
        String query = "SELECT c.name, COALESCE(SUM(t.amount), 0) as total " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.type = 'expense' " +
                "AND YEAR(t.transaction_date) = ? " +
                "AND MONTH(t.transaction_date) = ? " +
                "AND t.user_id = ? " +
                "GROUP BY c.name";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[TransactionDAO] Получение расходов по категориям за " + year + "-" + month +
                " для user_id: " + userId);
        if (userId == 0) {
            System.err.println("[TransactionDAO] ОШИБКА: user_id = 0!");
            return expenses;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                expenses.put(rs.getString("name"), rs.getDouble("total"));
                count++;
                System.out.println("[TransactionDAO] Категория: " + rs.getString("name") +
                        " = " + rs.getDouble("total"));
            }
            System.out.println("[TransactionDAO] Найдено категорий с расходами: " + count);
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Ошибка getExpensesByCategory: " + e.getMessage());
            e.printStackTrace();
        }
        return expenses;
    }
    public List<CategoryExpense> getCategoryExpensesList(int year, int month) {
        List<CategoryExpense> expenses = new ArrayList<>();
        String query = "SELECT c.name, COALESCE(SUM(t.amount), 0) as total " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.type = 'expense' " +
                "AND YEAR(t.transaction_date) = ? " +
                "AND MONTH(t.transaction_date) = ? " +
                "AND t.user_id = ? " +
                "GROUP BY c.name " +
                "ORDER BY total DESC";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[TransactionDAO] Получение списка расходов по категориям за " + year + "-" + month +
                " для user_id: " + userId);
        if (userId == 0) {
            System.err.println("[TransactionDAO] ОШИБКА: user_id = 0!");
            return expenses;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CategoryExpense expense = new CategoryExpense(
                        rs.getString("name"),
                        rs.getDouble("total")
                );
                expenses.add(expense);
                System.out.println("[TransactionDAO] " + expense.getCategoryName() +
                        ": " + expense.getAmount());
            }
            System.out.println("[TransactionDAO] Всего категорий: " + expenses.size());
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Ошибка getCategoryExpensesList: " + e.getMessage());
            e.printStackTrace();
        }
        return expenses;
    }
    public static class CategoryExpense {
        private final String categoryName;
        private final double amount;
        public CategoryExpense(String categoryName, double amount) {
            this.categoryName = categoryName;
            this.amount = amount;
        }
        public String getCategoryName() { return categoryName; }
        public double getAmount() { return amount; }
    }
}