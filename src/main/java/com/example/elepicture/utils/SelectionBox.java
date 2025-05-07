package com.example.elepicture.utils;

import javafx.animation.PauseTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class SelectionBox {
    private final Pane selectionLayer; // 选择框图层
    private final Rectangle selectionRect; // 选择框矩形
    private double startX, startY; // 起始坐标
    private final FlowPane imagePreviewPane; // 图片预览面板
    private final Set<VBox> allThumbnails; // 所有缩略图
    private final Set<VBox> selectedBoxes; // 选中的缩略图
    private boolean isDraggingSelectionBox = false; // 是否正在拖动选择框
    private final PauseTransition dragPause; // 拖动节流定时器
    private final AtomicReference<MouseEvent> lastEvent; // 最后一次鼠标事件
    private static final double DRAG_THRESHOLD = 5.0; // 拖动阈值，超过这个距离才算拖动
    private final Label statusLabel; // 添加状态标签引用

    public SelectionBox(FlowPane imagePreviewPane, Set<VBox> allThumbnails, Set<VBox> selectedBoxes,
                        Label statusLabel) {
        this.imagePreviewPane = imagePreviewPane;
        this.allThumbnails = allThumbnails;
        this.selectedBoxes = selectedBoxes;
        this.statusLabel = statusLabel;
        this.dragPause = new PauseTransition(Duration.millis(20));
        this.lastEvent = new AtomicReference<>();

        // 创建透明图层用于显示选择框
        selectionLayer = new Pane();
        selectionLayer.setMouseTransparent(false);
        selectionLayer.setStyle("-fx-background-color: transparent;");

        // 设置选择图层的大小与预览面板相同
        selectionLayer.prefWidthProperty().bind(imagePreviewPane.widthProperty());
        selectionLayer.prefHeightProperty().bind(imagePreviewPane.heightProperty());
        selectionLayer.maxWidthProperty().bind(imagePreviewPane.widthProperty());
        selectionLayer.maxHeightProperty().bind(imagePreviewPane.heightProperty());

        // 创建选择框矩形
        selectionRect = new Rectangle();
        selectionRect.setStroke(Color.rgb(0, 120, 215, 0.6)); // 半透明蓝色边框
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.rgb(0, 120, 215, 0.15)); // 半透明蓝色填充
        selectionRect.setStrokeLineCap(StrokeLineCap.SQUARE);
        selectionRect.setStrokeLineJoin(StrokeLineJoin.MITER);
        selectionRect.setStrokeDashOffset(0);
        selectionRect.setVisible(false);
        selectionLayer.getChildren().add(selectionRect);

        // 将选择框图层添加到预览面板
        // 使用StackPane包装FlowPane和选择图层
        if (imagePreviewPane.getParent() instanceof StackPane) {
            StackPane stackPane = (StackPane) imagePreviewPane.getParent();
            stackPane.getChildren().add(selectionLayer);
        } else {
            // 如果没有StackPane，创建一个新的
            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(imagePreviewPane, selectionLayer);

            // 替换原来的FlowPane
            if (imagePreviewPane.getParent() != null) {
                Pane parent = (Pane) imagePreviewPane.getParent();
                int index = parent.getChildren().indexOf(imagePreviewPane);
                parent.getChildren().set(index, stackPane);
            }
        }

        // 确保选择框显示在最上层
        selectionLayer.toFront();

        setupMouseEvents();
    }

    private void setupMouseEvents() {
        // 鼠标按下事件
        imagePreviewPane.setOnMousePressed(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && !event.isControlDown()) {
                startX = event.getX();
                startY = event.getY();
                isDraggingSelectionBox = false; // 初始状态设为false
                event.consume();
            }
        });

        // 鼠标拖动事件
        imagePreviewPane.setOnMouseDragged(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && !event.isControlDown()) {
                double currentX = event.getX();
                double currentY = event.getY();

                // 计算拖动距离
                double dragDistance = Math.sqrt(
                        Math.pow(currentX - startX, 2) +
                                Math.pow(currentY - startY, 2));

                // 如果拖动距离超过阈值，启动框选
                if (!isDraggingSelectionBox && dragDistance > DRAG_THRESHOLD) {
                    isDraggingSelectionBox = true;
                    selectionRect.setX(startX);
                    selectionRect.setY(startY);
                    selectionRect.setWidth(0);
                    selectionRect.setHeight(0);
                    selectionRect.setVisible(true);
                    clearSelection();
                }

                if (isDraggingSelectionBox) {
                    lastEvent.set(event);

                    if (currentX < startX) {
                        selectionRect.setX(currentX);
                        selectionRect.setWidth(startX - currentX);
                    } else {
                        selectionRect.setX(startX);
                        selectionRect.setWidth(currentX - startX);
                    }

                    if (currentY < startY) {
                        selectionRect.setY(currentY);
                        selectionRect.setHeight(startY - currentY);
                    } else {
                        selectionRect.setY(startY);
                        selectionRect.setHeight(currentY - startY);
                    }

                    dragPause.setOnFinished(e -> {
                        updateSelection(event.isControlDown());
                        updateStatusLabel();
                    });
                    dragPause.stop();
                    dragPause.play();
                }

                event.consume();
            }
        });

        // 鼠标释放事件
        imagePreviewPane.setOnMouseReleased(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (isDraggingSelectionBox) {
                    dragPause.stop();
                    updateSelection(event.isControlDown());
                    updateStatusLabel();
                    selectionRect.setVisible(false);
                }
                isDraggingSelectionBox = false;
                event.consume();
            }
        });
    }

    private void updateStatusLabel() {
        statusLabel.setText("已选中 " + selectedBoxes.size() + " 张图片");
    }

    private void clearSelection() {
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear();
        updateStatusLabel();
    }

    private void updateSelection(boolean isControlDown) {
        if (!isControlDown) {
            clearSelection();
        }

        for (VBox thumbnail : allThumbnails) {
            if (isThumbnailInSelection(thumbnail)) {
                if (!selectedBoxes.contains(thumbnail)) {
                    thumbnail.setStyle(
                            "-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: lightblue;");
                    selectedBoxes.add(thumbnail);
                }
            }
        }
    }

    private boolean isThumbnailInSelection(VBox thumbnail) {
        double thumbnailX = thumbnail.getLayoutX();
        double thumbnailY = thumbnail.getLayoutY();
        double thumbnailWidth = thumbnail.getWidth();
        double thumbnailHeight = thumbnail.getHeight();

        // 检查缩略图是否与选择框相交
        return !(thumbnailX + thumbnailWidth < selectionRect.getX() ||
                thumbnailX > selectionRect.getX() + selectionRect.getWidth() ||
                thumbnailY + thumbnailHeight < selectionRect.getY() ||
                thumbnailY > selectionRect.getY() + selectionRect.getHeight());
    }
}