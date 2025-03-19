package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;

import java.util.*;

import javafx.scene.control.Label;
import nm.sc.systemscope.modules.DataStorage;
import nm.sc.systemscope.modules.ScopeLineChart;
import nm.sc.systemscope.modules.SystemInformation;
import nm.sc.systemscope.modules.Theme;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class that controls events and implements the functions of the main interface
 */
public class ScopeChartsController {
    @FXML
    private ScopeLineChart tempCPUChart;
    @FXML
    private ScopeLineChart tempGPUChart;
    @FXML
    private ScopeLineChart usageCPUChart;
    @FXML
    private ScopeLineChart usageGPUChart;
    @FXML
    private Label labelAverageTempCPU;
    @FXML
    private Label labelAverageTempGPU;
    @FXML
    private Label labelAverageUsageCPU;
    @FXML
    private Label labelAverageUsageGPU;
    @FXML
    private Label labelLastTempCPU;
    @FXML
    private Label labelLastTempGPU;
    @FXML
    private Label labelLastUsageCPU;
    @FXML
    private Label labelLastUsageGPU;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private ScheduledExecutorService executorService;

    private Theme theme;

    private Scene scene;

    /**
     * A method that initializes the initial values for the created window
     */
    @FXML
    public void initialize(){
        applyTheme();
        tempCPUChart.setAxisX("Час");
        tempCPUChart.setAxisY("Температура (°C)");
        tempGPUChart.setAxisX("Час");
        tempGPUChart.setAxisY("Температура (°C)");

        usageCPUChart.setAxisX("Час");
        usageCPUChart.setAxisY("Використання (%)");
        usageGPUChart.setAxisX("Час");
        usageGPUChart.setAxisY("Використання (%)");

        tempCPUChart.setSeriesName("Температура CPU");
        List<XYChart.Data<String, Number>> savedData = DataStorage.loadCPUTemperatureData();
        if(savedData != null) {
            tempCPUChart.addAll(savedData);
        }

        tempGPUChart.setSeriesName("Температура GPU");
        savedData = DataStorage.loadGPUTemperatureData();
        if(savedData != null){
            tempGPUChart.addAll(savedData);
        }

        usageCPUChart.setSeriesName("Використання CPU");
        savedData = DataStorage.loadUsageCPUData();
        if(savedData != null){
            usageCPUChart.addAll(savedData);
        }

        usageGPUChart.setSeriesName("Використання GPU");
        savedData = DataStorage.loadUsageGPUData();
        if(savedData != null){
           usageGPUChart.addAll(savedData);
        }

        labelsUpdateFromMap(DataStorage.loadAveragesData());

        startBackgroundUpdate();
    }

