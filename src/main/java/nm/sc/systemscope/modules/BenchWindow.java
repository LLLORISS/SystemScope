package nm.sc.systemscope.modules;

import nm.sc.systemscope.ScopeHardware.ScopeCentralProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A floating benchmark window displaying system statistics such as CPU and GPU usage and temperature.
 */
public class BenchWindow {

    private JFrame frame;
    private JLabel tempCPULabel, tempGPULabel, CPUUsageLabel, GPUUsageLabel;
    private static List<Integer> temperaturesCPU, temperaturesGPU, usagesCPU, usagesGPU;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private boolean isShowCPUTemp, isShowCPUUsage, isShowGPUTemp, isShowGPUUsage;

    /**
     * Initializes the benchmark window and starts the scheduled updates.
     */
    public void initialize() {
        if(ScopeConfigManager.isShowBenchmark()) {
            scheduler.scheduleAtFixedRate(this::updateBenchmark, 0, 1, TimeUnit.SECONDS);

            isShowCPUTemp = ScopeConfigManager.isShowCPUTemp();
            isShowCPUUsage = ScopeConfigManager.isShowCPUUsage();
            isShowGPUTemp = ScopeConfigManager.isShowGPUTemp();
            isShowGPUUsage = ScopeConfigManager.isShowGPUUsage();

            frame = new JFrame("Bench Window");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLocation(0, 0);
            frame.setAlwaysOnTop(true);
            frame.setUndecorated(true);
            frame.getRootPane().setOpaque(false);
            frame.setBackground(new Color(0, 0, 0, 0));

            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

            TransparentPane glass = new TransparentPane();
            frame.setGlassPane(glass);
            glass.setVisible(true);

            JLabel head = new JLabel("SYSTEM SCOPE", SwingConstants.CENTER);
            head.setForeground(Color.YELLOW);
            head.setFont(new Font("Segoe UI", Font.BOLD, 25));
            frame.getContentPane().add(head);

            if(isShowCPUTemp) {
                temperaturesCPU = new ArrayList<>();
                tempCPULabel = new JLabel("CPU TEMP: " + ScopeCentralProcessor.getTemperatureCPU(), SwingConstants.LEFT);
                tempCPULabel.setForeground(Color.PINK);
                tempCPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                frame.getContentPane().add(tempCPULabel);
            }

            if(isShowCPUUsage) {
                usagesCPU = new ArrayList<>();
                CPUUsageLabel = new JLabel("CPU Usage: ", SwingConstants.LEFT);
                CPUUsageLabel.setForeground(Color.PINK);
                CPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                frame.getContentPane().add(CPUUsageLabel);
            }

            if(isShowGPUTemp) {
                temperaturesGPU = new ArrayList<>();
                tempGPULabel = new JLabel("GPU TEMP: ", SwingConstants.LEFT);
                tempGPULabel.setForeground(Color.PINK);
                tempGPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                frame.getContentPane().add(tempGPULabel);
            }

            if(isShowGPUUsage) {
                usagesGPU = new ArrayList<>();
                GPUUsageLabel = new JLabel("GPU Usage: ", SwingConstants.LEFT);
                GPUUsageLabel.setForeground(Color.PINK);
                GPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                frame.getContentPane().add(GPUUsageLabel);
            }

            updateBenchmark();

            frame.setVisible(true);
        }
    }

    /**
     * A transparent pane to capture and block mouse events.
     */
    private static class TransparentPane extends JComponent {
        public TransparentPane() {
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {

        }

        @Override protected void processMouseEvent(MouseEvent e) {

        }

        @Override protected void processMouseMotionEvent(MouseEvent e) {

        }
    }

    /**
     * Closes the benchmark window.
     */
    public void close() {
        if (frame != null) {
            frame.dispose();
        }
        this.shutdownScheduler();
    }

