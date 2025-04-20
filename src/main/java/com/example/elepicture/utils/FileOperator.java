package com.example.elepicture.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
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

    /**
     * 剪切文件到剪贴板
     * @param sources 要剪切的文件列表
     * @return 是否成功剪切
     */
    public boolean cut(List<File> sources) {
        if (sources == null || sources.isEmpty()) {
            return false;
        }

        clipboardManager.setClipboardFiles(sources);
        clipboardManager.setOperationType(ClipboardManager.OperationType.CUT);
        return true;
    }

    //粘贴
    public boolean paste(File destination) {
        if (!destination.isDirectory()) {
            showErrorDialog("粘贴失败", "目标位置不是一个有效的目录");
            return false;
        }
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
                File destFile = handleNameConflict(destPath.toFile());// 处理文件名冲突
                // 根据操作类型执行复制或剪切
                if (clipboardManager.getOperationType() == ClipboardManager.OperationType.COPY) {
                    // 复制操作
                    Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);//直接复制并覆盖已存在的文件
                }/* else {
                    // 剪切操作
                    if (source.isDirectory()) {
                        copyDirectory(source, destFile);
                        //deleteDirectory(source); // 剪切=复制+删除
                    } else {
                        Files.move(source.toPath(), destFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }*/
            } catch (IOException e) {
                e.printStackTrace();
                allSuccess = false;
            }
        }



        return allSuccess;
    }

    //重命名
    public boolean rename(Set<VBox> selectedBoxes, HashMap<VBox, File> boxFileMap) {
        File oldFile = boxFileMap.get(selectedBoxes.iterator().next());
        boolean success = false;
        if (oldFile == null) return success;
        //如果只选中了一个文件
        if (selectedBoxes.size() == 1){
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

        }
        //如果选中了多个文件
        return true;

    }

    /**
     * 处理文件名冲突
     * @param file 目标文件
     * @return 处理后的文件对象
     */
    private File handleNameConflict(File file) {
        if (!file.exists()) {
            return file;
        }

        String name = file.getName();
        String baseName;
        String extension = "";
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex > 0) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        } else {
            baseName = name;
        }

        int counter = 1;
        File newFile;
        do {
            newFile = new File(file.getParent(),
                    baseName + " (" + counter + ")" + extension);
            counter++;
        } while (newFile.exists());

        return newFile;
    }

    /**
     * 递归复制目录
     * @param source 源目录
     * @param destination 目标目录
     * @throws IOException
     */
    private void copyDirectory(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdir();
        }

        for (File file : source.listFiles()) {
            if (file.isDirectory()) {
                copyDirectory(file, new File(destination, file.getName()));
            } else {
                Files.copy(file.toPath(),
                        new File(destination, file.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }


    /**
     * 显示错误对话框
     * @param title 标题
     * @param message 消息内容
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
