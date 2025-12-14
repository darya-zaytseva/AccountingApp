package com.example.accountingapp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class BudgetDAO {
    public List<Budget> getBudgetsForMonth(String monthYear) {
        List<Budget> budgets = new ArrayList<>();
        String query = "SELECT b.*, c.name as category_name FROM budgets b " +
                "LEFT JOIN categories c ON b.category_id = c.id " +
                "WHERE b.month_year = ? AND b.user_id = ? " +
                "AND c.type = 'expense' " +
                "ORDER BY c.name";
        System.out.println("[BudgetDAO] Загрузка бюджетов за " + monthYear +
                " для user_id: " + DatabaseConnection.getCurrentUserId());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, monthYear);
            pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getInt("id"));
                budget.setCategoryId(rs.getInt("category_id"));
                budget.setCategoryName(rs.getString("category_name"));
                budget.setMonthYear(rs.getString("month_year"));
                budget.setAllocatedAmount(rs.getDouble("allocated_amount"));
                budget.setSpentAmount(rs.getDouble("spent_amount"));
                budget.setUserId(rs.getInt("user_id"));
                budgets.add(budget);
                System.out.println("[BudgetDAO] Найден бюджет: " + budget.getCategoryName() +
                        " - Выделено: " + budget.getAllocatedAmount() +
                        ", Потрачено: " + budget.getSpentAmount());
            }
            System.out.println("[BudgetDAO] Загружено бюджетов: " + budgets.size());
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] Ошибка загрузки бюджетов: " + e.getMessage());
            e.printStackTrace();
        }
        return budgets;
    }
    public boolean saveBudget(Budget budget) {
        boolean exists = checkBudgetExists(budget.getCategoryId(), budget.getMonthYear());
        String query;
        if (exists) {
            query = "UPDATE budgets SET allocated_amount = ?, spent_amount = ? " +
                    "WHERE category_id = ? AND month_year = ? AND user_id = ?";
        } else {
            query = "INSERT INTO budgets (category_id, month_year, allocated_amount, spent_amount, user_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        }
        System.out.println("[BudgetDAO] " + (exists ? "Обновление" : "Создание") +
                " бюджета: категория=" + budget.getCategoryId() +
                ", месяц=" + budget.getMonthYear() +
                ", сумма=" + budget.getAllocatedAmount() +
                ", user_id=" + DatabaseConnection.getCurrentUserId());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (exists) {
                pstmt.setDouble(1, budget.getAllocatedAmount());
                pstmt.setDouble(2, budget.getSpentAmount());
                pstmt.setInt(3, budget.getCategoryId());
                pstmt.setString(4, budget.getMonthYear());
                pstmt.setInt(5, DatabaseConnection.getCurrentUserId());
            } else {
                pstmt.setInt(1, budget.getCategoryId());
                pstmt.setString(2, budget.getMonthYear());
                pstmt.setDouble(3, budget.getAllocatedAmount());
                pstmt.setDouble(4, budget.getSpentAmount());
                pstmt.setInt(5, DatabaseConnection.getCurrentUserId());
            }
            int rowsAffected = pstmt.executeUpdate();
            boolean result = rowsAffected > 0;
            System.out.println("[BudgetDAO] Бюджет " + (result ? "сохранен" : "не сохранен") +
                    " (строк: " + rowsAffected + ")");
            return result;
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] Ошибка сохранения бюджета: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private boolean checkBudgetExists(int categoryId, String monthYear) {
        String query = "SELECT COUNT(*) as count FROM budgets " +
                "WHERE category_id = ? AND month_year = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            pstmt.setString(2, monthYear);
            pstmt.setInt(3, DatabaseConnection.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean updateSpentAmount(int categoryId, String monthYear, double amount) {
        String query = "UPDATE budgets SET spent_amount = spent_amount + ? " +
                "WHERE category_id = ? AND month_year = ? AND user_id = ?";
        System.out.println("[BudgetDAO] Обновление потраченной суммы: category=" + categoryId +
                ", month=" + monthYear + ", amount=" + amount +
                ", user_id=" + DatabaseConnection.getCurrentUserId());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, categoryId);
            pstmt.setString(3, monthYear);
            pstmt.setInt(4, DatabaseConnection.getCurrentUserId());
            int rowsAffected = pstmt.executeUpdate();
            boolean result = rowsAffected > 0;
            if (result) {
                System.out.println("[BudgetDAO] Потраченная сумма обновлена успешно");
            } else {
                System.out.println("[BudgetDAO] Бюджет для обновления не найден. " +
                        "Создайте бюджет для категории " + categoryId);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] Ошибка обновления бюджета: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public Budget getBudgetForCategory(int categoryId, String monthYear) {
        String query = "SELECT b.*, c.name as category_name FROM budgets b " +
                "LEFT JOIN categories c ON b.category_id = c.id " +
                "WHERE b.category_id = ? AND b.month_year = ? AND b.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            pstmt.setString(2, monthYear);
            pstmt.setInt(3, DatabaseConnection.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getInt("id"));
                budget.setCategoryId(rs.getInt("category_id"));
                budget.setCategoryName(rs.getString("category_name"));
                budget.setMonthYear(rs.getString("month_year"));
                budget.setAllocatedAmount(rs.getDouble("allocated_amount"));
                budget.setSpentAmount(rs.getDouble("spent_amount"));
                return budget;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean syncBudgetsWithRealExpenses(int year, int month) {
        String monthYear = String.format("%04d-%02d", year, month);
        System.out.println("[BudgetDAO] Синхронизация бюджетов с реальными расходами за " + monthYear +
                " для user_id: " + DatabaseConnection.getCurrentUserId());
        TransactionDAO transactionDAO = new TransactionDAO();
        List<TransactionDAO.CategoryExpense> realExpenses =
                transactionDAO.getCategoryExpensesList(year, month);
        if (realExpenses == null || realExpenses.isEmpty()) {
            System.out.println("[BudgetDAO] Нет реальных расходов для синхронизации");
            return false;
        }
        CategoryDAO categoryDAO = new CategoryDAO();
        int updated = 0;
        for (TransactionDAO.CategoryExpense expense : realExpenses) {
            int categoryId = categoryDAO.getCategoryIdByName(expense.getCategoryName());
            if (categoryId > 0) {
                Budget existingBudget = getBudgetForCategory(categoryId, monthYear);
                if (existingBudget != null) {
                    existingBudget.setSpentAmount(expense.getAmount());
                    if (saveBudget(existingBudget)) {
                        updated++;
                        System.out.println("[BudgetDAO] Обновлен бюджет для " + expense.getCategoryName() +
                                ": потрачено " + expense.getAmount());
                    }
                } else {
                    Budget newBudget = new Budget();
                    newBudget.setCategoryId(categoryId);
                    newBudget.setMonthYear(monthYear);
                    newBudget.setAllocatedAmount(expense.getAmount() * 1.3); // +30%
                    newBudget.setSpentAmount(expense.getAmount());
                    newBudget.setUserId(DatabaseConnection.getCurrentUserId());
                    if (saveBudget(newBudget)) {
                        updated++;
                        System.out.println("[BudgetDAO] Создан бюджет для " + expense.getCategoryName() +
                                " на основе реальных расходов");
                    }
                }
            }
        }
        System.out.println("[BudgetDAO] Обновлено/создано бюджетов: " + updated);
        return updated > 0;
    }
    public boolean deleteAllBudgetsForMonth(String monthYear) {
        String query = "DELETE FROM budgets WHERE month_year = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, monthYear);
            pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("[BudgetDAO] Удалено бюджетов за " + monthYear + ": " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] Ошибка удаления бюджетов: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteBudgetForCategory(int categoryId, String monthYear) {
        String query = "DELETE FROM budgets WHERE category_id = ? AND month_year = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            pstmt.setString(2, monthYear);
            pstmt.setInt(3, DatabaseConnection.getCurrentUserId());
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("[BudgetDAO] Удален бюджет для категории " + categoryId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[BudgetDAO] Ошибка удаления бюджета: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public double getTotalBudgetForMonth(String monthYear) {
        String query = "SELECT COALESCE(SUM(allocated_amount), 0) as total FROM budgets " +
                "WHERE month_year = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, monthYear);
            pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    public double getTotalSpentForMonth(String monthYear) {
        String query = "SELECT COALESCE(SUM(spent_amount), 0) as total FROM budgets " +
                "WHERE month_year = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, monthYear);
            pstmt.setInt(2, DatabaseConnection.getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}