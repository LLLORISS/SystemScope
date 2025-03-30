package nm.sc.systemscope.modules;

import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import java.util.List;

/**
 * Custom LineChart class that extends JavaFX's LineChart to provide additional functionalities,
 * including custom tooltips and data point management.
 */
public class ScopeLineChart extends LineChart<String, Number> {
    private final XYChart.Series<String, Number> series;
    private final int MAX_DATA_POINTS;

    /**
     * Constructor to initialize the chart with a CategoryAxis for X-axis and a NumberAxis for Y-axis.
     * It also sets the maximum data points to 20 by default.
     */
    public ScopeLineChart() {
        super(new CategoryAxis(), new NumberAxis());
        series = new XYChart.Series<>();
        this.getData().add(series);

        this.MAX_DATA_POINTS = 20;
    }

    /**
     * Sets the label for the X-axis.
     *
     * @param axis The label for the X-axis.
     */
    public void setAxisX(String axis) {
        getXAxis().setLabel(axis);
    }

    /**
     * Sets the label for the Y-axis.
     *
     * @param axis The label for the Y-axis.
     */
    public void setAxisY(String axis) {
        getYAxis().setLabel(axis);
    }

    /**
     * Sets the name of the series.
     *
     * @param name The name of the series.
     */
    public void setSeriesName(String name) {
        series.setName(name);
    }

    /**
     * Adds a new data point to the series.
     * If the number of data points exceeds the maximum limit, the oldest point is removed.
     *
     * @param data The data point to be added.
     */
    public void add(XYChart.Data<String, Number> data) {
        series.getData().add(data);

        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0);
        }

        setTips();
    }

    /**
     * Adds multiple data points to the series at once.
     *
     * @param data A list of data points to be added.
     */
    public void addAll(List<XYChart.Data<String, Number>> data) {
        series.getData().addAll(data);
        setTips();
    }

    /**
     * Gets the data of the series.
     *
     * @return An ObservableList of the series data.
     */
    public ObservableList<XYChart.Data<String, Number>> getSeriesData() {
        return series.getData();
    }

    /**
     * Calculates and returns the average value of the Y-values in the series.
     *
     * @return The average of the Y-values.
     */
    public int getAverageValue() {
        if (series.getData().isEmpty()) return 0;
        int average = 0;
        for (XYChart.Data<String, Number> dt : series.getData()) {
            average += (int) Double.parseDouble(dt.getYValue().toString());
        }
        return average / series.getData().size();
    }

    /**
     * Sets tooltips for each data point in the series.
     * The tooltip shows the Y-value of each data point when hovered.
     * It also adds mouse hover effects to change the stroke of the data point.
     */
    private void setTips() {
        if (!this.getData().isEmpty()) {
            for (XYChart.Series<String, Number> series : this.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        Tooltip tooltip = new Tooltip(data.getYValue().toString());
                        tooltip.setStyle("-fx-background-color: transparent; -fx-text-fill: White; -fx-font-size: 16px;");

                        tooltip.setShowDelay(javafx.util.Duration.ZERO);

                        Tooltip.install(data.getNode(), tooltip);

                        data.getNode().setOnMouseEntered(event -> data.getNode().setStyle("-fx-stroke: red; -fx-stroke-width: 2;"));

                        data.getNode().setOnMouseExited(event -> data.getNode().setStyle(""));
                    }
                }
            }
        }
    }
}