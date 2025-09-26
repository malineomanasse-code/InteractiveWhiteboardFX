module com.example.interactivewhiteboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing; // for SwingFXUtils
    requires java.desktop; // for ImageIO

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.example.interactivewhiteboard to javafx.fxml;
    exports com.example.interactivewhiteboard;
}
