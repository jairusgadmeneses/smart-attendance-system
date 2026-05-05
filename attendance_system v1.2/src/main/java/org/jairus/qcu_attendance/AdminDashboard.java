package org.jairus.qcu_attendance;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboard extends Application {

    private final String FONT_STACK = "-fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;";
    private final String DARK_TEXT = "#0F172A";
    private TableView<String[]> table;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #E2E8F0;");

        ImageView logoView = new ImageView();
        try {
            Image logoImage = new Image(new File("qcu_logo.jpg").toURI().toString());
            logoView.setImage(logoImage);
            logoView.fitWidthProperty().bind(root.widthProperty());
            logoView.fitHeightProperty().bind(root.heightProperty());
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Resource load failed: qcu_logo.jpg");
        }

        logoView.setEffect(new GaussianBlur(20));
        logoView.setOpacity(0.85);

        Label title = new Label("ADMIN CONSOLE - PENDING SCANS");
        title.setStyle(FONT_STACK + " -fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: " + DARK_TEXT + ";");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 8px;");
        refreshBtn.setOnAction(e -> refreshTableData());

        Button syncBtn = new Button("SYNC TO CLOUD");
        syncBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 8px;");
        syncBtn.setOnAction(e -> {
            ScannerLogic engine = new ScannerLogic();
            engine.syncOfflineLogsToCloud();
            refreshTableData();
        });

        Button earlyDismissalBtn = new Button("Enable Early Dismissal");
        earlyDismissalBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 8px;");
        earlyDismissalBtn.setOnAction(e -> {
            ScannerLogic.isEarlyDismissalActive = !ScannerLogic.isEarlyDismissalActive;
            if(ScannerLogic.isEarlyDismissalActive) {
                earlyDismissalBtn.setText("Disable Early Dismissal");
                earlyDismissalBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 8px;");
            } else {
                earlyDismissalBtn.setText("Enable Early Dismissal");
                earlyDismissalBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 8px;");
            }
        });

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(title, syncBtn, refreshBtn, earlyDismissalBtn);

        table = new TableView<>();
        TableColumn<String[], String> colBarcode = new TableColumn<>("Student ID");
        colBarcode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        TableColumn<String[], String> colTime = new TableColumn<>("Scanned At (Local)");
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        table.getColumns().addAll(colBarcode, colName, colTime);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        refreshTableData();

        VBox card = new VBox(25);
        card.setPadding(new Insets(35));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.75); -fx-background-radius: 16px;");
        card.setMaxWidth(950);
        card.getChildren().addAll(headerBox, table);

        root.getChildren().addAll(logoView, card);

        Scene scene = new Scene(root, 1050, 650);
        primaryStage.setTitle("Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshTableData() {
        ObservableList<String[]> data = FXCollections.observableArrayList();

        try (Connection conn = DatabaseManager.getLocalConnection()) {
            String sql = "SELECT l.barcode, s.full_name, l.scan_time " +
                    "FROM local_logs l " +
                    "JOIN local_students s ON l.barcode = s.barcode " +
                    "ORDER BY l.scan_time DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String barcode = rs.getString("barcode");
                String name = rs.getString("full_name");
                String time = rs.getString("scan_time");

                data.add(new String[]{barcode, name, time});
            }

            table.setItems(data);
            System.out.println("Data table refreshed. Pending records: " + data.size());

        } catch (Exception e) {
            System.out.println("Data retrieval error: " + e.getMessage());
            table.setItems(data);
        }
    }
}