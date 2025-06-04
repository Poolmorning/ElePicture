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

// 幻灯片播放窗口类，提供图片浏览、缩放和自动播放功能
public class SlideShowWindow {
    private final Stage stage; // 幻灯片主窗口
    private final List<File> imageFiles;// 图片文件列表
    private int currentIndex;// 当前显示图片的索引
    private ImageView imageView;// 图片显示组件
    private Timeline timeline; // 自动播放计时器
    private double currentZoom = 1.0;// 当前缩放级别
    private final double ZOOM_FACTOR = 0.1;// 每次缩放的变化量
    private final double MIN_ZOOM = 0.1;// 最小缩放级别
    private final double MAX_ZOOM = 5.0;// 最大缩放级别
    private Scene scene;// 主场景

    public SlideShowWindow(List<File> imageFiles, int startIndex) {
        this.imageFiles = imageFiles;
        this.currentIndex = startIndex;

        // 初始化舞台
        stage = new Stage();
        stage.setTitle("幻灯片播放");

        // 配置图片显示组件
        imageView = new ImageView();
        imageView.setPreserveRatio(true); // 保持图片宽高比
        imageView.setSmooth(true); // 启用平滑渲染
        imageView.setFitWidth(700); // 初始宽度
        imageView.setFitHeight(500); // 初始高度

        // 图片显示面板
        StackPane imagePane = new StackPane(imageView);
        imagePane.setPadding(new Insets(10));

        // 创建控制按钮
        Button prevButton = new Button(); // 上一张
        Button nextButton = new Button(); // 下一张
        Button zoomInButton = new Button(); // 放大
        Button zoomOutButton = new Button(); // 缩小
        Button playButton = new Button(); // 播放
        Button stopButton = new Button(); // 停止
        Button exitButton = new Button(); // 退出

        // 加载按钮图标
        loadImage("/image/播放.png", playButton);
        loadImage("/image/暂停.png", stopButton);
        loadImage("/image/放大.png", zoomInButton);
        loadImage("/image/缩小.png", zoomOutButton);
        loadImage("/image/向左.png", prevButton);
        loadImage("/image/向右.png", nextButton);
        loadImage("/image/退出.png", exitButton);

        // 设置按钮样式
        String buttonStyle = "-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 30px;";
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        zoomInButton.setStyle(buttonStyle);
        zoomOutButton.setStyle(buttonStyle);
        playButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        // 设置按钮事件
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
        root.setCenter(imagePane); // 图片显示区域
        root.setBottom(controlPanel); // 控制面板

        // 创建场景
        scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        // 设置鼠标滚轮缩放事件
        imagePane.setOnScroll(event -> {
            if (event.isControlDown()) { // 按住Ctrl键时缩放
                double zoomDelta = event.getDeltaY() > 0 ? ZOOM_FACTOR : -ZOOM_FACTOR;
                double newZoom = currentZoom + zoomDelta;
                // 限制缩放范围
                if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
                    currentZoom = newZoom;
                    applyZoom();
                }
                event.consume(); // 阻止事件继续传播
            }
        });

        // 显示初始图片
        updateImage();

        // 初始化自动播放计时器(1秒间隔)
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> showNextImage()));
        timeline.setCycleCount(Timeline.INDEFINITE); // 无限循环
    }

     // 更新当前显示的图片

    private void updateImage() {
        if (imageFiles.isEmpty()) return;
        File currentFile = imageFiles.get(currentIndex);
        Image image = new Image(currentFile.toURI().toString());
        imageView.setImage(image);
        applyZoom(); // 应用当前缩放
        // 更新窗口标题显示当前图片位置
        stage.setTitle("幻灯片播放 (" + (currentIndex + 1) + "/" + imageFiles.size() + ")");
    }

    //应用当前缩放级别
    private void applyZoom() {
        imageView.setScaleX(currentZoom);
        imageView.setScaleY(currentZoom);
    }

    //显示上一张图片
    private void showPreviousImage() {
        if (imageFiles.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            updateImage();
        } else {
            showAlert("已经是第一张图片");
        }
    }

    //显示下一张图片
    private void showNextImage() {
        if (imageFiles.isEmpty()) return;
        if (currentIndex < imageFiles.size() - 1) {
            currentIndex++;
            updateImage();
        } else {
            showAlert("已经是最后一张图片");
            stopSlideShow(); // 播放到最后自动停止
        }
    }

    //放大图片
    private void zoomIn() {
        if (currentZoom < MAX_ZOOM) {
            currentZoom += ZOOM_FACTOR;
            applyZoom();
        }
    }

    //缩小图片
    private void zoomOut() {
        if (currentZoom > MIN_ZOOM) {
            currentZoom -= ZOOM_FACTOR;
            applyZoom();
        }
    }

    //开始幻灯片自动播放
    private void startSlideShow() {
        timeline.play();
    }

    //停止幻灯片自动播放
    private void stopSlideShow() {
        timeline.stop();
    }

    // 显示临时提示信息
    private void showAlert(String message) {
        Label alertLabel = new Label(message);
        alertLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; " +
                "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10px;");
        alertLabel.setAlignment(Pos.CENTER);

        // 将提示标签添加到图片面板
        StackPane imagePane = (StackPane) ((BorderPane) scene.getRoot()).getCenter();
        imagePane.getChildren().add(alertLabel);
        StackPane.setAlignment(alertLabel, Pos.CENTER);

        // 1秒后自动移除提示
        Timeline fadeTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> imagePane.getChildren().remove(alertLabel)));
        fadeTimeline.play();
    }

    // 显示幻灯片窗口
    public void show() {
        // 设置窗口图标
        Image icon = new Image(getClass().getResourceAsStream("/image/图标.png"));
        stage.getIcons().add(icon);
        stage.show();
    }

    // 加载按钮图标
    private void loadImage(String path, Button button) {
        Image pic = new Image(getClass().getResourceAsStream(path));
        ImageView picture = new ImageView(pic);
        picture.setFitWidth(15);
        picture.setFitHeight(15);
        button.setGraphic(picture);
        button.setMinSize(30, 16);
        button.setMaxSize(30, 16);
    }
}