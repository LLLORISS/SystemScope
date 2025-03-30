package nm.sc.systemscope.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.control.Label;

import nm.sc.systemscope.modules.DataStorage;
import nm.sc.systemscope.modules.ScopeAlert;
import nm.sc.systemscope.modules.ScopeLogger;

/**
 * A class that creates a file selection window for the benchmark
 */
public class BenchSelectorController extends BaseScopeController {
    @FXML private Label selectedFileLabel;
    @FXML private Boolean startClicked = false;
    @FXML private TextField nameLogTextField;

    private String selectedFile;
    private String benchmarkName;

    /**
     * A method that checks the selected file and sets the path to the selected file
     */
    @FXML public void onSelectGameBtnClicked() {
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
            ScopeLogger.logWarn("File is not selected");
        }
    }

    /**
     * A method that checks the selected file and closes the window
     */
    @FXML public void onStartBenchmarkBtnClicked(){
        if(selectedFile != null && !selectedFile.isEmpty() && nameLogTextField.getText() != null
                && !nameLogTextField.getText().isEmpty() && !DataStorage.isLogFileExist(nameLogTextField.getText())){
            this.startClicked = true;
            this.benchmarkName = nameLogTextField.getText();
            this.close();
        }
        else if(nameLogTextField.getText() == null || nameLogTextField.getText().isEmpty()){
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Для запуску тестування необхідно ввести назву бенчмарку");
            alert.showAndWait();
        }
        else if(DataStorage.isLogFileExist(nameLogTextField.getText())){
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Лог з даною назвою вже існує");
            alert.showAndWait();
        }
        else{
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Для запуску тестування необхідно вибрати допустимий файл");
            alert.showAndWait();
        }
    }

    /**
     * A method that returns status of the button clicked
     * @return status of button
     */
    public boolean getStartClicked(){
        return this.startClicked;
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

    /**
     * A method that return the name of Benchmark
     * @return Name of benchmark
     */
    public String getBenchmarkName() { return this.benchmarkName; }

}
