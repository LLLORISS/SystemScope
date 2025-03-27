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
    private static final String logsFolderPath = "src/main/data/logs/";
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

        File logsFolder = new File(logsFolderPath);
        if(!logsFolder.exists() && logsFolder.mkdirs()){
            System.out.println("The 'logs' folder was created.");
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
     * Creates a log file and writes the benchmarking data, including temperatures and usage statistics for
     * CPU and GPU, as well as average values, into the file.
     *
     * @param fileName the name of the log file to be created. The extension (e.g., .txt) will be appended if missing.
     * @param tCPU a list of integer values representing the CPU temperatures during the benchmark.
     * @param tGPU a list of integer values representing the GPU temperatures during the benchmark.
     * @param uCPU a list of integer values representing the CPU usage percentages during the benchmark.
     * @param uGPU a list of integer values representing the GPU usage percentages during the benchmark.
     * @param atCPU the average CPU temperature during the benchmark.
     * @param atGPU the average GPU temperature during the benchmark.
     * @param auCPU the average CPU usage percentage during the benchmark.
     * @param auGPU the average GPU usage percentage during the benchmark.
     * @param time the timestamp of the benchmark (in seconds or milliseconds, depending on the context).
     *
     * @throws IOException if an I/O error occurs while creating or writing to the log file.
     *
     * This method first checks if the log file exists and creates a new file if necessary. It then writes
     * the data to the file, including the individual temperatures and usages as well as the average values.
     */
    public static void createLogFile(String fileName, List<Integer> tCPU, List<Integer> tGPU, List<Integer> uCPU, List<Integer> uGPU,
                                     int atCPU, int atGPU, int auCPU, int auGPU, double time) {
        String splittedName = fileName.split("\\.")[0];
        File file = new File(logsFolderPath + splittedName + ".txt");

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

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("Benchmark Log\n");
            writer.write("Timestamp: " + time + "\n");

            writer.write("CPU Temperature (°C): ");
            for (Integer temp : tCPU) {
                writer.write(temp + " ");
            }
            writer.write("\n");

            writer.write("GPU Temperature (°C): ");
            for (Integer temp : tGPU) {
                writer.write(temp + " ");
            }
            writer.write("\n");

            writer.write("CPU Usage (%): ");
            for (Integer usage : uCPU) {
                writer.write(usage + " ");
            }
            writer.write("\n");

            writer.write("GPU Usage (%): ");
            for (Integer usage : uGPU) {
                writer.write(usage + " ");
            }
            writer.write("\n");

            writer.write("Average CPU Temperature: " + atCPU + "\n");
            writer.write("Average GPU Temperature: " + atGPU + "\n");
            writer.write("Average CPU Usage: " + auCPU + "\n");
            writer.write("Average GPU Usage: " + auGPU + "\n");

            writer.write("--------------------------------------------------\n");

            System.out.println("Data written to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a log file with the specified name already exists in the designated logs folder.
     *
     * @param fileName the name of the log file to check for existence. The extension (e.g., .txt) will be automatically appended if missing.
     * @return {@code true} if the log file exists, otherwise {@code false}.
     */
    public static boolean isLogFileExist(String fileName){
        String temp = fileName.split("\\.")[0];
        File file = new File(logsFolderPath + temp + ".txt");

        if(!file.exists()){
            return false;
        }
        else{
            return true;
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

    /**
     * Retrieves a list of benchmark log files from the specified logs directory.
     * Each log file is represented as a {@link ScopeBenchLog} object, which contains the file's absolute path and name.
     *
     * @return a list of {@link ScopeBenchLog} objects representing the log files in the logs directory.
     *         If the directory does not exist or an error occurs while reading the directory, an empty list is returned.
     */
    public static List<ScopeBenchLog> getBenchLogs() {
        File logsDir = new File(logsFolderPath);
        List<ScopeBenchLog> logs = new ArrayList<>();

        if(logsDir.exists() && logsDir.isDirectory()) {
            File[] fileList = logsDir.listFiles();

            if(fileList != null) {
                for(File file : fileList) {
                    if(file.isFile()){
                        logs.add(new ScopeBenchLog(file.getAbsolutePath(), file.getName()));
                    }
                }
            }
            else {
                System.out.println("Помилка при отриманні списку файлів.");
            }
        }
        else {
            System.out.println("Вказаний шлях не є директорією.");
        }

        return logs;
    }
}
