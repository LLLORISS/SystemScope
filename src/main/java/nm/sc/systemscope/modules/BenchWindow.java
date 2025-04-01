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
    private JLabel tempCPULabel;
    private JLabel tempGPULabel;
    private JLabel CPUUsageLabel;
    private JLabel GPUUsageLabel;
    private static java.util.List<Integer> temperaturesCPU;
    private static java.util.List<Integer> temperaturesGPU;
    private static java.util.List<Integer> usagesCPU;
    private static List<Integer> usagesGPU;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Initializes the benchmark window and starts the scheduled updates.
     */
    public void initialize() {
        scheduler.scheduleAtFixedRate(this::updateBenchmark, 0, 1, TimeUnit.SECONDS);

        temperaturesCPU = new ArrayList<>();
        temperaturesGPU = new ArrayList<>();
        usagesCPU = new ArrayList<>();
        usagesGPU = new ArrayList<>();

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

        JLabel head= new JLabel("SYSTEM SCOPE", SwingConstants.CENTER);
        head.setForeground(Color.YELLOW);
        head.setFont(new Font("Segoe UI", Font.BOLD, 25));
        frame.getContentPane().add(head);

        tempCPULabel = new JLabel("CPU TEMP: " + ScopeCentralProcessor.getTemperatureCPU(), SwingConstants.LEFT);
        tempCPULabel.setForeground(Color.PINK);
        tempCPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(tempCPULabel);

        CPUUsageLabel = new JLabel("CPU Usage: ", SwingConstants.LEFT);
        CPUUsageLabel.setForeground(Color.PINK);
        CPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(CPUUsageLabel);

        tempGPULabel = new JLabel("GPU TEMP: " , SwingConstants.LEFT);
        tempGPULabel.setForeground(Color.PINK);
        tempGPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(tempGPULabel);

        GPUUsageLabel = new JLabel("GPU Usage: ", SwingConstants.LEFT);
        GPUUsageLabel.setForeground(Color.PINK);
        GPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(GPUUsageLabel);

        updateBenchmark();

        frame.setVisible(true);
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
    private void updateBenchmark(){
        String tempCPU = ScopeCentralProcessor.getTemperatureCPU();
        String tempGPU = SystemInformation.getTemperatureDiscreteGPU();
        String usageCPU = ScopeCentralProcessor.getCPUUsage();
        String usageGPU = SystemInformation.getGPUUsage();

        usageCPU = usageCPU.replace("%", "");
        usageGPU = usageGPU.replace("%", "");

        String intPartTempCPU = tempCPU.replaceAll("[^0-9.]", "");
        String intPartTempGPU = tempGPU.replaceAll("[^0-9.]", "");

        String intPartUsageCPU = usageCPU.split(",")[0];
        String intPartUsageGPU = usageGPU.split(",")[0];

        try {
            int intPartCPU = Integer.parseInt(intPartUsageCPU);
            int intPartGPU = Integer.parseInt(intPartUsageGPU);

            new Thread(() -> {
                temperaturesCPU.add((int) Double.parseDouble(intPartTempCPU.split("\\.")[0].trim()));
                temperaturesGPU.add((int) Double.parseDouble(intPartTempGPU.split("\\.")[0].trim()));

                usagesCPU.add(intPartCPU);
                usagesGPU.add(intPartGPU);
            }).start();

        } catch (NumberFormatException e) {
            ScopeLogger.logError("Error parsing temperature or usage values", e);
        }

        String finalUsageCPU = usageCPU;
        String finalUsageGPU = usageGPU;
        SwingUtilities.invokeLater(() -> {
            tempCPULabel.setText("CPU Temp: " + tempCPU);
            tempGPULabel.setText(tempGPU);
            CPUUsageLabel.setText("CPU Usage: " + finalUsageCPU);
            GPUUsageLabel.setText("GPU Usage: " + finalUsageGPU);
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
