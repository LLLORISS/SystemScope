package nm.sc.systemscope.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import javafx.scene.control.Label;
import nm.sc.systemscope.modules.ScopeAlert;

/**
 * A class that creates a file selection window for the benchmark
 */
public class BenchSelectorController {
    @FXML
    private Label selectedFileLabel;

    private Stage stage;
    @FXML
    private String selectedFile;

    /**
     * A method that sets the current stage
     */
    public void setStage(Stage stage){
        this.stage = stage;
        this.selectedFile = "";
    }

    /**
     * A method that checks the selected file and sets the path to the selected file
     */
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
            this.selectedFile = selectedFile.getAbsolutePath();

            selectedFileLabel.setText(selectedFile.getName());
        } else {
            System.out.println("Файл не вибрано.");
        }
    }

    /**
     * A method that checks the selected file and closes the window
     */
    @FXML
    public void onStartBenchmarkBtnClicked(){
        if(selectedFile != null && !selectedFile.isEmpty()){
            this.close();
        }
        else{
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Для запуску тестування необхідно вибрати допустимий файл");
            alert.showAndWait();
        }
    }

    /**
     * A method that closes the window
     */
    public void close(){
        if(stage != null){
            stage.close();
        }
    }

    /**
     * A method that returns the path to the selected file
     * @return Path to the selected file
     */
    public String getSelectedFile(){
        return this.selectedFile;
    }
}
