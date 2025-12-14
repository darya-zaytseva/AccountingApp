package com.example.accountingapp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {
    public boolean registerUser(String username, String email, String password) {
        String checkQuery = "SELECT COUNT(*) as count FROM users WHERE username = ? OR email = ?";
        String insertQuery = "INSERT INTO users (username, email, password_hash, created_at) VALUES (?, ?, ?, ?)";
        System.out.println("[UserDAO] Регистрация пользователя: " + username);
        try (Connection conn = getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("[UserDAO] Пользователь уже существует");
                return false;
            }
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, password);
            insertStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    System.out.println("[UserDAO] Новый пользователь создан с ID: " + userId);
                    createDefaultCategoriesForUser(userId, conn);
                    createInitialBalanceForUser(userId, conn);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Ошибка регистрации: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        System.out.println("[UserDAO] Аутентификация пользователя: " + username);
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                System.out.println("[UserDAO] Пользователь найден: " + username + " (ID: " + user.getId() + ")");
                return user;
            } else {
                System.out.println("[UserDAO] Пользователь не найден: " + username);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Ошибка аутентификации: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean userExists(String username) {
        String query = "SELECT COUNT(*) as count FROM users WHERE username = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void createDefaultCategoriesForUser(int userId, Connection conn) throws SQLException {
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
        System.out.println("[UserDAO] Созданы стандартные категории для пользователя ID: " + userId);
    }
    private void createInitialBalanceForUser(int userId, Connection conn) throws SQLException {
        String query = "INSERT INTO balance (total_balance, user_id, last_updated) VALUES (0.00, ?, NOW())";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        System.out.println("[UserDAO] Создан начальный баланс для пользователя ID: " + userId);
    }
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}