package nm.sc.systemscope.ScopeHardware;

import nm.sc.systemscope.modules.ScopeLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * The {@code IntelCard} class implements the {@link ScopeGraphicCard} interface
 * and provides methods to retrieve information about the temperature and GPU load
 * for Intel integrated graphics cards.
 * For temperature, the class uses the {@link ScopeCentralProcessor#getTemperatureCPU()} method
 * to fetch the CPU temperature, which may also represent the temperature of Intel's integrated GPU.
 * For GPU load, the class supports Linux and Unix-like systems (including macOS).
 * It uses the `intel_gpu_top` command to retrieve GPU load information. On Windows,
 * GPU load retrieval is not supported and the method returns a "Not supported." message.
 * This class handles errors gracefully and logs appropriate error messages
 * using {@link ScopeLogger} when failures occur during GPU load retrieval.
 */
public class IntelCard implements ScopeGraphicCard{
    /**
     * Retrieves the temperature for an Intel GPU.
     * This method calls the {@link ScopeCentralProcessor#getTemperatureCPU()} method to get the
     * CPU temperature, which may reflect the temperature of Intel's integrated GPU.
     *
     * @return a string with the temperature of the Intel GPU.
     */
    @Override public String getTemperature() {
        return ScopeCentralProcessor.getTemperatureCPU();
    }

    /**
     * Retrieves the GPU load for an Intel GPU.
     * This method checks the operating system to determine the appropriate command to execute:
     * - On Windows, GPU load retrieval is not supported, and a message "Not supported." is returned.
     * - On Linux and Unix-like systems (including macOS), it uses the `intel_gpu_top` command to get the load.
     *
     * @return a string with the load percentage for the Intel GPU, or an error message if not available.
     */
    @Override public String getGPULoad() {
        String os = System.getProperty("os.name").toLowerCase();
        StringBuilder result = new StringBuilder();

        try {
            if (os.contains("win")) {
                result.append("Not supported.");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                Process process = Runtime.getRuntime().exec("sudo intel_gpu_top -d 1 -n 1");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("Gpu") || line.contains("Render") || line.contains("Active")) {
                        result.append(line.trim()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            ScopeLogger.logError("Error while retrieving load for Intel GPU");
            result.append("Помилка при отриманні завантаження для Intel GPU");
        }

        return !result.isEmpty() ? result.toString() : "Не вдалося отримати завантаження Intel GPU";
    }
}
