package nm.sc.systemscope.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import nm.sc.systemscope.SystemInformation;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private Label TempCPULabel;
    @FXML
    private Label TempCPU;
    @FXML
    private Label TempGPU;

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

        scheduler.scheduleAtFixedRate(this::updateTemperature, 0, 2, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /*@FXML
    public void onExitBtnClicked(){
        System.exit(0);
    }*/

    @FXML
    public void onShowChartsClicked(){

    }

    @FXML
    public void showRamInfo(){
        String info = SystemInformation.getRamInfo();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Розширена інформація");
        alert.setHeaderText("Інформація про RAM:");
        alert.setContentText(info);
        alert.showAndWait();
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

        try {
            double tempCPU = parseTemperature(tempCPUString);
            double[] gpuTemps = parseMultipleTemperatures(tempGPUString);

            Platform.runLater(() -> {
                TempCPU.setText(tempCPUString);
                TempCPU.setTextFill(getColorByZone(tempCPU));

                String formattedGPU = String.format("Intel GPU: %.1f °C\nNVIDIA GPU: %.1f °C", gpuTemps[0], gpuTemps[1]);
                TempGPU.setText(formattedGPU);

                TempGPU.setTextFill(getColorByZone(Math.max(gpuTemps[0], gpuTemps[1])));
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


    public void shutdown() {
        scheduler.shutdown();
    }
}