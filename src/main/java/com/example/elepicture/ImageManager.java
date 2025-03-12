package com.example.elepicture;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageManager extends Application {
    private TreeView<File> directoryTree; // 目录树视图
    private FlowPane previewPane; // 图片预览面板
    private Label statusLabel; // 状态栏
    private Button slideshowButton, deleteButton, copyButton, pasteButton, renameButton, zoomInButton, zoomOutButton;// 功能按钮
    private List<File> selectedImages = new ArrayList<>(); // 选中的图片列表
    private File copiedImage; // 被复制的图片

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("电子图片管理程序");

        // 选择目录按钮
        Button selectDirButton = new Button("选择目录");
        selectDirButton.setOnAction(e -> openDirectoryChooser(primaryStage));

        // 目录树
        directoryTree = new TreeView<>();
        directoryTree.setOnMouseClicked(e -> loadImagesFromDirectory());
        ScrollPane treeScroll = new ScrollPane(directoryTree);
        treeScroll.setPrefWidth(200);

        // 图片预览面板
        previewPane = new FlowPane();
        ScrollPane previewScroll = new ScrollPane(previewPane);

        // 状态栏
        statusLabel = new Label("状态信息");

        // 功能按钮
        slideshowButton = new Button("播放幻灯片");
        slideshowButton.setOnAction(e -> startSlideshow());
        deleteButton = new Button("删除图片");
        copyButton = new Button("复制图片");
        pasteButton = new Button("粘贴图片");
        renameButton = new Button("重命名图片");
        zoomInButton = new Button("放大");
        zoomOutButton = new Button("缩小");

        HBox buttonBar = new HBox(10, slideshowButton, deleteButton, copyButton, pasteButton, renameButton, zoomInButton, zoomOutButton);

        // 布局管理
        BorderPane root = new BorderPane();
        root.setTop(selectDirButton);
        root.setLeft(treeScroll);
        root.setCenter(previewScroll);
        root.setBottom(statusLabel);
        root.setRight(buttonBar);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //打开目录选择器，并在目录树中显示选中的目录
    private void openDirectoryChooser(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            TreeItem<File> rootItem = new TreeItem<>(selectedDirectory);
            directoryTree.setRoot(rootItem);
            populateTreeView(rootItem);
        }
    }

    //填充目录树视图，显示目录中的图片文件
    private void populateTreeView(TreeItem<File> rootItem) {
        File dir = rootItem.getValue();
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() || file.getName().matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                        TreeItem<File> item = new TreeItem<>(file);
                        rootItem.getChildren().add(item);
                    }
                }
            }
        }
    }

    //加载并显示选定目录中的图片
    private void loadImagesFromDirectory() {
        previewPane.getChildren().clear();
        TreeItem<File> selectedItem = directoryTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            File selectedDir = selectedItem.getValue();
            if (selectedDir.isDirectory()) {
                File[] files = selectedDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                            ImageView imageView = new ImageView(new Image(file.toURI().toString(), 100, 100, true, true));
                            imageView.setOnMouseClicked(e -> selectImage(file, e));
                            previewPane.getChildren().add(imageView);
                        }
                    }
                }
            }
        }
    }

    //选择图片，支持单选和右键菜单
    private void selectImage(File file, javafx.scene.input.MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            if (!selectedImages.contains(file)) {
                selectedImages.add(file);
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {
            showContextMenu(file, e);
        }
    }

    //显示右键菜单（删除、复制、重命名）
    private void showContextMenu(File file, javafx.scene.input.MouseEvent e) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("删除");
        deleteItem.setOnAction(ev -> deleteImage(file));
        MenuItem copyItem = new MenuItem("复制");
        copyItem.setOnAction(ev -> copyImage(file));
        MenuItem renameItem = new MenuItem("重命名");
        renameItem.setOnAction(ev -> renameImage(file));
        contextMenu.getItems().addAll(deleteItem, copyItem, renameItem);
        contextMenu.show(previewPane, e.getScreenX(), e.getScreenY());
    }

    //删除选中的图片
    private void deleteImage(File file) {
        // 这里填充删除图片的逻辑
    }


    //复制选中的图片
    private void copyImage(File file) {
        // 这里填充复制图片的逻辑
    }

    //粘贴图片到当前选中的目录
    private void pasteImage() {
        // 这里填充粘贴图片的逻辑
    }

    //重命名选中的图片
    private void renameImage(File file) {
        // 这里填充重命名图片的逻辑
    }

    //开始幻灯片播放
    private void startSlideshow() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "幻灯片播放功能未实现");
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}




