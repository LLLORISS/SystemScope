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
import nm.sc.systemscope.modules.Theme;

import java.io.IOException;

public class SystemScopeMain extends Application {
    private SystemScopeController controller;

    @Override

    public void start(Stage stage) throws IOException {
        DataStorage.saveThemeToConfig(Theme.DARK);
        FXMLLoader fxmlLoader = new FXMLLoader(SystemScopeMain.class.getResource("SystemScopeMain-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        controller = fxmlLoader.getController();
        controller.setScene(scene);
        controller.applyTheme();
        setStageParams(stage);
        stage.setScene(scene);
        stage.show();
    }

    private void setStageParams(Stage stage) {
        stage.setTitle("SystemScope");

        stage.setMaximized(true);
        stage.setResizable(false);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        stage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.shutdown();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.shutdown();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}