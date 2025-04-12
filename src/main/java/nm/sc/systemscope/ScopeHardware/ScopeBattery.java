package nm.sc.systemscope.ScopeHardware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * The {@code ScopeBattery} class provides a method to retrieve the battery capacity
 * in milli ampere-hours (mAh) for both Windows and Linux operating systems.
 *
 * <p>On Windows, it uses PowerShell to obtain the full charge capacity.</p>
 * <p>On Linux, it reads from system files located in {@code /sys/class/power_supply/BAT1}.</p>
 *
 * <p>If the battery information is unavailable, the method returns {@code -1}.</p>
 */
public class ScopeBattery {

    /**
     * Retrieves the battery capacity in milli ampere-hours (mAh).
     *
     * <p>For Windows, it uses PowerShell to query the full charge capacity.</p>
     * <p>For Linux, it reads the battery capacity from system files.</p>
     *
     * @return the battery capacity in mAh, or {@code -1} if the information is unavailable.
     */
    public static int getBatteryCapacity(){
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                ProcessBuilder builder = new ProcessBuilder(
                        "powershell", "-command",
                        "(Get-WmiObject -Class BatteryStaticData -Namespace root/WMI).FullChargeCapacity"
                );
                builder.redirectErrorStream(true);
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String output = reader.lines().collect(Collectors.joining()).trim();
                    return output.isEmpty() ? -1 : Integer.parseInt(output);
                }
            } else if (os.contains("linux")) {
                String batteryPath = "/sys/class/power_supply/BAT1/energy_full";

                if (!Files.exists(Paths.get(batteryPath))) {
                    batteryPath = "/sys/class/power_supply/BAT1/charge_full";
                }

                if (!Files.exists(Paths.get(batteryPath))) {
                    return -1;
                }

                String capacity = Files.readString(Paths.get(batteryPath)).trim();
                return Integer.parseInt(capacity) / 1000;
            }
        }
        catch(IOException e) {
            return -1;
        }
        return -1;
    }
}
