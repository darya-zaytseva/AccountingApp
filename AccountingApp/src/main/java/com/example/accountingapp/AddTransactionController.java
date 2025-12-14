package com.example.accountingapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class AddTransactionController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private Label titleLabel;
    private String transactionType;
    private List<Category> categories = new ArrayList<>();
    public void setTransactionType(String type) {
        this.transactionType = type;
        if (typeComboBox != null) {
            typeComboBox.setValue(type.equals("income") ? "Доход" : "Расход");
        }
        if (titleLabel != null) {
            titleLabel.setText(type.equals("income") ? "Добавить доход" : "Добавить расход");
        }
        loadCategories();
    }
    @FXML
    private void initialize() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
        if (typeComboBox != null) {
            ObservableList<String> types = FXCollections.observableArrayList("Доход", "Расход");
            typeComboBox.setItems(types);
            typeComboBox.setValue("Расход");
            typeComboBox.setOnAction(event -> {
                String selectedType = typeComboBox.getValue();
                if (selectedType != null) {
                    transactionType = selectedType.equals("Доход") ? "income" : "expense";
                    loadCategories();
                }
            });
        }
        transactionType = "expense";
        loadCategories();
    }
    private void loadCategories() {
        if (categoryComboBox == null) return;
        categories.clear();
        categoryComboBox.getItems().clear();
        int userId = DatabaseConnection.getCurrentUserId();
        System.out.println("[AddTransactionController] Загрузка категорий для user_id: " + userId);
        if (userId == 0) {
            System.err.println("[AddTransactionController] ОШИБКА: user_id = 0! Пользователь не аутентифицирован.");
            categoryComboBox.getItems().add("Ошибка: пользователь не найден");
            categoryComboBox.setValue("Ошибка: пользователь не найден");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM categories WHERE type = ? AND user_id = ? ORDER BY name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, transactionType);
            pstmt.setInt(2, userId);  // Фильтр по user_id
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
                categoryComboBox.getItems().add(category.getName());
            }
            if (!categoryComboBox.getItems().isEmpty()) {
                categoryComboBox.setValue(categoryComboBox.getItems().get(0));
                System.out.println("[AddTransactionController] Загружено категорий: " + categories.size());
            } else {
                categoryComboBox.getItems().add("Нет категорий - добавьте сначала");
                categoryComboBox.setValue("Нет категорий - добавьте сначала");
                System.out.println("[AddTransactionController] Нет категорий для типа: " + transactionType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить категории: " + e.getMessage());
        }
    }
    public boolean saveTransaction() {
        try {
            System.out.println("\n=== [AddTransactionController] СОХРАНЕНИЕ ТРАНЗАКЦИИ ===");
            int userId = DatabaseConnection.getCurrentUserId();
            System.out.println("[AddTransactionController] Текущий user_id: " + userId);
            if (userId == 0) {
                showAlert("Ошибка", "Пользователь не авторизован. Перезайдите в систему.");
                return false;
            }
            if (amountField.getText() == null || amountField.getText().trim().isEmpty()) {
                showAlert("Ошибка", "Введите сумму");
                return false;
            }
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory == null ||
                    selectedCategory.contains("Нет категорий") ||
                    selectedCategory.contains("Ошибка")) {
                showAlert("Ошибка", "Сначала добавьте категории в соответствующем разделе");
                return false;
            }
            if (datePicker.getValue() == null) {
                showAlert("Ошибка", "Выберите дату");
                return false;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().replace(",", "."));
                if (amount <= 0) {
                    showAlert("Ошибка", "Сумма должна быть больше 0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Ошибка", "Введите корректную сумму (например: 1500.50)");
                return false;
            }
            String categoryName = selectedCategory;
            String description = descriptionField.getText();
            LocalDate date = datePicker.getValue();
            String type = typeComboBox.getValue().equals("Доход") ? "income" : "expense";
            System.out.println("[AddTransactionController] Тип операции: " + type);
            System.out.println("[AddTransactionController] Сумма: " + amount);
            System.out.println("[AddTransactionController] Категория: " + categoryName);
            System.out.println("[AddTransactionController] Дата: " + date);
            int categoryId = 0;
            for (Category cat : categories) {
                if (cat.getName().equals(categoryName)) {
                    categoryId = cat.getId();
                    break;
                }
            }
            if (categoryId == 0) {
                CategoryDAO categoryDAO = new CategoryDAO();
                Category category = categoryDAO.getCategoryByName(categoryName);
                if (category != null) {
                    categoryId = category.getId();
                    System.out.println("[AddTransactionController] Найдена категория в БД, ID: " + categoryId);
                }
            }
            if (categoryId == 0) {
                showAlert("Ошибка", "Категория '" + categoryName + "' не найдена для вашего пользователя");
                return false;
            }
            System.out.println("[AddTransactionController] ID категории: " + categoryId);
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO transactions (category_id, amount, description, type, transaction_date, user_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, categoryId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, description);
                pstmt.setString(4, type);
                pstmt.setDate(5, java.sql.Date.valueOf(date));
                pstmt.setInt(6, userId);
                int rows = pstmt.executeUpdate();
                System.out.println("[AddTransactionController] Вставлено строк: " + rows);
                if (rows > 0) {
                    System.out.println("[AddTransactionController] Обновление баланса...");
                    BalanceDAO balanceDAO = new BalanceDAO();
                    boolean balanceUpdated = balanceDAO.updateBalance(amount, type);
                    System.out.println("[AddTransactionController] Баланс обновлен: " + balanceUpdated);
                    if (type.equals("expense")) {
                        System.out.println("[AddTransactionController] Обновление бюджета...");
                        BudgetDAO budgetDAO = new BudgetDAO();
                        String monthYear = String.format("%04d-%02d", date.getYear(), date.getMonthValue());
                        System.out.println("[AddTransactionController] Месяц для бюджета: " + monthYear);
                        boolean budgetUpdated = budgetDAO.updateSpentAmount(categoryId, monthYear, amount);
                        System.out.println("[AddTransactionController] Бюджет обновлен: " + budgetUpdated);
                        if (!budgetUpdated) {
                            System.out.println("[AddTransactionController] Бюджет не найден для категории " + categoryId);
                        }
                    }
                    showAlert("Успех", "Транзакция сохранена!\n" +
                            "Сумма: " + String.format("%.2f ₽", amount) + "\n" +
                            "Тип: " + (type.equals("income") ? "Доход" : "Расход") + "\n" +
                            "Категория: " + categoryName);
                    return true;
                } else {
                    showAlert("Ошибка", "Не удалось сохранить транзакцию");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректную сумму (например: 1500.50)");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сохранить транзакцию: " + e.getMessage());
        }
        return false;
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (title.equals("Ошибка")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.showAndWait();
    }
    @FXML
    private void handleTypeChange() {
        String selectedType = typeComboBox.getValue();
        if (selectedType != null) {
            transactionType = selectedType.equals("Доход") ? "income" : "expense";
            System.out.println("[AddTransactionController] Изменен тип на: " + transactionType);
            loadCategories();
        }
    }
}