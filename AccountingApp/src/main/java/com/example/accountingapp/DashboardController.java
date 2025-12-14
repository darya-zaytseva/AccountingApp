package com.example.accountingapp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class DashboardController {
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthIncomeLabel;
    @FXML private Label monthExpenseLabel;
    @FXML private Label currentMonthLabel;
    @FXML private ProgressBar monthProgressBar;
    @FXML private Label daysLeftLabel;
    @FXML private StackPane contentPane;
    private BalanceDAO balanceDAO = new BalanceDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    @FXML
    public void initialize() {
        System.out.println("\n=== [DashboardController] ИНИЦИАЛИЗАЦИЯ ===");
        System.out.println("[DashboardController] Текущий пользователь: " +
                DatabaseConnection.getCurrentUsername() +
                " (ID: " + DatabaseConnection.getCurrentUserId() + ")");
        try {
            updateDashboard();
            showOverview();
            System.out.println("[DashboardController] Инициализация завершена успешно\n");
        } catch (Exception e) {
            System.err.println("[DashboardController] КРИТИЧЕСКАЯ ОШИБКА инициализации: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void updateDashboard() {
        System.out.println("\n=== [DashboardController] ОБНОВЛЕНИЕ ДАШБОРДА ===");
        try {
            updateCurrentMonthInfo();
            updateBalanceInfo();
            System.out.println("[DashboardController] Дашборд успешно обновлен\n");
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка обновления дашборда: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void updateCurrentMonthInfo() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        currentMonthLabel.setText(now.format(formatter));
        int daysInMonth = now.lengthOfMonth();
        int dayOfMonth = now.getDayOfMonth();
        int daysLeft = daysInMonth - dayOfMonth;
        daysLeftLabel.setText("Дней осталось: " + daysLeft);
        monthProgressBar.setProgress((double) dayOfMonth / daysInMonth);
        System.out.println("[DashboardController] Текущий месяц: " + now.format(formatter));
        System.out.println("[DashboardController] Дней прошло: " + dayOfMonth + ", осталось: " + daysLeft);
    }
    private void updateBalanceInfo() {
        try {
            System.out.println("[DashboardController] Получение баланса...");
            double balance = balanceDAO.getCurrentBalance();
            totalBalanceLabel.setText(String.format("%.2f ₽", balance));
            System.out.println("[DashboardController] Баланс: " + balance);
            LocalDate now = LocalDate.now();
            System.out.println("[DashboardController] Получение доходов за " + now.getYear() + "-" + now.getMonthValue());
            double income = transactionDAO.getTotalIncome(now.getYear(), now.getMonthValue());
            System.out.println("[DashboardController] Получение расходов за " + now.getYear() + "-" + now.getMonthValue());
            double expense = transactionDAO.getTotalExpense(now.getYear(), now.getMonthValue());
            System.out.println("[DashboardController] Доходы: " + income);
            System.out.println("[DashboardController] Расходы: " + expense);
            monthIncomeLabel.setText(String.format("%.2f ₽", income));
            monthExpenseLabel.setText(String.format("%.2f ₽", expense));
            monthIncomeLabel.getStyleClass().removeAll("income-label", "expense-label");
            if (income > expense) {
                monthIncomeLabel.getStyleClass().add("income-label");
            } else {
                monthIncomeLabel.getStyleClass().add("expense-label");
            }
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка обновления баланса: " + e.getMessage());
            e.printStackTrace();
            totalBalanceLabel.setText("0.00 ₽");
            monthIncomeLabel.setText("0.00 ₽");
            monthExpenseLabel.setText("0.00 ₽");
        }
    }
    @FXML
    private void showOverview() {
        try {
            System.out.println("[DashboardController] Загрузка обзора...");
            VBox overview = FXMLLoader.load(getClass().getResource("overview.fxml"));
            contentPane.getChildren().setAll(overview);
            System.out.println("[DashboardController] Обзор загружен успешно");
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка загрузки обзора: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить обзор: " + e.getMessage());
        }
    }
    @FXML
    private void showTransactions() {
        try {
            System.out.println("[DashboardController] Загрузка транзакций...");
            VBox transactions = FXMLLoader.load(getClass().getResource("transactions.fxml"));
            contentPane.getChildren().setAll(transactions);
            System.out.println("[DashboardController] Транзакции загружены успешно");
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка загрузки транзакций: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить транзакции: " + e.getMessage());
        }
    }
    @FXML
    private void showCategories() {
        try {
            System.out.println("[DashboardController] Загрузка категорий...");
            VBox categories = FXMLLoader.load(getClass().getResource("categories.fxml"));
            contentPane.getChildren().setAll(categories);
            System.out.println("[DashboardController] Категории загружены успешно");
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка загрузки категорий: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить категории: " + e.getMessage());
        }
    }
    @FXML
    private void showBudget() {
        try {
            System.out.println("[DashboardController] Загрузка бюджета...");
            VBox budget = FXMLLoader.load(getClass().getResource("budget.fxml"));
            contentPane.getChildren().setAll(budget);
            System.out.println("[DashboardController] Бюджет загружен успешно");
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка загрузки бюджета: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить бюджет: " + e.getMessage());
        }
    }
    @FXML
    private void showReports() {
        try {
            System.out.println("[DashboardController] Загрузка отчетов...");
            VBox reports = FXMLLoader.load(getClass().getResource("reports.fxml"));
            contentPane.getChildren().setAll(reports);
            System.out.println("[DashboardController] Отчеты загружены успешно");
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка загрузки отчетов: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить отчеты: " + e.getMessage());
        }
    }
    @FXML
    private void addIncome() {
        System.out.println("[DashboardController] Добавление дохода...");
        showAddTransactionDialog("income");
    }
    @FXML
    private void addExpense() {
        System.out.println("[DashboardController] Добавление расхода...");
        showAddTransactionDialog("expense");
    }
    @FXML
    private void setupBudget() {
        System.out.println("[DashboardController] Настройка бюджета...");
        showBudgetDialog();
    }
    @FXML
    private void transferRemaining() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Перенос остатка");
        alert.setHeaderText("Вы уверены?");
        alert.setContentText("Неиспользованные средства бюджета будут возвращены на общий баланс.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert("Успех", "Остаток бюджета перенесен на общий баланс!");
                updateDashboard();
            }
        });
    }
    private void showAddTransactionDialog(String type) {
        try {
            System.out.println("[DashboardController] Открытие диалога добавления транзакции: " + type);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(type.equals("income") ? "Добавить доход" : "Добавить расход");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_transaction.fxml"));
            dialog.getDialogPane().setContent(loader.load());
            AddTransactionController controller = loader.getController();
            controller.setTransactionType(type);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    if (controller.saveTransaction()) {
                        updateDashboard();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка открытия диалога: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
    }
    private void showBudgetDialog() {
        try {
            System.out.println("[DashboardController] Открытие диалога бюджета...");
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Настройка бюджета");
            dialog.setHeaderText("Настройка бюджета на месяц");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("setup_budget.fxml"));
            dialog.getDialogPane().setContent(loader.load());
            SetupBudgetController controller = loader.getController();
            controller.initialize();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            final Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            btnOk.addEventFilter(ActionEvent.ACTION, event -> {
                controller.saveBudget();
                updateDashboard();
            });
            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("[DashboardController] Ошибка открытия настройки бюджета: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть настройку бюджета: " + e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}