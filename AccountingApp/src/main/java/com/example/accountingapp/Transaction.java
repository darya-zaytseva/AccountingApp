package com.example.accountingapp;
import java.time.LocalDate;
import javafx.beans.property.*;
public class Transaction {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty categoryId = new SimpleIntegerProperty();
    private final StringProperty categoryName = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> transactionDate = new SimpleObjectProperty<>();
    public Transaction() {}
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty categoryIdProperty() { return categoryId; }
    public StringProperty categoryNameProperty() { return categoryName; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty typeProperty() { return type; }
    public ObjectProperty<LocalDate> transactionDateProperty() { return transactionDate; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public int getCategoryId() { return categoryId.get(); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public String getCategoryName() { return categoryName.get(); }
    public void setCategoryName(String categoryName) { this.categoryName.set(categoryName); }
    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public LocalDate getTransactionDate() { return transactionDate.get(); }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate.set(transactionDate); }
}