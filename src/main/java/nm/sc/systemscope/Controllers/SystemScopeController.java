package nm.sc.systemscope.Controllers;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class

SystemScopeController {
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

    private BenchWindow benchWindow = null;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @FXML
    public void initialize() {
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

            if(processes != null) {
                processList.getItems().setAll(processes);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        scheduler.scheduleAtFixedRate(this::updateTemperature, 0, 2, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void filterProcesses(String searchInput) throws IOException {
        List<ProcessInfo> filtered = ProcessInfoService.searchProcess(searchInput);

        observableList.clear();
        observableList.addAll(filtered);
    }

    @FXML
    public void onShowChartsClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource("ScopeCharts-view.fxml"));
        Parent root = loader.load();

        scopeChartsController = loader.getController();

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Графіки");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.showAndWait();
    }

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
                showMessage(Alert.AlertType.ERROR, "Не вдалося завершити процес");
            }
        }
        else{
            showMessage(Alert.AlertType.ERROR, "Виберіть процес для завершення");
        }
    }

    @FXML
    public void onRefreshProcessesBtnClicked(){
        updateProcessList();
    }

    @FXML
    public void onBenchClicked() {
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
                showMessage(Alert.AlertType.ERROR, "Перевірте правильність вибраного файлу.");
            });
            return false;
        }
    }

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

    @FXML
    public void showRamInfo(){
        String info = SystemInformation.getRamInfo();

        showMessage(Alert.AlertType.INFORMATION, info);
    }

    private static Paint getColorByZone(double temperature) {
        if (temperature <= 70) {
            return Color.GREEN;
        } else if (temperature <= 90) {
            return Color.ORANGE;
        } else {
            return Color.RED;
        }
    }

    private double parseTemperature(String tempString) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+(\\.\\d+)?");
        java.util.regex.Matcher matcher = pattern.matcher(tempString);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        } else {
            throw new NumberFormatException("Не вдалося знайти числове значення в: " + tempString);
        }
    }

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

    private double[] parseMultipleTemperatures(String tempString) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+(\\.\\d+)?");
        java.util.regex.Matcher matcher = pattern.matcher(tempString);

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

    public static void showMessage(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("SystemScope");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

    public void shutdown() {
        if(scopeChartsController != null){
            scopeChartsController.stopBackgroundUpdate();
        }
        DataStorage.cleanDataStorage();
        scheduler.shutdown();
    }
}