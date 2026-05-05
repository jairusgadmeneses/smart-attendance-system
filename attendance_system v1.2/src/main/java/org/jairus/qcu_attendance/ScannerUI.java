package org.jairus.qcu_attendance;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;

public class ScannerUI extends Application {

    private ScannerLogic engine = new ScannerLogic();
    private Label statusLabel;
    private VBox card;
    private GaussianBlur logoBlur;

    private VBox studentCardPill;
    private Label nameLabel, sectionLabel, timeLabel;

    private final String MAIN_TEXT = "#FFFFFF";
    private final String FONT_STACK = "-fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;";

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #020617;");

        ImageView logoView = new ImageView();
        try {
            logoView.setImage(new Image(new File("qcu_logo-removebg-preview.jpg").toURI().toString()));
            logoView.fitWidthProperty().bind(root.widthProperty());
            logoView.fitHeightProperty().bind(root.heightProperty());
            logoView.setPreserveRatio(true);
        } catch (Exception e) {}

        logoBlur = new GaussianBlur(40);
        logoView.setEffect(logoBlur);
        logoView.setOpacity(0.20);

        Label titleLabel = new Label("QCU SCANNING");
        titleLabel.setStyle(FONT_STACK + " -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + MAIN_TEXT + ";");

        TextField scannerInput = new TextField();
        scannerInput.setPromptText("INSERT QR CODE HERE...");
        scannerInput.setMaxWidth(300);

        String inputStyleIdle =
                "-fx-background-color: rgba(30, 41, 59, 0.7); " +
                        "-fx-border-color: rgba(255, 255, 255, 0.2); " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 50px; " +
                        "-fx-background-radius: 50px; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-prompt-text-fill: #94A3B8; " +
                        "-fx-padding: 12px 25px; " +
                        "-fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-alignment: center;";

        String inputStyleFocused =
                "-fx-background-color: rgba(15, 23, 42, 0.9); " +
                        "-fx-border-color: #3B82F6; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 50px; " +
                        "-fx-background-radius: 50px; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-padding: 12px 25px; " +
                        "-fx-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-alignment: center; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(59, 130, 246, 0.5), 15, 0, 0, 0);";

        scannerInput.setStyle(inputStyleIdle);
        scannerInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) scannerInput.setStyle(inputStyleFocused);
            else scannerInput.setStyle(inputStyleIdle);
        });

        statusLabel = new Label("READY TO SCAN");
        statusLabel.setStyle(FONT_STACK + " -fx-font-size: 14px; -fx-text-fill: " + MAIN_TEXT + "; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        studentCardPill = new VBox(5);
        studentCardPill.setAlignment(Pos.CENTER);
        studentCardPill.setPadding(new Insets(15));
        studentCardPill.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        nameLabel = new Label("Name");
        nameLabel.setStyle(FONT_STACK + "-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #0F172A;");
        sectionLabel = new Label("Section");
        sectionLabel.setStyle(FONT_STACK + "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3B82F6;");
        timeLabel = new Label("Time");
        timeLabel.setStyle(FONT_STACK + "-fx-font-size: 12px; -fx-text-fill: #64748B;");

        studentCardPill.getChildren().addAll(nameLabel, sectionLabel, timeLabel);
        studentCardPill.setOpacity(0);
        studentCardPill.setTranslateY(30);

        card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 30, 50, 30));
        card.setMaxWidth(400);
        setCardStyleIdle();
        card.getChildren().addAll(titleLabel, scannerInput, statusLabel, studentCardPill);

        root.getChildren().addAll(logoView, card);

        scannerInput.setOnAction(e -> {
            String scannedCode = scannerInput.getText().trim();
            if (!scannedCode.isEmpty()) {
                executeScanVisuals(scannedCode);
                scannerInput.clear();
            }
        });

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("QCU ATTENDANCE SCANNING");
        primaryStage.setScene(scene);
        primaryStage.show();
        scannerInput.requestFocus();
    }

    private void executeScanVisuals(String barcode) {
        String[] result = engine.processLogicScan(barcode);
        String status = result[0];
        nameLabel.setText(result[1]);
        sectionLabel.setText(result[2]);
        timeLabel.setText("LOGGED: " + result[3]);

        if (status.equals("SUCCESS")) {
            updateUI("ACCESS GRANTED", "#10B981", "rgba(16, 185, 129, 0.20)");
            animateCardIn();
        } else if (status.equals("LATE")) {
            updateUI("ACCESS GRANTED: LATE", "#F59E0B", "rgba(245, 158, 11, 0.20)");
            animateCardIn();
        } else if (status.equals("EXIT")) {
            updateUI("EXIT APPROVED", "#3B82F6", "rgba(59, 130, 246, 0.20)");
            animateCardIn();
        } else if (status.equals("ALREADY_IN")) {
            updateUI("ACCESS DENIED: DUPLICATE ENTRY", "#F59E0B", "rgba(245, 158, 11, 0.20)");
        } else {
            updateUI("ACCESS DENIED: UNKNOWN CREDENTIAL", "#EF4444", "rgba(239, 68, 68, 0.20)");
        }
        playFeedbackAnimation();
    }

    private void updateUI(String text, String color, String bgColor) {
        statusLabel.setText(text);
        statusLabel.setStyle(FONT_STACK + " -fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: " + color + ";");
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 24px; -fx-border-color: " + color + "; -fx-border-width: 3px; -fx-border-radius: 22px; -fx-effect: dropshadow(three-pass-box, " + color.replace("#", "rgba(").replace(")", ", 0.6)") + ", 50, 0, 0, 0);");
    }

    private void animateCardIn() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(350), studentCardPill);
        slide.setToY(0);
        FadeTransition fade = new FadeTransition(Duration.millis(350), studentCardPill);
        fade.setToValue(1.0);
        new ParallelTransition(slide, fade).play();
    }

    private void playFeedbackAnimation() {
        ScaleTransition pop = new ScaleTransition(Duration.millis(150), card);
        pop.setByX(0.02); pop.setByY(0.02); pop.setAutoReverse(true); pop.setCycleCount(2); pop.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            statusLabel.setText("READY TO SCAN");
            statusLabel.setStyle(FONT_STACK + " -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + MAIN_TEXT + "; -fx-letter-spacing: 2px;");
            setCardStyleIdle();
            studentCardPill.setOpacity(0);
            studentCardPill.setTranslateY(30);
        });
        delay.play();
    }

    private void setCardStyleIdle() {
        card.setStyle("-fx-background-color: rgba(15, 23, 42, 0.80); -fx-background-radius: 24px; -fx-border-color: rgba(255, 255, 255, 0.15); -fx-border-width: 2px; -fx-border-radius: 22px; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.6), 30, 0, 0, 10);");
    }

    public static void main(String[] args) {
        launch(args);
    }
}