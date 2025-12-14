package com.example.accountingapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class ReportsController implements Initializable {
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label balanceLabel;
    @FXML private TableView<CategoryReport> categoriesTable;
    private ObservableList<CategoryReport> currentData = FXCollections.observableArrayList();
    private TransactionDAO transactionDAO = new TransactionDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("\n=== [ReportsController] ИНИЦИАЛИЗАЦИЯ ===");
        System.out.println("[ReportsController] Текущий user_id: " + DatabaseConnection.getCurrentUserId());
        startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
        endDatePicker.setValue(LocalDate.now());
        setupTable();
        generateReport();
    }
    private void setupTable() {
        System.out.println("[ReportsController] Настройка таблицы отчетов");
        TableColumn<CategoryReport, String> categoryColumn = new TableColumn<>("Категория");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setPrefWidth(200);
        TableColumn<CategoryReport, String> amountColumn = new TableColumn<>("Сумма");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountFormatted"));
        amountColumn.setPrefWidth(150);
        TableColumn<CategoryReport, String> percentColumn = new TableColumn<>("%");
        percentColumn.setCellValueFactory(new PropertyValueFactory<>("percentageFormatted"));
        percentColumn.setPrefWidth(100);
        categoriesTable.getColumns().clear();
        categoriesTable.getColumns().addAll(categoryColumn, amountColumn, percentColumn);
    }
    @FXML
    private void generateReport() {
        System.out.println("\n=== [ReportsController] ГЕНЕРАЦИЯ ОТЧЕТА ===");
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate == null || endDate == null) {
            showAlert("Ошибка", "Выберите начальную и конечную даты");
            return;
        }
        if (startDate.isAfter(endDate)) {
            showAlert("Ошибка", "Начальная дата не может быть позже конечной");
            return;
        }
        System.out.println("[ReportsController] Период отчета: " + startDate + " - " + endDate);
        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = DatabaseConnection.getCurrentUserId();
            if (userId == 0) {
                showAlert("Ошибка", "Пользователь не авторизован");
                return;
            }
            ReportData reportData = getReportData(conn, startDate, endDate, userId);
            System.out.println("[ReportsController] Доходы: " + reportData.totalIncome);
            System.out.println("[ReportsController] Расходы: " + reportData.totalExpense);
            System.out.println("[ReportsController] Баланс: " + reportData.balance);
            System.out.println("[ReportsController] Категорий: " + reportData.categoryReports.size());
            totalIncomeLabel.setText(String.format("%.2f ₽", reportData.totalIncome));
            totalExpenseLabel.setText(String.format("%.2f ₽", reportData.totalExpense));
            balanceLabel.setText(String.format("%.2f ₽", reportData.balance));
            currentData.clear();
            currentData.addAll(reportData.categoryReports);
            categoriesTable.setItems(currentData);
            if (reportData.categoryReports.isEmpty()) {
                categoriesTable.setPlaceholder(new Label("Нет данных о расходах за выбранный период"));
            }
            System.out.println("[ReportsController] Отчет сгенерирован успешно");
        } catch (Exception e) {
            System.err.println("[ReportsController] Ошибка генерации отчета: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }
    private ReportData getReportData(Connection conn, LocalDate startDate, LocalDate endDate, int userId) throws SQLException {
        ReportData reportData = new ReportData();
        String totalsQuery = "SELECT " +
                "COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) as total_income, " +
                "COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0) as total_expense " +
                "FROM transactions " +
                "WHERE transaction_date BETWEEN ? AND ? " +
                "AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(totalsQuery)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            pstmt.setInt(3, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    reportData.totalIncome = rs.getDouble("total_income");
                    reportData.totalExpense = rs.getDouble("total_expense");
                    reportData.balance = reportData.totalIncome - reportData.totalExpense;
                }
            }
        }
        if (reportData.totalExpense > 0) {
            String categoriesQuery = "SELECT c.name as category_name, COALESCE(SUM(t.amount), 0) as total " +
                    "FROM transactions t " +
                    "LEFT JOIN categories c ON t.category_id = c.id " +
                    "WHERE t.type = 'expense' " +
                    "AND t.transaction_date BETWEEN ? AND ? " +
                    "AND t.user_id = ? " +
                    "GROUP BY c.name " +
                    "ORDER BY total DESC " +
                    "LIMIT 10";
            try (PreparedStatement pstmt = conn.prepareStatement(categoriesQuery)) {
                pstmt.setDate(1, Date.valueOf(startDate));
                pstmt.setDate(2, Date.valueOf(endDate));
                pstmt.setInt(3, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String categoryName = rs.getString("category_name");
                        double amount = rs.getDouble("total");
                        double percentage = (amount / reportData.totalExpense) * 100;
                        reportData.categoryReports.add(new CategoryReport(categoryName, amount, percentage));
                        System.out.println("[ReportsController] Категория: " + categoryName +
                                ", сумма: " + amount + ", процент: " + String.format("%.1f", percentage));
                    }
                }
            }
        }
        return reportData;
    }
    @FXML
    private void exportToExcel() {
        if (currentData.isEmpty()) {
            showAlert("Предупреждение", "Нет данных для экспорта. Сначала сгенерируйте отчет.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет Excel");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            String defaultFileName = "Финансовый_отчет_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            fileChooser.setInitialFileName(defaultFileName);
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Финансовый отчет");
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                CellStyle currencyStyle = workbook.createCellStyle();
                currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00\" ₽\""));
                CellStyle percentStyle = workbook.createCellStyle();
                percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("Финансовый отчет");
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);
                Row userRow = sheet.createRow(1);
                userRow.createCell(0).setCellValue("Пользователь:");
                userRow.createCell(1).setCellValue(DatabaseConnection.getCurrentUsername());
                Row dateRow = sheet.createRow(2);
                dateRow.createCell(0).setCellValue("Дата создания:");
                dateRow.createCell(1).setCellValue(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                Row periodRow = sheet.createRow(3);
                periodRow.createCell(0).setCellValue("Период отчета:");
                periodRow.createCell(1).setCellValue(
                        startDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                " - " +
                                endDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                );
                sheet.createRow(4);
                Row summaryHeaderRow = sheet.createRow(5);
                summaryHeaderRow.createCell(0).setCellValue("Сводная информация:");
                summaryHeaderRow.getCell(0).setCellStyle(headerStyle);
                Row incomeRow = sheet.createRow(6);
                incomeRow.createCell(0).setCellValue("Общий доход:");
                incomeRow.createCell(1).setCellValue(parseDoubleFromLabel(totalIncomeLabel.getText()));
                incomeRow.getCell(1).setCellStyle(currencyStyle);
                Row expenseRow = sheet.createRow(7);
                expenseRow.createCell(0).setCellValue("Общий расход:");
                expenseRow.createCell(1).setCellValue(parseDoubleFromLabel(totalExpenseLabel.getText()));
                expenseRow.getCell(1).setCellStyle(currencyStyle);
                Row balanceRow = sheet.createRow(8);
                balanceRow.createCell(0).setCellValue("Баланс:");
                balanceRow.createCell(1).setCellValue(parseDoubleFromLabel(balanceLabel.getText()));
                balanceRow.getCell(1).setCellStyle(currencyStyle);
                sheet.createRow(9);
                Row tableHeaderRow = sheet.createRow(10);
                tableHeaderRow.createCell(0).setCellValue("Топ категорий расходов:");
                tableHeaderRow.getCell(0).setCellStyle(headerStyle);
                Row columnHeaderRow = sheet.createRow(11);
                String[] headers = {"Категория", "Сумма", "%"};
                for (int i = 0; i < headers.length; i++) {
                    Cell headerCell = columnHeaderRow.createCell(i);
                    headerCell.setCellValue(headers[i]);
                    headerCell.setCellStyle(headerStyle);
                }
                int rowNum = 12;
                for (CategoryReport report : currentData) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(report.getCategory());
                    Cell amountCell = row.createCell(1);
                    amountCell.setCellValue(report.getAmount());
                    amountCell.setCellStyle(currencyStyle);
                    Cell percentCell = row.createCell(2);
                    percentCell.setCellValue(report.getPercentage() / 100);
                    percentCell.setCellStyle(percentStyle);
                }
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                Row footerRow = sheet.createRow(rowNum + 1);
                footerRow.createCell(0).setCellValue(
                        "Отчет создан в Домашней бухгалтерии: " +
                                LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                );
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                workbook.close();
                showAlert("Успех", "Excel отчет успешно сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Не удалось экспортировать в Excel:\n" + e.getMessage());
        }
    }
    private double parseDoubleFromLabel(String labelText) {
        try {
            String cleaned = labelText.replace("₽", "").replace(",", ".").trim();
            return Double.parseDouble(cleaned.replaceAll("[^\\d.-]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private static class ReportData {
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        double balance = 0.0;
        List<CategoryReport> categoryReports = new ArrayList<>();
    }
}