package com.example.elepicture;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class ImageManager extends Application {

    // imageExtensions：图片格式
    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    private final Set<VBox> selectedBoxes = new HashSet<>();
    private final Map<VBox, File> boxFileMap = new HashMap<>();
    private final List<File> clipboard = new ArrayList<>();
    private File currentDirectory = null;
    private ThumbnailManager thumbnailManager;
    private Label statusLabel;


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
        thumbnailManager = new ThumbnailManager();
        // 状态标签
        statusLabel = new Label("请选择一个目录查看图片。");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #f0f0f0;");

        ScrollPane imageScrollPane = new ScrollPane(imagePreviewPane);
        imageScrollPane.setFitToWidth(true);

        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File dir = newVal.getValue();
                currentDirectory = dir;
                thumbnailManager.generateThumbnails(dir, imagePreviewPane, statusLabel);
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
    //目录树的选择监听器
//        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal != null) {
//                File dir = newVal.getValue();
//                currentDirectory = dir;
//                File[] files = dir.listFiles();
//                if (files != null) {
//                    imagePreviewPane.getChildren().clear();
//                    selectedBoxes.clear();
//                    boxFileMap.clear();
//                    int count = 0;
//                    long totalSize = 0;
//                    for (File file : files) {
//                        if (isImageFile(file)) {
//                            try {
//                                Image img = new Image(new FileInputStream(file), 100, 100, true, true);
//                                ImageView imageView = new ImageView(img);
//                                imageView.setPreserveRatio(true);
//
//                                Label nameLabel = new Label(file.getName());
//                                VBox box = new VBox(imageView, nameLabel);
//                                box.setSpacing(5);
//                                box.setPadding(new Insets(5));
//                                box.setStyle("-fx-border-color: transparent;");
//                                boxFileMap.put(box, file);
//
//                                box.setOnMouseClicked(event -> {
//                                    if (event.getButton() == MouseButton.PRIMARY) {
//                                        if (event.isControlDown()) {
//                                            toggleSelectBox(box);
//                                        } else {
//                                            clearSelection();
//                                            selectBox(box);
//                                        }
//                                        statusLabel.setText("已选中 " + selectedBoxes.size() + " 张图片");
//                                    } else if (event.getButton() == MouseButton.SECONDARY) {
//                                        if (!selectedBoxes.contains(box)) {
//                                            clearSelection();
//                                            selectBox(box);
//                                        }
//
//                                    }
//                                });
//
//                                imagePreviewPane.getChildren().add(box);
//                                count++;
//                                totalSize += file.length();
//                            } catch (FileNotFoundException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                    }
//                    String readableSize = formatSize(totalSize);
//                    statusLabel.setText(String.format("目录：%s，共 %d 张图片，总大小：%s", dir.getAbsolutePath(), count, readableSize));
//                }
//            }
//        });

    public static void main(String[] args) {
        launch(args);
    }
}