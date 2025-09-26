package com.example.interactivewhiteboard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.embed.swing.SwingFXUtils; // for saving canvas
import javax.imageio.ImageIO; // for saving PNG
import java.io.File;


public class HelloApplication extends Application {
    private Color currentColor = Color.BLACK;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Interactive Whiteboard");

        // Layout
        BorderPane root = new BorderPane();

        // Canvas
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.setCenter(canvas);

        // Toolbar
        HBox toolbar = new HBox(10);
        Button clearBtn = new Button("Clear");
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        Button addImageBtn = new Button("Add Image");
        Button saveBtn = new Button("Save");
        toolbar.getChildren().addAll(clearBtn, colorPicker, addImageBtn, saveBtn);
        root.setTop(toolbar);

        // Drawing
        canvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        });
        canvas.setOnMouseDragged(e -> {
            gc.setStroke(currentColor);
            gc.setLineWidth(3);
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });

        // Color picker
        colorPicker.setOnAction(e -> currentColor = colorPicker.getValue());

        // Clear canvas
        clearBtn.setOnAction(e -> gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()));

        // Add image
        addImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                javafx.scene.image.Image image = new javafx.scene.image.Image(file.toURI().toString());
                gc.drawImage(image, 100, 100, 200, 150);
            }
        });

        // Save canvas
        saveBtn.setOnAction(e -> {
            WritableImage image = canvas.snapshot(null, null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Scene
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
