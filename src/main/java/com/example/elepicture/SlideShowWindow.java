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
    private final double[] zoomLevels = {0.5, 0.75, 1.0, 1.25, 1.5, 2.0};
    private int currentZoomLevel = 2; // 默认1.0倍

    public SlideShowWindow(List<File> imageFiles, int startIndex) {
        this.imageFiles = imageFiles;
        this.currentIndex = startIndex;

        stage = new Stage();
        stage.setTitle("幻灯片播放");

        // 初始化图片视图
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        updateImage();

        StackPane imagePane = new StackPane(imageView);
        imagePane.setPadding(new Insets(10));

        // 创建控制按钮
        Button prevButton = new Button("左");
        Button nextButton = new Button("右");
        Button zoomInButton = new Button("放大");
        Button zoomOutButton = new Button("缩小");
        Button playButton = new Button("播放");
        Button stopButton = new Button("停止");
        Button exitButton = new Button("退出");

        // 设置按钮样式
        String buttonStyle = "-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 30px;";
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        zoomInButton.setStyle(buttonStyle);
        zoomOutButton.setStyle(buttonStyle);
        playButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        // 按钮事件处理
        prevButton.setOnAction(e -> showPreviousImage());
        nextButton.setOnAction(e -> showNextImage());
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        playButton.setOnAction(e -> startSlideShow());
        stopButton.setOnAction(e -> stopSlideShow());
        exitButton.setOnAction(e -> stage.close());

        // 状态标签
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px;");

        // 控制面板
        HBox controlPanel = new HBox(10, prevButton, nextButton, zoomInButton, zoomOutButton,
                playButton, stopButton, exitButton, statusLabel);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f0f0f0;");

        // 主布局
        BorderPane root = new BorderPane();
        root.setCenter(imagePane);
        root.setBottom(controlPanel);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        // 窗口大小变化时调整图片大小
        stage.widthProperty().addListener((obs, oldVal, newVal) -> adjustImageSize());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> adjustImageSize());

        // 初始化幻灯片播放定时器
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> showNextImage()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateImage() {
        if (imageFiles.isEmpty()) return;

        File currentFile = imageFiles.get(currentIndex);
        Image image = new Image(currentFile.toURI().toString());
        imageView.setImage(image);
        adjustImageSize();

        // 更新窗口标题显示当前图片位置
        stage.setTitle("幻灯片播放 (" + (currentIndex + 1) + "/" + imageFiles.size() + ")");
    }

    private void adjustImageSize() {
        if (imageView.getImage() == null) return;

        double maxWidth = stage.getWidth() - 40;
        double maxHeight = stage.getHeight() - 100;

        double zoomFactor = zoomLevels[currentZoomLevel];
        double imageWidth = imageView.getImage().getWidth() * zoomFactor;
        double imageHeight = imageView.getImage().getHeight() * zoomFactor;

        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            double ratio = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
            imageView.setFitWidth(imageWidth * ratio);
            imageView.setFitHeight(imageHeight * ratio);
        } else {
            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(imageHeight);
        }
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
        if (currentZoomLevel < zoomLevels.length - 1) {
            currentZoomLevel++;
            adjustImageSize();
        }
    }

    private void zoomOut() {
        if (currentZoomLevel > 0) {
            currentZoomLevel--;
            adjustImageSize();
        }
    }

    private void startSlideShow() {
        timeline.play();
    }

    private void stopSlideShow() {
        timeline.stop();
    }

    private void showAlert(String message) {
        // 可以在这里实现一个简单的提示，比如在状态栏显示
        System.out.println(message);
    }

    public void show() {
        stage.show();
    }
}