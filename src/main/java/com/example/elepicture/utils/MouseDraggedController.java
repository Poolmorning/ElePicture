package com.example.elepicture.utils;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;

public class MouseDraggedController {
    private double width,height;
    private double setX,setY;
    public static final Rectangle rectangle = new Rectangle();
    public static Pane pane = new Pane();

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public MouseDraggedController(){
        rectangle.setArcHeight(6);
        rectangle.setArcWidth(10);
        rectangle.setFill(Color.rgb(200,200,200,0.3));
        pane.getChildren().add(rectangle);
        MouseDraggedOnFlowPane();
    }
    public void MouseDraggedOnFlowPane(){
        pane.setOnMousePressed(this::handleMousePressed);//这是鼠标按下事件
        pane.setOnMouseDragged(this::draggedMousePressed);//这是鼠标拖动事件
        pane.setOnMouseReleased(this::releasedMouse);//这是鼠标释放事件
    }

    private void releasedMouse(MouseEvent mouseEvent) {
        rectangle.setX(0);
        rectangle.setY(0);
        rectangle.setHeight(0);
        rectangle.setWidth(0);
    }

    private void handleMousePressed(MouseEvent mouseEvent) {
        setX = mouseEvent.getX();
        setY = mouseEvent.getY();
    }

    private void draggedMousePressed(MouseEvent mouseEvent) {
        width = mouseEvent.getX() -setX;
        height = mouseEvent.getY() - setY;
        rectangle.setX(setX);
        rectangle.setY(setY);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
    }
}
