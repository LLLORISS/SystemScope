package nm.sc.systemscope;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SystemScopeMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SystemScopeMain.class.getResource("SystemScopeMain-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1350,700);
        setStageParams(stage);
        stage.setScene(scene);
        stage.show();
    }

    private void setStageParams(Stage stage){
        stage.setTitle("SystemScope");

        stage.setMaximized(true);
        stage.setResizable(true);

        stage.setMinHeight(700);
        stage.setMinWidth(1350);

        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setMaxWidth(screenBounds.getWidth());
        stage.setMaxHeight(screenBounds.getHeight());
    }

    public static void main(String[] args) {
        launch();
    }
}