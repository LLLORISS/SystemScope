package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nm.sc.systemscope.modules.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.TextField;
import java.awt.Desktop;

/**
 * Controller class for handling the logs in the SystemScope application.
 * It provides methods to manage and interact with the logs, including opening, deleting, and displaying logs.
 * This class is responsible for managing the user interface of the logs list view, including interactions with the list and log files.
 */
public class LogsListViewController extends BaseScopeController {
    @FXML private ScopeListView<ScopeBenchLog> logsListView;
    @FXML private TextField searchField;

    private ObservableList<ScopeBenchLog> observableLogsList;
    private final String logsFolderPath = "src/main/data/logs";

    /**
     * Initializes the controller by populating the logs list and setting up a listener for the search field.
     */
    @FXML public void initialize(){
        List<ScopeBenchLog> logs = DataStorage.getBenchLogs();

        observableLogsList = FXCollections.observableArrayList(logs);

        Platform.runLater(() -> logsListView.setItems(observableLogsList));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try{
                filterProcesses(newValue);
            }
            catch(IOException e){
                ScopeLogger.logError("Error while filtering processes: ", e);
            }
        });
    }

    /**
     * Opens the selected log in the log viewer.
     */
    @FXML public void openLogBtnClicked(){
       ScopeBenchLog selected = logsListView.getSelectionModel().getSelectedItem();
       if(selected != null){
           String filePath = selected.getAbsolutePath();
           openLogViewer(filePath);
       }
    }

    /**
     * Deletes the selected log file and updates the list.
     * If the deletion fails, an error alert is displayed.
     */
    @FXML public void removeLogBtnClicked(){
        ScopeBenchLog selected = logsListView.getSelectionModel().getSelectedItem();
        if(selected != null){
            if(deleteSelectedLog(selected.getAbsolutePath())){
                Platform.runLater(this::updateList);
            }
            else{
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Не вдалося видалити файл");
                alert.showAndWait();
            }
        }
    }

    /**
     * Opens the logs directory in the default file explorer.
     * If the directory does not exist or cannot be opened, an error alert is displayed.
     */
    @FXML public void openDirBtnClicked() {
        if (Desktop.isDesktopSupported()) {
            try {
                File folder = new File(this.logsFolderPath);

                if (folder.exists() && folder.isDirectory()) {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().open(folder);
                        } catch (IOException e) {
                            Platform.runLater(() -> {
                                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Помилка при відкритті папки: " + e.getMessage());
                                alert.showAndWait();
                            });
                        }
                    }).start();
                } else {
                    Platform.runLater(() -> {
                        ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Вказана папка не існує або це не директорія");
                        alert.showAndWait();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Помилка: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        } else {
            Platform.runLater(() -> {
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Не підтримується вашою операційною системою");
                alert.showAndWait();
            });
        }
    }

    /**
     * Prompts the user to confirm the deletion of all log files in the logs directory.
     * If confirmed, all files are deleted.
     * If the operation is canceled, no files are deleted.
     */
    @FXML public void removeAllLogFiles(){
        ScopeAlert alert = new ScopeAlert(Alert.AlertType.CONFIRMATION, "Ви впевнені що хочете видалити всі файли логів?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                File directory = new File(this.logsFolderPath);

                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile()) {
                                if (file.delete()) {
                                    ScopeLogger.logInfo("File {} was deleted.", file.getName());
                                } else {
                                    ScopeLogger.logError("Error while deleting file {}", file.getName());
                                }
                            }
                        }
                        Platform.runLater(this::updateList);
                    } else {
                        ScopeLogger.logError("Failed to retrieve files from a directory.");
                    }
                } else {
                    ScopeLogger.logError("The specified path is not a directory.");
                }
            } else {
                ScopeLogger.logError("The delete operation is canceled");
            }
        });
    }

    /**
     * Updates the log list with the latest logs from the data storage.
     */
    @FXML public void updateLogsBtnClicked(){
        this.updateList();
    }

    /**
     * Deletes the log file at the specified file path.
     *
     * @param filePath The path to the log file to be deleted.
     * @return true if the file was successfully deleted, false otherwise.
     */
    private boolean deleteSelectedLog(String filePath){
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * Opens the log file in the log viewer.
     *
     * @param filePath The path to the log file to be opened.
     */
    private void openLogViewer(String filePath){
        try{
            ScopeLoaderFXML loader = new ScopeLoaderFXML("ScopeLogsViewer-view.fxml");

            ScopeLogsViewerController controller = (ScopeLogsViewerController) loader.getController();
            controller.loadLogFile(filePath);

            loader.getStage().setTitle("Перегляд");
            loader.show();
        }
        catch(IOException e){
            ScopeLogger.logError("Error opening log viewer for file: {}", filePath, e);
        }
    }

    /**
     * Updates the log list with the latest data.
     */
    private void updateList(){
        observableLogsList = FXCollections.observableArrayList(DataStorage.getBenchLogs());
        logsListView.setItems(observableLogsList);
    }

    /**
     * Filters the logs based on the search input and updates the logs list accordingly.
     *
     * @param searchInput The text entered the search field.
     * @throws IOException If there is an error during filtering.
     */
    private void filterProcesses(String searchInput) throws IOException {
        List<ScopeBenchLog> filtered = ScopeListView.searchItems(searchInput, DataStorage.getBenchLogs());

        Platform.runLater(()->{
            observableLogsList.clear();
            observableLogsList.addAll(filtered);
        });
    }
}
