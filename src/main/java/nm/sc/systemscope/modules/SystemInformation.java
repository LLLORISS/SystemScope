package nm.sc.systemscope.modules;

import oshi.SystemInfo;
import oshi.hardware.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

/**
 * The `SystemInfo` class provides methods for retrieving system information, including details
 * about the graphics cards, RAM, disk storage, CPU, and other hardware components.
 */
public class SystemInformation {
    private static final HardwareAbstractionLayer layer;

    static {
        SystemInfo systemInfo = new SystemInfo();
        layer = systemInfo.getHardware();
    }

    /**
     * Retrieves the names and vendors of the graphics cards in the system.
     *
     * @return a formatted string containing the names and vendors of the graphics cards.
     */
    public static String getGraphicCards() {
        List<GraphicsCard> gpus = layer.getGraphicsCards();
        return formatGPU(gpus.stream()
                .map(gpu -> gpu.getName() + " (" + gpu.getVendor() + ")")
                .collect(Collectors.joining(", ")));
    }

    /**
     * Retrieves the total amount of RAM in the system.
     *
     * @return a formatted string with the total RAM size.
     */
    public static String getRAM() {
        long totalMemory = layer.getMemory().getTotal();

        return formatMemory(totalMemory);
    }

    /**
     * Retrieves detailed information about the physical memory (RAM) in the system.
     * The method may not work if you do not have the appropriate access rights to system folders.
     * @return a string with detailed information about each physical memory module.
     */
    public static String getRamInfo(){
        try {
            List<PhysicalMemory> memoryList = layer.getMemory().getPhysicalMemory();
            if (memoryList.isEmpty()) {
                ScopeLogger.logError("Physical memory information is unavailable");
                return "Physical memory information is unavailable";
            }

            StringBuilder ramInfo = new StringBuilder();

            for (PhysicalMemory memory : memoryList) {
                ramInfo.append(String.format("Виробник: %s\n", memory.getManufacturer()));
                ramInfo.append(String.format("Тип пам'яті: %s\n", memory.getMemoryType()));
                ramInfo.append(String.format("Частота: %.2f GHz\n", memory.getClockSpeed() / 1000.0));
                ramInfo.append(String.format("Об'єм: %s\n", formatMemory(memory.getCapacity())));
            }

            return ramInfo.toString();
        }
        catch(Exception e){
            ScopeLogger.logError("Error while retrieving physical memory information: {}", e.getMessage());
            return "Помилка при отриманні інформації про фізичну пам'ять: " + e.getMessage();
        }
    }

    /**
     * Retrieves information about the disk storage in the system.
     *
     * @return a string with the models and sizes of the disks.
     */
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

    /**
     * Retrieves the current CPU temperature.
     *
     * @return a string with the current CPU temperature in Celsius.
     */
    public static String getTemperatureCPU(){
        return layer.getSensors().getCpuTemperature() + " °C";
    }

    /**
     * Retrieves the discrete graphics card (GPU) from the system, prioritizing NVIDIA or AMD.
     *
     * @return the discrete GPU if found, null otherwise.
     */
    private static GraphicsCard getDiscreteGPU() {
        List<GraphicsCard> gpus = layer.getGraphicsCards();

        for (GraphicsCard gpu : gpus) {
            String vendor = gpu.getVendor().toLowerCase();

            if (vendor.contains("nvidia") || vendor.contains("amd")) {
                return gpu;
            }
        }

        for (GraphicsCard gpu : gpus) {
            String vendor = gpu.getVendor().toLowerCase();
            if (vendor.contains("intel")) {
                return gpu;
            }
        }

        return null;
    }

    /**
     * Retrieves the temperature of the discrete GPU in the system.
     *
     * @return a string with the temperature of the discrete GPU, or "No data" if not available.
     */
    public static String getTemperatureDiscreteGPU() {
        GraphicsCard gpu = getDiscreteGPU();

        StringBuilder temperatures = new StringBuilder();

        if(gpu != null) {

            String vendor = gpu.getVendor().toLowerCase();

            if (vendor.contains("nvidia")) {
                temperatures.append(getTemperatureForNVIDIA());
            } else if (vendor.contains("amd")) {
                temperatures.append(getTemperatureForAMD());
            }
            else if(vendor.contains("intel")){
                temperatures.append(getTemperatureForIntel());
            }
        }
        return !temperatures.isEmpty() ? temperatures.toString() : "Немає даних";
    }

    /**
     * Retrieves the temperature of all GPUs in the system.
     *
     * @return a string with the temperatures of all GPUs, or "No data" if not available.
     */
    public static String getTemperatureGPU() {
        List<GraphicsCard> gpus = layer.getGraphicsCards();

        StringBuilder temperatures = new StringBuilder();

        for (GraphicsCard gpu : gpus) {
            String vendor = gpu.getVendor().toLowerCase();

            if (vendor.contains("nvidia")) {
                temperatures.append(getTemperatureForNVIDIA());
            } else if (vendor.contains("amd")) {
                temperatures.append(getTemperatureForAMD());
            } else if (vendor.contains("intel")) {
                temperatures.append(getTemperatureForIntel());
            } else {
                temperatures.append("Невідомий виробник відеокарти: ").append(gpu.getName()).append("\n");
            }
        }

        return !temperatures.isEmpty() ? temperatures.toString() : "Немає даних";
    }

