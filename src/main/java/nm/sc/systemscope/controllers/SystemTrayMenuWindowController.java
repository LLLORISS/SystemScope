package nm.sc.systemscope.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.application.Platform;
import nm.sc.systemscope.modules.Benchmark;
import nm.sc.systemscope.modules.ScopeLoaderFXML;
import nm.sc.systemscope.modules.SystemTrayManager;

import java.io.IOException;

/**
 * Controller for the system tray menu window.
 * Handles user interactions such as restoring the main application window and exiting the application.
 */
public class SystemTrayMenuWindowController extends BaseScopeController {
    @FXML private Button showButton, AIButton, benchmarkButton, exitButton;
    BaseScopeController aiChatController;

    /**
     * Initializes the controller with references to the primary stage and menu stage.
     * Sets up event handlers for the buttons, including showing the main window, opening the AI chat interface,
     * starting/stopping the benchmark, and exiting the application.
     *
     * @param primaryStage The main application window.
     * @param menuStage    The tray menu window.
     */
    public void initialize(Stage primaryStage, Stage menuStage) {

        if(Benchmark.getBenchmarkStarted()){
            benchmarkButton.setText("Зупинити бенчмарк");
        }

        showButton.setOnAction(e -> {
            primaryStage.show();
            menuStage.hide();
            SystemTrayManager.removeTrayIcon();
        });

        AIButton.setOnAction( e -> {
            ScopeLoaderFXML loader;
            try {
                loader = new ScopeLoaderFXML("Chat-view.fxml");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            aiChatController = (AiChatController) loader.getController();
            aiChatController.setStage(this.stage);

            Stage chatStage = loader.getStage();
            chatStage.setResizable(false);
            chatStage.setTitle("ScopeHelper");

            chatStage.setY(0);

            chatStage.setAlwaysOnTop(true);
            chatStage.show();
        });

        benchmarkButton.setOnAction(e->{
            if(!Benchmark.getBenchmarkStarted() ){
                Benchmark.startBenchmark();
                Platform.runLater(() -> benchmarkButton.setText("Зупинити бенчмарк"));
            }
            else {
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
