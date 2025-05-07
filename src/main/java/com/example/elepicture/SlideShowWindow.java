package com.example.elepicture;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.util.List;

public class SlideShowWindow {
    private final Stage stage;
    private final List<File> imageFiles;
    private int currentIndex;
    private ImageView imageView;
    private Timeline timeline;
    private double currentZoom = 1.0;
    private final double ZOOM_FACTOR = 0.1;
    private final double MIN_ZOOM = 0.1;
    private final double MAX_ZOOM = 5.0;
    private Scene scene;

    public SlideShowWindow(List<File> imageFiles, int startIndex) {
        this.imageFiles = imageFiles;
        this.currentIndex = startIndex;

        stage = new Stage();
        stage.setTitle("幻灯片播放");

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitWidth(700);
        imageView.setFitHeight(500);

        StackPane imagePane = new StackPane(imageView);
        imagePane.setPadding(new Insets(10));

        Button prevButton = new Button("左");
        Button nextButton = new Button("右");
        Button zoomInButton = new Button("放大");
        Button zoomOutButton = new Button("缩小");
        Button playButton = new Button("播放");
        Button stopButton = new Button("停止");
        Button exitButton = new Button("退出");

        String buttonStyle = "-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 30px;";
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        zoomInButton.setStyle(buttonStyle);
        zoomOutButton.setStyle(buttonStyle);
        playButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        prevButton.setOnAction(e -> showPreviousImage());
        nextButton.setOnAction(e -> showNextImage());
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        playButton.setOnAction(e -> startSlideShow());
        stopButton.setOnAction(e -> stopSlideShow());
        exitButton.setOnAction(e -> stage.close());

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px;");

        HBox controlPanel = new HBox(10, prevButton, nextButton, zoomInButton, zoomOutButton,
                playButton, stopButton, exitButton, statusLabel);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f0f0f0;");

        BorderPane root = new BorderPane();
        root.setCenter(imagePane);
        root.setBottom(controlPanel);

        scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        imagePane.setOnScroll(event -> {
            if (event.isControlDown()) {
                double zoomDelta = event.getDeltaY() > 0 ? ZOOM_FACTOR : -ZOOM_FACTOR;
                double newZoom = currentZoom + zoomDelta;
                if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
                    currentZoom = newZoom;
                    applyZoom();
                }
                event.consume();
            }
        });

        updateImage();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> showNextImage()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateImage() {
        if (imageFiles.isEmpty()) return;
        File currentFile = imageFiles.get(currentIndex);
        Image image = new Image(currentFile.toURI().toString());
        imageView.setImage(image);
        applyZoom();
        stage.setTitle("幻灯片播放 (" + (currentIndex + 1) + "/" + imageFiles.size() + ")");
    }

    private void applyZoom() {
        imageView.setScaleX(currentZoom);
        imageView.setScaleY(currentZoom);
    }

    private void showPreviousImage() {
        if (imageFiles.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            updateImage();
        } else {
            showAlert("已经是第一张图片");
        }
    }

    private void showNextImage() {
        if (imageFiles.isEmpty()) return;
        if (currentIndex < imageFiles.size() - 1) {
            currentIndex++;
            updateImage();
        } else {
            showAlert("已经是最后一张图片");
            stopSlideShow();
        }
    }

    private void zoomIn() {
        if (currentZoom < MAX_ZOOM) {
            currentZoom += ZOOM_FACTOR;
            applyZoom();
        }
    }

    private void zoomOut() {
        if (currentZoom > MIN_ZOOM) {
            currentZoom -= ZOOM_FACTOR;
            applyZoom();
        }
    }

    private void startSlideShow() {
        timeline.play();
    }

    private void stopSlideShow() {
        timeline.stop();
    }

    private void showAlert(String message) {
        Label alertLabel = new Label(message);
        alertLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10px;");
        alertLabel.setAlignment(Pos.CENTER);

        StackPane imagePane = (StackPane) ((BorderPane) scene.getRoot()).getCenter();
        imagePane.getChildren().add(alertLabel);
        StackPane.setAlignment(alertLabel, Pos.CENTER);

        Timeline fadeTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> imagePane.getChildren().remove(alertLabel)));
        fadeTimeline.play();
    }

    public void show() {
        stage.show();
    }
}
