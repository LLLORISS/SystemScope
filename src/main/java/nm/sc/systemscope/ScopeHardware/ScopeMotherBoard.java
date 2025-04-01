package nm.sc.systemscope.ScopeHardware;

import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * The {@code ScopeMotherBoard} class provides methods to retrieve information
 * about the system's motherboard, such as the manufacturer, model, and version.
 * This class utilizes the OSHI (Operating System and Hardware Information) library
 * to interact with the hardware and obtain motherboard details from the system's
 * hardware abstraction layer (HAL).
 */
public class ScopeMotherBoard {
    private static final Baseboard board;

    static{
        SystemInfo info = new SystemInfo();
        HardwareAbstractionLayer layer = info.getHardware();
        board = layer.getComputerSystem().getBaseboard();
    }

    /**
     * Retrieves the manufacturer of the motherboard.
     * This method uses the OSHI library to retrieve the manufacturer's name from
     * the motherboard's Baseboard information.
     *
     * @return a string containing the manufacturer of the motherboard.
     */
    public static String getManufacturer(){
        return board.getManufacturer();
    }

    /**
     * Retrieves the model of the motherboard.
     * This method uses the OSHI library to retrieve the model name from
     * the motherboard's Baseboard information.
     *
     * @return a string containing the model of the motherboard.
     */
    public static String getModel(){
        return board.getModel();
    }

    /**
     * Retrieves the version of the motherboard.
     * This method uses the OSHI library to retrieve the version number from
     * the motherboard's Baseboard information.
     *
     * @return a string containing the version of the motherboard.
     */
    public static String getVersion(){
        return board.getVersion();
    }
}
