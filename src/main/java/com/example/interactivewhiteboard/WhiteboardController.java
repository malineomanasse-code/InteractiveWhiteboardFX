package com.example.interactivewhiteboard;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class WhiteboardController implements Initializable {

    // FXML Components
    @FXML private Canvas drawingCanvas;
    @FXML private BorderPane mainPane;
    @FXML private VBox leftToolbar;
    @FXML private HBox topToolbar;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSizeSlider;
    @FXML private ComboBox<String> toolSelector;
    @FXML private Button pencilBtn, lineBtn, rectangleBtn, circleBtn, textBtn, eraserBtn;
    @FXML private Button addImageBtn, addTextBtn, clearBtn, saveBtn, loadBtn;
    @FXML private Label brushSizeLabel, statusLabel;
    @FXML private ToggleButton gridToggle;

    private GraphicsContext gc;
    private double startX, startY;
    private boolean drawing = false;
    private String currentTool = "PENCIL";
    private Color canvasBackground = Color.WHITE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeCanvas();
        setupEventHandlers();
        setupToolbar();
        updateStatus("Ready - Select a tool to start drawing");
    }

    private void initializeCanvas() {
        gc = drawingCanvas.getGraphicsContext2D();
        clearCanvas();

        // Make canvas responsive
        drawingCanvas.widthProperty().bind(mainPane.widthProperty().subtract(leftToolbar.getWidth() + 20));
        drawingCanvas.heightProperty().bind(mainPane.heightProperty().subtract(topToolbar.getHeight() + 20));

        // Redraw when resized
        drawingCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
        drawingCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
    }

    private void setupEventHandlers() {
        // Canvas mouse events
        drawingCanvas.setOnMousePressed(this::handleMousePressed);
        drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
        drawingCanvas.setOnMouseReleased(this::handleMouseReleased);

        // Color picker
        colorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            gc.setStroke(newVal);
            gc.setFill(newVal);
        });

        // Brush size slider
        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            gc.setLineWidth(newVal.doubleValue());
            brushSizeLabel.setText(String.format("Size: %.1f", newVal));
        });

        // Tool selector
        toolSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> setCurrentTool(newVal)
        );
    }

    private void setupToolbar() {
        // Initialize tool selector
        toolSelector.getItems().addAll("Pencil", "Line", "Rectangle", "Circle", "Text", "Eraser");
        toolSelector.setValue("Pencil");

        // Set button actions
        pencilBtn.setOnAction(e -> setCurrentTool("Pencil"));
        lineBtn.setOnAction(e -> setCurrentTool("Line"));
        rectangleBtn.setOnAction(e -> setCurrentTool("Rectangle"));
        circleBtn.setOnAction(e -> setCurrentTool("Circle"));
        textBtn.setOnAction(e -> setCurrentTool("Text"));
        eraserBtn.setOnAction(e -> setCurrentTool("Eraser"));

        addImageBtn.setOnAction(e -> addImage());
        addTextBtn.setOnAction(e -> addTextDialog());
        clearBtn.setOnAction(e -> clearCanvas());
        saveBtn.setOnAction(e -> saveCanvas());
        loadBtn.setOnAction(e -> loadImage());
        gridToggle.setOnAction(e -> toggleGrid());
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        drawing = true;

        if (currentTool.equals("Text")) {
            addTextAtPosition(startX, startY);
        } else {
            gc.beginPath();
            gc.moveTo(startX, startY);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!drawing) return;

        double currentX = event.getX();
        double currentY = event.getY();

        switch (currentTool) {
            case "Pencil":
                gc.lineTo(currentX, currentY);
                gc.stroke();
                break;
            case "Eraser":
                gc.setStroke(canvasBackground);
                gc.setLineWidth(brushSizeSlider.getValue() * 2);
                gc.lineTo(currentX, currentY);
                gc.stroke();
                gc.setStroke(colorPicker.getValue());
                gc.setLineWidth(brushSizeSlider.getValue());
                break;
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!drawing) return;

        double endX = event.getX();
        double endY = event.getY();

        switch (currentTool) {
            case "Line":
                gc.strokeLine(startX, startY, endX, endY);
                break;
            case "Rectangle":
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                double x = Math.min(startX, endX);
                double y = Math.min(startY, endY);
                gc.strokeRect(x, y, width, height);
                break;
            case "Circle":
                double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                break;
        }

        drawing = false;
        gc.beginPath();
    }

    private void setCurrentTool(String tool) {
        currentTool = tool;
        toolSelector.setValue(tool);
        updateStatus("Active Tool: " + tool);

        // Update button states
        pencilBtn.getStyleClass().remove("active-tool");
        lineBtn.getStyleClass().remove("active-tool");
        rectangleBtn.getStyleClass().remove("active-tool");
        circleBtn.getStyleClass().remove("active-tool");
        textBtn.getStyleClass().remove("active-tool");
        eraserBtn.getStyleClass().remove("active-tool");

        switch (tool) {
            case "Pencil": pencilBtn.getStyleClass().add("active-tool"); break;
            case "Line": lineBtn.getStyleClass().add("active-tool"); break;
            case "Rectangle": rectangleBtn.getStyleClass().add("active-tool"); break;
            case "Circle": circleBtn.getStyleClass().add("active-tool"); break;
            case "Text": textBtn.getStyleClass().add("active-tool"); break;
            case "Eraser": eraserBtn.getStyleClass().add("active-tool"); break;
        }
    }

    private void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                gc.drawImage(image, 50, 50, image.getWidth() / 2, image.getHeight() / 2);
                updateStatus("Image added successfully");
            } catch (Exception e) {
                showError("Error loading image: " + e.getMessage());
            }
        }
    }

    private void addTextDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter your text:");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                addTextAtPosition(100, 100); // Default position
            }
        });
    }

    private void addTextAtPosition(double x, double y) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter text to add at position (" + (int)x + ", " + (int)y + "):");

        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                gc.setFont(new javafx.scene.text.Font("Arial", brushSizeSlider.getValue() * 3));
                gc.fillText(text, x, y);
                updateStatus("Text added");
            }
        });
    }

    private void clearCanvas() {
        gc.setFill(canvasBackground);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        updateStatus("Canvas cleared");
    }

    private void redrawCanvas() {
        // This would redraw all elements if you had a drawing history
        // For now, just clear and notify
        updateStatus("Canvas resized");
    }

    private void saveCanvas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Whiteboard");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg")
        );

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(),
                        (int) drawingCanvas.getHeight());
                drawingCanvas.snapshot(null, writableImage);

                String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), extension, file);
                updateStatus("Canvas saved successfully: " + file.getName());
            } catch (Exception e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Image to Canvas");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                clearCanvas();
                gc.drawImage(image, 0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                updateStatus("Image loaded successfully");
            } catch (Exception e) {
                showError("Error loading image: " + e.getMessage());
            }
        }
    }

    private void toggleGrid() {
        if (gridToggle.isSelected()) {
            drawGrid();
            updateStatus("Grid enabled");
        } else {
            clearCanvas();
            updateStatus("Grid disabled");
        }
    }

    private void drawGrid() {
        double spacing = 20;
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (double x = 0; x < drawingCanvas.getWidth(); x += spacing) {
            gc.strokeLine(x, 0, x, drawingCanvas.getHeight());
        }
        for (double y = 0; y < drawingCanvas.getHeight(); y += spacing) {
            gc.strokeLine(0, y, drawingCanvas.getWidth(), y);
        }

        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(brushSizeSlider.getValue());
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}