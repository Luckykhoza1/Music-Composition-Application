package whitman.cs370proj.composer;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class MouseClickReleaseDetector {

    private boolean clickedAndReleasedSamePlace = false;
    private double startX, startY;

    public MouseClickReleaseDetector(Pane pane) {
        pane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                startX = event.getX();
                startY = event.getY();
                clickedAndReleasedSamePlace = false;
            }
        });

        pane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getX() == startX && event.getY() == startY) {
                    clickedAndReleasedSamePlace = true;
                } else {
                    clickedAndReleasedSamePlace = false;
                }
            }
        });
    }

    public boolean hasClickedAndReleasedSamePlace() {
        return clickedAndReleasedSamePlace;
    }
}
