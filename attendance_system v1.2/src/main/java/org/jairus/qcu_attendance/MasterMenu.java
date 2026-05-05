package org.jairus.qcu_attendance;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class MasterMenu extends Application {

    private final String FONT_STACK = "-fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #E2E8F0;");

        ImageView logoView = new ImageView();
        try {
            Image logoImage = new Image(new File("qcu_logo-removebg-preview.jpg").toURI().toString());
            logoView.setImage(logoImage);
            logoView.fitWidthProperty().bind(root.widthProperty());
            logoView.fitHeightProperty().bind(root.heightProperty());
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Could not load qcu_logo.jpg!");
        }
        logoView.setEffect(new GaussianBlur(20));
        logoView.setOpacity(0.85);

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50));
        card.setMaxSize(450, 400);

        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 16px; -fx-border-color: rgba(255, 255, 255, 0.6); -fx-border-width: 2px; -fx-border-radius: 14px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 30, 0, 0, 10);");

        Label title = new Label("System Module Selection");
        title.setStyle(FONT_STACK + "-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");

        Label subTitle = new Label("Select your operational mode");
        subTitle.setStyle(FONT_STACK + "-fx-font-size: 16px; -fx-text-fill: #475569; -fx-padding: 0 0 20 0;");

        Button scannerBtn = createMenuButton("Scan", "#2563EB", "#1D4ED8");
        scannerBtn.setOnAction(e -> {
            try {
                new ScannerUI().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button adminBtn = createMenuButton("Open Admin Dashboard", "#0F172A", "#334155");
        adminBtn.setOnAction(e -> {
            try {
                new AdminDashboard().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(title, subTitle, scannerBtn, adminBtn);
        root.getChildren().addAll(logoView, card);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Attendance System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createMenuButton(String text, String idleColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);

        String idleStyle = FONT_STACK + "-fx-background-color: " + idleColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 4);";
        String hoverStyle = FONT_STACK + "-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 6);";

        btn.setStyle(idleStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(idleStyle));

        btn.setOnMousePressed(e -> { btn.setScaleX(0.95); btn.setScaleY(0.95); });
        btn.setOnMouseReleased(e -> { btn.setScaleX(1.0); btn.setScaleY(1.0); });

        return btn;
    }
}