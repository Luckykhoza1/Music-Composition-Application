package whitman.cs370proj.composer;

//Point2D provides methods for working with points, such as computing the distance between two points.
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;



public class ResizableRectangle {
    private Rectangle rect;
    private Point2D dragAnchor;
    private static Paint color;
    public static Paint gray = Color.GRAY;
    public static Paint greenyellow = Color.GREENYELLOW;
    public static Paint blue = Color.BLUE;
    public static Paint darkkhaki = Color.DARKKHAKI;
    public static Paint hotpink = Color.HOTPINK;
    public static Paint lightskyblue = Color.LIGHTSKYBLUE;
    public static Paint black = Color.BLACK;
    public static Paint saddlebrown = Color.SADDLEBROWN;

    public static Paint currentColor;


    public static ResizableRectangle rectColor;


    

    public ResizableRectangle(double x, double y, Paint color) {
        // create a new rectangle and set its properties (color, width, height, etc.)
        rect = new Rectangle(x, y, 100, 10);
        
        rect.setFill(color);
        rect.setStroke(Color.GREEN);
        rect.setStrokeWidth(2);
    }

    

    public void enableDrag() {
        setDragListeners();
    }

    public static Paint getColor() {
        return color;
    }

    public static Paint setRectangleColor(int c) {
        int currentChannel = c;

        if (currentChannel == 0) {
            currentColor = ResizableRectangle.gray;
            return currentColor;
        }
        else if (currentChannel == 1) {
            currentColor = ResizableRectangle.greenyellow;
            return currentColor;
        } 
        else if (currentChannel == 2) {
            currentColor = ResizableRectangle.blue;
            return currentColor;
        } 
        else if (currentChannel == 3) {
            currentColor = ResizableRectangle.darkkhaki;
            return currentColor;
        } 
        else if (currentChannel == 4) {
            currentColor = ResizableRectangle.hotpink;
            return currentColor;
        } 
        else if (currentChannel == 5) {
            currentColor = ResizableRectangle.lightskyblue;
            return currentColor;
        } 
        else if (currentChannel == 6) {
            currentColor = ResizableRectangle.black;
            return currentColor;
        } 
        else if (currentChannel == 7) {
            currentColor = ResizableRectangle.saddlebrown;
            return currentColor;
        }
        return currentColor;
    }

    private void setDragListeners() {
        dragAnchor = new Point2D(0, 0);
        rect.setOnMousePressed((MouseEvent mouseEvent) -> {
            // when mouse is pressed, store initial position
            dragAnchor = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        });

        rect.setOnMouseDragged((MouseEvent mouseEvent) -> {
            // when drag event is detected, calculate offset of drag
            double offsetX = mouseEvent.getX() - dragAnchor.getX();
            double offsetY = mouseEvent.getY() - dragAnchor.getY();

            // move rectangle to new position
            rect.setX(rect.getX() + offsetX);
            rect.setY(rect.getY() + offsetY);

            // update anchor position
            dragAnchor = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        });
    }

    public Rectangle getRectangle() {
        return rect;
    }

}