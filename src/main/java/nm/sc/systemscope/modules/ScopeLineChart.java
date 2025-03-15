package nm.sc.systemscope.modules;

import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;

import java.util.List;

public class ScopeLineChart extends LineChart<String, Number> {
    private XYChart.Series<String, Number> series;
    private int MAX_DATA_POINTS;

    public ScopeLineChart() {
        super(new CategoryAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        this.getData().add(series);

        this.MAX_DATA_POINTS = 20;
    }

    public void setAxisX(String axis) {
        getXAxis().setLabel(axis);
    }

    public void setAxisY(String axis) {
        getYAxis().setLabel(axis);
    }

    public void setSeriesName(String name) {
        series.setName(name);
    }

    public void add(XYChart.Data<String, Number> data) {
        series.getData().add(data);

        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
        }

        setTips();
    }

    public void addAll(List<XYChart.Data<String, Number>> data) {
        series.getData().addAll(data);
        setTips();
    }

    public ObservableList<XYChart.Data<String, Number>> getSeriesData() {
        return series.getData();
    }

    public int getAverageValue() {
        if (series.getData().isEmpty()) return 0;
        int average = 0;
        for (XYChart.Data<String, Number> dt : series.getData()) {
            average += (int) Double.parseDouble(dt.getYValue().toString());
        }
        return average / series.getData().size();
    }

    public void setMaxDataPoints(int maxDataPoints){
        this.MAX_DATA_POINTS = maxDataPoints;
    }

    public int getMAX_DATA_POINTS() {
        return MAX_DATA_POINTS;
    }

    private void setTips() {
        if (!this.getData().isEmpty()) {
            for (XYChart.Series<String, Number> series : this.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        Tooltip tooltip = new Tooltip(data.getYValue().toString());
                        tooltip.setStyle("-fx-background-color: transparent; -fx-text-fill: White; -fx-font-size: 16px;");

                        tooltip.setShowDelay(javafx.util.Duration.ZERO);

                        Tooltip.install(data.getNode(), tooltip);

                        data.getNode().setOnMouseEntered(event -> {
                            data.getNode().setStyle("-fx-stroke: red; -fx-stroke-width: 2;");
                        });

                        data.getNode().setOnMouseExited(event -> {
                            data.getNode().setStyle("");
                        });
                    }
                }
            }
        }
    }
}