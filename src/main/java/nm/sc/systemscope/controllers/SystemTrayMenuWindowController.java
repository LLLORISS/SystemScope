package nm.sc.systemscope.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.application.Platform;
import nm.sc.systemscope.modules.Benchmark;
import nm.sc.systemscope.modules.SystemTrayManager;

/**
 * Controller for the system tray menu window.
 * Handles user interactions such as restoring the main application window and exiting the application.
 */
public class SystemTrayMenuWindowController extends BaseScopeController {
    @FXML private Button showButton;
    @FXML private Button benchmarkButton;
    @FXML private Button exitButton;

    /**
     * Initializes the controller with references to the primary stage and menu stage.
     * Sets up event handlers for the buttons.
     *
     * @param primaryStage The main application window.
     * @param menuStage    The tray menu window.
     */
    public void initialize(Stage primaryStage, Stage menuStage) {

        if(Benchmark.getBenchmarkStarted()){
            benchmarkButton.setText("Зупинити бенчмаркУ");
        }

        showButton.setOnAction(e -> {
            primaryStage.show();
            menuStage.hide();
            SystemTrayManager.removeTrayIcon();
        });

        benchmarkButton.setOnAction(e->{
            if(!Benchmark.getBenchmarkStarted() ){
                Benchmark.startBenchmark();
                Platform.runLater(() -> benchmarkButton.setText("Зупинити бенчмарк"));
            }
            else{
                Benchmark.stopBenchmark();
                Platform.runLater(() -> benchmarkButton.setText("Бенчмарк"));
            }
        });

        exitButton.setOnAction(e -> new Thread(() -> {
            Benchmark.stopBenchmark();

            Platform.runLater(() -> {
                Platform.exit();
                System.exit(0);
            });
        }).start());
    }
}
