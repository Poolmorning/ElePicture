package com.example.elepicture;

import com.example.elepicture.utils.ClipboardManager;
import com.example.elepicture.utils.FileOperator;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.geometry.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

public class ThumbnailManager {
    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};//可选的图片格式
    private final Set<VBox> selectedBoxes = new HashSet<>();//选中的图片框
    private final HashMap<VBox, File> boxFileMap = new HashMap<>();//图片框和文件的映射
    private int count = 0;//计数器
    private long totalSize = 0;//总大小

    public void generateThumbnails(File dir, FlowPane imagePreviewPane, Label statusLabel) {
        if (dir != null) {//如果所选不为空
            File[] files = dir.listFiles();//获取目录下的所有文件
            if (files != null) {//如果目录下有文件
                imagePreviewPane.getChildren().clear();//清空预览面板

                selectedBoxes.clear();//清空选中框
                boxFileMap.clear();//清空文件框映射
                //重置计数器和总大小
                count = 0;
                totalSize = 0;

                for (File file : files) {//遍历文件
                    if (isImageFile(file)) {//如果是图片文件
                        try {
                            //创建图片对象
                            Image img = new Image(new FileInputStream(file), 100, 100, true, true);
                            ImageView imageView = new ImageView(img);
                            imageView.setFitHeight(100);//设置高度
                            imageView.setFitWidth(100);//设置宽度
                            imageView.setPreserveRatio(true);//保持比例
                            //创建固定大小的图片框
                            StackPane container = new StackPane(imageView);
                            container.setMinSize(100, 100);//设置最小大小
                            container.setMaxSize(100, 100);//设置最大大小
                            //创建标签
                            Label nameLabel = new Label(file.getName());//获取文件名
                            //限制文件名长度
                            if (nameLabel.getText().length() > 10) {
                                nameLabel.setText(nameLabel.getText().substring(0, 10) + "...");
                            }
                            nameLabel.setAlignment(Pos.CENTER);
                            //创建图片框
                            VBox box = new VBox(container, nameLabel);
                            box.getStyleClass().add("thumbnail-box");  // 添加CSS类名
                            box.setSpacing(5);
                            box.setPadding(new Insets(5));
                            box.setStyle("-fx-border-color: transparent;");
                            boxFileMap.put(box, file);
                            //设置鼠标点击事件
                            box.setOnMouseClicked(event -> {
                                if (event.getButton() == MouseButton.PRIMARY) {//如果是左键点击
                                    if (event.isControlDown()) {//如果按下Ctrl键
                                        toggleSelectBox(box);//切换选中状态
                                    } else {
                                        clearSelection();//清空选中状态
                                        selectBox(box);//选中当前框
                                    }
                                    statusLabel.setText("已选中 " + selectedBoxes.size() + " 张图片");
                                } else if (event.getButton() == MouseButton.SECONDARY) {//如果是右键点击
                                    if (!selectedBoxes.contains(box)) {
                                        clearSelection();//清空选中状态
                                        selectBox(box);//选中当前框
                                        //显示右键菜单---待完成
                                        showMenu(box, event.getScreenX(), event.getScreenY(),statusLabel);//显示右键菜单
                                    }
                                }
                            });
                            //添加图片框到预览面板
                            imagePreviewPane.getChildren().add(box);
                            count++;//增加计数器
                            totalSize += file.length();//增加总大小
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                //标签显示信息
                String readableSize = formatSize(totalSize);//把总大小格式化为可读的字符串
                statusLabel.setText(String.format("共 %d 张图片，总大小：%s",
                        count, readableSize));//显示选中目录的图片数量和总大小
            }
        }
    }
    //显示右键菜单
    private void showMenu(VBox box, double x, double y,Label statusLabel) {
        ContextMenu contextMenu = new ContextMenu();

        // 初始化ClipboardManager和FileOperator
        ClipboardManager clipboardManager = new ClipboardManager();
        FileOperator fileOperator = new FileOperator(clipboardManager);

        // 获取当前选中的文件
        File currentFile = boxFileMap.get(box);
        File currentDir = currentFile.getParentFile();

        // 创建菜单项
        MenuItem copyItem = new MenuItem("复制");
        MenuItem cutItem = new MenuItem("剪切");
        MenuItem pasteItem = new MenuItem("粘贴");
        MenuItem deleteItem = new MenuItem("删除");
        MenuItem renameItem = new MenuItem("重命名");

        // 根据剪贴板状态设置粘贴项是否可用
        pasteItem.setDisable(!clipboardManager.hasData());

        // 复制文件
        copyItem.setOnAction(event -> {
            List<File> selectedFiles = getSelectedFiles();
            if (!selectedFiles.isEmpty()) {
                fileOperator.copy(selectedFiles);
                statusLabel.setText("已复制 " + selectedFiles.size() + " 个文件");
            }
        });

        // 剪切文件
        cutItem.setOnAction(event -> {
            List<File> selectedFiles = getSelectedFiles();
            if (!selectedFiles.isEmpty()) {
                fileOperator.cut(selectedFiles);
                statusLabel.setText("已剪切 " + selectedFiles.size() + " 个文件");
            }
        });

        // 粘贴文件
        pasteItem.setOnAction(event -> {
            if (fileOperator.paste(currentDir)) {
                // 刷新显示
                generateThumbnails(currentDir, (FlowPane) box.getParent(), statusLabel);
                statusLabel.setText("粘贴成功");
            }
        });

        // 删除文件
        deleteItem.setOnAction(event -> {
            List<File> selectedFiles = getSelectedFiles();
            if (!selectedFiles.isEmpty() && fileOperator.delete(selectedFiles)) {
                // 刷新显示
                generateThumbnails(currentDir, (FlowPane) box.getParent(), statusLabel);
                statusLabel.setText("删除成功");
            }
        });

        // 重命名文件
        renameItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog(currentFile.getName());
            dialog.setTitle("重命名");
            dialog.setHeaderText(null);
            dialog.setContentText("请输入新文件名:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                if (fileOperator.rename(currentFile, newName)) {
                    // 刷新显示
                    generateThumbnails(currentDir, (FlowPane) box.getParent(), statusLabel);
                    statusLabel.setText("重命名成功");
                }
            });
        });

        // 添加菜单项到上下文菜单
        contextMenu.getItems().addAll(copyItem, cutItem, pasteItem, deleteItem, renameItem);

        // 显示菜单
        contextMenu.show(box, x, y);

    }
    //获取当前选中的所有文件
    private List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for (VBox box : selectedBoxes) {
            selectedFiles.add(boxFileMap.get(box));
        }
        return selectedFiles;
    }
    //按下Ctrl键切换选中状态
    private void toggleSelectBox(VBox box) {
        if (selectedBoxes.contains(box)) {
            box.setStyle("-fx-border-color: transparent;");
            selectedBoxes.remove(box);
        } else {
            box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            selectedBoxes.add(box);
        }
    }
    //选中状态
    private void selectBox(VBox box) {
        box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
        selectedBoxes.add(box);
    }
    //清空选中状态
    private void clearSelection() {
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear();
    }
    //判断是否是图片文件
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return Arrays.stream(imageExtensions).anyMatch(name::endsWith);
    }
    //格式化大小
    private String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (size >= 1024 * 1024) {//如果大于1MB
            return df.format(size / 1024.0 / 1024.0) + " MB";
        } else if (size >= 1024) {//如果大于1KB
            return df.format(size / 1024.0) + " KB";
        } else {//如果小于1KB
            return size + " B";
        }
    }
}