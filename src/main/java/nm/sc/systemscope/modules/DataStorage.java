package nm.sc.systemscope.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.chart.XYChart;
import nm.sc.systemscope.adapters.XYChartDataAdapter;

import java.io.*;
import java.util.*;

/**
 * The {@code DataStorage} class provides functionality for saving and loading CPU/GPU
 * temperature and usage data, as well as storing theme settings in a configuration file.
 */
public class DataStorage {
    private static final String dataFolderPath = "src/main/data/";
    private static final String CPUtemperaturesPath = dataFolderPath + "CPUtemperatures.json";
    private static final String GPUtemperaturesPath = dataFolderPath + "GPUtemperatures.json";
    private static final String CPUusagePath = dataFolderPath + "UsageCPU.json";
    private static final String GPUusagePath = dataFolderPath + "UsageGPU.json";
    private static final String averagesPath = dataFolderPath + "Averages.json";

    private static final String configPath = "config.properties";
    static {
        createDataFolderAndFiles();
    }

    /**
     * Creates the necessary data folder and JSON files if they do not exist.
     */
    private static void createDataFolderAndFiles() {
        File dataFolder = new File(dataFolderPath);
        if (!dataFolder.exists() && dataFolder.mkdirs()) {
            System.out.println("The 'data' folder was created.");
        }

        createFile(CPUtemperaturesPath, "CPUtemperatures.json");
        createFile(GPUtemperaturesPath, "GPUtemperatures.json");
        createFile(CPUusagePath, "UsageCPU.json");
        createFile(GPUusagePath, "UsageGPU.json");
        createFile(averagesPath, "Averages.json");
    }

    /**
     * Creates a file if it does not exist.
     *
     * @param path The file path.
     * @param fileName The name of the file.
     */
    private static void createFile(String path, String fileName) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("File '" + fileName + "' was created.");
                } else {
                    System.out.println("Failed to create file '" + fileName + "'.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves CPU temperature data to a JSON file.
     *
     * @param data The list of CPU temperature data.
     */
    public static void saveCPUTemperatureData(List<XYChart.Data<String,Number>> data){
        writeData(CPUtemperaturesPath,data);
    }

    /**
     * Saves GPU temperature data to a JSON file.
     *
     * @param data The list of GPU temperature data.
     */
    public static void saveGPUTemperatureData(List<XYChart.Data<String, Number>> data){
        writeData(GPUtemperaturesPath, data);
    }

    /**
     * Saves CPU usage data to a JSON file.
     *
     * @param data The list of CPU usage data.
     */
    public static void saveUsageCPUData(List<XYChart.Data<String, Number>> data){
       writeData(CPUusagePath, data);
    }

    /**
     * Saves GPU usage data to a JSON file.
     *
     * @param data The list of GPU usage data.
     */
    public static void saveUsageGPUData(List<XYChart.Data<String, Number>> data){
        writeData(GPUusagePath, data);
    }

    /**
     * Saves average values to a JSON file.
     *
     * @param averages The map containing average values.
     */
    public static void saveAveragesData(Map<String, Integer> averages){
        try (FileWriter writer = new FileWriter(averagesPath)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(averages, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads CPU temperature data from a JSON file.
     *
     * @return A list of CPU temperature data.
     */
    public static List<XYChart.Data<String, Number>> loadCPUTemperatureData(){
       return readData(CPUtemperaturesPath);
    }

    /**
     * Loads GPU temperature data from a JSON file.
     *
     * @return A list of GPU temperature data.
     */
    public static List<XYChart.Data<String, Number>> loadGPUTemperatureData(){
        return readData(GPUtemperaturesPath);
    }

    /**
     * Loads CPU usage data from a JSON file.
     *
     * @return A list of CPU usage data.
     */
    public static List<XYChart.Data<String, Number>> loadUsageCPUData(){
        return readData(CPUusagePath);
    }

    /**
     * Loads GPU usage data from a JSON file.
     *
     * @return A list of GPU usage data.
     */
    public static List<XYChart.Data<String, Number>> loadUsageGPUData(){
        return readData(GPUusagePath);
    }

    /**
     * Loads the stored average values from a JSON file.
     *
     * @return A map containing average values.
     */
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

    /**
     * Clears all stored data by deleting the JSON files.
     */
    public static void cleanDataStorage() {
        deleteFile(CPUtemperaturesPath, "CPUtemperatures.json");
        deleteFile(GPUtemperaturesPath, "GPUtemperatures.json");
        deleteFile(CPUusagePath, "UsageCPU.json");
        deleteFile(GPUusagePath, "UsageGPU.json");
        deleteFile(averagesPath, "Averages.json");
    }

    /**
     * Deletes a file if it exists.
     *
     * @param path The file path.
     * @param fileName The name of the file.
     */
    private static void deleteFile(String path, String fileName) {
        File file = new File(path);
        if (file.exists() && file.delete()) {
            System.out.println(fileName + " was successfully deleted.");
        } else {
            System.out.println("Failed to delete " + fileName + ".");
        }
    }

    /**
     * Writes a list of data to a JSON file.
     *
     * @param path The file path.
     * @param data The data to be written.
     */
    private static void writeData(String path, List<XYChart.Data<String, Number>> data){
        try(FileWriter writer = new FileWriter(path)){
            Gson gson = new GsonBuilder().registerTypeAdapter(XYChart.Data.class, new XYChartDataAdapter()).create();
            gson.toJson(data,writer);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Reads data from a JSON file.
     *
     * @param path The file path.
     * @return A list of data.
     */
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

    /**
     * Loads the theme setting from the configuration file.
     *
     * @return The loaded theme, or {@code Theme.DARK} if an error occurs.
     */
    public static Theme loadThemeFromConfig() {
        Properties props = new Properties();
        try (InputStream stream = new FileInputStream(configPath)) {
            props.load(stream);
            String theme = props.getProperty("theme", "DARK");
            return Theme.valueOf(theme.toUpperCase());
        } catch (IOException | IllegalArgumentException e) {
            return Theme.DARK;
        }
    }

    /**
     * Saves the theme setting to the configuration file.
     *
     * @param theme The theme to save.
     */
    public static void saveThemeToConfig(Theme theme) {
        Properties props = new Properties();
        props.setProperty("theme", theme.name());

        try (OutputStream output = new FileOutputStream(configPath)) {
            props.store(output, "Theme Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
