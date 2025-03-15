package nm.sc.systemscope.modules;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BenchWindow {

    private JFrame frame;
    JLabel tempCPULabel;
    JLabel tempGPULabel;
    JLabel CPUUsageLabel;
    JLabel GPUUsageLabel;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void initialize() {
        scheduler.scheduleAtFixedRate(this::updateBenchmark, 0, 1, TimeUnit.SECONDS);

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

        tempCPULabel = new JLabel("CPU TEMP: " + SystemInformation.getTemperatureCPU(), SwingConstants.LEFT);
        tempCPULabel.setForeground(Color.PINK);
        tempCPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(tempCPULabel);

        CPUUsageLabel = new JLabel("CPU Usage: 2400", SwingConstants.LEFT);
        CPUUsageLabel.setForeground(Color.PINK);
        CPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(CPUUsageLabel);

        tempGPULabel = new JLabel("GPU TEMP: 85 °C" , SwingConstants.LEFT);
        tempGPULabel.setForeground(Color.PINK);
        tempGPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(tempGPULabel);

        GPUUsageLabel = new JLabel("GPU Usage: 2400", SwingConstants.LEFT);
        GPUUsageLabel.setForeground(Color.PINK);
        GPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(GPUUsageLabel);

        updateBenchmark();

        frame.setVisible(true);
    }

    private static class TransparentPane extends JComponent {
        public TransparentPane() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

        }

        @Override
        protected void processMouseEvent(MouseEvent e) {

        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {

        }
    }

    public void close() {
        if (frame != null) {
            frame.dispose();
        }
    }

    public boolean isVisible() {
        return frame != null && frame.isVisible();
    }

    private void updateBenchmark(){
        String tempCPU = SystemInformation.getTemperatureCPU();
        String tempGPU = SystemInformation.getTemperatureDiscreteGPU();
        String usageCPU = SystemInformation.getCPUUsage();
        String usageGPU = SystemInformation.getGPUUsage();

        SwingUtilities.invokeLater(()->{
            tempCPULabel.setText("CPU Temp: " + tempCPU);
            tempGPULabel.setText(tempGPU);
            CPUUsageLabel.setText("CPU Usage: " + usageCPU);
            GPUUsageLabel.setText("GPU Usage: " + usageGPU);
        });
    }

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
}
