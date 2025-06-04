package com.example.elepicture;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectoryTreeView extends TreeView<File> {

    // 构造方法
    public DirectoryTreeView() {
        super();
        setPrefWidth(250);//设置宽度
        initTree();//初始化树
    }

    //获取电脑所有磁盘根目录
    private void initTree() {
        //创建根节点"此电脑"
        TreeItem<File> rootItem = new TreeItem<>(new File("此电脑"));
        rootItem.setExpanded(true);//强制展开“此电脑”根节点----显示所有磁盘

        //获取所有磁盘根目录
        File[] roots = File.listRoots();
        for (File root : roots) {
            TreeItem<File> diskItem = createDirectoryItem(root);//创建磁盘节点
            rootItem.getChildren().add(diskItem);//将磁盘节点添加为"此电脑"的子节点
        }

        setRoot(rootItem);//设置根节点为"此电脑"
        setShowRoot(true);//显示根节点
    }

    //创建磁盘节点
    private TreeItem<File> createDirectoryItem(File directory) {
        TreeItem<File> item = new TreeItem<>(directory);

        //如果目录有子目录，添加占位符
        if (hasSubDirectories(directory)) {//检查当前目录是否包含子目录
            item.getChildren().add(new TreeItem<>());// 添加空占位节点（用于显示展开箭头图标），这个占位符会在节点展开时被实际内容替换
            //设置节点展开状态变化的监听器（实现懒加载）
            item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                // 当节点被展开时，且当前只有一个占位符子节点时
                if (isNowExpanded && item.getChildren().size() == 1 && item.getChildren().get(0).getValue() == null) {
                    loadSubDirectories(item);//加载实际的子目录内容
                }
            });
        }
        return item;
    }

    //加载并填充指定父节点的子目录结构
    private void loadSubDirectories(TreeItem<File> parentItem) {
        File parentDir = parentItem.getValue();//获取父节点对应的实际目录文件对象
        parentItem.getChildren().clear(); //清除占位符
        // 获取并排序子目录
        File[] subDirs = parentDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            // 对子目录进行排序处理（按名称不区分大小写排序）
            List<File> sortedDirs = Arrays.stream(subDirs)
                    .sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
                    .collect(Collectors.toList());
            //为每个子目录创建对应的树节点
            for (File dir : sortedDirs) {
                parentItem.getChildren().add(createDirectoryItem(dir)); //新节点会自动获得懒加载能力（包含占位符和展开监听器）
            }
        }
    }
    // 检查目录是否有子目录
    private boolean hasSubDirectories(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return false;
        }
        File[] subDirs = dir.listFiles(File::isDirectory);//获取子目录
        return subDirs != null && subDirs.length > 0;
    }
}
