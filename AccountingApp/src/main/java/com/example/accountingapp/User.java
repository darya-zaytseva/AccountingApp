package com.example.accountingapp;
import javafx.beans.property.*;
public class User {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<java.time.LocalDateTime> createdAt = new SimpleObjectProperty<>();
    public User() {}
    public User(String username, String email, String password) {
        this.username.set(username);
        this.email.set(email);
        this.password.set(password);
        this.createdAt.set(java.time.LocalDateTime.now());
    }
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public ObjectProperty<java.time.LocalDateTime> createdAtProperty() { return createdAt; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public java.time.LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt.set(createdAt); }
}