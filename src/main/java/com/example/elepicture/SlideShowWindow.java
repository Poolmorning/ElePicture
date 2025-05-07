package com.example.elepicture;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

import javafx.scene.control.ScrollPane;
import javafx.application.Platform;

public class SlideShowWindow {
    private final Stage stage;
    private final List<File> imageFiles;//要展示的图片文件列表
    private int currentIndex;//图片的索引
    private ImageView imageView;//图片视图
    private Timeline timeline;//幻灯片自动播放的时间轴控制器
    private final double[] zoomLevels = {0.1,0.5, 0.75, 1.0, 1.25, 1.5, 2.0};//50%, 75%, 100%, 125%, 150%, 200% 缩放
    private int currentZoomLevel = 0; // 默认1.0倍
    private Scene scene;

    private Label statusLabel;

    public SlideShowWindow(List<File> imageFiles, int startIndex) {
        this.imageFiles = imageFiles;//存储图片文件列表
        this.currentIndex = startIndex;//设置当前显示图片的索引

        stage = new Stage();
        stage.setTitle("幻灯片播放");

        //初始化图片视图
        imageView = new ImageView();
        imageView.setPreserveRatio(true);//保持图片宽高比
        imageView.setSmooth(true);//启用图片平滑处理

        //创建图片容器面板
        /*
        StackPane imagePane = new StackPane(imageView);
        imagePane.setPadding(new Insets(10));
        */

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setPadding(new Insets(10));

        // 使用 ScrollPane 包裹 StackPane
        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setPannable(true); // 鼠标可拖动
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(new Insets(10));

        /*
        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setPannable(true); // 启用鼠标拖动
        scrollPane.setFitToWidth(true); // 图片宽度适应窗口宽度（适度）
        scrollPane.setFitToHeight(true); // 图片高度适应窗口高度
        scrollPane.setPadding(new Insets(10));
        */

        //创建控制按钮
        Button prevButton = new Button("<-");//前一张图片
        Button nextButton = new Button("->");//下一张图片
        Button zoomInButton = new Button("放大");
        Button zoomOutButton = new Button("缩小");
        Button playButton = new Button("播放");
        Button stopButton = new Button("停止");
        Button exitButton = new Button("退出");

        //设置按钮样式
        String buttonStyle = "-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 30px;";
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        zoomInButton.setStyle(buttonStyle);
        zoomOutButton.setStyle(buttonStyle);
        playButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        //按钮事件处理
        prevButton.setOnAction(e -> showPreviousImage());//点击显示上一张图片
        nextButton.setOnAction(e -> showNextImage());//点击显示下一张图片
        zoomInButton.setOnAction(e -> zoomIn());//点击放大图片
        zoomOutButton.setOnAction(e -> zoomOut());//点击缩小图片
        playButton.setOnAction(e -> startSlideShow());//点击开始幻灯片自动播放
        stopButton.setOnAction(e -> stopSlideShow());//点击停止自动播放
        exitButton.setOnAction(e -> stage.close());//点击关闭窗口

        //状态标签
        //Label statusLabel = new Label();
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px;");
        //statusLabel.setStyle("-fx-font-size: 14px;");

        //控制面板
        HBox controlPanel = new HBox(10, prevButton, nextButton, zoomInButton, zoomOutButton, playButton, stopButton, exitButton, statusLabel);
        controlPanel.setAlignment(Pos.CENTER);//居中对齐
        controlPanel.setPadding(new Insets(10));
        controlPanel.setStyle("-fx-background-color: #f0f0f0;");

        //主布局
        BorderPane root = new BorderPane();

        /*
        root.setCenter(imagePane);
         */
        root.setCenter(scrollPane);

        root.setBottom(controlPanel);

        scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        updateImage();// 加载并显示当前索引的图片

        //窗口大小变化时调整图片大小
        stage.widthProperty().addListener((obs, oldVal, newVal) -> adjustImageSize());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> adjustImageSize());

