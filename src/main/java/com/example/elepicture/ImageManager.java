package com.example.elepicture;

import com.example.elepicture.utils.ClipboardManager;
import com.example.elepicture.utils.FileOperator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;

public class ImageManager extends Application {
    //private File currentDirectory = null;
    private ThumbnailManager thumbnailManager;
    private Label statusLabel;
    // 初始化ClipboardManager和FileOperator
    ClipboardManager clipboardManager = new ClipboardManager();
    FileOperator fileOperator = new FileOperator(clipboardManager);

    @Override
    public void start(Stage primaryStage) {
        // 初始化目录树
        DirectoryTreeView directoryTree = new DirectoryTreeView();
        directoryTree.setPrefWidth(200);
        // 初始化缩略图管理器
        FlowPane imagePreviewPane = new FlowPane();
        imagePreviewPane.setPadding(new Insets(10));
        imagePreviewPane.setHgap(10);
        imagePreviewPane.setVgap(10);
        imagePreviewPane.setPrefWrapLength(750); // 设置每行的宽度
        thumbnailManager = new ThumbnailManager();
        // 状态标签
        statusLabel = new Label("请选择一个目录查看图片。");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #f0f0f0;");

        ScrollPane imageScrollPane = new ScrollPane(imagePreviewPane);
        imageScrollPane.setFitToWidth(true);

        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {//当用户选择新节点时触发
                File dir = newVal.getValue();//获取选中节点对应的目录文件对象
                //currentDirectory = dir;
                thumbnailManager.generateThumbnails(dir, imagePreviewPane, statusLabel,fileOperator);
            }
        });

        // 布局组装
        VBox leftPane = new VBox(directoryTree);
        VBox.setVgrow(directoryTree, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftPane);
        mainLayout.setCenter(new ScrollPane(imagePreviewPane));
        mainLayout.setBottom(statusLabel);


        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("电子图片管理程序");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}