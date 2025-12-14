package com.example.accountingapp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;
    private UserDAO userDAO = new UserDAO();
    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }
    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Введите имя пользователя и пароль");
            return;
        }
        try {
            User user = userDAO.authenticateUser(username, password);
            if (user != null) {
                DatabaseConnection.setCurrentUser(user.getId(), user.getUsername());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.close();
                launchMainApp();
            } else {
                showError("Неверное имя пользователя или пароль");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }
    @FXML
    private void showRegisterForm() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
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
            showError("Не удалось загрузить форму регистрации");
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
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    @FXML
    private void clearError() {
        errorLabel.setVisible(false);
    }
}