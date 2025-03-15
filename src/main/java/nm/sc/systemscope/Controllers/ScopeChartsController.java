package nm.sc.systemscope.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

import javafx.scene.control.Label;
import nm.sc.systemscope.DataStorage;
import nm.sc.systemscope.SystemInformation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScopeChartsController {
    @FXML
    private LineChart<String, Number> tempCPUChart;
    @FXML
    private LineChart<String, Number> tempGPUChart;
    @FXML
    private LineChart<String, Number> usageCPUChart;
    @FXML
    private LineChart<String, Number> usageGPUChart;
    @FXML
    private CategoryAxis xTempCPU;
    @FXML
    private CategoryAxis xTempGPU;
    @FXML
    private CategoryAxis xUsageCPU;
    @FXML
    private CategoryAxis xUsageGPU;
    @FXML
    private NumberAxis yTempCPU;
    @FXML
    private NumberAxis yTempGPU;
    @FXML
    private NumberAxis yUsageCPU;
    @FXML
    private NumberAxis yUsageGPU;
    @FXML
    private Label labelAverageTempCPU;
    @FXML
    private Label labelAverageTempGPU;
    @FXML
    private Label labelAverageUsageCPU;
    @FXML
    private Label labelAverageUsageGPU;

    private XYChart.Series<String, Number> seriesCPU;
    private XYChart.Series<String, Number> seriesGPU;
    private XYChart.Series<String, Number> seriesUsageCPU;
    private XYChart.Series<String, Number> seriesUsageGPU;

    private int averageTempCPU;
    private int averageTempGPU;
    private int averageUsageCPU;
    private int averageUsageGPU;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int MAX_DATA_POINTS = 20;

    private ScheduledExecutorService executorService;

    @FXML
    public void initialize(){
        xTempCPU.setLabel("Час");
        yTempCPU.setLabel("Температура (°C)");
        xTempGPU.setLabel("Час");
        yTempGPU.setLabel("Температура (°C)");
        xUsageCPU.setLabel("Час");
        yUsageCPU.setLabel("Використання (%)");
        xUsageGPU.setLabel("Час");
        yUsageGPU.setLabel("Використання (%)");

        averageTempCPU = averageTempGPU = averageUsageCPU = averageUsageGPU = 0;

        labelAverageTempCPU.setText("0");
        labelAverageTempGPU.setText("0");
        labelAverageUsageCPU.setText("0");
        labelAverageUsageGPU.setText("0");

        seriesCPU = new XYChart.Series<>();
        seriesCPU.setName("Температура CPU");
        tempCPUChart.getData().add(seriesCPU);

        List<XYChart.Data<String, Number>> savedData = DataStorage.loadCPUTemperatureData();
        if(savedData != null) {
            seriesCPU.getData().addAll(savedData);

            averageTempCPU = getAverageData(savedData);
        }

        seriesGPU = new XYChart.Series<>();
        seriesGPU.setName("Температура GPU");
        tempGPUChart.getData().add(seriesGPU);

        savedData = DataStorage.loadGPUTemperatureData();
        if(savedData != null){
            seriesGPU.getData().addAll(savedData);

            averageTempGPU = getAverageData(savedData);
        }

        seriesUsageCPU = new XYChart.Series<>();
        seriesUsageCPU.setName("Використання CPU");
        usageCPUChart.getData().add(seriesUsageCPU);

        savedData = DataStorage.loadUsageCPUData();
        if(savedData != null){
            seriesUsageCPU.getData().addAll(savedData);

            averageUsageCPU = getAverageData(savedData);
        }

        seriesUsageGPU = new XYChart.Series<>();
        seriesUsageGPU.setName("Використання GPU");
        usageGPUChart.getData().add(seriesUsageGPU);

        savedData = DataStorage.loadUsageGPUData();
        if(savedData != null){
            seriesUsageGPU.getData().addAll(savedData);

            averageUsageGPU = getAverageData(savedData);
        }

        startBackgroundUpdate();
    }

    private void startBackgroundUpdate(){
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            updateCharts();
        }, 0, 2, TimeUnit.SECONDS);
    }

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
                    seriesCPU.getData().add(new XYChart.Data<>(currentTime, Double.parseDouble(finalTemperatureCPUString)));
                    seriesGPU.getData().add(new XYChart.Data<>(currentTime, Double.parseDouble(finalTemperatureGPUString)));
                    seriesUsageCPU.getData().add(new XYChart.Data<>(currentTime, intPartCPU));
                    seriesUsageGPU.getData().add(new XYChart.Data<>(currentTime, intPartGPU));

                    if (seriesCPU.getData().size() > MAX_DATA_POINTS) {
                        seriesCPU.getData().remove(0);
                    }
                    if (seriesGPU.getData().size() > MAX_DATA_POINTS) {
                        seriesGPU.getData().remove(0);
                    }
                    if (seriesUsageCPU.getData().size() > MAX_DATA_POINTS) {
                        seriesUsageCPU.getData().remove(0);
                    }
                    if (seriesUsageGPU.getData().size() > MAX_DATA_POINTS) {
                        seriesUsageGPU.getData().remove(0);
                    }

                    averageTempCPU = getAverageData(seriesCPU.getData());
                    this.labelAverageTempCPU.setText(averageTempCPU + " °C");

                    averageTempGPU = getAverageData(seriesGPU.getData());
                    this.labelAverageTempGPU.setText(averageTempGPU + " °C");

                    averageUsageCPU = getAverageData(seriesUsageCPU.getData());
                    this.labelAverageUsageCPU.setText(averageUsageCPU + " %");

                    averageUsageGPU = getAverageData(seriesUsageGPU.getData());
                    this.labelAverageUsageGPU.setText(averageUsageGPU + " %");

                    DataStorage.saveCPUTemperatureData(seriesCPU.getData());
                    DataStorage.saveGPUTemperatureData(seriesGPU.getData());
                    DataStorage.saveUsageCPUData(seriesUsageCPU.getData());
                    DataStorage.saveUsageGPUData(seriesUsageGPU.getData());
                    DataStorage.saveAveragesData(new int[]{averageTempCPU, averageTempGPU, averageUsageCPU, averageUsageGPU});
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

    private int getAverageData(List<XYChart.Data<String, Number>> data) {
        if (data.isEmpty()) return 0;
        int average = 0;
        for (XYChart.Data<String, Number> dt : data) {
            average +=(int) Double.parseDouble(dt.getYValue().toString());
        }
        return average / data.size();
    }

    public void stopBackgroundUpdate() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
