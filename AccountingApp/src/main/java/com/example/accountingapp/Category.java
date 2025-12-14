package com.example.accountingapp;
import javafx.beans.property.*;
public class Category {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty color = new SimpleStringProperty();
    private final StringProperty icon = new SimpleStringProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    public Category() {}
    public Category(String name, String type, String color, String icon) {
        this.name.set(name);
        this.type.set(type);
        this.color.set(color);
        this.icon.set(icon);
    }
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public StringProperty colorProperty() { return color; }
    public StringProperty iconProperty() { return icon; }
    public IntegerProperty userIdProperty() { return userId; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public String getColor() { return color.get(); }
    public void setColor(String color) { this.color.set(color); }
    public String getIcon() { return icon.get(); }
    public void setIcon(String icon) { this.icon.set(icon); }
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
}