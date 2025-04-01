package nm.sc.systemscope.ScopeHardware;

/**
 * The {@code ScopeGraphicCard} interface defines the methods that must be implemented by
 * any class representing a graphic card (GPU). This interface provides methods to
 * retrieve the temperature and load (utilization) of the GPU.
 * Classes implementing this interface should define the logic for retrieving GPU
 * information based on the specific GPU vendor and platform.
 */
public interface ScopeGraphicCard {
    /**
     * Retrieves the temperature of the GPU.
     * This method should return the temperature of the GPU as a string, typically in Celsius.
     *
     * @return a string representing the GPU temperature.
     */
    public String getTemperature();

    /**
     * Retrieves the load (utilization) of the GPU.
     * This method should return the GPU load as a percentage of utilization, indicating how much
     * the GPU is being used.
     *
     * @return a string representing the GPU load as a percentage.
     */
    public String getGPULoad();
}