    /**
     * A method that starts background data refreshing at intervals of 2 seconds
     */
    private void startBackgroundUpdate(){
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::updateCharts, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * A method that updates graphs and their data
     */
    private void updateCharts(){
        String currentTime = timeFormat.format(new Date());

        String temperatureCPUString;
        String temperatureGPUString;
        String usageCPUString;
        String usageGPUString;

        try {
            temperatureCPUString = SystemInformation.getTemperatureCPU();
            temperatureCPUString = temperatureCPUString.replaceAll("[^0-9.]", "");

            temperatureGPUString = SystemInformation.getTemperatureDiscreteGPU();
            temperatureGPUString = temperatureGPUString.replaceAll("[^0-9.]", "");

            usageCPUString = SystemInformation.getCPUUsage();
            int intPartCPU = Integer.parseInt(usageCPUString.split(",")[0]);

            usageGPUString = SystemInformation.getGPUUsage();
            usageGPUString = usageGPUString.replaceAll("[^0-9]", "");
            int intPartGPU = Integer.parseInt(usageGPUString.split(",")[0]);

            String finalTemperatureCPUString = temperatureCPUString;
            String finalTemperatureGPUString = temperatureGPUString;
            Platform.runLater(() -> {
                try {
                    tempCPUChart.add(new XYChart.Data<>(currentTime, Double.parseDouble(finalTemperatureCPUString)));
                    tempGPUChart.add(new XYChart.Data<>(currentTime, Double.parseDouble(finalTemperatureGPUString)));
                    usageCPUChart.add(new XYChart.Data<>(currentTime, intPartCPU));
                    usageGPUChart.add(new XYChart.Data<>(currentTime, intPartGPU));

                    this.labelLastTempCPU.setText(finalTemperatureCPUString + " °C");
                    this.labelLastTempGPU.setText(finalTemperatureGPUString + " °C");
                    this.labelLastUsageCPU.setText(intPartCPU + " %");
                    this.labelLastUsageGPU.setText(intPartGPU + " %");

                    labelsUpdateFromMap(DataStorage.loadAveragesData());

                    DataStorage.saveCPUTemperatureData(tempCPUChart.getSeriesData());
                    DataStorage.saveGPUTemperatureData(tempGPUChart.getSeriesData());
                    DataStorage.saveUsageCPUData(usageCPUChart.getSeriesData());
                    DataStorage.saveUsageGPUData(usageGPUChart.getSeriesData());
                    DataStorage.saveAveragesData(getAveragesMap(tempCPUChart.getAverageValue(), tempGPUChart.getAverageValue(), usageCPUChart.getAverageValue(),
                            usageGPUChart.getAverageValue(), (int) Double.parseDouble(finalTemperatureCPUString),
                            (int) Double.parseDouble(finalTemperatureGPUString), intPartCPU, intPartGPU));
                } catch (Exception e) {
                    System.err.println("Помилка при оновленні графіків: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Помилка при отриманні даних системи: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *
     * @param tempCPU Input temperature of CPU
     * @param tempGPU Input temperature of GPU
     * @param usageCPU Input usage of CPU
     * @param usageGPU Input usage of GPU
     * @param lastCPU Input data of last CPU temperature
     * @param lastGPU Input data of last GPU temperature
     * @param lastUsageCPU Input data of last Usage of CPU
     * @param lastUsageGPU Input data of last Usage of GPU
     * @return Map<String,Integer> which contains the average values of the input data
     */
    private Map<String, Integer> getAveragesMap(int tempCPU, int tempGPU, int usageCPU, int usageGPU, int lastCPU, int lastGPU, int lastUsageCPU, int lastUsageGPU){
        Map<String, Integer> result = new HashMap<>();

        result.put("cpu_temp", tempCPU);
        result.put("gpu_temp", tempGPU);
        result.put("cpu_usage", usageCPU);
        result.put("gpu_usage", usageGPU);
        result.put("last_cpu_temp", lastCPU);
        result.put("last_gpu_temp", lastGPU);
        result.put("last_usage_cpu", lastUsageCPU);
        result.put("last_usage_gpu", lastUsageGPU);

        return result;
    }

    /**
     * Method that updates text fields with data
     * @param data Accepts Map<String, Integer> of input data to update
     */
    private void labelsUpdateFromMap(Map<String, Integer> data){
        if(data != null && !data.isEmpty()) {
            this.labelAverageTempCPU.setText(data.get("cpu_temp") + " °C");
            this.labelAverageTempGPU.setText(data.get("gpu_temp") + " °C");
            this.labelAverageUsageCPU.setText(data.get("cpu_usage") + " %");
            this.labelAverageUsageGPU.setText(data.get("gpu_usage") + " %");

            labelLastTempCPU.setText(data.get("last_cpu_temp") + " °C");
            labelLastTempGPU.setText(data.get("last_gpu_temp") + " °C");
            labelLastUsageCPU.setText(data.get("last_usage_cpu") + " %");
            labelLastUsageGPU.setText(data.get("last_usage_gpu") + " %");
        }
    }

    /**
     * A method that sets current scene
     * @param scene
     */
    public void setScene(Scene scene){
        this.scene = scene;
    }

    /**
     * A method that applies a style file to the current window
     */
    public void applyTheme(){
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
     * Method to stop background data updates
     */
    public void stopBackgroundUpdate() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
