package com.example.accountingapp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CategoryDAO {
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories WHERE user_id = ? ORDER BY type, name";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[CategoryDAO] Загрузка категорий для user_id: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getString("type"));
                category.setColor(rs.getString("color"));
                category.setIcon(rs.getString("icon"));
                category.setUserId(rs.getInt("user_id"));
                categories.add(category);
            }
            System.out.println("[CategoryDAO] Загружено категорий: " + categories.size());
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Ошибка загрузки категорий: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }
    public List<Category> getCategoriesByType(String type) {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories WHERE type = ? AND user_id = ? ORDER BY name";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[CategoryDAO] Загрузка категорий типа '" + type +
                "' для user_id: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, type);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getString("type"));
                category.setColor(rs.getString("color"));
                category.setIcon(rs.getString("icon"));
                category.setUserId(rs.getInt("user_id"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    public boolean addCategory(Category category) {
        String query = "INSERT INTO categories (name, type, color, icon, user_id) VALUES (?, ?, ?, ?, ?)";
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[CategoryDAO] Добавление категории: " + category.getName() +
                " для user_id: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType());
            pstmt.setString(3, category.getColor());
            pstmt.setString(4, category.getIcon());
            pstmt.setInt(5, userId);
            boolean result = pstmt.executeUpdate() > 0;
            System.out.println("[CategoryDAO] Категория " + (result ? "добавлена" : "не добавлена"));
            return result;
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Ошибка добавления категории: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteCategory(int id) {
        String query = "DELETE FROM categories WHERE id = ? AND user_id = ?"; // Добавляем фильтр
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[CategoryDAO] Удаление категории id=" + id +
                " для user_id: " + userId);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            boolean result = pstmt.executeUpdate() > 0;
            System.out.println("[CategoryDAO] Категория " + (result ? "удалена" : "не удалена"));
            return result;
        } catch (SQLException e) {
            System.err.println("[CategoryDAO] Ошибка удаления категории: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public Category getCategoryByName(String name) {
        String query = "SELECT * FROM categories WHERE name = ? AND user_id = ? LIMIT 1";
        int userId = DatabaseConnection.getCurrentUserId();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setType(rs.getString("type"));
                category.setColor(rs.getString("color"));
                category.setIcon(rs.getString("icon"));
                category.setUserId(rs.getInt("user_id"));
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int getCategoryIdByName(String name) {
        Category category = getCategoryByName(name);
        return category != null ? category.getId() : 0;
    }
    public void createDefaultCategoriesForUser(int userId, Connection conn) throws SQLException {
        String[][] categories = {
                {"Зарплата", "income", "#48bb78", "💰"},
                {"Инвестиции", "income", "#4299e1", "📈"},
                {"Подарки", "income", "#d69e2e", "🎁"},
                {"Прочие доходы", "income", "#9f7aea", "💸"},
                {"Продукты", "expense", "#f56565", "🛒"},
                {"Транспорт", "expense", "#ed8936", "🚗"},
                {"Развлечения", "expense", "#d69e2e", "🎬"},
                {"Коммунальные", "expense", "#4299e1", "🏠"},
                {"Одежда", "expense", "#ecc94b", "👕"},
                {"Здоровье", "expense", "#68d391", "💊"},
                {"Образование", "expense", "#4fd1c7", "📚"},
                {"Подарки", "expense", "#f687b3", "🎁"},
                {"Другое", "expense", "#a0aec0", "📝"}
        };
        String query = "INSERT INTO categories (name, type, color, icon, user_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        for (String[] category : categories) {
            pstmt.setString(1, category[0]);
            pstmt.setString(2, category[1]);
            pstmt.setString(3, category[2]);
            pstmt.setString(4, category[3]);
            pstmt.setInt(5, userId);
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        System.out.println("[CategoryDAO] Созданы стандартные категории для пользователя ID: " + userId);
    }
}