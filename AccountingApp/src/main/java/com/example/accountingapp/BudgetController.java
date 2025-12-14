package com.example.accountingapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
public class BudgetController implements Initializable {
    @FXML private Label monthLabel;
    @FXML private Label totalBudgetLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingBudgetLabel;
    @FXML private ProgressBar totalBudgetProgress;
    @FXML private TableView<BudgetItem> budgetTableView;
    @FXML private VBox budgetContainer;
    private BudgetDAO budgetDAO = new BudgetDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    public static class BudgetItem {
        private String category;
        private double allocated;
        private double spent;
        private double remaining;
        private double percentage;
        public BudgetItem(String category, double allocated, double spent) {
            this.category = category;
            this.allocated = allocated;
            this.spent = spent;
            this.remaining = allocated - spent;
            this.percentage = allocated > 0 ? (spent / allocated) * 100 : 0;
        }
        public String getCategory() { return category; }
        public double getAllocated() { return allocated; }
        public double getSpent() { return spent; }
        public double getRemaining() { return remaining; }
        public double getPercentage() { return percentage; }
        public String getAllocatedFormatted() { return String.format("%.2f ₽", allocated); }
        public String getSpentFormatted() { return String.format("%.2f ₽", spent); }
        public String getRemainingFormatted() { return String.format("%.2f ₽", remaining); }
        public String getPercentageFormatted() { return String.format("%.1f%%", percentage); }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[BudgetController] Инициализация контроллера бюджета");
        setupUI();
        loadBudgetData();
        addActionButtons();
    }
    private void setupUI() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthLabel.setText(now.format(formatter));
        System.out.println("[BudgetController] Текущий месяц: " + now.format(formatter));
        setupTableColumns();
    }
    private void setupTableColumns() {
        budgetTableView.getColumns().clear();
        TableColumn<BudgetItem, String> catCol = new TableColumn<>("Категория");
        catCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        catCol.setPrefWidth(150);
        TableColumn<BudgetItem, String> allocatedCol = new TableColumn<>("Выделено");
        allocatedCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAllocatedFormatted()));
        allocatedCol.setPrefWidth(120);
        TableColumn<BudgetItem, String> spentCol = new TableColumn<>("Потрачено");
        spentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSpentFormatted()));
        spentCol.setPrefWidth(120);
        TableColumn<BudgetItem, String> remainingCol = new TableColumn<>("Остаток");
        remainingCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRemainingFormatted()));
        remainingCol.setPrefWidth(120);
        TableColumn<BudgetItem, String> percentCol = new TableColumn<>("% использования");
        percentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPercentageFormatted()));
        percentCol.setPrefWidth(100);
        budgetTableView.getColumns().addAll(catCol, allocatedCol, spentCol, remainingCol, percentCol);
        budgetTableView.setRowFactory(tv -> new TableRow<BudgetItem>() {
            @Override
            protected void updateItem(BudgetItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    getStyleClass().removeAll("budget-over", "budget-warning", "budget-ok");
                } else {
                    getStyleClass().removeAll("budget-over", "budget-warning", "budget-ok");
                    if (item.getPercentage() > 100) {
                        getStyleClass().add("budget-over");
                    } else if (item.getPercentage() > 80) {
                        getStyleClass().add("budget-warning");
                    } else {
                        getStyleClass().add("budget-ok");
                    }
                }
            }
        });
    }
    private void loadBudgetData() {
        System.out.println("\n=== [BudgetController] ЗАГРУЗКА ДАННЫХ БЮДЖЕТА ===");
        try {
            LocalDate now = LocalDate.now();
            String monthYear = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
            System.out.println("[BudgetController] Месяц: " + monthYear);
            System.out.println("[BudgetController] Текущая дата: " + now);
            double realTotalExpenses = transactionDAO.getTotalExpense(now.getYear(), now.getMonthValue());
            System.out.println("[BudgetController] Реальные расходы из транзакций: " + realTotalExpenses);
            List<Budget> budgets = budgetDAO.getBudgetsForMonth(monthYear);
            ObservableList<BudgetItem> budgetItems = FXCollections.observableArrayList();
            double totalAllocated = 0;
            double totalSpent = 0;
            if (budgets != null && !budgets.isEmpty()) {
                System.out.println("[BudgetController] РЕЖИМ: Используем данные из таблицы budgets");
                for (Budget budget : budgets) {
                    totalAllocated += budget.getAllocatedAmount();
                    totalSpent += budget.getSpentAmount();
                    budgetItems.add(new BudgetItem(
                            budget.getCategoryName(),
                            budget.getAllocatedAmount(),
                            budget.getSpentAmount()
                    ));
                    System.out.println("[BudgetController] " + budget.getCategoryName() +
                            " - Выделено: " + budget.getAllocatedAmount() +
                            ", Потрачено: " + budget.getSpentAmount());
                }
                syncBudgetsWithRealExpenses(now.getYear(), now.getMonthValue(), budgets);
            } else {
                System.out.println("[BudgetController] РЕЖИМ: Бюджетов в БД нет.");
                showNoBudgetMessage();
                return;
            }
            budgetTableView.setItems(budgetItems);
            updateSummary(totalAllocated, totalSpent);
            System.out.println("[BudgetController] ЗАГРУЗКА ЗАВЕРШЕНА: Выделено=" + totalAllocated +
                    ", Потрачено=" + totalSpent +
                    ", Остаток=" + (totalAllocated - totalSpent));
        } catch (Exception e) {
            System.err.println("[BudgetController] КРИТИЧЕСКАЯ ОШИБКА в loadBudgetData: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить данные бюджета: " + e.getMessage());
            loadTestData();
        }
    }
    private void syncBudgetsWithRealExpenses(int year, int month, List<Budget> budgets) {
        String monthYear = String.format("%04d-%02d", year, month);
        List<TransactionDAO.CategoryExpense> realExpenses =
                transactionDAO.getCategoryExpensesList(year, month);
        if (realExpenses == null || realExpenses.isEmpty()) {
            System.out.println("[BudgetController] Нет реальных расходов для синхронизации");
            return;
        }
        System.out.println("[BudgetController] Синхронизация с реальными расходами...");
        for (TransactionDAO.CategoryExpense realExpense : realExpenses) {
            for (Budget budget : budgets) {
                if (budget.getCategoryName().equals(realExpense.getCategoryName())) {
                    if (Math.abs(budget.getSpentAmount() - realExpense.getAmount()) > 0.01) {
                        System.out.println("[BudgetController] Расхождение для " + realExpense.getCategoryName() +
                                ": бюджет=" + budget.getSpentAmount() + ", реально=" + realExpense.getAmount());
                        budget.setSpentAmount(realExpense.getAmount());
                        budgetDAO.saveBudget(budget);
                    }
                    break;
                }
            }
        }
    }
    private void showNoBudgetMessage() {
        budgetTableView.setPlaceholder(new Label("Бюджет не настроен для этого месяца.\n" +
                "Нажмите 'Настроить бюджет' в боковой панели."));
        totalBudgetLabel.setText("0.00 ₽");
        totalSpentLabel.setText("0.00 ₽");
        remainingBudgetLabel.setText("0.00 ₽");
        totalBudgetProgress.setProgress(0);
    }
    private void updateSummary(double totalAllocated, double totalSpent) {
        double remaining = totalAllocated - totalSpent;
        double progress = totalAllocated > 0 ? totalSpent / totalAllocated : 0;
        totalBudgetLabel.setText(String.format("%.2f ₽", totalAllocated));
        totalSpentLabel.setText(String.format("%.2f ₽", totalSpent));
        remainingBudgetLabel.setText(String.format("%.2f ₽", remaining));
        totalBudgetProgress.setProgress(progress);
        totalBudgetProgress.getStyleClass().removeAll("budget-progress-over", "budget-progress-warning", "budget-progress-ok");
        if (progress > 1.0) {
            totalBudgetProgress.getStyleClass().add("budget-progress-over");
        } else if (progress > 0.8) {
            totalBudgetProgress.getStyleClass().add("budget-progress-warning");
        } else {
            totalBudgetProgress.getStyleClass().add("budget-progress-ok");
        }
    }
    private void addActionButtons() {
    }
    @FXML
    private void refreshBudget() {
        System.out.println("\n[BudgetController] Обновление данных бюджета...");
        loadBudgetData();
        showAlert("Обновлено", "Данные бюджета обновлены!");
    }
    @FXML
    private void syncWithRealData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Синхронизация бюджета");
        confirm.setHeaderText("Обновить бюджет реальными данными?");
        confirm.setContentText("Потраченные суммы в бюджете будут обновлены\n" +
                "на основе фактических расходов.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    LocalDate now = LocalDate.now();
                    boolean success = budgetDAO.syncBudgetsWithRealExpenses(now.getYear(), now.getMonthValue());
                    if (success) {
                        showAlert("Синхронизация завершена", "Бюджет успешно синхронизирован");
                        loadBudgetData();
                    } else {
                        showAlert("Информация", "Нет данных о реальных расходах для синхронизации");
                    }
                } catch (Exception e) {
                    System.err.println("[BudgetController] Ошибка синхронизации: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Ошибка", "Ошибка синхронизации: " + e.getMessage());
                }
            }
        });
    }
    @FXML
    private void updateBudget() {
        showAlert("Информация", "Используйте кнопку 'Настроить бюджет' в боковой панели");
    }
    @FXML
    private void deleteBudget() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление бюджета");
        confirm.setHeaderText("Вы уверены, что хотите удалить бюджет?");
        confirm.setContentText("Это действие удалит все настройки бюджета\n" +
                "для текущего месяца. Восстановить будет невозможно.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    LocalDate now = LocalDate.now();
                    String monthYear = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
                    boolean success = budgetDAO.deleteAllBudgetsForMonth(monthYear);
                    if (success) {
                        showAlert("Успех", "Бюджет успешно удален!");
                        loadBudgetData();
                    } else {
                        showAlert("Информация", "Бюджет не найден или уже удален");
                    }
                } catch (Exception e) {
                    showAlert("Ошибка", "Ошибка удаления бюджета: " + e.getMessage());
                }
            }
        });
    }
    private void loadTestData() {
        System.out.println("[BudgetController] Используем тестовые данные");
        ObservableList<BudgetItem> testData = FXCollections.observableArrayList(
                new BudgetItem("Продукты", 12740, 9800),
                new BudgetItem("Транспорт", 7280, 5600),
                new BudgetItem("Развлечения", 5460, 4200),
                new BudgetItem("Коммунальные услуги", 9100, 7000),
                new BudgetItem("Одежда", 1820, 1400)
        );
        budgetTableView.setItems(testData);
        double totalAllocated = 36400;
        double totalSpent = 28000;
        double remaining = totalAllocated - totalSpent;
        double progress = totalSpent / totalAllocated;
        totalBudgetLabel.setText(String.format("%.2f ₽", totalAllocated));
        totalSpentLabel.setText(String.format("%.2f ₽", totalSpent));
        remainingBudgetLabel.setText(String.format("%.2f ₽", remaining));
        totalBudgetProgress.setProgress(progress);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}