package nm.sc.systemscope;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class BenchWindow {
    private JFrame frame;

    public void initialize() {
        frame = new JFrame("Bench Window");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(200, 200);
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

        JLabel tempCPULabel = new JLabel("CPU TEMP: 45 °C", SwingConstants.LEFT);
        tempCPULabel.setForeground(Color.PINK);
        tempCPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(tempCPULabel);

        JLabel CPUUsageLabel = new JLabel("CPU Usage: 2400", SwingConstants.LEFT);
        CPUUsageLabel.setForeground(Color.PINK);
        CPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(CPUUsageLabel);

        JLabel tempGPULabel = new JLabel("GPU TEMP: 85 °C", SwingConstants.LEFT);
        tempGPULabel.setForeground(Color.PINK);
        tempGPULabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(tempGPULabel);

        JLabel GPUUsageLabel = new JLabel("GPU Usage: 2400", SwingConstants.LEFT);
        GPUUsageLabel.setForeground(Color.PINK);
        GPUUsageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        frame.getContentPane().add(GPUUsageLabel);

        JLabel FPSLabel = new JLabel("FPS: 144", SwingConstants.LEFT);
        FPSLabel.setForeground(Color.ORANGE);
        FPSLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        frame.getContentPane().add(FPSLabel);

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
}
