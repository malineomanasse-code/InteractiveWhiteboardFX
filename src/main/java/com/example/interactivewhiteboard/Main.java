package com.example.interactivewhiteboard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javax.imageio.ImageIO;
import java.io.File;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;


public class Main extends Application {
    private Canvas drawingCanvas;
    private GraphicsContext gc;
    private double startX, startY;
    private boolean drawing = false;
    private String currentTool = "pencil";
    private Color currentColor = Color.BLACK;
    private double brushSize = 3.0;
    private boolean gridEnabled = false;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Interactive Digital Whiteboard - Limkokwing University");

        // Main layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Create canvas
        drawingCanvas = new Canvas(1000, 700);
        gc = drawingCanvas.getGraphicsContext2D();
        setupCanvas();

        // Center area with canvas
        StackPane centerPane = new StackPane(drawingCanvas);
        centerPane.getStyleClass().add("center-pane");
        root.setCenter(centerPane);

        // Create toolbars
        root.setTop(createTopToolbar());
        root.setLeft(createLeftToolbar());

        // Setup event handlers
        setupMouseEvents();

        // Scene with CSS
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());


        // Make window resizable
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    private void setupCanvas() {
        // Set white background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        // Set initial drawing settings
        gc.setStroke(currentColor);
        gc.setLineWidth(brushSize);
        gc.setFill(currentColor);

        // Set line cap using string constant instead of enum
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

    }

    private HBox createTopToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.getStyleClass().add("top-toolbar");
        toolbar.setPadding(new Insets(10));

        Button saveBtn = createButton("💾 Save", "save-btn");
        Button loadBtn = createButton("📁 Load", "load-btn");
        Button addImageBtn = createButton("🖼️ Add Image", "image-btn");
        Button addTextBtn = createButton("📝 Add Text", "text-btn");
        Button clearBtn = createButton("🗑️ Clear", "clear-btn");
        ToggleButton gridBtn = createToggleButton("📊 Grid");

        // Button actions
        saveBtn.setOnAction(e -> saveCanvas());
        loadBtn.setOnAction(e -> loadImage());
        addImageBtn.setOnAction(e -> addImage());
        addTextBtn.setOnAction(e -> addText());
        clearBtn.setOnAction(e -> clearCanvas());
        gridBtn.setOnAction(e -> toggleGrid(gridBtn.isSelected()));

        toolbar.getChildren().addAll(saveBtn, loadBtn, createSeparator(),
                addImageBtn, addTextBtn, createSeparator(),
                clearBtn, gridBtn);
        return toolbar;
    }

    private VBox createLeftToolbar() {
        VBox toolbar = new VBox(15);
        toolbar.getStyleClass().add("left-toolbar");
        toolbar.setPadding(new Insets(15));
        toolbar.setPrefWidth(200);

        // Drawing tools section
        Label toolsLabel = new Label("Drawing Tools");
        toolsLabel.getStyleClass().add("section-label");

        // Tool buttons
        ToggleGroup toolGroup = new ToggleGroup();
        ToggleButton pencilBtn = createToolToggleButton("✏️ Pencil", "pencil", toolGroup, true);
        ToggleButton lineBtn = createToolToggleButton("📏 Line", "line", toolGroup, false);
        ToggleButton rectBtn = createToolToggleButton("⬜ Rectangle", "rectangle", toolGroup, false);
        ToggleButton circleBtn = createToolToggleButton("⭕ Circle", "circle", toolGroup, false);
        ToggleButton textBtn = createToolToggleButton("🔤 Text", "text", toolGroup, false);
        ToggleButton eraserBtn = createToolToggleButton("🧽 Eraser", "eraser", toolGroup, false);

        // Tool buttons layout
        HBox toolRow1 = new HBox(5);
        HBox toolRow2 = new HBox(5);
        toolRow1.getChildren().addAll(pencilBtn, lineBtn);
        toolRow2.getChildren().addAll(rectBtn, circleBtn);

        VBox toolsBox = new VBox(10);
        toolsBox.getChildren().addAll(toolRow1, toolRow2, textBtn, eraserBtn);

        // Color picker
        Label colorLabel = new Label("Color");
        colorLabel.getStyleClass().add("section-label");
        ColorPicker colorPicker = new ColorPicker(currentColor);
        colorPicker.getStyleClass().add("color-picker");
        colorPicker.setOnAction(e -> {
            currentColor = colorPicker.getValue();
            gc.setStroke(currentColor);
            gc.setFill(currentColor);
        });

        // Brush size
        Label sizeLabel = new Label("Brush Size");
        sizeLabel.getStyleClass().add("section-label");
        Slider sizeSlider = new Slider(1, 50, brushSize);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.getStyleClass().add("size-slider");
        Label sizeValue = new Label(String.format("Size: %.1f", brushSize));
        sizeValue.getStyleClass().add("size-value");

        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brushSize = newVal.doubleValue();
            gc.setLineWidth(brushSize);
            sizeValue.setText(String.format("Size: %.1f", brushSize));
        });

        // Quick colors
        Label quickColorLabel = new Label("Quick Colors");
        quickColorLabel.getStyleClass().add("section-label");
        HBox quickColors = new HBox(5);
        quickColors.getChildren().addAll(
                createColorButton(Color.RED, "Red"),
                createColorButton(Color.GREEN, "Green"),
                createColorButton(Color.BLUE, "Blue"),
                createColorButton(Color.BLACK, "Black"),
                createColorButton(Color.WHITE, "White")
        );

        toolbar.getChildren().addAll(
                toolsLabel, toolsBox, createSeparator(),
                colorLabel, colorPicker, createSeparator(),
                sizeLabel, sizeSlider, sizeValue, createSeparator(),
                quickColorLabel, quickColors
        );

        return toolbar;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        return separator;
    }

    private Button createButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("button", styleClass);
        return btn;
    }

    private ToggleButton createToolToggleButton(String text, String tool, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().addAll("tool-button");
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.setOnAction(e -> setCurrentTool(tool));
        return btn;
    }

    private ToggleButton createToggleButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("toggle-button");
        return btn;
    }

    private Button createColorButton(Color color, String colorName) {
        Button btn = new Button();
        btn.setPrefSize(30, 30);
        btn.setStyle(String.format("-fx-background-color: #%s; -fx-border-color: #333;",
                color.toString().substring(2, 8)));
        btn.setTooltip(new Tooltip(colorName));
        btn.setOnAction(e -> {
            currentColor = color;
            gc.setStroke(currentColor);
            gc.setFill(currentColor);
        });
        return btn;
    }

    private void setupMouseEvents() {
        drawingCanvas.setOnMousePressed(this::handleMousePressed);
        drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
        drawingCanvas.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        drawing = true;

        if (currentTool.equals("text")) {
            addTextAtPosition(startX, startY);
            drawing = false;
        } else {
            gc.beginPath();
            gc.moveTo(startX, startY);

            if (currentTool.equals("rectangle") || currentTool.equals("circle")) {
                // For shapes, we'll draw on release
            } else {
                gc.stroke();
            }
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!drawing || currentTool.equals("text")) return;

        double x = event.getX();
        double y = event.getY();

        switch (currentTool) {
            case "pencil":
                gc.lineTo(x, y);
                gc.stroke();
                break;
            case "eraser":
                Color originalColor = currentColor;
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(brushSize * 3);
                gc.lineTo(x, y);
                gc.stroke();
                gc.setStroke(originalColor);
                gc.setLineWidth(brushSize);
                break;
            case "line":
                // Redraw canvas to show temporary line
                redrawTemporaryShape(x, y);
                break;
            case "rectangle":
            case "circle":
                // Redraw canvas to show temporary shape
                redrawTemporaryShape(x, y);
                break;
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!drawing) return;

        double endX = event.getX();
        double endY = event.getY();

        switch (currentTool) {
            case "line":
                gc.strokeLine(startX, startY, endX, endY);
                break;
            case "rectangle":
                double width = endX - startX;
                double height = endY - startY;
                gc.strokeRect(startX, startY, width, height);
                break;
            case "circle":
                double radiusX = Math.abs(endX - startX) / 2;
                double radiusY = Math.abs(endY - startY) / 2;
                double centerX = (startX + endX) / 2;
                double centerY = (startY + endY) / 2;
                gc.strokeOval(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);
                break;
        }

        drawing = false;
    }

    private void redrawTemporaryShape(double currentX, double currentY) {
        // This would require maintaining a drawing history for proper implementation
        // For simplicity, we'll just draw directly in the drag handler
    }

    private void setCurrentTool(String tool) {
        currentTool = tool;
        System.out.println("Tool set to: " + tool);
    }

    private void addImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                // Draw image at center of canvas
                double x = (drawingCanvas.getWidth() - image.getWidth() / 2) / 2;
                double y = (drawingCanvas.getHeight() - image.getHeight() / 2) / 2;
                gc.drawImage(image, x, y, image.getWidth() / 2, image.getHeight() / 2);
                showAlert("Success", "Image added successfully!");
            } catch (Exception e) {
                showError("Error loading image: " + e.getMessage());
            }
        }
    }

    private void addText() {
        addTextAtPosition(100, 100);
    }

    private void addTextAtPosition(double x, double y) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter text to add at position (" + (int)x + ", " + (int)y + "):");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            if (!text.trim().isEmpty()) {
                gc.setFont(Font.font("Arial", brushSize * 4));
                gc.fillText(text, x, y);
            }
        });
    }

    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        gc.setStroke(currentColor);
        gc.setFill(currentColor);

        if (gridEnabled) {
            drawGrid();
        }
    }

    private void toggleGrid(boolean show) {
        gridEnabled = show;
        if (show) {
            drawGrid();
            showAlert("Grid", "Grid enabled");
        } else {
            clearCanvas();
            showAlert("Grid", "Grid disabled");
        }
    }

    private void drawGrid() {
        double spacing = 20;
        Color originalColor = currentColor;
        double originalWidth = brushSize;

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        // Draw vertical lines
        for (double x = 0; x < drawingCanvas.getWidth(); x += spacing) {
            gc.strokeLine(x, 0, x, drawingCanvas.getHeight());
        }

        // Draw horizontal lines
        for (double y = 0; y < drawingCanvas.getHeight(); y += spacing) {
            gc.strokeLine(0, y, drawingCanvas.getWidth(), y);
        }

        // Restore original settings
        gc.setStroke(originalColor);
        gc.setLineWidth(originalWidth);
    }

    private void saveCanvas() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Whiteboard");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG files (*.jpg)", "*.jpg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showSaveDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(),
                        (int) drawingCanvas.getHeight());
                drawingCanvas.snapshot(null, writableImage);

                String filename = file.getName().toLowerCase();
                String format = "png"; // default

                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    format = "jpg";
                }

                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), format, file);
                showAlert("Success", "Canvas saved successfully!");
            } catch (Exception e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Image to Canvas");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(drawingCanvas.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                clearCanvas();
                gc.drawImage(image, 0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                showAlert("Success", "Image loaded successfully!");
            } catch (Exception e) {
                showError("Error loading image: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}