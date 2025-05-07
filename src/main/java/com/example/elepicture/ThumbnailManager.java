package com.example.elepicture;

import com.example.elepicture.utils.FileOperator;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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

    public Set<VBox> getAllThumbnails() {
        return allThumbnails;
    }

    public void generateThumbnails(File dir, FlowPane imagePreviewPane, Label statusLabel, FileOperator fileOperator) {
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

    private class ThumbnailLoaderService extends Service<Void> {
        private File dir;
        private FlowPane imagePreviewPane;
        private Label statusLabel;
        private FileOperator fileOperator;

        public void setParameters(File dir, FlowPane imagePreviewPane, Label statusLabel, FileOperator fileOperator) {
            this.dir = dir;
            this.imagePreviewPane = imagePreviewPane;
            this.statusLabel = statusLabel;
            this.fileOperator = fileOperator;
        }

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

    private void createThumbnail(File file, FlowPane imagePreviewPane, File dir, Label statusLabel, FileOperator fileOperator) {
        Image img = new Image(file.toURI().toString(), 100, 100, true, true, true);
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        Pane container = new Pane(imageView);
        container.setMinSize(100, 100);
        container.setMaxSize(100, 100);
        container.setPickOnBounds(false);

        Label nameLabel = new Label(file.getName());
        if (nameLabel.getText().length() > 10) {
            nameLabel.setText(nameLabel.getText().substring(0, 10) + "...");
        }
        nameLabel.setAlignment(Pos.CENTER);

        VBox box = new VBox(container, nameLabel);
        box.setSpacing(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-border-color: transparent;");

        boxFileMap.put(box, file);
        allThumbnails.add(box);
        box.setFocusTraversable(true);

        box.setOnMouseClicked(event -> {
            box.requestFocus();
            if (event.getButton() == MouseButton.PRIMARY) {
                if (contextMenu != null) {
                    contextMenu.hide();
                }
                if (event.getClickCount() == 2) {
                    List<File> imageFiles = getCurrentDirectoryImages(dir);
                    if (!imageFiles.isEmpty()) {
                        int index = imageFiles.indexOf(boxFileMap.get(box));
                        if (index >= 0) {
                            SlideShowWindow slideShow = new SlideShowWindow(imageFiles, index);
                            slideShow.show();
                        }
                    }
                }

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
                }
                selectBox(box);
                showMenu(dir, box, event.getScreenX(), event.getScreenY(), statusLabel, fileOperator, imagePreviewPane);
            }
        });

        box.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case C:
                        fileOperator.copy(selectedBoxes, boxFileMap);
                        statusLabel.setText("已复制 " + selectedBoxes.size() + " 个文件");
                        event.consume();
                        break;
                    case V:
                        fileOperator.paste(dir);
                        statusLabel.setText("已粘贴 " + selectedBoxes.size() + " 个文件");
                        generateThumbnails(dir, thisPane, statusLabel, fileOperator);
                        break;
                    case D:
                        try {
                            fileOperator.delete(selectedBoxes, boxFileMap);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        generateThumbnails(dir, thisPane, statusLabel, fileOperator);
                        break;
                    case X:
                        fileOperator.cut(selectedBoxes, boxFileMap);
                        statusLabel.setText("已剪切 " + selectedBoxes.size() + " 个文件");
                        generateThumbnails(dir, thisPane, statusLabel, fileOperator);
                }
            }
        });

        imagePreviewPane.getChildren().add(box);
        count++;
        totalSize += file.length();
    }

    private void showMenu(File currentDir, VBox box, double x, double y, Label statusLabel, FileOperator fileOperator, FlowPane imagePreviewPane) {
        if (contextMenu != null) {
            contextMenu.hide();
        }

        File currentFile = boxFileMap.get(box);

        MenuItem copyItem = new MenuItem("复制");
        MenuItem cutItem = new MenuItem("剪切");
        MenuItem pasteItem = new MenuItem("粘贴");
        MenuItem deleteItem = new MenuItem("删除");
        MenuItem renameItem = new MenuItem("重命名");

        copyItem.setOnAction(event -> {
            fileOperator.copy(selectedBoxes, boxFileMap);
            statusLabel.setText("已复制 " + selectedBoxes.size() + " 个文件");
        });

        cutItem.setOnAction(event -> {
            fileOperator.cut(selectedBoxes, boxFileMap);
            statusLabel.setText("已剪切 " + selectedBoxes.size() + " 个文件");
            generateThumbnails(currentDir, thisPane, statusLabel, fileOperator);
        });

        pasteItem.setOnAction(event -> {
            fileOperator.paste(currentDir);
            statusLabel.setText("已粘贴 " + selectedBoxes.size() + " 个文件");
            generateThumbnails(currentDir, thisPane, statusLabel, fileOperator);
        });

        deleteItem.setOnAction(event -> {
            try {
                fileOperator.delete(selectedBoxes, boxFileMap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            generateThumbnails(currentDir, thisPane, statusLabel, fileOperator);
        });

        renameItem.setOnAction(event -> {
            fileOperator.rename(selectedBoxes, boxFileMap);
            generateThumbnails(currentDir, thisPane, statusLabel, fileOperator);
        });

        if (contextMenu == null) {
            contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(copyItem, cutItem, pasteItem, deleteItem, renameItem);
        }

        contextMenu.show(box, x, y);
        if (box == null) {
            contextMenu.show(imagePreviewPane, x, y);
        }
    }

    private List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for (VBox box : selectedBoxes) {
            selectedFiles.add(boxFileMap.get(box));
        }
        return selectedFiles;
    }

    private void toggleSelectBox(VBox box) {
        if (selectedBoxes.contains(box)) {
            box.setStyle("-fx-border-color: transparent;");
            selectedBoxes.remove(box);
        } else {
            box.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: lightblue;");
            selectedBoxes.add(box);
        }
    }

    private void selectBox(VBox box) {
        box.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: lightblue;");
        selectedBoxes.add(box);
    }

    private void clearSelection() {
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return Arrays.stream(imageExtensions).anyMatch(name::endsWith);
    }

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
}