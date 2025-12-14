package com.example.accountingapp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink loginLink;
    private UserDAO userDAO = new UserDAO();
    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }
    @FXML
    private void register() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }
        if (username.length() < 3) {
            showError("Имя пользователя должно содержать минимум 3 символа");
            return;
        }
        if (!isValidEmail(email)) {
            showError("Введите корректный email");
            return;
        }
        if (password.length() < 6) {
            showError("Пароль должен содержать минимум 6 символов");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Пароли не совпадают");
            return;
        }
        if (userDAO.userExists(username)) {
            showError("Пользователь с таким именем уже существует");
            return;
        }
        try {
            boolean success = userDAO.registerUser(username, email, password);
            if (success) {
                User user = userDAO.getUserByUsername(username);
                if (user != null) {
                    DatabaseConnection.setCurrentUser(user.getId(), user.getUsername());
                }
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.close();
                launchMainApp();
            } else {
                showError("Ошибка регистрации пользователя");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка подключения к базе данных");
        }
    }
    @FXML
    private void showLoginForm() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMinWidth(500);
            stage.setMinHeight(600);
            stage.setMaxWidth(500);
            stage.setMaxHeight(600);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось загрузить форму входа");
        }
    }
    private void launchMainApp() {
        try {
            Stage primaryStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("Домашняя бухгалтерия - " + usernameField.getText());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Не удалось запустить приложение");
        }
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    @FXML
    private void clearError() {
        errorLabel.setVisible(false);
    }
}