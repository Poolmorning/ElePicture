package com.example.elepicture;

import com.example.elepicture.utils.ClipboardManager;
import com.example.elepicture.utils.FileOperator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.List;

/**
 * 电子图片管理程序的主类，负责初始化用户界面并协调目录树、缩略图预览和文件操作
 */
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
        directoryTree.setPrefWidth(200); // 设置目录树宽度

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

        //将缩略图区域包装到滚动面板中
        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {//当用户选择新节点时触发
                File dir = newVal.getValue();//获取选中节点对应的目录文件对象
                //currentDirectory = dir;
                thumbnailManager.generateThumbnails(dir, imagePreviewPane, statusLabel,fileOperator);
            }
        });

        /*
         * 初始化幻灯片播放按钮
         * 该按钮用于启动当前目录的幻灯片播放功能，从第一张图片开始播放
         */
        Button slideShowButton = new Button("幻灯片播放");

// 设置按钮样式：
// - 字体大小14像素
// - 红色背景(#ff4444)
// - 白色文字
        slideShowButton.setStyle("-fx-font-size: 14px; -fx-background-color: #ff4444; -fx-text-fill: white;");

// 设置按钮点击事件处理
        slideShowButton.setOnAction(e -> {
            // 1. 获取目录树中当前选中的节点
            TreeItem<File> selectedItem = directoryTree.getSelectionModel().getSelectedItem();

            // 检查是否有选中的目录
            if (selectedItem != null) {
                // 2. 获取选中节点对应的目录文件对象
                File dir = selectedItem.getValue();

                // 3. 通过缩略图管理器获取当前目录下的所有图片文件
                List<File> imageFiles = thumbnailManager.getCurrentDirectoryImages(dir);

                // 检查目录中是否存在图片文件
                if (!imageFiles.isEmpty()) {
                    // 4. 创建幻灯片播放窗口
                    // 参数1: 图片文件列表
                    // 参数2: 起始图片索引(0表示从第一张开始)
                    SlideShowWindow slideShow = new SlideShowWindow(imageFiles, 0);

                    // 5. 显示幻灯片播放窗口
                    slideShow.show();
                }
            }
        });

        // 布局组装
        HBox bottomPanel = new HBox(10, slideShowButton, statusLabel);
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setPadding(new Insets(5));
        bottomPanel.setStyle("-fx-background-color: #f0f0f0;");

        VBox leftPane = new VBox(directoryTree);
        VBox.setVgrow(directoryTree, Priority.ALWAYS);// 目录树填充左侧剩余空间

        HBox bf =new HBox(statusLabel,bottomPanel);
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftPane);
        mainLayout.setCenter(imageScrollPane);
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