package nm.sc.systemscope;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.CentralProcessor;

import java.io.BufferedReader;
import java.io.IOException;
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

    public static String getCPUUsage(){
        CentralProcessor processor = layer.getProcessor();

        long delay = 1000;

        double loadCPU = processor.getSystemCpuLoad(delay) * 100;

        return String.format("%.2f%%", loadCPU);
    }

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

        return usage.length() > 0 ? usage.toString() : "Немає даних";

    }

    private static String getGPULoadForNvidia(){
        try {
            String os = System.getProperty("os.name").toLowerCase();

            Process process;
            if (os.contains("win")) {
                process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");
            } else {
                process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            return line + "%";
        } catch (IOException e) {
            return "Не вдалося отримати використання для NVIDIA";
        }
    }

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

                if (result.length() == 0) {
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
            e.printStackTrace();
            result.append("Помилка при отриманні завантаження для AMD GPU");
        }

        return result.length() > 0 ? result.toString() : "Не вдалося отримати завантаження GPU";
    }

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
            e.printStackTrace();
            result.append("Помилка при отриманні завантаження для Intel GPU");
        }

        return result.length() > 0 ? result.toString() : "Не вдалося отримати завантаження Intel GPU";
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
