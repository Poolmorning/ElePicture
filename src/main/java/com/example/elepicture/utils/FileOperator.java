package com.example.elepicture.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;


//文件操作工具类，提供文件的增删改查功能
public class FileOperator {

    private ClipboardManager clipboardManager;

    public FileOperator(ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    /**
     * 删除文件或目录（支持批量删除）
     * @param targets 要删除的文件列表
     * @return 是否成功删除
     */
    public boolean delete(List<File> targets) {
        if (targets == null || targets.isEmpty()) {
            return false;
        }

        // 确认对话框
        if (!showConfirmationDialog("确认删除",
                "确定要删除选中的 " + targets.size() + " 个文件/目录吗？")) {
            return false;
        }

        boolean allSuccess = true;
        for (File target : targets) {
            if (!deleteSingleFile(target)) {
                allSuccess = false;
            }
        }

        if (!allSuccess) {
            showErrorDialog("部分文件删除失败", "某些文件可能被占用或没有权限删除");
        }

        return allSuccess;
    }

    private boolean deleteSingleFile(File file) {
        try {
            if (file.isDirectory()) {
                // 递归删除目录
                deleteDirectory(file);
            } else {
                Files.delete(file.toPath());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteSingleFile(file);
            }
        }
        Files.delete(directory.toPath());
    }

    /**
     * 复制文件到剪贴板
     * @param sources 要复制的文件列表
     * @return 是否成功复制
     */
    public boolean copy(List<File> sources) {
        if (sources == null || sources.isEmpty()) {
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

    /**
     * 粘贴文件到目标目录
     * @param destination 目标目录
     * @return 是否成功粘贴
     */
    public boolean paste(File destination) {
        if (!destination.isDirectory()) {
            showErrorDialog("粘贴失败", "目标位置不是一个有效的目录");
            return false;
        }

        List<File> filesToPaste = clipboardManager.getClipboardFiles();
        if (filesToPaste == null || filesToPaste.isEmpty()) {
            return false;
        }

        boolean allSuccess = true;

        for (File source : filesToPaste) {
            try {
                Path destPath = destination.toPath().resolve(source.getName());

                // 处理文件名冲突
                File destFile = handleNameConflict(destPath.toFile());

                if (clipboardManager.getOperationType() == ClipboardManager.OperationType.COPY) {
                    // 复制操作
                    if (source.isDirectory()) {
                        copyDirectory(source, destFile);
                    } else {
                        Files.copy(source.toPath(), destFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    // 剪切操作
                    if (source.isDirectory()) {
                        copyDirectory(source, destFile);
                        deleteDirectory(source); // 剪切=复制+删除
                    } else {
                        Files.move(source.toPath(), destFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                allSuccess = false;
            }
        }

        if (!allSuccess) {
            showErrorDialog("粘贴失败", "某些文件可能没有权限或已存在");
        }

        return allSuccess;
    }

    /**
     * 重命名文件
     * @param oldFile 原文件
     * @param newName 新名称
     * @return 是否成功重命名
     */
    public boolean rename(File oldFile, String newName) {
        if (oldFile == null || newName == null || newName.trim().isEmpty()) {
            return false;
        }

        if (oldFile.getName().equals(newName)) {
            return true; // 名称相同，无需重命名
        }

        File newFile = new File(oldFile.getParent(), newName);

        // 检查新文件名是否合法
        if (!isValidFilename(newName)) {
            showErrorDialog("重命名失败", "文件名包含非法字符");
            return false;
        }

        // 检查是否已存在同名文件
        if (newFile.exists()) {
            showErrorDialog("重命名失败", "已存在同名文件");
            return false;
        }

        try {
            Files.move(oldFile.toPath(), newFile.toPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("重命名失败", "文件可能被占用或没有权限");
            return false;
        }
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
     * 检查文件名是否合法
     * @param name 文件名
     * @return 是否合法
     */
    private boolean isValidFilename(String name) {
        return !name.contains("\\") && !name.contains("/")
                && !name.contains(":") && !name.contains("*")
                && !name.contains("?") && !name.contains("\"")
                && !name.contains("<") && !name.contains(">")
                && !name.contains("|");
    }

    /**
     * 显示确认对话框
     * @param title 标题
     * @param message 消息内容
     * @return 用户是否确认
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
