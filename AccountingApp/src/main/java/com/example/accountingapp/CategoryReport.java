package com.example.accountingapp;
public class CategoryReport {
    private final String category;
    private final double amount;
    private final double percentage;
    public CategoryReport(String category, double amount, double percentage) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public double getPercentage() { return percentage; }
    public String getAmountFormatted() { return String.format("%.2f ₽", amount); }
    public String getPercentageFormatted() { return String.format("%.1f%%", percentage); }
}