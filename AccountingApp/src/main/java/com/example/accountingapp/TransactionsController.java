package com.example.accountingapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
public class TransactionsController implements Initializable {
    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private Label totalAmountLabel;
    private TransactionDAO transactionDAO = new TransactionDAO();
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFilters();
        loadTransactions();
    }
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        dateColumn.setCellFactory(column -> new TableCell<Transaction, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                }
            }
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f ₽", amount));
                    Transaction transaction = getTableRow().getItem();
                    if (transaction != null) {
                        if (transaction.getType().equals("income")) {
                            setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #f56565; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (type.equals("income")) {
                        setText("📈 Доход");
                        setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold;");
                    } else {
                        setText("📉 Расход");
                        setStyle("-fx-text-fill: #f56565; -fx-font-weight: bold;");
                    }
                }
            }
        });
        transactionsTable.setItems(transactions);
    }
    private void setupFilters() {
        ObservableList<String> months = FXCollections.observableArrayList();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            LocalDate month = now.minusMonths(i);
            months.add(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
        monthFilter.setItems(months);
        monthFilter.setValue(now.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        ObservableList<String> types = FXCollections.observableArrayList(
                "Все типы", "Доходы", "Расходы"
        );
        typeFilter.setItems(types);
        typeFilter.setValue("Все типы");
        monthFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadTransactions();
        });
        typeFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadTransactions();
        });
    }
    private void loadTransactions() {
        transactions.clear();
        String selectedMonth = monthFilter.getValue();
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        if (selectedMonth != null && !selectedMonth.isEmpty()) {
            try {
                String[] parts = selectedMonth.split(" ");
                String monthName = parts[0];
                year = Integer.parseInt(parts[1]);
                month = getMonthNumber(monthName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<Transaction> allTransactions = transactionDAO.getTransactionsByMonth(year, month);
        String typeFilterValue = typeFilter.getValue();
        for (Transaction transaction : allTransactions) {
            if (typeFilterValue == null || typeFilterValue.equals("Все типы") ||
                    (typeFilterValue.equals("Доходы") && transaction.getType().equals("income")) ||
                    (typeFilterValue.equals("Расходы") && transaction.getType().equals("expense"))) {
                transactions.add(transaction);
            }
        }
        calculateTotal();
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
    private void calculateTotal() {
        double total = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("income")) {
                total += transaction.getAmount();
            } else {
                total -= transaction.getAmount();
            }
        }
        totalAmountLabel.setText(String.format("%.2f ₽", total));
        if (total >= 0) {
            totalAmountLabel.setStyle("-fx-text-fill: #48bb78; -fx-font-weight: bold; -fx-font-size: 18;");
        } else {
            totalAmountLabel.setStyle("-fx-text-fill: #f56565; -fx-font-weight: bold; -fx-font-size: 18;");
        }
    }
    @FXML
    private void refreshTransactions() {
        loadTransactions();
    }
}