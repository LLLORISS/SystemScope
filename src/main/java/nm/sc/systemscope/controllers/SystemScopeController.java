package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.io.IOException;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import nm.sc.systemscope.ScopeHardware.ScopeBattery;
import nm.sc.systemscope.ScopeHardware.ScopeCentralProcessor;
import nm.sc.systemscope.ScopeHardware.ScopeMotherBoard;
import nm.sc.systemscope.ScopeHardware.ScopeUsbDevice;
import nm.sc.systemscope.modules.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class that manages the main SystemScope window
 */
public class SystemScopeController extends BaseScopeController {
    @FXML private Label InfoPC;
    @FXML private Label Baseboard;
    @FXML private Label CPU;
    @FXML private Label GPU;
    @FXML private Label RAM;
    @FXML private Label DiskStorage;
    @FXML private Label BatteryCapacity;
    @FXML private Label TempCPU;
    @FXML private Label TempGPU;
    @FXML private Label FansSpeed;
    @FXML private Button benchBtn;
    @FXML private Button themeToggleBtn;
    @FXML private TextField searchField;
    @FXML private ScopeListView<ProcessInfo> processList;
    @FXML private ScopeListView<ScopeUsbDevice> devicesList;

    private ObservableList<ProcessInfo> observableList;
    private ObservableList<ScopeUsbDevice> observableDevicesList;
    private ScopeChartsController scopeChartsController;
    private AiChatController aiChatController;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * A method that initializes initial values
     */
    @FXML public void initialize() {
        Platform.runLater(() -> {
            scene = themeToggleBtn.getScene();

            InfoPC.setText(SystemInformation.getComputerName());
            String baseboardString = ScopeMotherBoard.getManufacturer() + " "
                    + ScopeMotherBoard.getModel() + " " + ScopeMotherBoard.getVersion();
            Baseboard.setText(baseboardString);
            CPU.setText(ScopeCentralProcessor.getProcessorName());
            GPU.setText(SystemInformation.getGraphicCards());
            RAM.setText(SystemInformation.getRAM());
            DiskStorage.setText(SystemInformation.getDiskStorage());
            updateTemperature();
            BatteryCapacity.setText(ScopeBattery.getBatteryCapacity() + " mAh");
        });

        try{
            List<ProcessInfo> processes = ProcessInfoService.getRunningProcesses();
            List<ScopeUsbDevice> devices = SystemInformation.getScopeUsbDevices(SystemInformation.getUsbDevices());

            observableList = FXCollections.observableArrayList(processes);
            observableDevicesList = FXCollections.observableArrayList(devices);

            Platform.runLater(() -> {
                processList.setItems(observableList);
                devicesList.setItems(observableDevicesList);
            });

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    filterProcesses(newValue);
                } catch (IOException e) {
                    ScopeLogger.logError("Error while filtering processes: {}", e.getMessage(), e);
                }
            });
        }
        catch(IOException e){
            ScopeLogger.logError("Error while initializing processes and devices: {}", e.getMessage(), e);
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
        List<ProcessInfo> filtered = ScopeListView.searchItems(searchInput, ProcessInfoService.getRunningProcesses());

        Platform.runLater(()->{
            observableList.clear();
            observableList.addAll(filtered);
        });
    }

    /**
     * Creates and opens a window with charts
     * @throws IOException if an error occurs when opening the window
     */
    @FXML public void onShowChartsClicked() throws IOException {
        ScopeLoaderFXML loader = new ScopeLoaderFXML("ScopeCharts-view.fxml");

        scopeChartsController = (ScopeChartsController) loader.getController();

        loader.getStage().setResizable(false);
        loader.getStage().setTitle("Графіки");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        loader.getStage().setWidth(screenBounds.getWidth());
        loader.getStage().setHeight(screenBounds.getHeight());
        loader.showAndWait();
    }

    /**
     * Handles the event when the "Bench Logs" button is clicked. This method loads the "LogsList-view.fxml" file,
     * sets up the associated controller, and displays a new window (stage) with the list of logs.
     * The stage is non-resizable, and the title of the window is set to "Логи".
     *
     * @throws IOException if there is an issue loading the FXML file or setting up the scene.
     */
    @FXML public void onBenchLogsClicked() throws IOException {
        ScopeLoaderFXML loader = new ScopeLoaderFXML("LogsList-view.fxml");

        loader.getStage().setResizable(false);
        loader.getStage().setTitle("Логи");

        loader.showAndWait();
    }

    /**
     * Opens the "Chat-view" FXML and initializes the AI chat controller.
     * This method loads the FXML file for the chat view, sets up the controller,
     * and displays the chat window with the specified settings (like non-resizable, always on top).
     *
     * @throws IOException If an error occurs during the loading of the FXML file.
     */
    @FXML public void onOpenAnalyzeChat() throws IOException {
        ScopeLoaderFXML loader = new ScopeLoaderFXML("Chat-view.fxml");

        aiChatController = (AiChatController) loader.getController();
        aiChatController.setStage(this.stage);

        Stage chatStage = loader.getStage();
        chatStage.setResizable(false);
        chatStage.setTitle("ScopeHelper");

        chatStage.setY(0);

        chatStage.setAlwaysOnTop(true);
        chatStage.show();
    }

    /**
     * A method that forcibly terminates the selected process
     */
    @FXML public void onKillSelectedProcessClicked(){
        ProcessInfo selectedProcess = processList.getSelectionModel().getSelectedItem();
        if(selectedProcess != null){
            try{
                if(ProcessInfoService.killProcess(selectedProcess.getPid())){
                    processList.getItems().remove(selectedProcess);
                }
            }
            catch(IOException | InterruptedException e){
                ScopeLogger.logError("Failed to terminate the process", e);
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Не вдалося завершити процес");
                alert.showAndWait();
            }
        }
        else{
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Виберіть процес для завершення");
            alert.showAndWait();
        }
    }

    /**
     * Updates the list of processes
     */
    @FXML public void onRefreshProcessesBtnClicked(){
        updateProcessList();
    }

    /**
     * Updates the list of devices
     */
    @FXML public void onRefreshDevicesBtnClicked(){
        updateDevicesList();
    }

    /**
     * Changes the theme color to the opposite (dark or light)
     */
    @FXML public void onThemeToggleClicked(){

        if(this.theme.getTheme() == Theme.DARK){
            theme.setTheme(Theme.LIGHT);
        }
        else {
            theme.setTheme(Theme.DARK);
        }
        Platform.runLater(() -> {
            this.theme.applyTheme();
            this.updateTheme();
        });
    }

    /**
     * A method that launches a window with a file selection for the benchmark and controls the benchmarking process
     */
    @FXML public void onBenchClicked(){
        if(!Benchmark.getBenchmarkStarted()) {
            Benchmark.startBenchmark(this);
        }
        else{
            Benchmark.stopBenchmark();
        }
    }

    /**
     * A method that creates a dialog box and displays additional information about RAM.
     */
    @FXML public void showRamInfo(){
        String info = SystemInformation.getRamInfo();

        ScopeAlert alert = new ScopeAlert(Alert.AlertType.INFORMATION, info);
        alert.showAndWait();
    }

    /**
     * A method that determines the color relative to the input data on topics
     * @param temperature temperature indicator
     * @return Color relative to the temperature (temperature <= 70° - Green, temperature >= 70 and <= 90 - Orange, temperature > 90 - RED
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
            throw new NumberFormatException("Failed to find a numeric value in: " + tempString);
        }
    }

    /**
     * Updates the text of the theme toggle button based on the current theme.
     * <p>
     * indicating the option to switch to the light theme.,
     * indicating the option to switch to the dark theme.
     */
    public void updateTheme() {
        Platform.runLater(()->{
            if (theme.getTheme() == Theme.DARK) {
                themeToggleBtn.setText("Світла тема");
            } else {
                themeToggleBtn.setText("Темна тема");
            }
        });
    }

    /**
     * A method that updates the current values of components
     */
    private void updateTemperature() {
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() {
                String temperatureCPU = ScopeCentralProcessor.getTemperatureCPU();
                String tempGPUString = SystemInformation.getTemperatureGPU();
                String Fans = SystemInformation.getFansRPM();

                try {
                    double tempCPU = parseTemperature(temperatureCPU);
                    double[] gpuTemps = parseMultipleTemperatures(tempGPUString);

                    Platform.runLater(() -> {
                        TempCPU.setText(temperatureCPU + " °C");
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
                    ScopeLogger.logError("Temperature parsing error: {}", e.getMessage());
                }

                return null;
            }
        };

        new Thread(updateTask).start();
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
            throw new NumberFormatException("Found less than two temperature values in: " + tempString);
        }

        return temperatures;
    }

    /**
     * A method that updates devices list
     */
    private void updateDevicesList(){
        List<ScopeUsbDevice> devices = SystemInformation.getScopeUsbDevices(SystemInformation.getUsbDevices());

        observableDevicesList.clear();

        observableDevicesList.addAll(devices);

        devicesList.setItems(observableDevicesList);
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
            ScopeLogger.logError("Failed to update process list", e);
        }
    }

    /**
     * Updates the text and style of the benchmark button based on the current state of the benchmark.
     * If the benchmark has started, the button text will change to "Зупинити бенчмарк" (Stop Benchmark)
     * and the style class will be updated to indicate the stop state. If the benchmark is not started,
     * the button text will change to "Запустити бенчмарк" (Start Benchmark) and the style class will
     * revert to the start state.
     * <p>
     * This method ensures that UI updates are performed on the JavaFX application thread using
     * {@link Platform#runLater(Runnable)}.
     * </p>
     */
    public void swapBenchButton(){
        Platform.runLater(() -> {
            if(Benchmark.getBenchmarkStarted()) {
                benchBtn.setText("Зупинити бенчмарк");
                benchBtn.getStyleClass().removeAll("main-button");
                benchBtn.getStyleClass().add("main-button-stop");
            }
            else{
                benchBtn.setText("Запустити бенчмарк");
                benchBtn.getStyleClass().removeAll("main-button-stop");
                benchBtn.getStyleClass().add("main-button");
            }
        });
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