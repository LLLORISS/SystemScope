package nm.sc.systemscope;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nm.sc.systemscope.modules.*;

import java.io.IOException;

/**
 * The main class to run the program
 */
public class SystemScopeMain extends Application {
    /**
     * Starts the JavaFX application.
     *
     * @param stage The primary stage for this application.
     * @throws IOException If there is an issue loading the FXML file.
     */
    @Override public void start(Stage stage) throws IOException {
        try {
            DataStorage.saveThemeToConfig(Theme.DARK);

            ScopeLoaderFXML loader = new ScopeLoaderFXML("SystemScopeMain-view.fxml");
            loader.setStage(stage);
            setStageParams(stage);

            stage.setMaximized(true);

            SystemTrayManager.addToSystemTray(stage);

            loader.show();
        }
        catch(IOException e){
            ScopeLogger.logError("Error during application startup: {}", e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Configures the main stage parameters such as size, title, and close event handling.
     *
     * @param stage The primary stage to configure.
     */
    private void setStageParams(Stage stage) {
        stage.setTitle("SystemScope");

        stage.setMaximized(true);
        stage.setResizable(false);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
    }

    /**
     * The main entry point of the application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}