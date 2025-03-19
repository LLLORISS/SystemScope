package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nm.sc.systemscope.*;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.Scene;

import java.io.IOException;
import javafx.collections.ObservableList;
import nm.sc.systemscope.modules.*;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that manages the main SystemScope window
 */
public class SystemScopeController {
    @FXML
    private Label InfoPC;
    @FXML
    private Label CPU;
    @FXML
    private Label GPU;
    @FXML
    private Label RAM;
    @FXML
    private Label DiskStorage;
    @FXML
    private Label TempCPU;
    @FXML
    private Label TempGPU;
    @FXML
    private Label FansSpeed;
    @FXML
    private Button benchBtn;
    @FXML
    private ListView<ProcessInfo> processList;
    private ObservableList<ProcessInfo> observableList;
    @FXML
    private TextField searchField;

    private ScopeChartsController scopeChartsController;

    private Theme theme;

    private Scene scene;

    private BenchWindow benchWindow = null;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * A method that initializes initial values
     */
    @FXML
    public void initialize() {
        theme = DataStorage.loadThemeFromConfig();
        applyTheme();
        Platform.runLater(() -> {
            InfoPC.setText(SystemInformation.getComputerName());
            CPU.setText(SystemInformation.getProcessorName());
            GPU.setText(SystemInformation.getGraphicCards());
            RAM.setText(SystemInformation.getRAM());
            DiskStorage.setText(SystemInformation.getDiskStorage());
            updateTemperature();
        });

        try{
            List<ProcessInfo> processes = ProcessInfoService.getRunningProcesses();

            observableList = FXCollections.observableArrayList(processes);
            processList.setItems(observableList);

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    filterProcesses(newValue);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            processList.getItems().setAll(processes);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        scheduler.scheduleAtFixedRate(this::updateTemperature, 0, 2, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * A method that filters all similar processes by a given name
     * @param searchInput input data for the search
     * @throws IOException if an error occurs during process filtering
     */
    private void filterProcesses(String searchInput) throws IOException {
        List<ProcessInfo> filtered = ProcessInfoService.searchProcess(searchInput);

        observableList.clear();
        observableList.addAll(filtered);
    }

    /**
     * Creates and opens a window with charts
     * @throws IOException if an error occurs when opening the window
     */
    @FXML
    public void onShowChartsClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource("ScopeCharts-view.fxml"));
        Parent root = loader.load();

        scopeChartsController = loader.getController();

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        scopeChartsController.setScene(scene);
        scopeChartsController.applyTheme();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Графіки");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.showAndWait();
    }

    /**
     * A method that forcibly terminates the selected process
     */
    @FXML
    public void onKillSelectedProcessClicked(){
        ProcessInfo selectedProcess = processList.getSelectionModel().getSelectedItem();
        if(selectedProcess != null){
            try{
                if(ProcessInfoService.killProcess(selectedProcess.getPid())){
                    processList.getItems().remove(selectedProcess);
                }
            }
            catch(IOException | InterruptedException e){
                e.printStackTrace();
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Не вдалося завершити процес");
                alert.showAndWait();
            }
        }
        else{
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Виберіть процес для завершення");
        }
    }

    /**
     * Updates the list of processes
     */
    @FXML
    public void onRefreshProcessesBtnClicked(){
        updateProcessList();
    }

    /**
     * Changes the theme color to the opposite (dark or light)
     */
    @FXML
    public void onThemeToggleClicked(){
        if(theme.equals(Theme.DARK)){
            theme = Theme.LIGHT;
            DataStorage.saveThemeToConfig(Theme.LIGHT);
        }
        else if(theme.equals(Theme.LIGHT)){
            theme = Theme.DARK;
            DataStorage.saveThemeToConfig(Theme.DARK);
        }
        applyTheme();
    }

    /**
     * A method that launches a window with a file selection for the benchmark and controls the benchmarking process
     */
    @FXML
    public void onBenchClicked(){
        try {
            if (benchWindow == null && !Benchmark.getBenchmarkStarted()) {
                FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource("BenchSelector-view.fxml"));
                Parent root = loader.load();

                BenchSelectorController controller = loader.getController();

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                controller.setStage(stage);
                stage.setTitle("Вибір гри");
                stage.showAndWait();

                System.out.println("Process name " + controller.getSelectedFile());
                String selectedFile = controller.getSelectedFile();
                Benchmark.setAbsolutePath(selectedFile);

                if (selectedFile == null || selectedFile.isEmpty()) {
                    System.out.println("Файл не вибрано, скасовано.");
                    return;
                }

                new Thread(() -> {
                    Benchmark.setBenchmarkStarted(true);
                    if (launchFile(System.getProperty("os.name").toLowerCase())) {
                        benchWindow = new BenchWindow();
                        benchWindow.initialize();

                        waitForFileToClose();

                        Platform.runLater(() -> {
                            if (benchWindow != null) {
                                benchWindow.close();
                                benchWindow = null;
                            }

                            benchBtn.setText("Запустити бенчмарк");
                            benchBtn.getStyleClass().removeAll("main-button-stop");
                            benchBtn.getStyleClass().add("main-button");
                        });
                    }
                }).start();
            }
            else{
                Benchmark.setBenchmarkStarted(false);

                String processName = Benchmark.getProcessName();

                if(processName != null && !processName.isEmpty()){
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command("bash", "-c", "pgrep -f \"" + processName + "\"");

                    try{
                        Process process = processBuilder.start();
                        process.waitFor();

                        String processID = new String(process.getInputStream().readAllBytes()).trim();

                        if (!processID.isEmpty()) {
                            ProcessBuilder killProcessBuilder = new ProcessBuilder("bash", "-c", "kill -9 " + processID);
                            killProcessBuilder.start();
                        } else {
                            System.out.println("Процес не знайдений.");
                        }
                        Benchmark.clearInfo();
                    }
                    catch(IOException | InterruptedException e){
                        e.printStackTrace();
                    }

                }

                Platform.runLater(() -> {
                    if (benchWindow != null) {
                        benchWindow.close();
                        benchWindow = null;
                    }

                    benchBtn.setText("Запустити бенчмарк");
                    benchBtn.getStyleClass().removeAll("main-button-stop");
                    benchBtn.getStyleClass().add("main-button");
                });
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Method that runs the selected file for the benchmark
     * @param os name of the operating system
     * @return the result of running the file
     */
    private boolean launchFile(String os){
        try{
            ProcessBuilder processBuilder;

            String selectedFile = Benchmark.getAbsolutePath();

            if(os.contains("win")){
                processBuilder = new ProcessBuilder(selectedFile);
            }
            else{
                processBuilder = new ProcessBuilder("bash", "-c", "chmod +x \"" + selectedFile + "\" && \"" + selectedFile + "\"");
            }

            Platform.runLater(() -> {
                benchBtn.setText("Зупинити бенчмарк");

                benchBtn.getStyleClass().removeAll("main-button");
                benchBtn.getStyleClass().add("main-button-stop");
            });

            Benchmark.setBenchmarkStarted(true);

            processBuilder.inheritIO();
            Process process = processBuilder.start();

            Thread.sleep(5000);
            return process.isAlive();
        }
        catch(Exception e){
            System.err.println("Помилка запуску гри: " + e.getMessage());
            Platform.runLater(() -> {
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Перевірте правильність вибраного файлу.");
                alert.showAndWait();
            });
            return false;
        }
    }

    /**
     * A method that monitors the current status of the game, the method ends when the benchmark file is closed
     */
    private void waitForFileToClose(){
        try{
            String selectedFile = Benchmark.getAbsolutePath();
            String processName = selectedFile.substring(selectedFile.lastIndexOf("/") + 1).replace(".sh", "");
            Benchmark.setProcessName(processName);
            boolean isRunning = true;

            while(isRunning){
                Thread.sleep(3000);
                isRunning = isProcessRunning(processName, System.getProperty("os.name").toLowerCase());
            }

            System.out.println("Гра завершена, закриваємо бенчмарк...");
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * A method that checks if the process is running in the operating system
     * @param processName process name
     * @param os operating system name
     * @return running status
     */
    private boolean isProcessRunning(String processName, String os){
        try{
            Process process;
            if(os.contains("win")){
                process = new ProcessBuilder("tasklist").start();
            }
            else{
                process = new ProcessBuilder("bash", "-c", "pgrep -c -f \"" + processName + "\"").start();
            }

            String output = new String(process.getInputStream().readAllBytes());
            return Integer.parseInt(output.trim()) > 0;
        }
        catch(Exception e){
            return false;
        }
    }

    /**
     * A method that creates a dialog box and displays additional information about RAM.
     * The method may not work if you do not have the appropriate access rights to system folders.
     */
    @FXML
    public void showRamInfo(){
        String info = SystemInformation.getRamInfo();

        ScopeAlert alert = new ScopeAlert(Alert.AlertType.INFORMATION, info);
        alert.showAndWait();
    }

    /**
     * A method that determines the color relative to the input data on topics
     * @param temperature temperature indicator
     * @return Сolor relative to the temperature (temperature <= 70° - Green, temperature >= 70 and <= 90 - Orange, temperature > 90 - RED
     */
    private static Paint getColorByZone(double temperature) {
        if (temperature <= 70) {
            return Color.GREEN;
        } else if (temperature <= 90) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    /**
     * A method that converts a string with temperature to a number by removing extra characters
     * @param tempString input string
     * @return Temperature value
     */
    private double parseTemperature(String tempString) {
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(tempString);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        } else {
            throw new NumberFormatException("Не вдалося знайти числове значення в: " + tempString);
        }
    }

    /**
     * A method that applies a style file to the current window
     */
    public void applyTheme(){
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
     * A method that updates the current values of components
     */
    private void updateTemperature() {
        String tempCPUString = SystemInformation.getTemperatureCPU();
        String tempGPUString = SystemInformation.getTemperatureGPU();
        String Fans = SystemInformation.getFansRPM();

        try {
            double tempCPU = parseTemperature(tempCPUString);
            double[] gpuTemps = parseMultipleTemperatures(tempGPUString);

            Platform.runLater(() -> {
                TempCPU.setText(tempCPUString);
                TempCPU.setTextFill(getColorByZone(tempCPU));

                String formattedGPU = gpuTemps.length > 1
                        ? String.format("Intel GPU: %.1f °C\nNVIDIA GPU: %.1f °C", gpuTemps[0], gpuTemps[1])
                        : String.format("GPU: %.1f °C", gpuTemps[0]);
                TempGPU.setText(formattedGPU);

                TempGPU.setTextFill(getColorByZone(Math.max(gpuTemps[0], gpuTemps.length > 1 ? gpuTemps[1] : gpuTemps[0])));

                FansSpeed.setText(Fans);
                if (Fans.equals("Не знайдено")) {
                    FansSpeed.setTextFill(Color.ORANGE);
                } else {
                    FansSpeed.setTextFill(Color.GREEN);
                }
            });

        } catch (NumberFormatException e) {
            System.err.println("Помилка парсингу температури: " + e.getMessage());
        }
    }

    /**
     * A method that converts the temperature of several video cards to numbers
     * @param tempString input string with temperatures
     * @return An array of numbers with temperatures
     */
    private double[] parseMultipleTemperatures(String tempString) {
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(tempString);

        double[] temperatures = new double[2];
        int index = 0;

        while (matcher.find() && index < 2) {
            temperatures[index++] = Double.parseDouble(matcher.group());
        }

        if (index < 2) {
            throw new NumberFormatException("Знайдено менше двох температур у: " + tempString);
        }

        return temperatures;
    }

    /**
     * A method that updates processes list
     */
    private void updateProcessList() {
        try {
            List<ProcessInfo> processes = ProcessInfoService.getRunningProcesses();

            observableList.clear();

            observableList.addAll(processes);

            processList.setItems(observableList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that sets the scene
     * @param scene scene for display
     */
    public void setScene(Scene scene){
        this.scene = scene;
    }

    /**
     * A method that closes all necessary events and scheduled operations
     */
    public void shutdown() {
        if(scopeChartsController != null){
            scopeChartsController.stopBackgroundUpdate();
        }
        DataStorage.cleanDataStorage();
        scheduler.shutdown();
    }
}