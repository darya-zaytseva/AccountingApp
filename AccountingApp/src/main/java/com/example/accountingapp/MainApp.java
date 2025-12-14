package com.example.accountingapp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        showLoginWindow(primaryStage);
    }
    private void showLoginWindow(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle("Домашняя бухгалтерия - Вход");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMinWidth(500);
            stage.setMinHeight(600);
            stage.setMaxWidth(500);
            stage.setMaxHeight(600);
            stage.centerOnScreen();
            stage.show();
            stage.setOnCloseRequest(event -> {
                System.out.println("[MainApp] Приложение закрывается...");
                if (DatabaseConnection.getConnection() != null) {
                    try {
                        DatabaseConnection.getConnection().close();
                        System.out.println("[MainApp] Соединение с БД закрыто");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert(stage);
        }
    }
    private void showErrorAlert(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Не удалось запустить приложение");
        alert.setContentText("Проверьте наличие файлов FXML и подключение к базе данных.");
        alert.showAndWait();
        stage.close();
    }
    public static void main(String[] args) {
        System.out.println("=== Запуск Домашней бухгалтерии ===");
        System.out.println("Размер окна входа: 500x600");
        launch(args);
    }
}