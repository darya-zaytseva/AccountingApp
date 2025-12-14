package com.example.accountingapp;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
public class SetupBudgetController {
    @FXML private ComboBox<String> monthComboBox;
    @FXML private TableView<BudgetItem> budgetTable;
    @FXML private Label totalBudgetLabel;
    private ObservableList<BudgetItem> budgetItems = FXCollections.observableArrayList();
    private CategoryDAO categoryDAO = new CategoryDAO();
    public static class BudgetItem {
        private final SimpleStringProperty categoryName;
        private final SimpleDoubleProperty newAmount;
        public BudgetItem(String categoryName, double amount) {
            this.categoryName = new SimpleStringProperty(categoryName);
            this.newAmount = new SimpleDoubleProperty(amount);
        }
        public String getCategoryName() { return categoryName.get(); }
        public void setCategoryName(String value) { categoryName.set(value); }
        public double getNewAmount() { return newAmount.get(); }
        public void setNewAmount(double value) { newAmount.set(value); }
        public SimpleStringProperty categoryNameProperty() { return categoryName; }
        public SimpleDoubleProperty newAmountProperty() { return newAmount; }
    }
    @FXML
    public void initialize() {
        loadMonths();
        setupTable();
        loadCategories();
        calculateTotal();
    }
    private void setupTable() {
        budgetTable.getColumns().clear();
        TableColumn<BudgetItem, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(cellData -> cellData.getValue().categoryNameProperty());
        categoryCol.setPrefWidth(150);
        TableColumn<BudgetItem, Double> amountCol = new TableColumn<>("Бюджет (₽)");
        amountCol.setCellValueFactory(cellData -> cellData.getValue().newAmountProperty().asObject());
        amountCol.setPrefWidth(120);
        amountCol.setCellFactory(column -> new TableCell<BudgetItem, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₽", amount));
                }
            }
        });
        budgetTable.getColumns().addAll(categoryCol, amountCol);
        budgetTable.setItems(budgetItems);
        budgetTable.setRowFactory(tv -> {
            TableRow<BudgetItem> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Редактировать сумму");
            editItem.setOnAction(event -> {
                BudgetItem item = row.getItem();
                if (item != null) {
                    showEditDialog(item);
                }
            });
            contextMenu.getItems().add(editItem);
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });
    }
    private void showEditDialog(BudgetItem item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getNewAmount()));
        dialog.setTitle("Редактирование бюджета");
        dialog.setHeaderText("Категория: " + item.getCategoryName());
        dialog.setContentText("Введите новую сумму бюджета:");
        dialog.showAndWait().ifPresent(newValue -> {
            try {
                double amount = Double.parseDouble(newValue.replace(",", "."));
                item.setNewAmount(amount);
                budgetTable.refresh();
                calculateTotal();
            } catch (NumberFormatException e) {
                showAlert("Ошибка", "Введите корректное число!");
            }
        });
    }
    private void loadMonths() {
        ObservableList<String> months = FXCollections.observableArrayList();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        for (int i = 0; i < 12; i++) {
            LocalDate month = now.plusMonths(i);
            months.add(month.format(formatter));
        }
        monthComboBox.setItems(months);
        monthComboBox.setValue(now.format(formatter));
        monthComboBox.setOnAction(event -> {
        });
    }
    private void loadCategories() {
        budgetItems.clear();
        String selectedMonth = monthComboBox.getValue();
        String monthYear = convertMonthToFormat(selectedMonth);
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            if (categories != null && !categories.isEmpty()) {
                for (Category category : categories) {
                    if ("expense".equals(category.getType())) {
                        double existingBudget = 0.0;
                        BudgetDAO budgetDAO = new BudgetDAO();
                        Budget budget = budgetDAO.getBudgetForCategory(category.getId(), monthYear);
                        if (budget != null) {
                            existingBudget = budget.getAllocatedAmount();
                        }
                        budgetItems.add(new BudgetItem(category.getName(), existingBudget));
                    }
                }
            } else {
                addDefaultCategories();
            }
        } catch (Exception e) {
            addDefaultCategories();
        }
        calculateTotal();
    }
    private void addDefaultCategories() {
        String[] defaultCategories = {
                "Продукты", "Транспорт", "Развлечения",
                "Коммунальные услуги", "Одежда", "Здоровье",
                "Образование", "Подарки", "Другое"
        };
        for (String category : defaultCategories) {
            budgetItems.add(new BudgetItem(category, 0.0));
        }
    }
    private void calculateTotal() {
        double total = 0;
        for (BudgetItem item : budgetItems) {
            total += item.getNewAmount();
        }
        totalBudgetLabel.setText(String.format("%.2f ₽", total));
    }
    @FXML
    public void saveBudget() {
        String selectedMonth = monthComboBox.getValue();
        if (selectedMonth == null || selectedMonth.isEmpty()) {
            showAlert("Ошибка", "Выберите месяц");
            return;
        }
        String monthYear = convertMonthToFormat(selectedMonth);
        BudgetDAO budgetDAO = new BudgetDAO();
        int savedCount = 0;
        StringBuilder result = new StringBuilder();
        for (BudgetItem item : budgetItems) {
            double amount = item.getNewAmount();
            if (amount > 0) {
                int categoryId = findCategoryIdByName(item.getCategoryName());
                if (categoryId > 0) {
                    Budget budget = new Budget();
                    budget.setCategoryId(categoryId);
                    budget.setMonthYear(monthYear);
                    budget.setAllocatedAmount(amount);
                    if (budgetDAO.saveBudget(budget)) {
                        savedCount++;
                        result.append("✓ ").append(item.getCategoryName())
                                .append(": ").append(String.format("%.2f ₽", amount))
                                .append("\n");
                    }
                }
            }
        }
        if (savedCount > 0) {
            showAlert("Успех",
                    "Бюджет на " + selectedMonth + " сохранен!\n\n" +
                            "Сохранено категорий: " + savedCount + "\n" +
                            "Общая сумма: " + totalBudgetLabel.getText() + "\n\n" +
                            result.toString());
        } else {
            showAlert("Предупреждение",
                    "Бюджет не сохранен. Установите суммы для категорий (больше 0).");
        }
    }
    private int findCategoryIdByName(String categoryName) {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            if (categories != null) {
                for (Category cat : categories) {
                    if (cat.getName().equals(categoryName)) {
                        return cat.getId();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private String convertMonthToFormat(String monthName) {
        try {
            String[] parts = monthName.split(" ");
            String monthNameStr = parts[0];
            int year = Integer.parseInt(parts[1]);
            int month = getMonthNumber(monthNameStr);
            return String.format("%04d-%02d", year, month);
        } catch (Exception e) {
            LocalDate now = LocalDate.now();
            return String.format("%04d-%02d", now.getYear(), now.getMonthValue());
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
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(300);
        alert.getDialogPane().setMinWidth(400);
        alert.showAndWait();
    }
}