package nm.sc.systemscope.ScopeHardware;

import nm.sc.systemscope.modules.ScopeLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The {@code NvidiaCard} class implements the {@link ScopeGraphicCard} interface
 * and provides methods to retrieve information about the temperature and GPU load
 * for NVIDIA graphics cards.
 * The class handles errors gracefully by logging them through {@link ScopeLogger} and
 * providing appropriate error messages if the commands fail.
 */
public class NvidiaCard implements ScopeGraphicCard{
    /**
     * Retrieves the temperature for an NVIDIA GPU.
     *
     * @return a string with the temperature of the NVIDIA GPU.
     */
    @Override public String getTemperature(){
        try {
            Process process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=temperature.gpu --format=csv,noheader");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append("NVIDIA GPU: ").append(line.trim()).append(" °C\n");
            }
            return result.toString();
        } catch (Exception e) {
            ScopeLogger.logError("Error while retrieving temperature for NVIDIA GPU");
            return "Помилка при отриманні температури для NVIDIA GPU";
        }
    }

    /**
     * Retrieves the GPU load for an NVIDIA GPU.
     *
     * @return a string with the load percentage for the NVIDIA GPU.
     */
    @Override public String getGPULoad(){
        try {
            Process process;
            process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            return "Не вдалося отримати використання для NVIDIA";
        }
    }
}
