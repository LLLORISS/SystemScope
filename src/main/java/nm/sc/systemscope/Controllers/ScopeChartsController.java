package nm.sc.systemscope.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;

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
    private CategoryAxis xTempCPU;
    @FXML
    private CategoryAxis xTempGPU;
    @FXML
    private NumberAxis yTempCPU;
    @FXML
    private NumberAxis yTempGPU;

    private XYChart.Series<String, Number> seriesCPU;
    private XYChart.Series<String, Number> seriesGPU;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int MAX_DATA_POINTS = 30;

    private ScheduledExecutorService executorService;

    @FXML
    public void initialize(){
        xTempCPU.setLabel("Час");
        yTempCPU.setLabel("Температура (°C)");
        xTempGPU.setLabel("Час");
        yTempGPU.setLabel("Температура (°C)");

        seriesCPU = new XYChart.Series<>();
        seriesCPU.setName("Температура CPU");
        tempCPUChart.getData().add(seriesCPU);

        List<XYChart.Data<String, Number>> savedData = DataStorage.loadCPUTemperatureData();
        if(savedData != null) {
            seriesCPU.getData().addAll(savedData);
        }

        seriesGPU = new XYChart.Series<>();
        seriesGPU.setName("Температура GPU");
        tempGPUChart.getData().add(seriesGPU);

        savedData = DataStorage.loadGPUTemperatureData();
        if(savedData != null){
            seriesGPU.getData().addAll(savedData);
        }

        startBackgroundUpdate();
    }

    private void startBackgroundUpdate(){
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::updateCharts, 0 , 2, TimeUnit.SECONDS);
    }

    private void updateCharts(){
        String currentTime = timeFormat.format(new Date());
        String temperatureCPUString = SystemInformation.getTemperatureCPU();
        temperatureCPUString = temperatureCPUString.replaceAll("[^0-9.]", "");

        String temperatureGPUString = SystemInformation.getTemperatureDiscreteGPU();
        temperatureGPUString = temperatureGPUString.replaceAll("[^0-9.]", "");

        String finalTemperatureCPUString = temperatureCPUString;
        String finalTemperatureGPUString = temperatureGPUString;
        Platform.runLater(()->{
            seriesCPU.getData().add(new XYChart.Data<>(currentTime, Double.parseDouble(finalTemperatureCPUString)));
            seriesGPU.getData().add(new XYChart.Data<>(currentTime, Double.parseDouble(finalTemperatureGPUString)));

            if(seriesCPU.getData().size() > MAX_DATA_POINTS){
                seriesCPU.getData().remove(0);
            }
            if(seriesGPU.getData().size() > MAX_DATA_POINTS){
                seriesGPU.getData().remove(0);
            }

            DataStorage.saveCPUTemperatureData(seriesCPU.getData());
            DataStorage.saveGPUTemperatureData(seriesGPU.getData());
        });
    }

    public void stopBackgroundUpdate(){
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
