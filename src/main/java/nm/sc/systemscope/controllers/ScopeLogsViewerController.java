package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller for the log file viewer. Responsible for loading and displaying the content of a log file.
 */
public class ScopeLogsViewerController extends BaseScopeController{
    @FXML private TextArea area;

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
}