    /**
     * Updates the benchmark data displayed in the window.
     */
    private void updateBenchmark() {
        String tempCPU = null;
        String tempGPU = null;
        String usageCPU = null;
        String usageGPU = null;

        if (isShowCPUTemp) {
            tempCPU = ScopeCentralProcessor.getTemperatureCPU();
        }
        if (isShowGPUTemp) {
            tempGPU = SystemInformation.getTemperatureDiscreteGPU();
        }
        if (isShowCPUUsage) {
            usageCPU = ScopeCentralProcessor.getCPUUsage();
        }
        if (isShowGPUUsage) {
            usageGPU = SystemInformation.getGPUUsage();
        }

        try {
            if (tempCPU != null) {
                String intPartTempCPU = tempCPU.replaceAll("[^0-9.]", "");
                temperaturesCPU.add((int) Double.parseDouble(intPartTempCPU.split("\\.")[0].trim()));
            }
            if (tempGPU != null) {
                String intPartTempGPU = tempGPU.replaceAll("[^0-9.]", "");
                temperaturesGPU.add((int) Double.parseDouble(intPartTempGPU.split("\\.")[0].trim()));
            }
            if (usageCPU != null) {
                usageCPU = usageCPU.replace("%", "");
                String intPartUsageCPU = usageCPU.split(",")[0];
                int intPartCPU = Integer.parseInt(intPartUsageCPU);
                usagesCPU.add(intPartCPU);
            }
            if (usageGPU != null) {
                usageGPU = usageGPU.replace("%", "");
                String intPartUsageGPU = usageGPU.split(",")[0];
                int intPartGPU = Integer.parseInt(intPartUsageGPU);
                usagesGPU.add(intPartGPU);
            }
        } catch (NumberFormatException e) {
            ScopeLogger.logError("Error parsing temperature or usage values", e);
        }

        String finalTempCPU = tempCPU;
        String finalTempGPU = tempGPU;
        String finalUsageCPU = usageCPU;
        String finalUsageGPU = usageGPU;

        SwingUtilities.invokeLater(() -> {
            if (finalTempCPU != null) tempCPULabel.setText("CPU Temp: " + finalTempCPU);
            if (finalTempGPU != null) tempGPULabel.setText(finalTempGPU);
            if (finalUsageCPU != null) CPUUsageLabel.setText("CPU Usage: " + finalUsageCPU);
            if (finalUsageGPU != null) GPUUsageLabel.setText("GPU Usage: " + finalUsageGPU);
        });
    }

    /**
     * Shuts down the scheduler to stop periodic updates.
     */
    private void shutdownScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Calculates the average of a list of integers.
     *
     * @param list the list of integers to average
     * @return the average of the integers in the list
     */
    private static int average(List<Integer> list){
        int sum = 0;
        if(list != null && !list.isEmpty()) {
            for (Integer integer : list) {
                sum += integer;
            }

            return sum / list.size();
        }
        return sum;
    }

    /**
     * Returns the average CPU temperature from the collected data.
     *
     * @return the average CPU temperature
     */
    public static int getAverageTempCPU(){
        return average(temperaturesCPU);
    }

    /**
     * Returns the average GPU temperature from the collected data.
     *
     * @return the average GPU temperature
     */
    public static int getAverageTempGPU() {
        return average(temperaturesGPU);
    }

    /**
     * Returns the average CPU usage from the collected data.
     *
     * @return the average CPU usage
     */
    public static int getAverageUsageCPU() {
        return average(usagesCPU);
    }

    /**
     * Returns the average GPU usage from the collected data.
     *
     * @return the average GPU usage
     */
    public static int getAverageUsageGPU() {
        return average(usagesGPU);
    }

    /**
     * Returns the list of collected CPU temperatures.
     *
     * @return the list of CPU temperatures
     */
    public static List<Integer> getTemperaturesCPU(){
        return temperaturesCPU;
    }

    /**
     * Returns the list of collected GPU temperatures.
     *
     * @return the list of GPU temperatures
     */
    public static List<Integer> getTemperaturesGPU() {
        return temperaturesGPU;
    }

    /**
     * Returns the list of collected CPU usage values.
     *
     * @return the list of CPU usage values
     */
    public static List<Integer> getUsagesCPU() {
        return usagesCPU;
    }

    /**
     * Returns the list of collected GPU usage values.
     *
     * @return the list of GPU usage values
     */
    public static List<Integer> getUsagesGPU() {
        return usagesGPU;
    }
}
