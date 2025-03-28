package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.Scene;
import nm.sc.systemscope.modules.ScopeTheme;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller for the log file viewer. Responsible for loading and displaying the content of a log file.
 */
public class ScopeLogsViewerController {
    @FXML private TextArea area;

    private Scene scene;
    private ScopeTheme theme;

    /**
     * Initializes the controller. This method is invoked after the FXML is loaded,
     * but no functionality is set for initialization here.
     */
    @FXML private void initialize(){
        Platform.runLater(() -> {
            theme = new ScopeTheme(scene);
            theme.applyTheme();
        });
    }

    /**
     * Loads the content of a log file into the TextArea for display.
     * If the file exists and is readable, its content will be loaded.
     * If the file does not exist, a message will be displayed indicating the file is not found.
     *
     * @param filePath the path of the log file to be loaded.
     */
    public void loadLogFile(String filePath){
        Path file = Path.of(filePath);
        if (Files.exists(file)) {
            try {
                String content = Files.readString(file);
                Platform.runLater(() -> area.setText(content));
            } catch (IOException e) {
                Platform.runLater(() -> area.setText("Помилка при відкритті файлу: " + e.getMessage()));
            }
        } else {
            area.setText("Файл не знайдено!");
        }
    }

    /**
     * Sets the scene for this controller and applies the appropriate theme.
     *
     * @param scene the scene to be set for this controller.
     */
    public void setScene(Scene scene){
        this.scene = scene;
    }
}
