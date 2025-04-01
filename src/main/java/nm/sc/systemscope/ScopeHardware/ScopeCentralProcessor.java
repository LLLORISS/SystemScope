package nm.sc.systemscope.ScopeHardware;
import oshi.SystemInfo;
import oshi.hardware.*;

/**
 * The {@code ScopeCentralProcessor} class provides methods to retrieve information about the system's CPU,
 * such as the processor name, CPU usage, and CPU temperature.
 *
 * This class uses the OSHI (Operating System and Hardware Information) library to access hardware details.
 * The CPU details are retrieved through the HardwareAbstractionLayer (HAL) provided by OSHI.
 */
public class ScopeCentralProcessor {
    private static final HardwareAbstractionLayer layer;

    static {
        SystemInfo systemInfo = new SystemInfo();
        layer = systemInfo.getHardware();
    }

    /**
     * Retrieves the name of the processor.
     *
     * @return a string containing the processor's name.
     */
    public static String getProcessorName(){
        return layer.getProcessor().getProcessorIdentifier().getName();
    }

    /**
     * Retrieves the current CPU usage percentage.
     *
     * @return a string with the CPU usage as a percentage.
     */
    public static String getCPUUsage(){
        CentralProcessor processor = layer.getProcessor();
        long delay = 1000;
        double loadCPU = processor.getSystemCpuLoad(delay) * 100;
        return String.valueOf((int) Math.round(loadCPU));
    }

    /**
     * Retrieves the current CPU temperature.
     *
     * @return a string with the current CPU temperature in Celsius.
     */
    public static String getTemperatureCPU(){
        return String.valueOf(layer.getSensors().getCpuTemperature());
    }
}