        //初始化幻灯片播放定时器
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> showNextImage()));
        timeline.setCycleCount(Timeline.INDEFINITE);//设置循环次数为无限
    }
    //更新当前显示的图片
    private void updateImage() {
        if (imageFiles.isEmpty()) return;

        File currentFile = imageFiles.get(currentIndex);//获取当前索引对应的图片文件
        Image image = new Image(currentFile.toURI().toString());//加载图片
        imageView.setImage(image);
        adjustImageSize();//调整图片显示大小以适应窗口

        // 更新窗口标题显示当前图片位置
        stage.setTitle("幻灯片播放 (" + (currentIndex + 1) + "/" + imageFiles.size() + ")");

        //状态同步
        updateZoomStatus();
    }
    //调整图片显示大小以适应窗口
    private void adjustImageSize() {
       /*
        if (imageView.getImage() == null) return;
        //图片可用的最大宽度和高度
        double maxWidth = scene.getWidth() - 40;
        double maxHeight = scene.getHeight() - 100;
        //根据当前缩放级别计算图片的实际宽度和高度
        double zoomFactor = zoomLevels[currentZoomLevel];
        double imageWidth = imageView.getImage().getWidth();
        double imageHeight = imageView.getImage().getHeight();
        //检查缩放后的图片是否超出窗口可用空间,按照最大宽高比缩放
        if (imageWidth > maxWidth || imageHeight > maxHeight){
            imageWidth = maxWidth * zoomFactor;
            imageHeight = maxHeight * zoomFactor;
        }
        //检查缩放后的图片是否超出窗口可用空间
        if (imageWidth > maxWidth || imageHeight > maxHeight) {// 如果超出，计算保持宽高比的最大缩放比例
            double ratio = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
            currentZoomLevel--;
            imageView.setFitWidth(imageWidth*ratio);
            imageView.setFitHeight(imageHeight*ratio);
            //System.out.println("maxWidth * ratio " + imageWidth*ratio);
            //System.out.println("maxHeight * ratio " +imageWidth*ratio);

        } else {// 如果没有超出窗口，直接使用缩放后的原始尺寸
            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(imageHeight);
        }
        */



        if (imageView.getImage() == null) return;

        // 原始图片尺寸
        double originalWidth = imageView.getImage().getWidth();
        double originalHeight = imageView.getImage().getHeight();

        // 应用当前缩放级别
        double zoomFactor = zoomLevels[currentZoomLevel];
        imageView.setFitWidth(originalWidth * zoomFactor);
        imageView.setFitHeight(originalHeight * zoomFactor);

        // 获取ScrollPane视口和内容尺寸
        ScrollPane scrollPane = (ScrollPane) scene.lookup(".scroll-pane");
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        // 计算居中滚动位置
        double hScroll = (imageView.getFitWidth() - viewportWidth) / 2;
        double vScroll = (imageView.getFitHeight() - viewportHeight) / 2;

        // 设置视口位置（需放在Platform.runLater中确保UI更新完成）
        Platform.runLater(() -> {
            scrollPane.setHvalue(hScroll / (imageView.getFitWidth() - viewportWidth));
            scrollPane.setVvalue(vScroll / (imageView.getFitHeight() - viewportHeight));
        });


        /*
        if (imageView.getImage() == null) return;

        // 原始图片大小
        double originalWidth = imageView.getImage().getWidth();
        double originalHeight = imageView.getImage().getHeight();

        // 当前缩放因子
        double zoomFactor = zoomLevels[currentZoomLevel];

        // 计算缩放后的尺寸
        double newWidth = originalWidth * zoomFactor;
        double newHeight = originalHeight * zoomFactor;

        // 应用缩放后的尺寸
        imageView.setFitWidth(newWidth);
        imageView.setFitHeight(newHeight);

        */


    }
    //显示上一张图片
    private void showPreviousImage() {
        if (imageFiles.isEmpty()) return;

        if (currentIndex > 0) {
            currentIndex--;//索引减1，切换到上一张图片
            updateImage();//更新图片
        } else {
            showAlert("已经是第一张图片");
        }
    }
    //显示下一张图片
    private void showNextImage() {
        if (imageFiles.isEmpty()) return;

        if (currentIndex < imageFiles.size() - 1) {
            currentIndex++;//索引加1，切换到下一张图片
            updateImage();//更新图片
        } else {
            showAlert("已经是最后一张图片");
            stopSlideShow();
        }
    }
    //放大图片
    private void zoomIn() {
        /*
        if (currentZoomLevel < zoomLevels.length - 1) {
            currentZoomLevel++;//增加缩放级别
            adjustImageSize();
        }
        */
        if (currentZoomLevel < zoomLevels.length - 1) {
            currentZoomLevel++;
            adjustImageSize();
            updateZoomStatus();
        } else {
            showAlert("已达到最大缩放级别");
        }
    }

    private void zoomOut() {
        /*
        if (currentZoomLevel > 0) {
            currentZoomLevel--;//减小缩放级别
            adjustImageSize();
        }
        */
        if (currentZoomLevel > 0) {
            currentZoomLevel--;
            adjustImageSize();
            updateZoomStatus();
        } else {
            showAlert("已达到最小缩放级别");
        }
    }

    private void updateZoomStatus() {
        int percent = (int) (zoomLevels[currentZoomLevel] * 100);
        statusLabel.setText("缩放: " + percent + "%");
    }

    private void startSlideShow() {
        timeline.play();
    }

    private void stopSlideShow() {
        timeline.stop();
    }

    private void showAlert(String message) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
        popupStage.initStyle(StageStyle.UNDECORATED); // 无边框

        // 创建消息内容
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10px;");
        StackPane pane = new StackPane(messageLabel);

        // 设置场景
        Scene popupScene = new Scene(pane);
        popupStage.setScene(popupScene);
        popupStage.setAlwaysOnTop(true);
        popupStage.show();

        // 设置 1.5 秒后关闭窗口的定时器
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> popupStage.close());
        delay.play();
    }

    public void show() {
        stage.show();
    }
}