    /**
     * Retrieves the temperature for an NVIDIA GPU.
     *
     * @return a string with the temperature of the NVIDIA GPU.
     */
    private static String getTemperatureForNVIDIA() {
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
     * Retrieves the temperature for an AMD GPU.
     *
     * @return a string with the temperature of the AMD GPU.
     */
    private static String getTemperatureForAMD() {
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
     * Retrieves the temperature for an Intel GPU.
     *
     * @return a string with the temperature of the Intel GPU.
     */
    private static String getTemperatureForIntel() {
        return "Intel GPU: " + getTemperatureCPU() + " °C\n";
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
     * Retrieves the model name of the computer.
     *
     * @return the computer's model name.
     */
    public static String getComputerName() {
        String model = layer.getComputerSystem().getModel();

        return model.split("_")[0].trim();
    }

    /**
     * Retrieves the fan speeds in RPM (Revolutions Per Minute).
     *
     * @return a string with the fan speeds or "Not found" if no fan data is available.
     */
    public static String getFansRPM(){
        int[] fanSpeeds = layer.getSensors().getFanSpeeds();
        if(fanSpeeds.length == 0){
            return "Не знайдено";
        }

        StringBuilder builder = new StringBuilder();
        for(int fan : fanSpeeds){
            builder.append(fan).append(" ");
        }

        return builder.append("RPM").toString();
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

        return String.format("%.2f%%", loadCPU);
    }

    /**
     * Retrieves the GPU usage percentage for the discrete GPU.
     *
     * @return a string with the GPU usage, or a message indicating no GPU is found.
     */
    public static String getGPUUsage(){
        GraphicsCard gpu = getDiscreteGPU();

        if (gpu == null) {
            return "Дискретна відеокарта не знайдена";
        }

        String vendor = gpu.getVendor().toLowerCase();
        StringBuilder usage = new StringBuilder();

        if (vendor.contains("nvidia")) {
            usage.append(getGPULoadForNvidia());
        } else if (vendor.contains("amd")) {
            usage.append(getGPULoadForAMD());
        } else if (vendor.contains("intel")) {
            usage.append(getGPULoadForIntel());
        } else {
            usage.append("Не підтримується виробником");
        }

        return !usage.isEmpty() ? usage.toString() : "Немає даних";

    }

    /**
     * Retrieves the GPU load for an NVIDIA GPU.
     *
     * @return a string with the load percentage for the NVIDIA GPU.
     */
    private static String getGPULoadForNvidia(){
        try {
            String os = System.getProperty("os.name").toLowerCase();

            Process process;
            process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            return "Не вдалося отримати використання для NVIDIA";
        }
    }

    /**
     * Retrieves the GPU load for an AMD GPU.
     *
     * @return a string with the load percentage for the AMD GPU.
     */
    private static String getGPULoadForAMD() {
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

    /**
     * Retrieves the GPU load for an Intel GPU.
     *
     * @return a string with the load percentage for the Intel GPU.
     */
    private static String getGPULoadForIntel() {
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

    /**
     * Retrieves a list of USB devices connected to the system.
     *
     * @return a list of {@link UsbDevice} objects representing the USB devices connected to the system
     */
    public static List<UsbDevice> getUsbDevices(){
        SystemInfo info = new SystemInfo();
        return info.getHardware().getUsbDevices(false);
    }

    /**
     * Converts a list of {@link UsbDevice} objects into a list of {@link ScopeUsbDevice} objects.
     * Each {@link UsbDevice} in the input list is wrapped into a {@link ScopeUsbDevice}.
     *
     * @param usbDevices the list of {@link UsbDevice} objects to be converted
     * @return a list of {@link ScopeUsbDevice} objects representing the converted USB devices
     */
    public static List<ScopeUsbDevice> getScopeUsbDevices(List<UsbDevice> usbDevices){
        List<ScopeUsbDevice> devices = new ArrayList<>();

        for(UsbDevice device : usbDevices){
            devices.add(new ScopeUsbDevice(device));
        }

        return devices;
    }

    /**
     * Converts memory size from bytes to a human-readable format (GB).
     *
     * @param totalMemory the memory size in bytes.
     * @return a string representing the memory size in gigabytes.
     */
    private static String formatMemory(long totalMemory){
        double memoryInGB = totalMemory / (1024.0 * 1024.0 * 1024.0);
        return String.format("%.2f GB", memoryInGB);
    }

    /**
     * Formats the graphics card information.
     *
     * @param cards a string containing the graphics card details.
     * @return a formatted string with the graphics card names and models.
     */
    private static String formatGPU(String cards) {
        if (cards != null && !cards.isEmpty()) {
            String[] cardsList = cards.split(",");

            StringBuilder formattedCards = new StringBuilder();

            for (String card : cardsList) {
                card = card.trim();

                if (card.contains("Intel")) {
                    formattedCards.append("Intel UHD Graphics");
                } else if (card.contains("NVIDIA")) {
                    int startIndex = card.indexOf("[") + 1;
                    int endIndex = card.indexOf("]");
                    String model = card.substring(startIndex, endIndex).trim();

                    model = model.replaceAll(" /.*", "");

                    formattedCards.append("NVIDIA ").append(model);
                } else if (card.contains("AMD")) {
                    formattedCards.append("AMD ").append(card.replaceAll(".*\\[([A-Za-z0-9\\s]+)].*", "$1"));
                }

                if (!formattedCards.isEmpty() && cardsList.length > 1 && !card.equals(cardsList[cardsList.length - 1])) {
                    formattedCards.append("\n");
                }
            }

            String result = formattedCards.toString().trim();
            if (result.endsWith(" /")) {
                result = result.substring(0, result.length() - 2);
            }

            return result;
        }
        return "Відеокарти не знайдено";
    }
}
