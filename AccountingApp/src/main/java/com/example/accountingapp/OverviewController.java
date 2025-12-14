package com.example.accountingapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
public class OverviewController implements Initializable {
    @FXML private Label totalExpensesLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label budgetUsageLabel;
    @FXML private Label remainingBudgetLabel;
    @FXML private Label savingsLabel;
    @FXML private ProgressBar budgetProgressBar;
    @FXML private ComboBox<String> monthFilter;
    @FXML private PieChart expensesChart;
    private TransactionDAO transactionDAO = new TransactionDAO();
    private BudgetDAO budgetDAO = new BudgetDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMonthFilter();
        loadData();
        setupChart();
    }
    private void setupMonthFilter() {
        if (monthFilter != null) {
            ObservableList<String> months = FXCollections.observableArrayList();
            LocalDate now = LocalDate.now();
            DateTimeFormatter russianFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
                    .withLocale(new java.util.Locale("ru"));
            for (int i = 0; i < 12; i++) {
                LocalDate month = now.minusMonths(i);
                String monthName = month.format(russianFormatter);
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                months.add(monthName);
            }
            monthFilter.setItems(months);
            String currentMonth = now.format(russianFormatter);
            currentMonth = currentMonth.substring(0, 1).toUpperCase() + currentMonth.substring(1);
            monthFilter.setValue(currentMonth);
            monthFilter.setOnAction(event -> loadData());
        }
    }
    private void loadData() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        if (monthFilter != null && monthFilter.getValue() != null) {
            try {
                String selectedMonth = monthFilter.getValue();
                System.out.println("[OverviewController] Выбран месяц: " + selectedMonth);
                String[] parts = selectedMonth.split(" ");
                if (parts.length >= 2) {
                    String monthName = parts[0].toLowerCase();
                    year = Integer.parseInt(parts[1]);
                    month = getMonthNumber(monthName);
                    System.out.println("[OverviewController] Преобразовано: год=" + year + ", месяц=" + month);
                }
            } catch (Exception e) {
                System.err.println("[OverviewController] Ошибка преобразования месяца: " + e.getMessage());
            }
        }
        double income = transactionDAO.getTotalIncome(year, month);
        double expense = transactionDAO.getTotalExpense(year, month);
        double balance = income - expense;
        if (totalIncomeLabel != null) totalIncomeLabel.setText(String.format("%.2f ₽", income));
        if (totalExpensesLabel != null) totalExpensesLabel.setText(String.format("%.2f ₽", expense));
        String monthYear = String.format("%04d-%02d", year, month);
        var budgets = budgetDAO.getBudgetsForMonth(monthYear);
        double totalBudget = budgets.stream().mapToDouble(Budget::getAllocatedAmount).sum();
        double totalSpent = budgets.stream().mapToDouble(Budget::getSpentAmount).sum();
        double usage = totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0;
        double remaining = totalBudget - totalSpent;
        if (budgetUsageLabel != null) budgetUsageLabel.setText(String.format("%.1f%%", usage));
        if (remainingBudgetLabel != null) remainingBudgetLabel.setText(String.format("%.2f ₽", remaining));
        if (budgetProgressBar != null) {
            budgetProgressBar.setProgress(usage / 100.0);
            budgetProgressBar.getStyleClass().removeAll("progress-over", "progress-warning", "progress-ok");
            if (usage > 100) {
                budgetProgressBar.getStyleClass().add("progress-over");
            } else if (usage > 80) {
                budgetProgressBar.getStyleClass().add("progress-warning");
            } else {
                budgetProgressBar.getStyleClass().add("progress-ok");
            }
        }
        if (savingsLabel != null) {
            double savingsRate = income > 0 ? (balance / income) * 100 : 0;
            savingsLabel.setText(String.format("Сбережения: %.1f%%", savingsRate));
            savingsLabel.getStyleClass().removeAll("savings-positive", "savings-negative");
            if (savingsRate > 0) {
                savingsLabel.getStyleClass().add("savings-positive");
            } else {
                savingsLabel.getStyleClass().add("savings-negative");
            }
        }
    }
    private void setupChart() {
        if (expensesChart != null) {
            try {
                LocalDate now = LocalDate.now();
                int year = now.getYear();
                int month = now.getMonthValue();
                List<TransactionDAO.CategoryExpense> expenses =
                        transactionDAO.getCategoryExpensesList(year, month);
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                if (expenses != null && !expenses.isEmpty()) {
                    for (TransactionDAO.CategoryExpense expense : expenses) {
                        pieChartData.add(new PieChart.Data(
                                expense.getCategoryName(),
                                expense.getAmount()
                        ));
                    }
                } else {
                    pieChartData.add(new PieChart.Data("Продукты", 15000));
                    pieChartData.add(new PieChart.Data("Транспорт", 8000));
                    pieChartData.add(new PieChart.Data("Развлечения", 7000));
                    pieChartData.add(new PieChart.Data("Коммунальные", 12000));
                    pieChartData.add(new PieChart.Data("Другое", 5000));
                }
                expensesChart.setData(pieChartData);
                expensesChart.setTitle("Расходы по категориям за " + monthFilter.getValue());
            } catch (Exception e) {
                e.printStackTrace();
                expensesChart.setTitle("Не удалось загрузить данные диаграммы");
            }
        }
    }
    private int getMonthNumber(String monthName) {
        switch (monthName.toLowerCase()) {
            case "январь": return 1;
            case "февраль": return 2;
            case "март": return 3;
            case "апрель": return 4;
            case "май": return 5;
            case "июнь": return 6;
            case "июль": return 7;
            case "август": return 8;
            case "сентябрь": return 9;
            case "октябрь": return 10;
            case "ноябрь": return 11;
            case "декабрь": return 12;
            default: return LocalDate.now().getMonthValue();
        }
    }
}