package nm.sc.systemscope.modules;

import nm.sc.systemscope.ScopeHardware.*;
import oshi.SystemInfo;
import oshi.hardware.*;
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
                NvidiaCard nvidia = new NvidiaCard();
                temperatures.append(nvidia.getTemperature());
            } else if (vendor.contains("amd")) {
                AmdCard amd = new AmdCard();
                temperatures.append(amd.getTemperature());
            }
            else if(vendor.contains("intel")){
                IntelCard intel = new IntelCard();
                temperatures.append(intel.getTemperature());
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
                NvidiaCard nvidia = new NvidiaCard();
                temperatures.append(nvidia.getTemperature());
            } else if (vendor.contains("amd")) {
                AmdCard amd = new AmdCard();
                temperatures.append(amd.getTemperature());
            } else if (vendor.contains("intel")) {
                IntelCard intel = new IntelCard();
                temperatures.append(intel.getTemperature());
            } else {
                temperatures.append("Невідомий виробник відеокарти: ").append(gpu.getName()).append("\n");
            }
        }

        return !temperatures.isEmpty() ? temperatures.toString() : "Немає даних";
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
            NvidiaCard nvidia = new NvidiaCard();
            usage.append(nvidia.getGPULoad());
        } else if (vendor.contains("amd")) {
            AmdCard amd = new AmdCard();
            usage.append(amd.getGPULoad());
        } else if (vendor.contains("intel")) {
            IntelCard intel = new IntelCard();
            usage.append(intel.getGPULoad());
        } else {
            usage.append("Не підтримується виробником");
        }

        return !usage.isEmpty() ? usage.toString() : "Немає даних";
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
