package com.example.elepicture;

import com.example.elepicture.utils.ClipboardManager;
import com.example.elepicture.utils.FileOperator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.List;

//电子图片管理程序的主类
public class ImageManager extends Application {
    private ThumbnailManager thumbnailManager;
    private Label statusLabel;
    private TextField addressBar;
    private File currentDir;
    private FlowPane imagePreviewPane;
    ClipboardManager clipboardManager = new ClipboardManager();
    FileOperator fileOperator = new FileOperator(clipboardManager);

    @Override
    public void start(Stage primaryStage) {
        // 创建顶部工具栏
        VBox topToolbar = new VBox(5);
        topToolbar.setPadding(new Insets(5));
        topToolbar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        // 创建地址栏
        HBox addressBarBox = new HBox(5);
        addressBar = new TextField();
        addressBar.setPromptText("输入文件夹路径");
        addressBar.setPrefWidth(600);
        addressBar.setOnAction(e -> navigateToDirectory(addressBar.getText()));

        Button browseButton = new Button();
        loadImage("/image/打开.png", browseButton);
        browseButton.setOnAction(e -> chooseDirectory(primaryStage));

        addressBarBox.getChildren().addAll(addressBar, browseButton);
        addressBarBox.setPadding(new Insets(0, 0, 5, 0));
        addressBarBox.setStyle("-fx-background-color: #f0f0f0;");

        // 创建操作栏
        HBox operationBar = new HBox(5);
        operationBar.setPadding(new Insets(5, 0, 0, 0));
        operationBar.setStyle("-fx-background-color: #f0f0f0;");

        // 创建操作按钮
        Button copyButton = new Button();
        Button pasteButton = new Button();
        Button cutButton = new Button();
        Button deleteButton = new Button();
        Button slideShowButton = new Button();

        // 设置按钮图标
        loadImage("/image/复制.png", copyButton);
        loadImage("/image/粘贴.png", pasteButton);
        loadImage("/image/剪切.png", cutButton);
        loadImage("/image/删除.png", deleteButton);
        loadImage("/image/播放.png", slideShowButton);

        // 添加按钮事件
        copyButton.setOnAction(e -> {
            fileOperator.copy(thumbnailManager.getSelectedBoxes(), thumbnailManager.getBoxFileMap());
            statusLabel.setText("已复制 " + thumbnailManager.getSelectedBoxes().size() + " 个文件");
        });

        pasteButton.setOnAction(e -> {
            if (currentDir != null) {
                fileOperator.paste(currentDir);
                refreshDisplay();
            }
        });

        cutButton.setOnAction(e -> {
            fileOperator.cut(thumbnailManager.getSelectedBoxes(), thumbnailManager.getBoxFileMap());
            statusLabel.setText("已剪切 " + thumbnailManager.getSelectedBoxes().size() + " 个文件");
            refreshDisplay();
        });

        deleteButton.setOnAction(e -> {
            try {
                if (fileOperator.delete(thumbnailManager.getSelectedBoxes(), thumbnailManager.getBoxFileMap())) {
                    refreshDisplay();
                }
            } catch (Exception ex) {
                showError("删除失败", ex.getMessage());
            }
        });

        //reloadButton.setOnAction(e -> refreshDisplay());

        operationBar.getChildren().addAll(copyButton, pasteButton, cutButton, deleteButton,slideShowButton);

        // 将地址栏和操作栏添加到顶部工具栏
        topToolbar.getChildren().addAll(addressBarBox, operationBar);

        // 初始化目录树
        DirectoryTreeView directoryTree = new DirectoryTreeView();
        directoryTree.setPrefWidth(250);

        // 初始化缩略图管理器
        imagePreviewPane = new FlowPane();
        imagePreviewPane.setPadding(new Insets(10));
        imagePreviewPane.setHgap(10);
        imagePreviewPane.setVgap(10);
        imagePreviewPane.setPrefWrapLength(750);
        thumbnailManager = new ThumbnailManager();

        // 初始化底部状态栏
        statusLabel = new Label("请选择一个目录查看图片。");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #f0f0f0;");

        // 将缩略图区域包装到滚动面板中
        ScrollPane imageScrollPane = new ScrollPane(imagePreviewPane);
        imageScrollPane.setFitToWidth(true);


        slideShowButton.setOnAction(e -> {
            TreeItem<File> selectedItem = directoryTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                File dir = selectedItem.getValue();
                List<File> imageFiles = thumbnailManager.getCurrentDirectoryImages(dir);
                if (!imageFiles.isEmpty()) {
                    SlideShowWindow slideShow = new SlideShowWindow(imageFiles, 0);
                    slideShow.show();
                }
            }
        });

        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File dir = newVal.getValue();
                currentDir = dir;
                addressBar.setText(dir.getAbsolutePath());
                thumbnailManager.generateThumbnails(dir, imagePreviewPane, statusLabel, fileOperator);
            }
        });

        // 布局组装
        HBox bottomPanel = new HBox(statusLabel);
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setPadding(new Insets(5));
        bottomPanel.setStyle("-fx-background-color: #f0f0f0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        HBox bf = new HBox(statusLabel, spacer, bottomPanel);

        VBox leftPane = new VBox(directoryTree);
        VBox.setVgrow(directoryTree, Priority.ALWAYS);
        leftPane.setMinWidth(150);
        leftPane.setPrefWidth(250);

        ScrollPane centerPane = imageScrollPane;

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(leftPane, centerPane);
        splitPane.setDividerPositions(0.25);
        SplitPane.setResizableWithParent(leftPane, false);

        mainLayout.setTop(topToolbar);
        mainLayout.setCenter(splitPane);
        mainLayout.setLeft(leftPane);
        mainLayout.setBottom(bf);

        Image icon = new Image(getClass().getResourceAsStream("/image/图标.png"));
        primaryStage.getIcons().add(icon);

        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("电子图片管理程序");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseDirectory(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择文件夹");
        if (currentDir != null) {
            directoryChooser.setInitialDirectory(currentDir);
        }
        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            currentDir = selectedDir;
            addressBar.setText(currentDir.getAbsolutePath());
            refreshDisplay();
        }
    }

    private void navigateToDirectory(String path) {
        File newDir = new File(path);
        if (newDir.exists() && newDir.isDirectory()) {
            currentDir = newDir;
            refreshDisplay();
        } else {
            showError("路径错误", "指定的路径不存在或不是有效的文件夹");
        }
    }

    private void refreshDisplay() {
        if (currentDir != null) {
            thumbnailManager.generateThumbnails(currentDir, imagePreviewPane, statusLabel, fileOperator);
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //加载按钮图片
    private void loadImage(String path,Button button) {
        Image pic = new Image(getClass().getResourceAsStream(path));
        ImageView picture = new ImageView(pic);
        picture.setFitWidth(20);
        picture.setFitHeight(20);
        button.setGraphic(picture);
        button.setMinSize(60, 25);
        button.setMaxSize(60, 25);
    }

    public static void main(String[] args) {
        launch(args);
    }
}