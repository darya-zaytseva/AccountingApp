package com.example.accountingapp;
import javafx.util.StringConverter;
public class DoubleStringConverter extends StringConverter<Double> {
    @Override
    public String toString(Double value) {
        return value != null ? String.format("%.2f", value) : "0.00";
    }
    @Override
    public Double fromString(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(value.replace("₽", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}