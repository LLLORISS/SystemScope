package nm.sc.systemscope.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    private Stage stage;
    @FXML
    private String selectedFile;

    public void setStage(Stage stage){
        this.stage = stage;
        this.selectedFile = "";
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
            this.selectedFile = selectedFile.getAbsolutePath();

            selectedFileLabel.setText(selectedFile.getName());
        } else {
            System.out.println("Файл не вибрано.");
        }
    }

    @FXML
    public void onStartBenchmarkBtnClicked(){
        if(selectedFile != null && !selectedFile.isEmpty()){
            this.close();
        }
        else{
            SystemScopeController.showMessage(Alert.AlertType.ERROR, "Для запуску тестування необхідно вибрати допустимий файл");
        }
    }

    public void close(){
        if(stage != null){
            stage.close();
        }
    }

    public String getSelectedFile(){
        return this.selectedFile;
    }
}
