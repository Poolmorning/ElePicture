package com.example.elepicture.utils;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

//文件操作工具类，提供文件的增删改查功能
public class FileOperator {

    private ClipboardManager clipboardManager;

    public FileOperator(ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    //删除文件
    public boolean delete(Set<VBox> selectedBoxes, HashMap<VBox, File> boxFileMap) throws IOException {
        //获取其中的文件
        List<File> targets = selectedBoxes.stream()
                .map(boxFileMap::get)
                .filter(Objects::nonNull)
                .toList();

        // 确认对话框
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION,
                "确定要删除选中的 " + targets.size() + " 个文件吗？");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return false; // 用户取消删除
        }

        for (File file : targets) {
            Files.delete(file.toPath());
        }
        return true;

    }

    //复制文件到剪贴板
    public boolean copy(Set<VBox> selectedBoxes, HashMap<VBox, File> boxFileMap) {
        //获取其中的文件
        List<File> sources = selectedBoxes.stream()
                .map(boxFileMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (sources.isEmpty()) {
            return false;
        }

        clipboardManager.setClipboardFiles(sources);
        clipboardManager.setOperationType(ClipboardManager.OperationType.COPY);
        return true;
    }

    //剪切文
    public boolean cut(Set<VBox> selectedBoxes, HashMap<VBox, File> boxFileMap) {
        //获取其中的文件
        List<File> sources = selectedBoxes.stream()
                .map(boxFileMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (sources.isEmpty()) {
            return false;
        }

        clipboardManager.setClipboardFiles(sources);
        clipboardManager.setOperationType(ClipboardManager.OperationType.CUT);
        return true;
    }

    //粘贴
    public boolean paste(File destination) {
        //从剪贴板管理器获取待粘贴的文件列表
        List<File> filesToPaste = clipboardManager.getClipboardFiles();
        // 检查剪贴板是否有文件
        if (filesToPaste == null || filesToPaste.isEmpty()) {
            return false;
        }
        boolean allSuccess = true; // 标记是否所有文件都处理成功
        //遍历所有待粘贴文件
        for (File source : filesToPaste) {
            try {
                Path destPath = destination.toPath().resolve(source.getName());//目标路径
                System.out.println(destPath);
                File destFile = handleNameConflict(destPath.toFile());// 处理文件名冲突
                // 根据操作类型执行复制或剪切
                if (clipboardManager.getOperationType() == ClipboardManager.OperationType.COPY) {
                    // 复制操作
                    Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);//直接复制并覆盖已存在的文件
                }else {
                    // 剪切操作
                    Files.move(source.toPath(), destFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
                allSuccess = false;
            }
        }



        return allSuccess;
    }

    //重命名
    public boolean rename(Set<VBox> selectedBoxes, HashMap<VBox, File> boxFileMap) {

        //如果只选中了一个文件
        if (selectedBoxes.size() == 1){
            File oldFile = boxFileMap.get(selectedBoxes.iterator().next());//获取第一个图片
            boolean success = false;
            if (oldFile == null) return success;
            // 获取不带后缀的文件名
            String oldName = oldFile.getName();
            int dotIndex = oldName.lastIndexOf('.');
            String baseName = (dotIndex > 0) ? oldName.substring(0, dotIndex) : oldName;
            String extension = (dotIndex > 0) ? oldName.substring(dotIndex) : "";

            //弹出⼀个对话框，让⽤户输⼊新的⽂件名，不要改变⽂件的后缀名
            TextInputDialog dialog = new TextInputDialog(baseName);
            dialog.setTitle("重命名");
            dialog.setHeaderText("请输入新的文件名");
            dialog.setContentText("新的文件名：");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return success; // 用户取消

            String newName = result.get().trim();
            if (newName.isEmpty()) return success; // 空名称

            // 如果用户输入了后缀，使用用户输入的后缀
            if (newName.contains(".")) {
                extension = "";
            }
            File newFile = new File(oldFile.getParentFile(), newName + extension);
            // 检查是否重命名到自己
            if (oldFile.equals(newFile)) return true;
            // 执行重命名
            success = oldFile.renameTo(newFile);
            return success;

        } else{//如果选中了多个文件
            List<File> selectedFiles = selectedBoxes.stream()
                    .map(boxFileMap::get)
                    .filter(Objects::nonNull)
                    .toList();//获取所有图片
            // 创建批量重命名对话框
            VBox dialogContent = new VBox(10);
            TextField prefixField = new TextField();
            prefixField.setPromptText("名称前缀");
            TextField startNumberField = new TextField("1");
            startNumberField.setPromptText("起始编号");
            TextField digitsField = new TextField("4");
            digitsField.setPromptText("编号位数");
            dialogContent.getChildren().addAll(
                new Label("名称前缀:"), prefixField,
                new Label("起始编号:"), startNumberField,
                new Label("编号位数:"), digitsField
            );

            Alert dialog = new Alert(AlertType.CONFIRMATION);
            dialog.setTitle("批量重命名");
            dialog.setHeaderText("请输入批量重命名参数");
            dialog.getDialogPane().setContent(dialogContent);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return false; // 用户取消
            }

            // 获取用户输入
            String prefix = prefixField.getText().trim();
            if (prefix.isEmpty()) {
                showErrorAlert("名称前缀不能为空");
                return false;
            }
            int startNumber;
            try {
                startNumber = Integer.parseInt(startNumberField.getText());
                if (startNumber < 0) {
                    showErrorAlert("起始编号必须大于等于0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showErrorAlert("起始编号必须是整数");
                return false;
            }
            int digits;
            try {
                digits = Integer.parseInt(digitsField.getText());
                if (digits <= 0) {
                    showErrorAlert("编号位数必须大于0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showErrorAlert("编号位数必须是整数");
                return false;
            }
            // 执行批量重命名
            boolean allSuccess = true;
            int currentNumber = startNumber;
            for (File file : selectedFiles) {
                // 获取文件扩展名
                String oldName = file.getName();
                int dotIndex = oldName.lastIndexOf('.');
                String extension = (dotIndex > 0) ? oldName.substring(dotIndex) : "";

                // 生成序号部分
                String numberStr = String.format("%0" + digits + "d", currentNumber);

                // 构建新文件名
                String newName = prefix + numberStr + extension;
                File newFile = new File(file.getParentFile(), newName);

                // 检查是否重命名到自己
                if (!file.equals(newFile)) {
                    // 执行重命名
                    if (!file.renameTo(newFile)) {
                        allSuccess = false;
                    }
                }

                currentNumber++;
            }
            return allSuccess;
        }


    }
    //错误提示
    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //处理文件名冲突
    private File handleNameConflict(File file) {
        if (!file.exists()) {//首先检查文件是否已存在
            return file;
        }
        //分离文件名和扩展名
        String name = file.getName();//获取完整文件名
        String baseName;//主文件名
        String extension = "";//后缀
        int dotIndex = name.lastIndexOf('.');//最后一个点号位置

        if (dotIndex > 0) {//确认点号不在开头位置
            baseName = name.substring(0, dotIndex);//获取点号前的部分
            extension = name.substring(dotIndex);//获取点号及之后的部分
        } else {
            baseName = name;//无扩展名时整个作为主文件名
        }
        //副本文件名
        int counter = 1;//起始序号
        File newFile;
        do {
            newFile = new File(file.getParent(), baseName + " (" + counter + ")" + extension);
            counter++;
        } while (newFile.exists());//循环直到生成不相同的文件名

        return newFile;
    }
}
