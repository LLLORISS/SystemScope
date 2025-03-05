package nm.sc.systemscope;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
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
        return formatGPU(gpus.stream()
                .map(gpu -> gpu.getName() + " (" + gpu.getVendor() + ")")
                .collect(Collectors.joining(", ")));
    }

    public static String getRAM() {
        long totalMemory = layer.getMemory().getTotal();

        return formatMemory(totalMemory);
    }

    public static String getRamInfo(){
        List<PhysicalMemory> memoryList = layer.getMemory().getPhysicalMemory();
        if (memoryList.isEmpty()) {
            return "Інформація про фізичну пам'ять недоступна";
        }

        StringBuilder ramInfo = new StringBuilder();

        for(PhysicalMemory memory : memoryList){
            ramInfo.append(String.format("Виробник: %s\n", memory.getManufacturer()));
            ramInfo.append(String.format("Тип пам'яті: %s\n", memory.getMemoryType()));
            ramInfo.append(String.format("Частота: %.2f GHz\n", memory.getClockSpeed() / 1000.0));
            ramInfo.append(String.format("Об'єм: %s\n", formatMemory(memory.getCapacity())));
        }

        return ramInfo.toString();
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

    public static String getTemperatureCPU(){
        return layer.getSensors().getCpuTemperature() + " °C";
    }

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
            e.printStackTrace();
            return "Помилка при отриманні температури для NVIDIA GPU";
        }
    }

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
            e.printStackTrace();
            return "Помилка при отриманні температури для AMD GPU";
        }
    }

    private static String getTemperatureForIntel() {
        return "Intel GPU: " + getTemperatureCPU() + " °C\n";
    }

    public static String getProcessorName(){
        return layer.getProcessor().getProcessorIdentifier().getName();
    }

    public static String getComputerName() {
        String model = layer.getComputerSystem().getModel();

        return model.split("_")[0].trim();
    }

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

    private static String formatMemory(long totalMemory){
        double memoryInGB = totalMemory / (1024.0 * 1024.0 * 1024.0);
        return String.format("%.2f GB", memoryInGB);
    }

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
                    formattedCards.append("AMD ").append(card.replaceAll(".*\\[([A-Za-z0-9\\s]+)\\].*", "$1"));
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
