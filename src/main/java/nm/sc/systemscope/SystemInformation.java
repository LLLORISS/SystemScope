package nm.sc.systemscope;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import java.util.List;

public class SystemInformation {
    private static SystemInfo systemInfo;
    private static HardwareAbstractionLayer layer;

    static {
        systemInfo = new SystemInfo();
        layer = systemInfo.getHardware();
    }

    public static String getGraphicCards() {
        List<GraphicsCard> gpus = layer.getGraphicsCards();
        return gpus.stream()
                .map(gpu -> gpu.getName() + " (" + gpu.getVendor() + ")")
                .collect(Collectors.joining(", "));
    }

    public static String getTotalMemory(){
        long totalMemory = layer.getMemory().getTotal();

        return formatMemory(totalMemory);
    }

    public static String getDiskStorage() {
        List<HWDiskStore> disks = layer.getDiskStores();

        StringBuilder diskInfo = new StringBuilder();

        for (HWDiskStore disk : disks) {

            String diskModel = disk.getModel();
            long totalSpace = disk.getSize();

            diskInfo.append(String.format("Диск: %s, Об'єм: %s\n", diskModel, formatMemory(totalSpace)));
        }

        return diskInfo.toString();
    }

    public static String getOS(){
        return System.getProperty("os.name");
    }

    public static String getTemperatureCPU(){
        return String.valueOf(layer.getSensors().getCpuTemperature());
    }

    public static String getTemperatureGPU(){
        String Vendor = getGPUVendor();

        try {
            Process process;
            BufferedReader reader;
            String line;

            switch (Vendor) {
                case "NVIDIA": {
                    process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=temperature.gpu --format=csv,noheader");
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    line = reader.readLine();
                    return line != null ? line + " °C" : "Немає даних";
                }
                case "AMD": {
                    String os = System.getProperty("os.name").toLowerCase();
                    if(os.contains("win")){
                        process = Runtime.getRuntime().exec("wmic /namespace:\\\\root\\wmi PATH MSAcpi_ThermalZoneTemperature get CurrentTemperature");
                        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        reader.readLine();
                        line = reader.readLine();
                        return (line != null && !line.isEmpty()) ? ((Integer.parseInt(line.trim()) - 2732) / 10) + " °C" : "Немає даних";
                    } else {
                        process = Runtime.getRuntime().exec("sensors | grep -i 'gpu'");
                        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        line = reader.readLine();
                        return line != null ? line.trim() : "Немає даних";
                    }
                }
                case "INTEL": {
                    process = Runtime.getRuntime().exec("sensors | grep -i 'edge'");
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    line = reader.readLine();
                    return line != null ? line.trim() : "Немає даних";
                }
                default: {
                    return "Невідомий виробник GPU";
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return "Помилка при отриманні температури";
        }
    }

    public static String getProcessorName(){
        return layer.getProcessor().getProcessorIdentifier().getName();
    }

    public static String getComputerName(){
        return layer.getComputerSystem().getModel();
    }

    public static String getGPUVendor() {
        List<GraphicsCard> gpus = layer.getGraphicsCards();

        if (gpus.isEmpty()) return "Відеокарту не знайдено";

        String vendor = gpus.get(0).getVendor();

        if (vendor.toLowerCase().contains("nvidia")) return "NVIDIA";
        if (vendor.toLowerCase().contains("amd") || vendor.toLowerCase().contains("advanced micro devices")) return "AMD";
        if (vendor.toLowerCase().contains("intel")) return "Intel";

        return "Невідомий виробник";
    }

    private static String formatMemory(long totalMemory){
        double memoryInGB = totalMemory / (1024.0 * 1024.0 * 1024.0);
        return String.format("%.2f GB", memoryInGB);
    }
}
