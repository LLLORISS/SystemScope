package nm.sc.systemscope;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nm.sc.systemscope.controllers.SystemScopeController;
import nm.sc.systemscope.modules.DataStorage;
import nm.sc.systemscope.modules.SystemTrayManager;
import nm.sc.systemscope.modules.Theme;

import java.io.IOException;

/**
 * The main class to run the program
 */
public class SystemScopeMain extends Application {
    private SystemScopeController controller;

    /**
     * Starts the JavaFX application.
     *
     * @param stage The primary stage for this application.
     * @throws IOException If there is an issue loading the FXML file.
     */
    @Override
    public void start(Stage stage) throws IOException {
        try {
            DataStorage.saveThemeToConfig(Theme.DARK);
            FXMLLoader fxmlLoader = new FXMLLoader(SystemScopeMain.class.getResource("SystemScopeMain-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            controller = fxmlLoader.getController();
            controller.setScene(scene);
            controller.applyTheme();
            setStageParams(stage);
            stage.setScene(scene);

            SystemTrayManager.addToSystemTray(stage);
            stage.show();
        }
        catch(IOException e){
            e.printStackTrace();
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
     * Stops the application, ensuring proper cleanup.
     *
     * @throws Exception If an error occurs during shutdown.
     */
    @Override
    public void stop() throws Exception {
       if (controller != null){
           controller.shutdown();
        }
        super.stop();
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