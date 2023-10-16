package whitman.cs370proj.composer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet; // Import the HashSet class
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set; // Import the Set interface
import java.util.Stack;

import javax.sound.midi.ShortMessage;

import javafx.animation.Interpolator;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class MainController {

    private final MidiPlayer melody = new MidiPlayer(60, 100);
    List<Note> notes = new ArrayList<Note>();
    Set<Rectangle> selectedRectangles = new HashSet<Rectangle>();
    Set<ResizableRectangle> selectedRectangos = new HashSet<ResizableRectangle>();   
    private TextInputDialog saveAsMenu = new TextInputDialog();
    private FileChooser fileChooser = new FileChooser();
    public File currentFile;
    List<Note> selectedNotes = new ArrayList<Note>();
    public boolean isDragging;
    private int selectedIndex;
    @FXML
    private Button playButton;
    @FXML
    private Button stopButton;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private Pane musicPane;
    @FXML
    private RadioButton harpischordButton;
    public int channel;
    public int instrument;
    public double releasedX;
    @FXML
    public Paint currentColor;
    @FXML
    ObservableList<Rectangle> rectanglesList = FXCollections.observableArrayList();
    @FXML
    ToggleGroup instruments = new ToggleGroup();

    @FXML
    private Line timerLine = new Line();
    
    public TranslateTransition moveLine = new TranslateTransition(Duration.millis(20000), timerLine);
    // private Paint color;

    boolean selection = false;
    public double currentX;
    public double currentY;
    public double placeX;
    public double placeY;
    public int placeChannel;
    public int placeDuration;

    ArrayList<Double> xArrayList = new ArrayList<Double>();
    ArrayList<Double> yArrayList = new ArrayList<Double>();
    ArrayList<Integer> channelArrayList = new ArrayList<Integer>();
    ArrayList<Integer> durationArrayList = new ArrayList<Integer>();

    Stack<ArrayList<Double>> xStack = new Stack<>();
    Stack<ArrayList<Double>> xStackRedo = new Stack<>();

    Stack<ArrayList<Double>> yStack = new Stack<>();
    Stack<ArrayList<Double>> yStackRedo = new Stack<>();

    Stack<ArrayList<Integer>> channelStack = new Stack<>();
    Stack<ArrayList<Integer>> channelStackRedo = new Stack<>();

    Stack<ArrayList<Integer>> durationStack = new Stack<>();
    Stack<ArrayList<Integer>> durationStackRedo = new Stack<>();

    private Rectangle selectionBox;

    @FXML 
    public void handleUndo() {
        
        if (xStack.isEmpty()) {
            System.out.println("Nothing to undo");
        } else {
            // xStackRedo.push(xStack.pop());
            xArrayList = xStack.pop();
            xStackRedo.push(xArrayList);

            yArrayList = yStack.pop();
            yStackRedo.push(yArrayList);

            channelArrayList = channelStack.pop();
            channelStackRedo.push(channelArrayList);

            durationArrayList = durationStack.pop();
            durationStackRedo.push(durationArrayList);

            // musicPane.getChildren().clear();
            // selectedRectangles.clear();

            for (int i = 0; i < xArrayList.size(); i++) {
                double newX = xArrayList.get(i);
                double newY = yArrayList.get(i);
                int newChannel = channelArrayList.get(i);
                int newDuration = durationArrayList.get(i);
                if (isPaste) {
                    // selectedRectangles.clear();
                    Rectangle removedRectangle = rectanglesList.remove(i);
                    musicPane.getChildren().remove(removedRectangle);
                    System.out.println("Deleted rectangle");
                }
                else{
                    drawRectangle(newX, newY, false, newChannel, newDuration, setInstrumentColor(newChannel));
                    isPaste = false;
                    isDelete = false;
                }
            }

            System.out.println("Undo stack" + xStackRedo.toString());
            // System.out.println("Undo stack" + yStackRedo.toString());
            // System.out.println("Redo stack" + channelStackRedo.toString());
            // System.out.println("Undo stack" + durationStackRedo.toString());

        }
    }

    @FXML
    public void handleRedo() {
        if (xStackRedo.isEmpty()) {
            System.out.println("Nothing to redo");
        } else {
            // xStack.push(xStackRedo.pop());
            xArrayList = xStackRedo.pop();
            xStack.push(xArrayList);

            yArrayList = yStackRedo.pop();
            yStack.push(yArrayList);

            channelArrayList = channelStackRedo.pop();
            channelStack.push(channelArrayList);

            durationArrayList = durationStackRedo.pop();
            durationStack.push(durationArrayList);

            // musicPane.getChildren().clear();
            selectedRectangles.clear();

            for (int i = 0; i < xArrayList.size(); i++) {
                double newX = xArrayList.get(i);
                double newY = yArrayList.get(i);
                int newChannel = channelArrayList.get(i);
                int newDuration = durationArrayList.get(i);

                if (isDelete) {
                    musicPane.getChildren().remove(rectanglesList.get(i));
                    System.out.println("Deleted rectangle");
                }
                else {
                    isDelete = false;
                    drawRectangle(newX, newY, false, newChannel, newDuration, setInstrumentColor(newChannel));
                }
            }

            System.out.println("Redo stack" + xStack.toString());
            // System.out.println("Undo stack" + yStackRedo.toString());
            // System.out.println("Redo stack" + channelStackRedo.toString());
            // System.out.println("Undo stack" + durationStackRedo.toString());

        }

    }

    @FXML
    /**
     * This method draws red rectangles of a predifine size on the music pane.
     * If the user clicks to draw another rectangle, the previous ones will be set to black
     * If the user holds down the control key, the rectangles will be drawn in red
     * @param x the longitude value of the mouse click positiion on the screeen
     * @param y the latitude value of the mouse click positiion on the screeen.
     * 
     */
    public void drawRectangle(double x, double y, boolean ctrl, int channel, int duration, Paint color) {

        //TODO: Make it so selected rectangles and instrudent colors are saved when switching instruments 
        // and selected nots should have borders not red
        color = setInstrumentColor(channel);
        setInstrument();

        //Correct the y value to be between 0 and 127
        double remainder = (y % 10);
        double correctedY = y - remainder;

        //Create a new rectangle from the resizable rectangle class
        ResizableRectangle resizableRectangle = new ResizableRectangle(x, correctedY, color);
        Rectangle rectangle = resizableRectangle.getRectangle();
        
        // If not ctrl, set all rectangles to have no border, unless they are selected
        // If ctrl, all rectangles drawn will bhave a border
        if (!ctrl){
            for (Rectangle rect : selectedRectangles) {
                rect.setStroke(null);
            }
            selectedRectangles.clear(); 
        }
        selectedRectangles.add(rectangle);
        //adds each rectangle to an array that we can check later.
        rectanglesList.add(rectangle);

        //selectedRectangles = getSelectedRectangles();


        // //adds each rectangle to the pane to be displayed.
        musicPane.getChildren().add(rectangle);

        // add rectangle coordinates to a list of notes
        int sceneX = (int) x;
        int sceneY = (int) y;

        Note newNote = new Note(sceneY, sceneX, channel, duration);
        notes.add(newNote);

        addToMelody(sceneY, sceneX);
        
    }

    @FXML
    public void drawComposerChart() {
        for (int i = 0; i < 1280; i = i + 10) {
            Line line = new Line();
            line.setStartX(0.0);
            line.setStartY(i);
            line.setEndX(2000.0);
            line.setEndY(i);
            musicPane.getChildren().add(line);
        }
    }
 
    @FXML
    public void setInstrumentGroup() {
        
        ToggleButton piano = new ToggleButton("Piano");
        ToggleButton harpischord = new ToggleButton("Harpischord");
        ToggleButton marimba = new ToggleButton("Marimba");
        ToggleButton organ = new ToggleButton("Organ");
        ToggleButton accordion = new ToggleButton("Accordion");
        ToggleButton guitar = new ToggleButton("Guitar");
        ToggleButton violin = new ToggleButton("Violin");
        ToggleButton frenchHorn = new ToggleButton("French Horn");
        
        

        piano.setToggleGroup(instruments);
        harpischord.setToggleGroup(instruments);
        marimba.setToggleGroup(instruments);
        organ.setToggleGroup(instruments);
        accordion.setToggleGroup(instruments);
        guitar.setToggleGroup(instruments);
        violin.setToggleGroup(instruments);
        frenchHorn.setToggleGroup(instruments);
    }
    
    /**
     * This method returns the channel and instrument values of the selected instrument
     * @return values which is an array of the channel and instrument values
     */
    @FXML
    public int[] setInstrument() {
        ToggleButton selectedInstrument = (ToggleButton) instruments.getSelectedToggle();
        String currentInstrument = selectedInstrument.getText();
        
        int[] values = {channel, instrument};


        // Paint currentColor;


        if (currentInstrument.equals("Piano")) {
            channel = 0;
            instrument = 0;
            return values;
        } 
        else if (currentInstrument.equals("Harpischord")) {
            channel = 1;
            instrument = 6;
            return values;
        } 
        else if (currentInstrument.equals("Marimba")) {
            channel = 2;
            instrument = 12;
            return values;
        } 
        else if (currentInstrument.equals("Organ")) {
            channel = 3;
            instrument = 19;
            return values;
        } 
        else if (currentInstrument.equals("Accordion")) {
            channel = 4;
            instrument = 21;
            return values;
        } 
        else if (currentInstrument.equals("Guitar")) {
            channel = 5;
            instrument = 24;
            return values;
        } 
        else if (currentInstrument.equals("Violin")) {
            channel = 6;
            instrument = 40;
            return values;
        } 
        else if (currentInstrument.equals("French Horn")) {
            channel = 7;
            instrument = 60;
            return values;
        }
         
        else {
            System.out.println("no selected instrument");
            channel = -1;
            instrument = -1;  
            return values;
        }
    }

    @FXML
    public Paint setInstrumentColor(int c) {
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
           
    @FXML
    public void initialize() {
        drawComposerChart();
        drawTimerLine();
        setInstrumentGroup();
        
        musicPane.addEventHandler(Event.ANY, new EventHandler<Event>() {

            //TODO: Pressing mouse in composition pane stops note playback and removes red
            // line.

            @Override
            public void handle(Event event){  
                if(event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    handleMousePressed(event);
                    setInstrument();
                }
                if(event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    handleMouseReleased(event);
                    System.out.println("Selected rectangles: " + selectedRectangles.size()  + " ---- All Rectangles: " + rectanglesList.size());
                }
                if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    handleMouseDragged(event);

                    // TODO: Pressing control key and mouse in composition panel (not on note bar)
                    // selects like normal, but already-selected note bars stay selected regardless
                    // of selectirect.setFill(color);on rectangle position.
                }

                
            }
        });
        moveLine.setInterpolator(Interpolator.LINEAR);
    }

    /**
     * This method handles the mouse click event
     * @param event which is the mouse click event

     */
    protected void handleMousePressed(Event event) {
        // get the current mouse coordinates
        currentX = ((MouseEvent) event).getX();
        currentY = ((MouseEvent) event).getY();
        
        // check if the current point is contained in a rectangle
        if (emptySpace()) {
            selectionBox = createSelectionBox(currentX, currentY);
        } else{
            
            // check if the mouse click is in the last 5 pixels of the right side of the rectangle
            // if it is, then we are resizing the rectangle
            if (isInRightEdge(currentX, currentY)) {
                resizeRectangles(currentX, currentY);
            } else {
                //if the mouse click is not in the last 5 pixels of the right side of the rectangle
                //then we are dragging the rectangle
                // assert true;
                moveRectangles(currentX, currentY);
            }
        }
    }
    

    /**
     * This method handles the mouse clicked event
     * If the mouse is clicked on an empty space, then a new rectangle is created
     * If the mouse is clicked on a rectangle, then the rectangle is selected and appropriate actions are taken
     * @param event which is the mouse release event
     */
    protected void handleMouseClicked(Event event) {

        currentX = ((MouseEvent) event).getX();
        currentY = ((MouseEvent) event).getY();

        boolean ctrl = ((MouseEvent) event).isControlDown();


        // Create a rectangle if the current point is contained in a rectangle
        if (emptySpace()) {

            // If control is held down, then create a rectangle and select it immediately
            if (ctrl) {
                drawRectangle(currentX, currentY, ctrl, setInstrument()[0], 100, setInstrumentColor(setInstrument()[0]));
            } else {
                drawRectangle(currentX, currentY, ctrl, setInstrument()[0], 100, setInstrumentColor(setInstrument()[0]));
            }
        } 
        
        // If the current point is contained in a rectangle, then select the rectangle
        else {
            for (Rectangle rect : rectanglesList) {

                boolean rectangleExists = rect.contains(((MouseEvent) event).getX(), ((MouseEvent) event).getY());

                // if control is held down, then select multiple rectangles
                if (ctrl) {
                    if (rectangleExists) {
                        rect.setStroke(Color.GREEN);
                        rect.setStrokeWidth(2);
                        selectedRectangles.add(rect);
                    }
                } else {
                    if (rectangleExists) {
                        rect.setStroke(Color.GREEN);
                        rect.setStrokeWidth(2);
                        selectedRectangles.clear();
                        selectedRectangles.add(rect);
                    } else {
                        rect.setStroke(null);
                    }
                }
            }
        }
        // System.out.println("selected rectangles: " + selectedRectangles.size());
    }
    
    /**
     * Checks if the mouse click is in the last 5 pixels of the right side of the rectangle
     * @param cuurentX is the current x coordinate of the mouse
     * @param currentY is the current y coordinate of the mouse
     * @return true if the mouse click is in the last 5 pixels of the right side of the rectangle
     */
    private boolean isInRightEdge(double currentX, double currentY) {
        Rectangle rect = getRectangle(selectedRectangles, currentX, currentY);
        if (rect != null) {
            return (rect.getX() + rect.getWidth() - currentX) < 10;
        }
        return false;
    }

    /**
     * Resizes the rectangles
     * @params currentX is the current x coordinate of the mouse
     * @params currentY is the current y coordinate of the mouse
     */
    private void resizeRectangles(double currentX, double currentY) {

        Rectangle currentRect = getRectangle(selectedRectangles, currentX, currentY);

        currentRect.setOnMouseDragged(event -> {
            double newWidth = Math.max(10, event.getX() - currentRect.getX());
            double diff = newWidth - currentRect.getWidth();

            currentRect.setWidth(Math.max(10, event.getX() - currentRect.getX()));

            for (Rectangle rect : selectedRectangles) {
                if (rect != currentRect) {
                    rect.setWidth(rect.getWidth() + diff);
                }
            }           
        });
    }

    /**
     * Moves the rectangles around the screen
     * @params currentX is the current x coordinate of the mouse
     * @params currentY is the current y coordinate of the mouse
     */
    private void moveRectangles(double currentX, double currentY) {

        Rectangle currentRect = getRectangle(selectedRectangles, currentX, currentY);

        currentRect.setOnMouseDragged(event -> {

            // update mouse start points
            double startX = currentRect.getX();
            double startY = currentRect.getY();

            currentRect.setX(event.getX() - currentRect.getWidth() / 2);
            currentRect.setY(event.getY() - (event.getY() % 10));

            // update mouse end points
            double endX = currentRect.getX();
            double endY = currentRect.getY();

            // move the other rectangles
            for (Rectangle rect : selectedRectangles) {
                if (rect != currentRect) {
                    rect.setX(rect.getX() + endX - startX);
                    rect.setY(rect.getY() + endY - startY);
                }
            }
        });
    }


    /**
     * Checks if the given location is a rectangle in a given list of rectangles
     * @return a rectangle 
     */
    private Rectangle getRectangle(Iterable<Rectangle> rectanglesList, double currentX, double currentY) {
        for (Rectangle rect : rectanglesList) {
            if (rect.contains(currentX, currentY)) {
                return rect;
            }
        }
        return null;
    }

    /**
     * Draw a note bar or selection box  mouse realease
     * @return none
     */
    protected void handleMouseReleased(Event event) {

        // check if the mouse was pressed and released in the same area
        boolean clickedAndReleasedSamePlace = currentX == ((MouseEvent) event).getX() && currentY == ((MouseEvent) event).getY();

        if (clickedAndReleasedSamePlace) {
            handleMouseClicked(event);
        }

        if (isDragging) {
            musicPane.getChildren().remove(selectionBox);
            selectionBox = null;
        }
        isDragging = MouseEvent.MOUSE_DRAGGED == event.getEventType();
    }


    /**
     * Draw a selection box on mouse drag
     * @param event is the mouse event
     * @return none
     */
    protected void handleMouseDragged(Event event) {

        isDragging = MouseEvent.MOUSE_DRAGGED == event.getEventType();

        if(selectionBox != null){
            selectionBox.setX(Math.min(((MouseEvent) event).getX(), currentX));
            selectionBox.setY(Math.min(((MouseEvent) event).getY(), currentY));
            selectionBox.setWidth(Math.abs(((MouseEvent) event).getX() - currentX));
            selectionBox.setHeight(Math.abs(((MouseEvent) event).getY() - currentY));
            checkSelection(selectionBox);
        }

    }
    
    /**
     * This method checks if the selection box contains any rectangles
     *
     * @param current mouse coordinates
     */
    private Rectangle createSelectionBox(double currentX, double currentY) {
        // create a new rectangle
        Rectangle selectBox = new Rectangle();
        selectBox.setStroke(Color.BLACK);
        selectBox.setFill(Color.TRANSPARENT);
        selectBox.getStrokeDashArray().addAll(3.0, 3.0);

        // add selection box to the pane
        selectBox.setX(currentX);
        selectBox.setY(currentY);

        selectBox.setWidth(0);
        selectBox.setHeight(0);
        musicPane.getChildren().add(selectBox);

        // return the selection box
        return selectBox;
    }


    /**
     * This method the area that has been clicekd is empty or already has objects drawn on the pane
     *@return boolean false current mouse coordinates are not on an empty screen Pane
     */
    public boolean emptySpace(){
        if(rectanglesList.isEmpty()){
            return true;
        }
        for (Rectangle rect : rectanglesList) {
            if (rect.contains(currentX, currentY)) {
                return false;
            }
        }
        return true;
    }        

    /**
     * This method returns a list of the selected rectangles
     * 
     * @return selectedRectangles which is a list of the selected rectangles
     */
    public Set<Rectangle> getSelectedRectangles(){
        selectedRectangles.clear();
        for (Rectangle rect : rectanglesList) {
            boolean hasBorder = rect.strokeProperty().getValue() != null;
            if (hasBorder){
                selectedRectangles.add(rect);
            }
        }
        return selectedRectangles;
    }

    @FXML
    public void handlePlay() {
        moveLine.jumpTo(Duration.ZERO);
        startTimerLine();
        //for(Note note : notes){
            //addToMelody(note.getPitch(), note.getTick());
        //}
        melody.play();
    }

    @FXML
    public void handleStop() {
        moveLine.jumpTo(Duration.ZERO);
        moveLine.stop();
        musicPane.getChildren().remove(timerLine);
        melody.stop();
    }

    /**
     * This method clears the 3 array lists that are used. DRY???
     */
    public void clearArrayLists() {
        xArrayList.clear();
        yArrayList.clear();
        channelArrayList.clear();
        durationArrayList.clear();
    }

    @FXML
    public void handleCut(){
        isDelete = true;
        clearArrayLists();
        handleCopy();
        for (Rectangle rectangle : selectedRectangles){
            placeX = rectangle.getX();
            xArrayList.add(placeX);
            placeY = rectangle.getY();
            yArrayList.add(placeY);

            musicPane.getChildren().remove(rectangle);
        }
        selectedRectangles.clear();
        selectedNotes.clear();

        for(Note note : selectedNotes){
            placeChannel = note.getChannel();
            channelArrayList.add(placeChannel);
            placeDuration = note.getDuration();
            durationArrayList.add(placeDuration);

            notes.remove(note);
        }
        selectedRectangles.clear();
        selectedNotes.clear();

        xStack.push(xArrayList);
        yStack.push(yArrayList);

        channelStack.push(channelArrayList);
        durationStack.push(durationArrayList);

        xStackRedo.push(xArrayList);
        yStackRedo.push(yArrayList);

        channelStackRedo.push(channelArrayList);
        durationStackRedo.push(durationArrayList);
    }

    @FXML
    public void handleCopy() {
        getSelectedRectangles();
        clearArrayLists();
        System.out.println(selectedRectangles);
        System.out.println(selectedNotes);

        for (Rectangle rectangle : selectedRectangles) {
            placeX = rectangle.getX();
            xArrayList.add(placeX);
            placeY = rectangle.getY();
            yArrayList.add(placeY);
            
        }

        for (Note note: selectedNotes) {
            placeChannel = note.getChannel();
            channelArrayList.add(placeChannel);
            placeDuration = note.getDuration();
            durationArrayList.add(placeDuration);
        }

        xStack.push(xArrayList);
        yStack.push(yArrayList);

        channelStack.push(channelArrayList);
        durationStack.push(durationArrayList);

        System.out.println(xArrayList);
        System.out.println(yArrayList);
        System.out.println(channelArrayList);
        System.out.println(durationArrayList);

    }
    

    boolean isPaste = false;
    @FXML
    public void handlePaste() {
        // handleCopy();
        // clearArrayLists();
        isPaste = true;
        for (int i = 0; i < xArrayList.size(); i++) {
            double newX = xArrayList.get(i);
            double newY = yArrayList.get(i);
            int newChannel = channelArrayList.get(i);
            int newDuration = durationArrayList.get(i);
            drawRectangle(newX, newY, false, newChannel, newDuration, setInstrumentColor(newChannel));
        }

        xStack.push(xArrayList);
        yStack.push(yArrayList);

        channelStack.push(channelArrayList);
        durationStack.push(durationArrayList);
    }

    @FXML
    public void handleSelectAll(){      
        for(Rectangle rectangle : rectanglesList){
            rectangle.setStroke(Color.GREEN);
            rectangle.setStrokeWidth(2);
            selectedRectangles.add(rectangle);
        }
        for(Note element : notes){
            selectedNotes.add(element);
        }
        System.out.println(selectedNotes.size());
    }

    boolean isDelete = false;

    @FXML
    public void handleDelete() {
        isDelete = true;
        clearArrayLists();
        for (Rectangle rectangle : selectedRectangles){
            placeX = rectangle.getX();
            xArrayList.add(placeX);
            placeY = rectangle.getY();
            yArrayList.add(placeY);

            musicPane.getChildren().remove(rectangle);
            rectanglesList.remove(rectangle);
        }
        for(Note note : selectedNotes){
            placeChannel = note.getChannel();
            channelArrayList.add(placeChannel);
            placeDuration = note.getDuration();
            durationArrayList.add(placeDuration);

            notes.remove(note);
        }
        selectedRectangles.clear();
        selectedNotes.clear();

        xStack.push(xArrayList);
        yStack.push(yArrayList);

        channelStack.push(channelArrayList);
        durationStack.push(durationArrayList);

        xStackRedo.push(xArrayList);
        yStackRedo.push(yArrayList);

        channelStackRedo.push(channelArrayList);
        durationStackRedo.push(durationArrayList);

    }

    @FXML
    public void handleAbout(){
        Dialog<String> about = new Dialog<String>();
        about.setTitle("About");
        ButtonType close = new ButtonType("Close", ButtonData.CANCEL_CLOSE);
        about.getDialogPane().getButtonTypes().add(close);
        about.setContentText("Program for creating your own melodies. Created by Dustin, Lucky, Mateus, and Silivan.");
        about.showAndWait();
    }


    @FXML
    public void handleSave(){
        System.out.println("The save button is working.");
        // save current changes to existing file
        // if there isn't an existing file, else redirect to handleSaveAs
        if (currentFile == null) {
            handleSaveAs();
        } else {
            try {
                //clears the files
                try (RandomAccessFile raf = new RandomAccessFile(currentFile, "rw")) {
                    raf.setLength(0);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                
                FileWriter myWriter = new FileWriter(currentFile);
                for(Note eachNote : notes){
                    myWriter.write(Integer.toString(eachNote.getPitch()) + " ");
                    myWriter.write(Integer.toString(eachNote.getDuration()) + " ");
                    myWriter.write(Integer.toString(eachNote.getTick()) + " ");
                    myWriter.write(Integer.toString(eachNote.getChannel()));
                    myWriter.write("\n");
                }
                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSaveAs(){
        // create new file and save composition to that file
        //System.out.println("The save as button is working.");
        saveAsMenu.setHeaderText("Enter file name:");
        saveAsMenu.setTitle("Save As");
        saveAsMenu.showAndWait();
        String name = saveAsMenu.getEditor().getText();
        File newFile = new File(name + ".txt");
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }
        try{
            FileWriter myWriter = new FileWriter(newFile);
            for(Note eachNote : notes){
                myWriter.write(Integer.toString(eachNote.getPitch()) + " ");
                myWriter.write(Integer.toString(eachNote.getDuration()) + " ");
                myWriter.write(Integer.toString(eachNote.getTick()) + " ");
                myWriter.write(Integer.toString(eachNote.getChannel()));
                myWriter.write("\n");
                currentFile = newFile;
            }
            myWriter.close();
        }catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNew(){
        System.out.println("The new button is working.");
        // create a new, blank composition and corresponding blank file to save it in
        // if current composition already saved or blank, clear compositon
        // otherwise, dialog box with yes, no, or cancel options
        // if yes -> save
        // if no -> clear
        // if cancel -> close dialog and do nothing
        Alert newMenu = new Alert(AlertType.CONFIRMATION);
        newMenu.setContentText("Do you want to continue without saving?");
        newMenu.setTitle("New Composition");
        Optional<ButtonType> choice = newMenu.showAndWait();
        if(choice.get() == ButtonType.OK){
            handleSelectAll();
            handleDelete();
            isDelete = false;
        }else{
            if(currentFile == null){
                handleSaveAs();
            }else{
                handleSave();
                handleSelectAll();
                handleDelete();
                isDelete = false;
                currentFile = null;
            }
        }
    }

    @FXML
    public void handleOpen(){
        System.out.println("The open button is working.");
        // open existing file, add notes and rectangles to composition pane
        // if unsaved, offer chance to save
        // if saved or once saved or empty, user can choose a file to open
        // write choosen file into composition pane
        int tempTick;
        int tempDuration;
        int tempChannel;
        int tempPitch;
        fileChooser.setTitle("Open");
        File openFile = fileChooser.showOpenDialog(null);
        currentFile = openFile;
        try {
            Scanner fileScanner = new Scanner(openFile);
            while(fileScanner.hasNextLine()){
                tempPitch = Integer.parseInt(fileScanner.next());
                tempDuration = Integer.parseInt(fileScanner.next());
                tempTick = Integer.parseInt(fileScanner.next());
                tempChannel = Integer.parseInt(fileScanner.next());
                drawRectangle(tempTick, tempPitch, false, tempChannel, tempDuration, setInstrumentColor(tempChannel));
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }
    }


    @FXML
    public void handleExit() {
        System.exit(0);
    }

        /**
     * This method adds a midiEvent with a specific channel and instrument to the melody. 
     * @param pitch 
     * @param tick
     */
    @FXML
    private void addToMelody(int pitch, int tick){
        int instrumentSelector[] = setInstrument();
        channel = instrumentSelector[0];
        instrument = instrumentSelector[1];

        melody.addMidiEvent(ShortMessage.PROGRAM_CHANGE + channel, instrument, 0 ,tick, 0);
        melody.addNote(pitch / 10, 127, tick, 100, channel, 0);
    }

    @FXML
    public void drawTimerLine(){
        timerLine.setStroke(Color.RED); 
        timerLine.setStartX(0);
        timerLine.setStartY(0);
        timerLine.setEndX(0);
        timerLine.setEndY(1280);
    }

    @FXML
    public void startTimerLine(){
        musicPane.getChildren().add(timerLine);
        moveLine.setByX(2000f);
        moveLine.playFromStart();
    }

    public void editNotes(int endTick){
        for(Note element:selectedNotes){
            element.setDuration(element.getTick() + endTick);
        }
    }

    /**
     * If a rectangle is within the bounds of the selectBox, it will be filled red. Else, it will remain as it easy.
     * @param selectBox - the selectBox from the drawSelBox() method
     * @return none
     */
    @FXML
    public void checkSelection(Shape selectionBox) {
        
        // Loop through each rectangle in the list
        for (Rectangle rectangle: rectanglesList) {
            // Check if the rectangle intersects with the selectionBox
            Shape intersect = Shape.intersect(rectangle, selectionBox);
            if (intersect.getBoundsInLocal().getWidth() != -1) {

                // If the rectangle intersects, set its fill color to red
                rectangle.setStroke(Color.GREEN);
                rectangle.setStrokeWidth(2); 
                selectedIndex = rectanglesList.indexOf(rectangle);
                selectedRectangles.add(rectangle);
                selectedNotes.add(notes.get(selectedIndex));
            } else {
                // If the rectangle doesn't intersect, set its fill color to black
                rectangle.setStroke(null);
                rectangle.setStrokeWidth(0);
            }
        }
        // return selection;
    }

}

