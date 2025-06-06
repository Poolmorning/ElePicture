package com.example.elepicture;

import com.example.elepicture.utils.FileOperator;
import com.example.elepicture.utils.SelectionBox;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;
import javafx.geometry.Insets;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class ThumbnailManager {
    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    private final Set<VBox> selectedBoxes = new HashSet<>();
    private final HashMap<VBox, File> boxFileMap = new HashMap<>();
    private int count = 0;
    private long totalSize = 0;
    private ContextMenu contextMenu;
    private final Set<VBox> allThumbnails = new HashSet<>();
    private FlowPane thisPane;
    private ThumbnailLoaderService thumbnailLoaderService;
    private FlowPane imagePane;
    private SelectionBox selectionBox;
    private boolean isDraggingSelectionBox = false;
    private File cur;

    public Set<VBox> getAllThumbnails() {
        return allThumbnails;
    }

    public void generateThumbnails(File dir, FlowPane imagePreviewPane, Label statusLabel, FileOperator fileOperator) {
        cur = dir;
        this.imagePane = imagePreviewPane; // 存储引用
        this.thisPane = imagePreviewPane; // 同时更新 thisPane（如果其他地方用到）
        // 取消正在进行的加载任务
        if (thumbnailLoaderService != null) {
            thumbnailLoaderService.cancel();
        }

        // 创建新的服务
        thumbnailLoaderService = new ThumbnailLoaderService();
        thumbnailLoaderService.setParameters(dir, imagePreviewPane, statusLabel, fileOperator);

        // 设置成功完成时的处理
        thumbnailLoaderService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                String readableSize = formatSize(totalSize);
                statusLabel.setText(String.format("共 %d 张图片，总大小：%s", count, readableSize));
            }
        });

        // 设置失败时的处理
        thumbnailLoaderService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                statusLabel.setText("加载缩略图时出错: " + thumbnailLoaderService.getException().getMessage());
            }
        });

        // 启动服务
        thumbnailLoaderService.restart();
    }
    //加载服务内部类
    private class ThumbnailLoaderService extends Service<Void> {
        private File dir;
        private FlowPane imagePreviewPane;
        private Label statusLabel;
        private FileOperator fileOperator;
        // 设置参数方法
        public void setParameters(File dir, FlowPane imagePreviewPane, Label statusLabel, FileOperator fileOperator) {
            this.dir = dir;
            this.imagePreviewPane = imagePreviewPane;
            this.statusLabel = statusLabel;
            this.fileOperator = fileOperator;
        }
        // 创建任务
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    if (dir != null) {
                        File[] files = dir.listFiles();
                        if (files != null) {
                            int total = files.length;
                            int processed = 0;

                            // 在UI线程初始化界面
                            Platform.runLater(() -> {
                                imagePreviewPane.getChildren().clear();
                                selectedBoxes.clear();
                                boxFileMap.clear();
                                allThumbnails.clear();
                                count = 0;
                                totalSize = 0;

                                // 框选
                                selectionBox = new SelectionBox(imagePreviewPane, allThumbnails, selectedBoxes, statusLabel);
                                // 设置空白处点击事件
                                imagePreviewPane.setOnMouseClicked(event -> {
                                    if (event.getButton() == MouseButton.PRIMARY && event.getTarget() == imagePreviewPane) {
                                        if (contextMenu != null) {
                                            contextMenu.hide();
                                        }
                                        clearSelection();
                                        statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize));
                                    } else if (event.getButton() == MouseButton.SECONDARY && event.getTarget() == imagePreviewPane) {
                                        clearSelection();
                                        showMenu(dir, null, event.getScreenX(), event.getScreenY(), statusLabel, fileOperator, imagePreviewPane);
                                        statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize));
                                    }
                                });
                            });
                            // 遍历文件列表，创建缩略图
                            for (File file : files) {
                                if (isCancelled()) {
                                    break;
                                }

                                if (isImageFile(file)) {
                                    final File currentFile = file;

                                    // 在UI线程创建并添加缩略图
                                    Platform.runLater(() -> {
                                        createThumbnail(currentFile, imagePreviewPane, dir, statusLabel, fileOperator);
                                    });

                                    processed++;
                                    updateProgress(processed, total);
                                }
                            }
                        }
                    }
                    return null;
                }
            };
        }
    }
    // 创建单个缩略图
    private void createThumbnail(File file, FlowPane imagePreviewPane, File dir, Label statusLabel, FileOperator fileOperator) {
        // 创建并配置图片视图
        Image img = new Image(file.toURI().toString(), 100, 100, true, true, true);
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        // 创建图片容器
        Pane container = new Pane(imageView);
        container.setMinSize(100, 100);
        container.setMaxSize(100, 100);
        container.setPickOnBounds(false);
        // 创建文件名标签
        Label nameLabel = new Label(file.getName());
        if (nameLabel.getText().length() > 10) {
            nameLabel.setText(nameLabel.getText().substring(0, 10) + "...");
        }
        nameLabel.setAlignment(Pos.CENTER);
        // 创建缩略图VBox
        VBox box = new VBox(container, nameLabel);
        box.setSpacing(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-border-color: transparent;");
        // 添加到映射和集合
        boxFileMap.put(box, file);
        allThumbnails.add(box);
        box.setFocusTraversable(true);
        // 设置鼠标点击事件
        box.setOnMouseClicked(event -> {
            box.requestFocus();
            if (event.getButton() == MouseButton.PRIMARY) {// 左键点击事件
                if (contextMenu != null) {
                    contextMenu.hide();
                }
                if (event.getClickCount() == 2) {// 双击事件，播放幻灯片
                    List<File> imageFiles = getCurrentDirectoryImages(dir);
                    if (!imageFiles.isEmpty()) {
                        int index = imageFiles.indexOf(boxFileMap.get(box));
                        if (index >= 0) {
                            SlideShowWindow slideShow = new SlideShowWindow(imageFiles, index);
                            slideShow.show();
                        }
                    }
                }

                if (event.isControlDown()) {// 按下Ctrl键选中
                    toggleSelectBox(box);
                } else {
                    clearSelection();
                    selectBox(box);
                }
                statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize)+"——已选中 " + selectedBoxes.size() + " 张图片");
            } else if (event.getButton() == MouseButton.SECONDARY) {//右键菜单
                if (!selectedBoxes.contains(box)) {// 如果当前框未被选中
                    clearSelection();// 清除所有选中状态
                }
                selectBox(box);// 选中当前框
                statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize)+"——已选中 " + selectedBoxes.size() + " 张图片");
                showMenu(dir, box, event.getScreenX(), event.getScreenY(), statusLabel, fileOperator, imagePreviewPane);// 显示右键菜单
            }
        });
        //设置键盘事件，快捷键
        box.setOnKeyPressed(event -> {
            if (event.isControlDown()) {// 按下Ctrl键时
                switch (event.getCode()) {
                    case C:// 复制
                        fileOperator.copy(selectedBoxes, boxFileMap);
                        statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize)+"——已复制 " + selectedBoxes.size() + " 个文件");
                        event.consume();
                        break;
                    case V:// 粘贴
                        fileOperator.paste(dir);
                        generateThumbnails(dir, thisPane, statusLabel, fileOperator);
                        break;
                    case D:// 删除
                        try {
                            fileOperator.delete(selectedBoxes, boxFileMap);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        generateThumbnails(dir, thisPane, statusLabel, fileOperator);
                        break;
                    case X:// 剪切
                        fileOperator.cut(selectedBoxes, boxFileMap);
                        statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize)+"——已剪切 " + selectedBoxes.size() + " 个文件");
                }
            }
        });
        this.imagePane.getChildren().add(box);
        //imagePreviewPane.getChildren().add(box);
        count++;
        totalSize += file.length();
    }
    // 显示右键菜单
    private void showMenu(File currentDir, VBox box, double x, double y, Label statusLabel, FileOperator fileOperator, FlowPane imagePreviewPane) {
        if (contextMenu != null) {
            contextMenu.hide();
        }

        //File currentFile = boxFileMap.get(box);
        // 创建菜单项
        MenuItem copyItem = new MenuItem("复制");
        MenuItem cutItem = new MenuItem("剪切");
        MenuItem pasteItem = new MenuItem("粘贴");
        MenuItem deleteItem = new MenuItem("删除");
        MenuItem renameItem = new MenuItem("重命名");
        //复制
        copyItem.setOnAction(event -> {
            fileOperator.copy(getSelectedBoxes(), getBoxFileMap());
            statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize)+"——已复制 " + selectedBoxes.size() + " 个文件");
        });
        //剪切
        cutItem.setOnAction(event -> {
            fileOperator.cut(getSelectedBoxes(), getBoxFileMap());
            statusLabel.setText("共 " + count + " 张图片，总大小：" + formatSize(totalSize)+"——已剪切 " + selectedBoxes.size() + " 个文件");
        });
        //粘贴
        pasteItem.setOnAction(event -> {
            fileOperator.paste(cur);
            generateThumbnails(cur, imagePreviewPane, statusLabel, fileOperator);
        });
        //删除
        deleteItem.setOnAction(event -> {
            try {
                fileOperator.delete(getSelectedBoxes(), getBoxFileMap());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            generateThumbnails(cur, imagePreviewPane, statusLabel, fileOperator);
        });
        //重命名
        renameItem.setOnAction(event -> {
            fileOperator.rename(getSelectedBoxes(), getBoxFileMap());
            generateThumbnails(cur, imagePreviewPane, statusLabel, fileOperator);
        });
        // 初始化菜单
        if (contextMenu == null) {
            contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(copyItem, cutItem, pasteItem, deleteItem, renameItem);
        }
        // 显示菜单
        contextMenu.show(box, x, y);
        if (box == null) {
            contextMenu.show(imagePreviewPane, x, y);
        }

    }
    // 获取当前选中的文件列表
    private List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for (VBox box : selectedBoxes) {
            selectedFiles.add(boxFileMap.get(box));
        }
        return selectedFiles;
    }
    // 按下crtl选中框
    private void toggleSelectBox(VBox box) {
        if (selectedBoxes.contains(box)) {
            box.setStyle("-fx-border-color: transparent;");
            selectedBoxes.remove(box);
        } else {
            box.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: lightblue;");
            selectedBoxes.add(box);
        }
    }
    // 选中框
    private void selectBox(VBox box) {
        box.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: lightblue;");
        selectedBoxes.add(box);
    }
    // 取消选中框
    private void clearSelection() {
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear();
    }
    // 判断是否是图片文件
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
    // 获取当前目录下的所有图片文件
    public List<File> getCurrentDirectoryImages(File dir) {
        List<File> imageFiles = new ArrayList<>();
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isImageFile(file)) {
                        imageFiles.add(file);
                    }
                }
            }
        }
        return imageFiles;
    }
    // 获取当前选中的图片框集合
    public Set<VBox> getSelectedBoxes() {
        return selectedBoxes;
    }
    // 获取图片框到文件的映射
    public HashMap<VBox, File> getBoxFileMap() {
        return boxFileMap;
    }
}