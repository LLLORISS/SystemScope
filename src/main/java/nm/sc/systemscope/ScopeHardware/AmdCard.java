package nm.sc.systemscope.ScopeHardware;

import nm.sc.systemscope.modules.ScopeLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The {@code AmdCard} class implements the {@link ScopeGraphicCard} interface and provides methods to retrieve
 * information about the temperature and GPU load for AMD graphics cards.
 * The class supports different operating systems, including Windows and Linux-based systems.
 * On Windows, it uses the `wmic` command to gather GPU information. On Linux and Unix-like systems,
 * it uses the `sensors` command or `radeontop` utility to retrieve GPU data.
 * Both methods handle errors gracefully and log appropriate messages using {@link ScopeLogger} when failures occur.
 */
public class AmdCard implements ScopeGraphicCard{
    /**
     * Retrieves the temperature for an AMD GPU.
     * This method executes platform-specific commands to retrieve the temperature of the AMD GPU.
     * On Windows, it uses WMIC to get the temperature.
     * On Linux or Unix-based systems, it uses the `sensors` command to extract GPU temperature.
     *
     * @return a string containing the temperature of the AMD GPU, in degrees Celsius.
     *         If an error occurs, it returns an error message.
     */
    @Override public String getTemperature() {
        try {
            Process process;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("wmic /namespace:\\\\root\\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature");
            } else {
                process = Runtime.getRuntime().exec("sensors | grep -i 'gpu'");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append("AMD GPU: ").append(line.trim()).append(" °C\n");
            }
            return result.toString();
        } catch (Exception e) {
            ScopeLogger.logError("Error while retrieving temperature for AMD GPU");
            return "Помилка при отриманні температури для AMD GPU";
        }
    }

    /**
     * Retrieves the GPU load for an AMD GPU.
     * This method executes platform-specific commands to retrieve the load percentage of the AMD GPU.
     * On Windows, it uses WMIC to get the load percentage.
     * On Linux or Unix-based systems, it uses the `sensors` command, and if necessary, falls back to `radeontop` to gather GPU load information.
     *
     * @return a string containing the load percentage of the AMD GPU.
     *         If an error occurs, it returns an error message.
     *         If the load cannot be retrieved, it returns a message stating that the load could not be obtained.
     */
    @Override public String getGPULoad() {
        String os = System.getProperty("os.name").toLowerCase();
        StringBuilder result = new StringBuilder();

        try {
            if (os.contains("win")) {
                Process process = Runtime.getRuntime().exec("wmic path Win32_VideoController get LoadPercentage");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("%")) {
                        result.append(line.trim()).append("\n");
                    }
                }
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                Process process = Runtime.getRuntime().exec("sensors | grep -i 'gpu'");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line.trim()).append("\n");
                }

                if (result.isEmpty()) {
                    process = Runtime.getRuntime().exec("radeontop -d 1 -n 1");
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Load")) {
                            result.append(line.trim()).append("\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            ScopeLogger.logError("Error while retrieving load for AMD GPU");
            result.append("Помилка при отриманні завантаження для AMD GPU");
        }

        return !result.isEmpty() ? result.toString() : "Не вдалося отримати завантаження GPU";
    }
}
