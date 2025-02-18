package nm.sc.systemscope;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class SystemInformation {
    private static SystemInfo systemInfo;
    private static HardwareAbstractionLayer layer;

    static {
        systemInfo = new SystemInfo();
        layer = systemInfo.getHardware();
    }

    public static String getProcessorName(){
        return layer.getProcessor().getProcessorIdentifier().getName();
    }

    public static String getComputerName(){
        return layer.getComputerSystem().getModel();
    }
}
