package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import nm.sc.systemscope.SystemScopeMain;
import nm.sc.systemscope.modules.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javafx.scene.Scene;
import java.util.List;
import javafx.scene.control.TextField;
import java.awt.Desktop;

/**
 * Controller class for handling the logs in the SystemScope application.
 * It provides methods to manage and interact with the logs, including opening, deleting, and displaying logs.
 * This class is responsible for managing the user interface of the logs list view, including interactions with the list and log files.
 */
public class LogsListViewController {
    @FXML private ScopeListView<ScopeBenchLog> logsListView;
    @FXML private TextField searchField;

    private Theme theme;
    private Scene scene;
    private ObservableList<ScopeBenchLog> observableLogsList;
    private final String logsFolderPath = "src/main/data/logs";

    /**
     * Initializes the controller by populating the logs list and setting up a listener for the search field.
     */
    @FXML public void initialize(){
        List<ScopeBenchLog> logs = DataStorage.getBenchLogs();

        observableLogsList = FXCollections.observableArrayList(logs);

        Platform.runLater(() -> {
            logsListView.setItems(observableLogsList);
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try{
                filterProcesses(newValue);
            }
            catch(IOException e){
                e.printStackTrace();
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
                updateList();
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
                                    System.out.println("Файл " + file.getName() + " був видалений.");
                                } else {
                                    System.out.println("Не вдалося видалити файл " + file.getName());
                                }
                            }
                        }
                    } else {
                        System.out.println("Не вдалося отримати файли з директорії.");
                    }
                } else {
                    System.out.println("Вказаний шлях не є директорією.");
                }
            } else {
                System.out.println("Операція видалення скасована.");
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
            FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource("ScopeLogsViewer-view.fxml"));
            Parent root = loader.load();

            ScopeLogsViewerController controller = loader.getController();
            controller.loadLogFile(filePath);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            controller.setScene(scene);
            stage.setTitle("Перегляд");
            stage.setScene(scene);
            stage.show();
        }
        catch(IOException e){
            e.printStackTrace();
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
     * Applies the selected theme to the scene.
     * The theme is loaded from the configuration.
     */
    private void applyTheme(){
        theme = DataStorage.loadThemeFromConfig();

        if (this.scene != null) {
            this.scene.getStylesheets().clear();

            String themeStyleFile = "";

            if (theme == Theme.DARK) {
                themeStyleFile = "/nm/sc/systemscope/CSS/styles.css";
            } else if (theme == Theme.LIGHT) {
                themeStyleFile = "/nm/sc/systemscope/CSS/light-styles.css";
            }
            this.scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(themeStyleFile)).toExternalForm());
        }
    }

    /**
     * Filters the logs based on the search input and updates the logs list accordingly.
     *
     * @param searchInput The text entered in the search field.
     * @throws IOException If there is an error during filtering.
     */
    private void filterProcesses(String searchInput) throws IOException {
        List<ScopeBenchLog> filtered = ScopeListView.searchItems(searchInput, DataStorage.getBenchLogs());

        Platform.runLater(()->{
            observableLogsList.clear();
            observableLogsList.addAll(filtered);
        });
    }

    /**
     * Sets the scene for the controller.
     *
     * @param scene The scene to be set for this controller.
     */
    public void setScene(Scene scene){
        this.scene = scene;

        applyTheme();
    }
}
