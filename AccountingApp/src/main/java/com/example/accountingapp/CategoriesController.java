package com.example.accountingapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
public class CategoriesController implements Initializable {
    @FXML private TableView<Category> incomeCategoriesTable;
    @FXML private TableView<Category> expenseCategoriesTable;
    @FXML private TableColumn<Category, String> incomeNameColumn;
    @FXML private TableColumn<Category, String> incomeColorColumn;
    @FXML private TableColumn<Category, String> incomeIconColumn;
    @FXML private TableColumn<Category, String> expenseNameColumn;
    @FXML private TableColumn<Category, String> expenseColorColumn;
    @FXML private TableColumn<Category, String> expenseIconColumn;
    private ObservableList<Category> incomeCategories = FXCollections.observableArrayList();
    private ObservableList<Category> expenseCategories = FXCollections.observableArrayList();
    private CategoryDAO categoryDAO = new CategoryDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        loadCategories();
        setupDeleteButtons();
    }
    private void setupTables() {
        if (incomeNameColumn != null) {
            incomeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (incomeColorColumn != null) {
            incomeColorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        }
        if (incomeIconColumn != null) {
            incomeIconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
        }

        if (expenseNameColumn != null) {
            expenseNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (expenseColorColumn != null) {
            expenseColorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        }
        if (expenseIconColumn != null) {
            expenseIconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
        }
        incomeCategoriesTable.setItems(incomeCategories);
        expenseCategoriesTable.setItems(expenseCategories);
    }
    private void setupDeleteButtons() {
        TableColumn<Category, Void> incomeDeleteCol = new TableColumn<>("Действия");
        incomeDeleteCol.setPrefWidth(100);
        incomeDeleteCol.setCellFactory(param -> new TableCell<Category, Void>() {
            private final Button deleteBtn = new Button("🗑");
            {
                deleteBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category, "income");
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        TableColumn<Category, Void> expenseDeleteCol = new TableColumn<>("Действия");
        expenseDeleteCol.setPrefWidth(100);
        expenseDeleteCol.setCellFactory(param -> new TableCell<Category, Void>() {
            private final Button deleteBtn = new Button("🗑️");
            {
                deleteBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category, "expense");
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        if (incomeCategoriesTable.getColumns().size() < 4) {
            incomeCategoriesTable.getColumns().add(incomeDeleteCol);
        }
        if (expenseCategoriesTable.getColumns().size() < 4) {
            expenseCategoriesTable.getColumns().add(expenseDeleteCol);
        }
    }
    private void loadCategories() {
        incomeCategories.clear();
        expenseCategories.clear();
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            if (categories != null && !categories.isEmpty()) {
                for (Category category : categories) {
                    if ("income".equals(category.getType())) {
                        incomeCategories.add(category);
                    } else if ("expense".equals(category.getType())) {
                        expenseCategories.add(category);
                    }
                }
            } else {
                addTestCategories();
            }
        } catch (Exception e) {
            e.printStackTrace();
            addTestCategories();
        }
    }
    private void addTestCategories() {
        addCategoryToDAO(new Category("Зарплата", "income", "#48bb78", "💰"));
        addCategoryToDAO(new Category("Инвестиции", "income", "#4299e1", "📈"));
        addCategoryToDAO(new Category("Подарки", "income", "#d69e2e", "🎁"));
        addCategoryToDAO(new Category("Продукты", "expense", "#f56565", "🛒"));
        addCategoryToDAO(new Category("Транспорт", "expense", "#ed8936", "🚗"));
        addCategoryToDAO(new Category("Развлечения", "expense", "#d69e2e", "🎬"));
        addCategoryToDAO(new Category("Коммунальные", "expense", "#4299e1", "🏠"));
        loadCategories();
    }
    private void addCategoryToDAO(Category category) {
        try {
            categoryDAO.addCategory(category);
        } catch (Exception e) {
            System.err.println("Ошибка добавления тестовой категории: " + e.getMessage());
        }
    }
    @FXML
    private void showAddCategoryDialog() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Добавить категорию");
        dialog.setHeaderText("Добавить новую категорию");
        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Доход", "Расход");
        typeCombo.setValue("Расход");
        TextField nameField = new TextField();
        nameField.setPromptText("Название категории");
        TextField colorField = new TextField();
        colorField.setPromptText("Цвет (например: #48bb78)");
        colorField.setText("#48bb78");
        TextField iconField = new TextField();
        iconField.setPromptText("Иконка (например: 📊)");
        iconField.setText("📝");
        grid.add(new Label("Тип:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Название:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Цвет:"), 0, 2);
        grid.add(colorField, 1, 2);
        grid.add(new Label("Иконка:"), 0, 3);
        grid.add(iconField, 1, 3);
        dialog.getDialogPane().setContent(grid);
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String type = typeCombo.getValue().equals("Доход") ? "income" : "expense";
                return new Category(
                        nameField.getText().trim(),
                        type,
                        colorField.getText().trim(),
                        iconField.getText().trim()
                );
            }
            return null;
        });
        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(category -> {
            if (categoryDAO.addCategory(category)) {
                showAlert("Успех", "Категория '" + category.getName() + "' добавлена!");
                loadCategories();
            } else {
                showAlert("Ошибка", "Не удалось добавить категорию");
            }
        });
    }
    private void deleteCategory(Category category, String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление категории");
        alert.setHeaderText("Вы уверены, что хотите удалить категорию?");
        alert.setContentText("Категория: " + category.getName() + "\n\n" +
                "Внимание: Если с этой категорией связаны транзакции, " +
                "они останутся без категории!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (categoryDAO.deleteCategory(category.getId())) {
                showAlert("Успех", "Категория '" + category.getName() + "' удалена!");
                loadCategories();
            } else {
                showAlert("Ошибка", "Не удалось удалить категорию");
            }
        }
    }
    @FXML
    private void deleteAllIncomeCategories() {
        deleteAllCategoriesByType("income", "доходов");
    }
    @FXML
    private void deleteAllExpenseCategories() {
        deleteAllCategoriesByType("expense", "расходов");
    }
    private void deleteAllCategoriesByType(String type, String typeName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление всех категорий");
        alert.setHeaderText("Вы уверены, что хотите удалить ВСЕ категории " + typeName + "?");
        alert.setContentText("Это действие невозможно отменить!\n" +
                "Все связанные транзакции останутся без категории.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deletedCount = 0;
            List<Category> categories = type.equals("income") ? incomeCategories : expenseCategories;
            List<Category> categoriesCopy = new ArrayList<>(categories);
            for (Category category : categoriesCopy) {
                if (categoryDAO.deleteCategory(category.getId())) {
                    deletedCount++;
                }
            }
            showAlert("Результат", "Удалено категорий: " + deletedCount + " из " + categoriesCopy.size());
            loadCategories();
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