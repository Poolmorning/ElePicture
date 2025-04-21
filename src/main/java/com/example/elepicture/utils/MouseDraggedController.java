package com.example.elepicture.utils;

import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;
import java.util.Set;

public class MouseDraggedController {
    private final Pane parentPane; // 父容器（FlowPane）
    private double startX, startY;
    private Rectangle selectionRect;
    private final Set<VBox> allThumbnails;

    public MouseDraggedController(Pane parentPane, Set<VBox> allThumbnails,Set<VBox> selectedBoxes) {
        this.parentPane = parentPane;
        this.allThumbnails = allThumbnails;
        setupMouseHandlers(selectedBoxes);
    }

    private void setupMouseHandlers(Set<VBox> selectedBoxes) {
        // 创建选择矩形（初始不可见）
        selectionRect = new Rectangle(0, 0, 0, 0);
        selectionRect.setFill(Color.rgb(0, 0, 255, 0.2));
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setVisible(false);
        parentPane.getChildren().add(selectionRect);

        // 鼠标按下事件
        parentPane.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                startX = event.getX();
                startY = event.getY();
                selectionRect.setX(startX);
                selectionRect.setY(startY);
                selectionRect.setWidth(0);
                selectionRect.setHeight(0);
                selectionRect.setVisible(true);
            }
        });

        // 鼠标拖动事件
        parentPane.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double currentX = event.getX();
                double currentY = event.getY();

                // 更新选择矩形的位置和大小
                selectionRect.setX(Math.min(startX, currentX));
                selectionRect.setY(Math.min(startY, currentY));
                selectionRect.setWidth(Math.abs(currentX - startX));
                selectionRect.setHeight(Math.abs(currentY - startY));

                // 检查哪些缩略图在选择区域内
                updateSelectedBoxes(selectedBoxes);
            }
        });

        // 鼠标释放事件
        parentPane.setOnMouseReleased(event -> {
            selectionRect.setVisible(false);
        });
    }

    private void updateSelectedBoxes(Set<VBox> selectedBoxes) {
        // 清除之前的选择样式
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear();

        // 获取选择矩形的边界
        double rectMinX = selectionRect.getX();
        double rectMaxX = rectMinX + selectionRect.getWidth();
        double rectMinY = selectionRect.getY();
        double rectMaxY = rectMinY + selectionRect.getHeight();

        // 检查所有缩略图是否在选择区域内
        for (VBox box : allThumbnails) {
            // 获取缩略图在父容器中的位置和大小
            Bounds bounds = box.getBoundsInParent();
            double boxMinX = bounds.getMinX();
            double boxMaxX = bounds.getMaxX();
            double boxMinY = bounds.getMinY();
            double boxMaxY = bounds.getMaxY();

            // 检查是否有重叠
            boolean overlaps = !(boxMaxX < rectMinX || boxMinX > rectMaxX ||
                    boxMaxY < rectMinY || boxMinY > rectMaxY);

            if (overlaps) {
                box.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
                selectedBoxes.add(box);
            }
        }
    }

    public Set<VBox> getSelectedBoxes(Set<VBox> selectedBoxes) {
        return selectedBoxes;
    }
}
