package com.example.accountingapp;
import javafx.beans.property.*;
public class Budget {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty categoryId = new SimpleIntegerProperty();
    private final StringProperty categoryName = new SimpleStringProperty();
    private final StringProperty monthYear = new SimpleStringProperty();
    private final DoubleProperty allocatedAmount = new SimpleDoubleProperty();
    private final DoubleProperty spentAmount = new SimpleDoubleProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    public Budget() {}
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty categoryIdProperty() { return categoryId; }
    public StringProperty categoryNameProperty() { return categoryName; }
    public StringProperty monthYearProperty() { return monthYear; }
    public DoubleProperty allocatedAmountProperty() { return allocatedAmount; }
    public DoubleProperty spentAmountProperty() { return spentAmount; }
    public IntegerProperty userIdProperty() { return userId; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public int getCategoryId() { return categoryId.get(); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public String getCategoryName() { return categoryName.get(); }
    public void setCategoryName(String categoryName) { this.categoryName.set(categoryName); }
    public String getMonthYear() { return monthYear.get(); }
    public void setMonthYear(String monthYear) { this.monthYear.set(monthYear); }
    public double getAllocatedAmount() { return allocatedAmount.get(); }
    public void setAllocatedAmount(double allocatedAmount) { this.allocatedAmount.set(allocatedAmount); }
    public double getSpentAmount() { return spentAmount.get(); }
    public void setSpentAmount(double spentAmount) { this.spentAmount.set(spentAmount); }
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public double getRemainingAmount() {
        return allocatedAmount.get() - spentAmount.get();
    }
    public double getUsagePercentage() {
        return allocatedAmount.get() > 0 ?
                (spentAmount.get() / allocatedAmount.get()) * 100 : 0;
    }
}