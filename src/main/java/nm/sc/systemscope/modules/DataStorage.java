package nm.sc.systemscope.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.chart.XYChart;
import nm.sc.systemscope.adapters.XYChartDataAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStorage {
    private static final String dataFolderPath = "src/main/data/";
    private static final String CPUtemperaturesPath = dataFolderPath + "CPUtemperatures.json";
    private static final String GPUtemperaturesPath = dataFolderPath + "GPUtemperatures.json";
    private static final String CPUusagePath = dataFolderPath + "UsageCPU.json";
    private static final String GPUusagePath = dataFolderPath + "UsageGPU.json";
    private static final String averagesPath = dataFolderPath + "Averages.json";

    static {
        createDataFolderAndFiles();
    }

    private static void createDataFolderAndFiles(){
        File dataFolder = new File(dataFolderPath);
        if(!dataFolder.exists()){
            if(dataFolder.mkdirs()){
                System.out.println("Папка 'data' була створена.");
            }
            else{
                System.out.println("Не вдалося створити папку 'data'.");
            }
        }

        File cpuFile = new File(CPUtemperaturesPath);
        if (!cpuFile.exists()) {
            try {
                if (cpuFile.createNewFile()) {
                    System.out.println("Файл 'CPUtemperatures.json' був створений.");
                } else {
                    System.out.println("Не вдалося створити файл 'CPUtemperatures.json'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File gpuFile = new File(GPUtemperaturesPath);
        if (!gpuFile.exists()) {
            try {
                if (gpuFile.createNewFile()) {
                    System.out.println("Файл 'GPUtemperatures.json' був створений.");
                } else {
                    System.out.println("Не вдалося створити файл 'GPUtemperatures.json'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File usageCPUFile = new File(CPUusagePath);
        if (!usageCPUFile.exists()) {
            try {
                if (usageCPUFile.createNewFile()) {
                    System.out.println("Файл 'UsageCPU.json' був створений.");
                } else {
                    System.out.println("Не вдалося створити файл 'UsageCPU.json'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File usageGPUFile = new File(GPUusagePath);
        if (!usageGPUFile.exists()) {
            try {
                if (usageGPUFile.createNewFile()) {
                    System.out.println("Файл 'UsageGPU.json' був створений.");
                } else {
                    System.out.println("Не вдалося створити файл 'UsageGPU.json'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File averagesFile = new File(averagesPath);
        if (!averagesFile.exists()) {
            try {
                if (averagesFile.createNewFile()) {
                    System.out.println("Файл 'Averages.json' був створений.");
                } else {
                    System.out.println("Не вдалося створити файл 'Averages.json'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveCPUTemperatureData(List<XYChart.Data<String,Number>> data){
        writeData(CPUtemperaturesPath,data);
    }

    public static void saveGPUTemperatureData(List<XYChart.Data<String, Number>> data){
        writeData(GPUtemperaturesPath, data);
    }

    public static void saveUsageCPUData(List<XYChart.Data<String, Number>> data){
       writeData(CPUusagePath, data);
    }

    public static void saveUsageGPUData(List<XYChart.Data<String, Number>> data){
        writeData(GPUusagePath, data);
    }

    public static void saveAveragesData(Map<String, Integer> averages){
        try (FileWriter writer = new FileWriter(averagesPath)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(averages, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<XYChart.Data<String, Number>> loadCPUTemperatureData(){
       return readData(CPUtemperaturesPath);
    }

    public static List<XYChart.Data<String, Number>> loadGPUTemperatureData(){
        return readData(GPUtemperaturesPath);
    }

    public static List<XYChart.Data<String, Number>> loadUsageCPUData(){
        return readData(CPUusagePath);
    }

    public static List<XYChart.Data<String, Number>> loadUsageGPUData(){
        return readData(GPUusagePath);
    }

    public static Map<String, Integer> loadAveragesData() {
        Map<String, Integer> averages = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(averagesPath))) {
            Gson gson = new GsonBuilder().create();
            averages = gson.fromJson(reader, new TypeToken<Map<String, Integer>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return averages;
    }

    public static void cleanDataStorage(){
        try {
            File cpuFile = new File(CPUtemperaturesPath);
            if (cpuFile.exists()) {
                if (cpuFile.delete()) {
                    System.out.println("CPUtemperatures.json був успішно видалений.");
                } else {
                    System.out.println("Не вдалося видалити CPUtemperatures.json.");
                }
            }

            File gpuFile = new File(GPUtemperaturesPath);
            if (gpuFile.exists()) {
                if (gpuFile.delete()) {
                    System.out.println("GPUtemperatures.json був успішно видалений.");
                } else {
                    System.out.println("Не вдалося видалити GPUtemperatures.json.");
                }
            }

            File usageCPUFile = new File(CPUusagePath);
            if (usageCPUFile.exists()) {
                if (usageCPUFile.delete()) {
                    System.out.println("UsageCPU.json був успішно видалений.");
                } else {
                    System.out.println("Не вдалося видалити UsageCPU.json.");
                }
            }
            File usageGPUFile = new File(GPUusagePath);
            if (usageGPUFile.exists()) {
                if (usageGPUFile.delete()) {
                    System.out.println("UsageGPU.json був успішно видалений.");
                } else {
                    System.out.println("Не вдалося видалити UsageGPU.json.");
                }
            }
            File averagesFile = new File(averagesPath);
            if (averagesFile.exists()) {
                if (averagesFile.delete()) {
                    System.out.println("Averages.json був успішно видалений.");
                } else {
                    System.out.println("Не вдалося видалити Averages.json.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeData(String path, List<XYChart.Data<String, Number>> data){
        try(FileWriter writer = new FileWriter(path)){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            gson.toJson(data,writer);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private static List<XYChart.Data<String, Number>> readData(String path){
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(path))){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            data = gson.fromJson(reader, new TypeToken<List<XYChart.Data<String, Number>>>() {}.getType());
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return data;
    }
}
