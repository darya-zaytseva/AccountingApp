module com.example.accountingapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires itextpdf;
    opens com.example.accountingapp to javafx.fxml;
    exports com.example.accountingapp;
}