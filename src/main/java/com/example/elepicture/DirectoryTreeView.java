package com.example.elepicture;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectoryTreeView extends TreeView<File> {

    public DirectoryTreeView() {
        super();
        setPrefWidth(200);
        initTree();
    }

    private void initTree() {
        // 创建根节点"计算机"
        TreeItem<File> rootItem = new TreeItem<>(new File("此电脑"));
        rootItem.setExpanded(true);

        // 获取所有磁盘根目录
        File[] roots = File.listRoots();
        for (File root : roots) {
            TreeItem<File> diskItem = createDirectoryItem(root);
            rootItem.getChildren().add(diskItem);
        }

        setRoot(rootItem);
        setShowRoot(true);
    }

    private TreeItem<File> createDirectoryItem(File directory) {
        TreeItem<File> item = new TreeItem<>(directory);

        // 如果目录有子目录，添加占位符
        if (hasSubDirectories(directory)) {
            item.getChildren().add(new TreeItem<>());

            // 设置展开监听器实现懒加载
            item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && item.getChildren().size() == 1 &&
                        item.getChildren().get(0).getValue() == null) {
                    loadSubDirectories(item);
                }
            });
        }
        return item;
    }

    private void loadSubDirectories(TreeItem<File> parentItem) {
        File parentDir = parentItem.getValue();
        parentItem.getChildren().clear(); // 清除占位符

        // 获取并排序子目录
        File[] subDirs = parentDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            List<File> sortedDirs = Arrays.stream(subDirs)
                    .sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
                    .collect(Collectors.toList());

            for (File dir : sortedDirs) {
                parentItem.getChildren().add(createDirectoryItem(dir));
            }
        }
    }

    private boolean hasSubDirectories(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return false;
        }
        File[] subDirs = dir.listFiles(File::isDirectory);
        return subDirs != null && subDirs.length > 0;
    }
}
