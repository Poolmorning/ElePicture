package com.example.elepicture;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.geometry.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ThumbnailManager {
    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
    private final Set<VBox> selectedBoxes = new HashSet<>();
    private final HashMap<VBox, File> boxFileMap = new HashMap<>();

    public void generateThumbnails(File dir, javafx.scene.layout.FlowPane imagePreviewPane, Label statusLabel) {
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                imagePreviewPane.getChildren().clear();
                // 设置 FlowPane 属性
                imagePreviewPane.setHgap(10);
                imagePreviewPane.setVgap(10);
                imagePreviewPane.setPadding(new Insets(10));
                imagePreviewPane.setPrefWrapLength(750);//固定一行的宽度，暂时要改的


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
                statusLabel.setText(String.format("目录：%s，共 %d 张图片，总大小：%s",
                        dir.getAbsolutePath(), count, readableSize));
            }
        }
    }

    private void toggleSelectBox(VBox box) {
        if (selectedBoxes.contains(box)) {
            box.setStyle("-fx-border-color: transparent;");
            selectedBoxes.remove(box);
        } else {
            box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            selectedBoxes.add(box);
        }
    }

    private void selectBox(VBox box) {
        box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
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
}