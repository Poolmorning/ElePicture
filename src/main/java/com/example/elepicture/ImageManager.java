package com.example.elepicture;

import com.example.elepicture.utils.ClipboardManager;
import com.example.elepicture.utils.FileOperator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;


import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;


import java.io.*;
import java.util.List;

 //电子图片管理程序的主类
public class ImageManager extends Application {
    // 缩略图管理器，负责生成和显示图片缩略图
    private ThumbnailManager thumbnailManager;
    // 状态栏标签，用于显示当前操作状态（如目录加载进度）
    private Label statusLabel;
    // 剪贴板管理器，处理文件的复制/粘贴操作
    ClipboardManager clipboardManager = new ClipboardManager();
    // 文件操作器，封装文件的删除、重命名等操作
    FileOperator fileOperator = new FileOperator(clipboardManager);

    @Override
    public void start(Stage primaryStage) {
        // 初始化目录树
        DirectoryTreeView directoryTree = new DirectoryTreeView();
        directoryTree.setPrefWidth(250); // 设置目录树宽度

        // 初始化缩略图管理器
        FlowPane imagePreviewPane = new FlowPane();
        imagePreviewPane.setPadding(new Insets(10));
        imagePreviewPane.setHgap(10);
        imagePreviewPane.setVgap(10);
        imagePreviewPane.setPrefWrapLength(750); // 设置每行的宽度
        thumbnailManager = new ThumbnailManager();

        // 初始化底部状态栏
        statusLabel = new Label("请选择一个目录查看图片。");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #f0f0f0;");

        // 将缩略图区域包装到滚动面板中
        ScrollPane imageScrollPane = new ScrollPane(imagePreviewPane);
        imageScrollPane.setFitToWidth(true);

        //初始化幻灯片播放按钮
        Image play = new Image(getClass().getResourceAsStream("/image/24gf-playCircle-copy.png"));
        //Image play = new Image("/image/24gf-playCircle-copy.png");//不知道为什么相对路径用不了？
        ImageView playIcon = new ImageView(play);
        playIcon.setFitWidth(15);
        playIcon.setFitHeight(15);
        Button slideShowButton = new Button(/*"幻灯片播放"*/);
        slideShowButton.setGraphic(playIcon);
        //设置按钮大小
        slideShowButton.setMinSize(30, 16);
        slideShowButton.setMaxSize(30, 16);

        //点击事件处理
        slideShowButton.setOnAction(e -> {
            //获取目录树中当前选中的节点
            TreeItem<File> selectedItem = directoryTree.getSelectionModel().getSelectedItem();
            //检查是否有选中的目录
            if (selectedItem != null) {
                // 获取选中节点对应的目录文件对象
                File dir = selectedItem.getValue();

                //获取当前目录下的所有图片文件
                List<File> imageFiles = thumbnailManager.getCurrentDirectoryImages(dir);
                //检查目录中是否存在图片文件
                if (!imageFiles.isEmpty()) {
                    SlideShowWindow slideShow = new SlideShowWindow(imageFiles, 0);
                    slideShow.show();
                }else{
                    // 创建一个新窗口（Stage）
                    Stage popupStage = new Stage();
                    popupStage.initModality(Modality.APPLICATION_MODAL); // 模态窗口
                    popupStage.initStyle(StageStyle.UNDECORATED); // 无边框

                    // 创建消息内容
                    Label messageLabel = new Label(" 该目录没有图片！");
                    messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10px;");
                    StackPane pane = new StackPane(messageLabel);

                    // 设置场景
                    Scene popupScene = new Scene(pane);
                    popupStage.setScene(popupScene);
                    popupStage.setAlwaysOnTop(true);
                    popupStage.show();

                    // 设置 1.5 秒后关闭窗口的定时器
                    PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                    delay.setOnFinished(event -> popupStage.close());
                    delay.play();
                }
            }
        });

        //将缩略图区域包装到滚动面板中
        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {//当用户选择新节点时触发
                File dir = newVal.getValue();//获取选中节点对应的目录文件对象
                //currentDirectory = dir;
                thumbnailManager.generateThumbnails(dir, imagePreviewPane, statusLabel,fileOperator);
            }
        });

        // 布局组装
        HBox bottomPanel = new HBox(10, slideShowButton, statusLabel);
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setPadding(new Insets(5));
        bottomPanel.setStyle("-fx-background-color: #f0f0f0;");

        // 添加间隔区域使按钮右对齐
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // 关键：让间隔区域自动扩展

        BorderPane mainLayout = new BorderPane();
        HBox bf =new HBox(statusLabel,spacer,bottomPanel);

        VBox leftPane = new VBox(directoryTree);

        VBox.setVgrow(directoryTree, Priority.ALWAYS);// 目录树填充左侧剩余空间
        leftPane.setMinWidth(150);   // 最小宽度
        leftPane.setPrefWidth(250);  // 默认显示宽度

        // 中间图片预览面板（已包装为 ScrollPane）
        ScrollPane centerPane = imageScrollPane;

        // 使用 SplitPane 实现左右拖动
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL); // 横向分割
        splitPane.getItems().addAll(leftPane, centerPane);
        splitPane.setDividerPositions(0.25); // 左侧占25%宽度
        SplitPane.setResizableWithParent(leftPane, false);

        // 放入 BorderPane 中
        mainLayout.setCenter(splitPane); // 使用 splitPane 作为中间部分

        mainLayout.setLeft(leftPane);
        //mainLayout.setCenter(imageScrollPane);
        mainLayout.setBottom(bf);
        //mainLayout.setBottom();

        // 创建场景并显示窗口
        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("电子图片管理程序");
        primaryStage.setScene(scene);
        primaryStage.show();

    }










    /**
     * 启动JavaFX应用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
}