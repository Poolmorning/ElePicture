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

/**
 * 选择框工具类，用于实现图片缩略图的框选功能
 */
public class SelectionBox {

    private final Pane selectionLayer;// 选择框图层，用于显示选择框
    private final Rectangle selectionRect;// 选择框矩形图形
    private double startX, startY;// 鼠标拖拽起始坐标
    private final FlowPane imagePreviewPane;// 图片缩略图容器面板
    private final Set<VBox> allThumbnails;// 所有缩略图集合
    private final Set<VBox> selectedBoxes;// 当前选中的缩略图集合
    private boolean isDraggingSelectionBox = false;// 是否正在拖拽选择框的标志
    private final PauseTransition dragPause;// 拖拽节流定时器，用于减少频繁更新
    private final AtomicReference<MouseEvent> lastEvent;// 记录最后一次鼠标事件
    private static final double DRAG_THRESHOLD = 5.0;// 拖拽阈值，超过这个距离(5px)才视为拖拽操作
    private final Label statusLabel;// 状态标签，用于显示选中数量

    /**
     * 构造函数
     * @param imagePreviewPane 缩略图容器面板
     * @param allThumbnails 所有缩略图集合
     * @param selectedBoxes 选中缩略图集合
     * @param statusLabel 状态标签
     */
    public SelectionBox(FlowPane imagePreviewPane, Set<VBox> allThumbnails, Set<VBox> selectedBoxes,
                        Label statusLabel) {
        this.imagePreviewPane = imagePreviewPane;
        this.allThumbnails = allThumbnails;
        this.selectedBoxes = selectedBoxes;
        this.statusLabel = statusLabel;

        // 初始化节流定时器(20ms间隔)
        this.dragPause = new PauseTransition(Duration.millis(20));
        this.lastEvent = new AtomicReference<>();

        // 创建透明选择框图层
        selectionLayer = new Pane();
        selectionLayer.setMouseTransparent(false);
        selectionLayer.setStyle("-fx-background-color: transparent;");

        // 绑定选择图层尺寸与缩略图面板一致
        selectionLayer.prefWidthProperty().bind(imagePreviewPane.widthProperty());
        selectionLayer.prefHeightProperty().bind(imagePreviewPane.heightProperty());
        selectionLayer.maxWidthProperty().bind(imagePreviewPane.widthProperty());
        selectionLayer.maxHeightProperty().bind(imagePreviewPane.heightProperty());

        // 初始化选择框矩形
        selectionRect = new Rectangle();
        selectionRect.setStroke(Color.rgb(0, 120, 215, 0.6)); // 半透明蓝色边框
        selectionRect.setStrokeWidth(1); // 边框宽度1px
        selectionRect.setFill(Color.rgb(0, 120, 215, 0.15)); // 半透明蓝色填充
        selectionRect.setStrokeLineCap(StrokeLineCap.SQUARE); // 线帽样式
        selectionRect.setStrokeLineJoin(StrokeLineJoin.MITER); // 线连接样式
        selectionRect.setStrokeDashOffset(0); // 虚线偏移
        selectionRect.setVisible(false); // 初始不可见
        selectionLayer.getChildren().add(selectionRect);

        // 将选择框图层添加到界面
        setupSelectionLayer();

        // 设置鼠标事件监听
        setupMouseEvents();
    }

    /**
     * 将选择框图层添加到界面
     */
    private void setupSelectionLayer() {
        // 检查是否已有StackPane容器
        if (imagePreviewPane.getParent() instanceof StackPane) {
            StackPane stackPane = (StackPane) imagePreviewPane.getParent();
            stackPane.getChildren().add(selectionLayer);
        } else {
            // 创建新的StackPane容器
            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(imagePreviewPane, selectionLayer);

            // 替换原FlowPane
            if (imagePreviewPane.getParent() != null) {
                Pane parent = (Pane) imagePreviewPane.getParent();
                int index = parent.getChildren().indexOf(imagePreviewPane);
                parent.getChildren().set(index, stackPane);
            }
        }

        // 确保选择框显示在最上层
        selectionLayer.toFront();
    }

    /**
     * 设置鼠标事件监听
     */
    private void setupMouseEvents() {
        // 鼠标按下事件
        imagePreviewPane.setOnMousePressed(event -> {
            // 仅处理左键点击且未按住Ctrl键的情况
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && !event.isControlDown()) {
                startX = event.getX(); // 记录起始X坐标
                startY = event.getY(); // 记录起始Y坐标
                isDraggingSelectionBox = false; // 重置拖拽状态
                event.consume(); // 阻止事件冒泡
            }
        });

