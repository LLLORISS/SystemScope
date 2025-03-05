package nm.sc.systemscope.Controllers;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import javafx.scene.control.Label;

public class BenchSelectorController {
    @FXML
    private Label selectedFileLabel;
    @FXML
    private Button selectButton;
    @FXML
    private Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    public void onSelectGameBtnClicked() {
        FileChooser fileChooser = new FileChooser();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Game Executable Files", "*.exe"));
        } else if (System.getProperty("os.name").toLowerCase().contains("nix") ||
                System.getProperty("os.name").toLowerCase().contains("nux") ||
                System.getProperty("os.name").toLowerCase().contains("mac")) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Shell Script Files", "*.sh"));
        }

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String gamePath = selectedFile.getAbsolutePath();

            selectedFileLabel.setText(selectedFile.getName());

            System.out.println("Вибрано гру: " + gamePath);
        } else {
            System.out.println("Файл не вибрано.");
        }
    }

    @FXML
    public void onStartBenchmarkBtnClicked(){

    }
}
