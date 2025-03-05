package nm.sc.systemscope;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nm.sc.systemscope.Controllers.SystemScopeController;

import java.io.IOException;

public class SystemScopeMain extends Application {
    private SystemScopeController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SystemScopeMain.class.getResource("SystemScopeMain-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1350,700);
        controller = fxmlLoader.getController();
        setStageParams(stage);
        stage.setScene(scene);
        stage.show();
    }

    private void setStageParams(Stage stage) {
        stage.setTitle("SystemScope");

        stage.setMaximized(true);
        stage.setResizable(true);

        stage.setMinHeight(700);
        stage.setMinWidth(1450);

        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setMaxWidth(screenBounds.getWidth());
        stage.setMaxHeight(screenBounds.getHeight());

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