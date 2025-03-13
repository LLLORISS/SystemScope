package nm.sc.systemscope;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.chart.XYChart;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String dataFolderPath = "src/main/data/";
    private static final String CPUtemperaturesPath = dataFolderPath + "CPUtemperatures.json";
    private static final String GPUtemperaturesPath = dataFolderPath + "GPUtemperatures.json";

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
    }

    public static void saveCPUTemperatureData(List<XYChart.Data<String,Number>> data){
        try(FileWriter writer = new FileWriter(CPUtemperaturesPath)){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            gson.toJson(data, writer);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void saveGPUTemperatureData(List<XYChart.Data<String, Number>> data){
        try(FileWriter writer = new FileWriter(GPUtemperaturesPath)){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            gson.toJson(data,writer);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static List<XYChart.Data<String, Number>> loadCPUTemperatureData(){
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(CPUtemperaturesPath))){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            data = gson.fromJson(reader, new TypeToken<List<XYChart.Data<String, Number>>>() {}.getType());
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return data;
    }

    public static List<XYChart.Data<String, Number>> loadGPUTemperatureData(){
        List<XYChart.Data<String, Number>> data = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(GPUtemperaturesPath))){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            data = gson.fromJson(reader, new TypeToken<List<XYChart.Data<String, Number>>>() {}.getType());
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return data;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
