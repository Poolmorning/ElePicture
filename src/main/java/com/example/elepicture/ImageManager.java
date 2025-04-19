package com.example.elepicture;

import javafx.application.Application;
import javafx.collections.FXCollections;
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

    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    private final Set<VBox> selectedBoxes = new HashSet<>();
    private final Map<VBox, File> boxFileMap = new HashMap<>();
    private final List<File> clipboard = new ArrayList<>();
    private File currentDirectory = null;


    @Override
    public void start(Stage primaryStage) {
        //Button chooseDirButton = new Button("选择目录");

        //TreeView<File> directoryTree = new TreeView<>();
        DirectoryTreeView directoryTree = new DirectoryTreeView();
        directoryTree.setPrefWidth(200);

        FlowPane imagePreviewPane = new FlowPane();
        imagePreviewPane.setPadding(new Insets(10));
        imagePreviewPane.setHgap(10);
        imagePreviewPane.setVgap(10);
        ScrollPane imageScrollPane = new ScrollPane(imagePreviewPane);
        imageScrollPane.setFitToWidth(true);

        Label statusLabel = new Label("请选择一个目录查看图片。");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-background-color: #f0f0f0;");

         //目录树的选择监听器
        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File dir = newVal.getValue();
                currentDirectory = dir;
                File[] files = dir.listFiles();
                if (files != null) {
                    imagePreviewPane.getChildren().clear();
                    selectedBoxes.clear();
                    boxFileMap.clear();
                    int count = 0;
                    long totalSize = 0;
                    for (File file : files) {
                        if (isImageFile(file)) {
                            try {
                                Image img = new Image(new FileInputStream(file), 100, 100, true, true);
                                ImageView imageView = new ImageView(img);
                                imageView.setPreserveRatio(true);

                                Label nameLabel = new Label(file.getName());
                                VBox box = new VBox(imageView, nameLabel);
                                box.setSpacing(5);
                                box.setPadding(new Insets(5));
                                box.setStyle("-fx-border-color: transparent;");
                                boxFileMap.put(box, file);

                                box.setOnMouseClicked(event -> {
                                    if (event.getButton() == MouseButton.PRIMARY) {
                                        if (event.isControlDown()) {
                                            toggleSelectBox(box);
                                        } else {
                                            clearSelection();
                                            selectBox(box);
                                        }
                                        statusLabel.setText("已选中 " + selectedBoxes.size() + " 张图片");
                                    } else if (event.getButton() == MouseButton.SECONDARY) {
                                        if (!selectedBoxes.contains(box)) {
                                            clearSelection();
                                            selectBox(box);
                                        }
                                        showContextMenu(box, event.getScreenX(), event.getScreenY(), imagePreviewPane, statusLabel);
                                    }
                                });

                                imagePreviewPane.getChildren().add(box);
                                count++;
                                totalSize += file.length();
                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    String readableSize = formatSize(totalSize);
                    statusLabel.setText(String.format("目录：%s，共 %d 张图片，总大小：%s", dir.getAbsolutePath(), count, readableSize));
                }
            }
        });

        VBox leftPane = new VBox(directoryTree);
        VBox.setVgrow(directoryTree, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftPane);
        mainLayout.setCenter(imageScrollPane);
        mainLayout.setBottom(statusLabel);

        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("电子图片管理程序");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    // 右键菜单
    private void showContextMenu(VBox box, double x, double y, Pane parent, Label statusLabel) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("删除");
        MenuItem copyItem = new MenuItem("复制");
        MenuItem pasteItem = new MenuItem("粘贴");
        MenuItem renameItem = new MenuItem("重命名");

        copyItem.setOnAction(e -> {
            clipboard.clear();
            for (VBox b : selectedBoxes) {
                File file = boxFileMap.get(b);
                if (file != null && file.exists()) {
                    clipboard.add(file);
                }
            }
            statusLabel.setText("已复制 " + clipboard.size() + " 张图片");
        });

        pasteItem.setOnAction(e -> {
            int pastedCount = 0;
            if (currentDirectory != null && clipboard.size() > 0) {
                for (File source : clipboard) {
                    String newName = getUniqueName(currentDirectory, source.getName());
                    File dest = new File(currentDirectory, newName);
                    try {
                        copyFile(source, dest);
                        pastedCount++;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                // 刷新当前目录
                TreeItem<File> currentItem = new TreeItem<>(currentDirectory);
                currentItem.setExpanded(true);
                currentItem.getParent().setExpanded(false); // 强制刷新
                currentItem.getParent().setExpanded(true);
                statusLabel.setText("粘贴完成，共粘贴 " + pastedCount + " 张图片");
            }
        });

        renameItem.setOnAction(e -> statusLabel.setText("[待实现] 重命名 " + selectedBoxes.size() + " 张图片"));
        deleteItem.setOnAction(e ->
        {
            if (selectedBoxes.isEmpty()) {
                statusLabel.setText("请先选择要删除的图片");
                return;
            }

            new Alert(Alert.AlertType.CONFIRMATION,
                    "确定要删除选中的 " + selectedBoxes.size() + " 张图片吗？\n此操作不可撤销！",
                    ButtonType.OK, ButtonType.CANCEL)
                    .showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        int deletedCount = (int) selectedBoxes.stream()
                                .map(boxFileMap::get)
                                .filter(Objects::nonNull)
                                .filter(File::exists)
                                .filter(File::delete)
                                .count();

                        //refreshImagePreview(currentDirectory, statusLabel);
                        statusLabel.setText("已删除 " + deletedCount + " 张图片");
                    });
        });

        contextMenu.getItems().addAll(copyItem, pasteItem, deleteItem, renameItem);
        contextMenu.show(box, x, y);
    }
    // 复制文件
    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
    // 获取唯一文件名
    private String getUniqueName(File directory, String originalName) {
        File file = new File(directory, originalName);
        if (!file.exists()) return originalName;

        String name = originalName;
        String baseName = name;
        String extension = "";
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }
        int count = 1;
        while (file.exists()) {
            name = baseName + "_copy" + count + extension;
            file = new File(directory, name);
            count++;
        }
        return name;
    }
    // 切换选择框
    private void toggleSelectBox(VBox box) {
        if (selectedBoxes.contains(box)) {
            box.setStyle("-fx-border-color: transparent;");
            selectedBoxes.remove(box);
        } else {
            box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            selectedBoxes.add(box);
        }
    }
    // 选择框
    private void selectBox(VBox box) {
        box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
        selectedBoxes.add(box);
    }
    // 清除选择
    private void clearSelection() {
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear();
    }
    // 判断是否为图片文件
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return Arrays.stream(imageExtensions).anyMatch(name::endsWith);
    }
    // 格式化文件大小
    private String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (size >= 1024 * 1024) {
            return df.format(size / 1024.0 / 1024.0) + " MB";
        } else if (size >= 1024) {
            return df.format(size / 1024.0) + " KB";
        } else {
            return size + " B";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}





