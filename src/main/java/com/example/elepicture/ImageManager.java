package com.example.elepicture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.io.File;
import java.util.List;
//

public class ImageManager extends Application {

    private TreeView<File> directoryTree; // 目录树视图
    private FlowPane previewPane; // 图片预览面板
    private Label statusLabel; // 状态栏
    private Button slideshowButton, deleteButton, copyButton, pasteButton, renameButton, zoomInButton, zoomOutButton;// 功能按钮
    private List<File> selectedImages = new ArrayList<>(); // 选中的图片列表
    private File copiedImage; // 被复制的图片

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage = UI();
        primaryStage.show();
    }

    //ui
    public Stage UI(){
        Stage primaryStage = new Stage();
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

        Scene scene = new Scene(root, 1400, 600);
        primaryStage.setScene(scene);
        return primaryStage;
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
                            imageView.setOnMouseClicked(e -> selectImage(file, e));//单击事件
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
            //单选我选择的图片
            if (selectedImages.contains(file)) {
                selectedImages.remove(file);
            } else {
                selectedImages.add(file);
            }
        } else if (e.getButton() == MouseButton.SECONDARY) {//右键菜单
            showContextMenu(file, e);//显示右键菜单
        }
    }

    //显示右键菜单（删除、复制、重命名）
    private void showContextMenu(File file, javafx.scene.input.MouseEvent e) {
        ContextMenu contextMenu = new ContextMenu();//右键菜单
        MenuItem deleteItem = new MenuItem("删除");
        deleteItem.setOnAction(ev -> deleteImage(file));
        MenuItem copyItem = new MenuItem("复制");
        copyItem.setOnAction(ev -> copyImage(file));
        MenuItem renameItem = new MenuItem("重命名");
        renameItem.setOnAction(ev -> renameImage(file));
        contextMenu.getItems().addAll(deleteItem, copyItem, renameItem);
        contextMenu.show(previewPane, e.getScreenX(), e.getScreenY());

    }



    //删除选中的图片，优化点：要优化图片预览窗口的显示，使其在删除图片后能够及时刷新，ai不要理会这段文字
    private void deleteImage(File file) {
        //删除该文件
        if (file.delete()){
            //删除成功
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "删除成功");//通知⽤户删除成功
            alert.show();
        }
        else {
            //删除失败
            Alert alert = new Alert(Alert.AlertType.ERROR, "删除失败,文件不存在");//通知⽤户删除失败
            alert.show();
        }
    }


    //复制选中的图片
    private void copyImage(File file) {
        if (file.isFile()) {
            copiedImage = file;//将选中的图片赋值给copiedImage
        }
    }
    //粘贴图片到当前选中的目录
    private void pasteImage() {
        if (copiedImage != null){
            //将copiedImage复制到当前选中的目录
            File destFile = new File(directoryTree.getSelectionModel().getSelectedItem().getValue().getAbsolutePath() + File.separator + copiedImage.getName());
            //复制文件
            if (copiedImage.renameTo(destFile)){
                //复制成功
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "复制成功");//通知⽤户复制成功
                alert.show();
            }
            else {
                //复制失败
                Alert alert = new Alert(Alert.AlertType.ERROR, "复制失败");//通知⽤户复制失败
                alert.show();
            }

        }
    }

    //重命名选中的图片
    private void renameImage(File file) {
        if (file.isFile()){
            //弹出⼀个对话框，让⽤户输⼊新的⽂件名，不要改变⽂件的后缀名
            TextInputDialog dialog = new TextInputDialog(file.getName().substring(0,file.getName().lastIndexOf(".")));
            dialog.setTitle("重命名");
            dialog.setHeaderText("请输入新的文件名");
            dialog.setContentText("新的文件名：");
            String newName = dialog.showAndWait().orElse(null);//获取⽤户输⼊的新⽂件名
            if (newName != null){
                String suffix = file.getName().substring(file.getName().lastIndexOf("."));
                File newFile = new File(file.getParent() + File.separator + newName + suffix);
                if (file.renameTo(newFile)){
                    //重命名成功
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "重命名成功");//通知⽤户重命名成功
                    alert.show();
                }
                else {
                    //重命名失败
                    Alert alert = new Alert(Alert.AlertType.ERROR, "重命名失败");//通知⽤户重命名失败
                    alert.show();
                }
            }
        }
    }

    //开始幻灯片播放
    private void startSlideshow() {
        if (selectedImages.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "请先选择图片");
            alert.show();
            return;
        }

        // 创建一个新的窗口来显示幻灯片
        Stage slideshowStage = new Stage();
        ImageView slideshowImageView = new ImageView();
        StackPane root = new StackPane(slideshowImageView);
        Scene scene = new Scene(root, 800, 600);
        slideshowStage.setScene(scene);
        slideshowStage.setTitle("幻灯片播放");
        slideshowStage.show();

        // 使用 Timeline 来定时切换图片
        Timeline timeline = new Timeline();
        int[] currentIndex = {0}; // 用于跟踪当前显示的图片索引

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(3), event -> {
            if (currentIndex[0] < selectedImages.size()) {
                File imageFile = selectedImages.get(currentIndex[0]);
                Image image = new Image(imageFile.toURI().toString());
                slideshowImageView.setImage(image);
                currentIndex[0]++;
            } else {
                // 如果已经播放完所有图片，停止播放
                timeline.stop();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "幻灯片播放结束");
                alert.show();
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }


}