        // 鼠标拖拽事件
        imagePreviewPane.setOnMouseDragged(event -> {
            // 仅处理左键拖拽且未按住Ctrl键的情况
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && !event.isControlDown()) {
                double currentX = event.getX();
                double currentY = event.getY();

                // 计算拖拽距离(欧几里得距离)
                double dragDistance = Math.sqrt(
                        Math.pow(currentX - startX, 2) +
                                Math.pow(currentY - startY, 2));

                // 检查是否超过拖拽阈值
                if (!isDraggingSelectionBox && dragDistance > DRAG_THRESHOLD) {
                    isDraggingSelectionBox = true; // 标记为拖拽状态
                    // 初始化选择框位置和大小
                    selectionRect.setX(startX);
                    selectionRect.setY(startY);
                    selectionRect.setWidth(0);
                    selectionRect.setHeight(0);
                    selectionRect.setVisible(true); // 显示选择框
                    clearSelection(); // 清空当前选择
                }

                // 更新选择框
                if (isDraggingSelectionBox) {
                    lastEvent.set(event); // 记录事件

                    // 计算并更新选择框位置和大小
                    updateSelectionRect(currentX, currentY);

                    // 设置节流定时器
                    dragPause.setOnFinished(e -> {
                        updateSelection(event.isControlDown()); // 更新选中状态
                        updateStatusLabel(); // 更新状态标签
                    });
                    dragPause.stop();
                    dragPause.play(); // 启动定时器
                }

                event.consume(); // 阻止事件冒泡
            }
        });

        // 鼠标释放事件
        imagePreviewPane.setOnMouseReleased(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (isDraggingSelectionBox) {
                    dragPause.stop(); // 停止定时器
                    updateSelection(event.isControlDown()); // 最终更新选中状态
                    updateStatusLabel(); // 更新状态标签
                    selectionRect.setVisible(false); // 隐藏选择框
                }
                isDraggingSelectionBox = false; // 重置拖拽状态
                event.consume(); // 阻止事件冒泡
            }
        });
    }

    /**
     * 更新选择框的位置和大小
     * @param currentX 当前鼠标X坐标
     * @param currentY 当前鼠标Y坐标
     */
    private void updateSelectionRect(double currentX, double currentY) {
        // 水平方向计算
        if (currentX < startX) {
            selectionRect.setX(currentX);
            selectionRect.setWidth(startX - currentX);
        } else {
            selectionRect.setX(startX);
            selectionRect.setWidth(currentX - startX);
        }

        // 垂直方向计算
        if (currentY < startY) {
            selectionRect.setY(currentY);
            selectionRect.setHeight(startY - currentY);
        } else {
            selectionRect.setY(startY);
            selectionRect.setHeight(currentY - startY);
        }
    }

    /**
     * 更新状态标签显示选中数量
     */
    private void updateStatusLabel() {
        statusLabel.setText("已选中 " + selectedBoxes.size() + " 张图片");
    }

    /**
     * 清空当前选择
     */
    private void clearSelection() {
        // 清除所有选中缩略图的样式
        for (VBox box : selectedBoxes) {
            box.setStyle("-fx-border-color: transparent;");
        }
        selectedBoxes.clear(); // 清空选中集合
        updateStatusLabel(); // 更新状态标签
    }

    /**
     * 更新选中状态
     * @param isControlDown 是否按住Ctrl键(多选模式)
     */
    private void updateSelection(boolean isControlDown) {
        // 非多选模式先清空当前选择
        if (!isControlDown) {
            clearSelection();
        }

        // 检查每个缩略图是否在选择框内
        for (VBox thumbnail : allThumbnails) {
            if (isThumbnailInSelection(thumbnail)) {
                if (!selectedBoxes.contains(thumbnail)) {
                    // 设置选中样式
                    thumbnail.setStyle(
                            "-fx-border-color: black; -fx-border-width: 1px; -fx-background-color: lightblue;");
                    selectedBoxes.add(thumbnail); // 添加到选中集合
                }
            }
        }
    }

    /**
     * 判断缩略图是否在选择框内
     * @param thumbnail 缩略图容器
     * @return 是否在选择框内
     */
    private boolean isThumbnailInSelection(VBox thumbnail) {
        // 获取缩略图位置和尺寸
        double thumbnailX = thumbnail.getLayoutX();
        double thumbnailY = thumbnail.getLayoutY();
        double thumbnailWidth = thumbnail.getWidth();
        double thumbnailHeight = thumbnail.getHeight();

        // 检查是否与选择框相交(矩形碰撞检测)
        return !(thumbnailX + thumbnailWidth < selectionRect.getX() || // 缩略图在选择框左侧
                thumbnailX > selectionRect.getX() + selectionRect.getWidth() || // 缩略图在选择框右侧
                thumbnailY + thumbnailHeight < selectionRect.getY() || // 缩略图在选择框上方
                thumbnailY > selectionRect.getY() + selectionRect.getHeight()); // 缩略图在选择框下方
    }
}