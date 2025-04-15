package nm.sc.systemscope.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.scene.chart.XYChart;
import nm.sc.systemscope.adapters.ChatMessageAdapter;
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
    private static final String CPUsagePath = dataFolderPath + "UsageCPU.json";
    private static final String GPUsagePath = dataFolderPath + "UsageGPU.json";
    private static final String averagesPath = dataFolderPath + "Averages.json";
    private static final String configPath = "config.properties";
    private static final String configAIPath = "config_ai.properties";
    private static final String chatHistoryPath = dataFolderPath + "chatHistory.json";

    static {
        createDataFolderAndFiles();
    }

    /**
     * Creates the necessary data folder and JSON files if they do not exist.
     */
    private static void createDataFolderAndFiles() {
        File dataFolder = new File(dataFolderPath);
        if (!dataFolder.exists() && dataFolder.mkdirs()) {
            ScopeLogger.logInfo("The 'data' folder was created.");
        }

        File logsFolder = new File(logsFolderPath);
        if(!logsFolder.exists() && logsFolder.mkdirs()){
            ScopeLogger.logInfo("The 'logs' folder was created.");
        }

        createFile(CPUtemperaturesPath, "CPUtemperatures.json");
        createFile(GPUtemperaturesPath, "GPUtemperatures.json");
        createFile(CPUsagePath, "UsageCPU.json");
        createFile(GPUsagePath, "UsageGPU.json");
        createFile(averagesPath, "Averages.json");
        createFile(chatHistoryPath, "ChatHistory.json");
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
                    ScopeLogger.logInfo("File '{}' was created.", fileName);
                } else {
                    ScopeLogger.logInfo("Failed to create file '{}'.", fileName);
                }
            } catch (IOException e) {
                ScopeLogger.logError("Error while creating file: {}", fileName, e);
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
     */
    public static void createLogFile(String gameName, String fileName, List<Integer> tCPU, List<Integer> tGPU, List<Integer> uCPU, List<Integer> uGPU,
                                     int atCPU, int atGPU, int auCPU, int auGPU, double time) {
        String splitName = fileName.split("\\.")[0];
        File file = new File(logsFolderPath + splitName + ".txt");

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    ScopeLogger.logInfo("Log file '{}' was created.", fileName);
                } else {
                    ScopeLogger.logError("Failed to create file '{}'.", fileName);
                }
            } catch (IOException e) {
                ScopeLogger.logError("Error creating file '{}': ", fileName, e);
            }
        }

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("Benchmark Log\n");
            writer.write("Selected file: " + gameName + "\n");
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

            writer.write("--------------------------------------------------\n\n");
            writer.write("-----------------ScopeHelper report---------------\n");

            writer.write(ScopeAIHelper.request("Analyze Data on ukrainian and english: " +
                    "Average CPU Temp: " + atCPU + "\n" +
                    "Average CPU Usage: " + auCPU + "\n" +
                    "Average GPU Temp: " + atGPU + "\n" +
                    "Average GPU Usage: " + auGPU + "\n" +
                    "Game name: " + gameName));

            writer.write("--------------------------------------------------\n");

            ScopeLogger.logInfo("Data written to file: {}", fileName);
        } catch (IOException e) {
            ScopeLogger.logError("Error while creating or writing to file: {}", fileName, e);
        }
    }

    /**
     * Checks if a log file with the specified name already exists in the designated log's folder.
     *
     * @param fileName the name of the log file to check for existence. The extension (e.g., .txt) will be automatically appended if missing.
     * @return {@code true} if the log file exists, otherwise {@code false}.
     */
    public static boolean isLogFileExist(String fileName){
        String temp = fileName.split("\\.")[0];
        File file = new File(logsFolderPath + temp + ".txt");

        return file.exists();
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
       writeData(CPUsagePath, data);
    }

    /**
     * Saves GPU usage data to a JSON file.
     *
     * @param data The list of GPU usage data.
     */
    public static void saveUsageGPUData(List<XYChart.Data<String, Number>> data){
        writeData(GPUsagePath, data);
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
            ScopeLogger.logError("Error while saving data: ", e);
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
        return readData(CPUsagePath);
    }

    /**
     * Loads GPU usage data from a JSON file.
     *
     * @return A list of GPU usage data.
     */
    public static List<XYChart.Data<String, Number>> loadUsageGPUData(){
        return readData(GPUsagePath);
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
            ScopeLogger.logError("Error while loading averages data: ", e);
        }
        return averages;
    }

    /**
     * Clears all stored data by deleting the JSON files.
     */
    public static void cleanDataStorage() {
        deleteFile(CPUtemperaturesPath, "CPUtemperatures.json");
        deleteFile(GPUtemperaturesPath, "GPUtemperatures.json");
        deleteFile(CPUsagePath, "UsageCPU.json");
        deleteFile(GPUsagePath, "UsageGPU.json");
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
            ScopeLogger.logInfo("{} was successfully deleted.", fileName);
        } else {
            ScopeLogger.logInfo("Failed to delete {}.", fileName);
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
            ScopeLogger.logError("Error while writing data to file: {}", path, e);
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
            ScopeLogger.logError("Error while reading data from file: {}", path, e);
        }catch (JsonSyntaxException e) {
            ScopeLogger.logError("Error while parsing JSON from file: {}", path, e);
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
            ScopeLogger.logError("Error while saving theme to config file: " + configPath, e);
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
                ScopeLogger.logError("Error while getting list of files");
            }
        }
        else {
            ScopeLogger.logError("the specified path is not a directory");
        }

        return logs;
    }

    /**
     * Loads AI configuration data from a properties file and returns it as a {@link Map}.
     * The configuration includes the API key, API URL, model, and model description.
     * The values are stored in a map with keys:
     * - "API_KEY"
     * - "API_URL"
     * - "MODEL"
     * - "MODEL_DESCRIPTION"
     *
     * <p>This method loads the configuration from the properties file specified by {@code configAIPath}.</p>
     *
     * @return A {@link Map} containing the configuration values. The map contains the following keys:
     *         - "API_KEY" : The API key for the AI service.
     *         - "API_URL" : The URL of the AI service, with spaces removed.
     *         - "MODEL" : The name of the AI model.
     *         - "MODEL_DESCRIPTION" : A description of the AI model.
     */
    public static Map<String,String> getConfigAI(){
        Map<String,String> result = new HashMap<>();
        try(InputStream input = new FileInputStream(configAIPath)){
            Properties prop = new Properties();
            prop.load(input);

            result.put("API_KEY", prop.getProperty("API_KEY"));
            result.put("API_URL", prop.getProperty("API_URL").replace(" ", ""));
            result.put("MODEL", prop.getProperty("model"));
            result.put("MODEL_DESCRIPTION", prop.getProperty("model_description"));
        }
        catch(IOException e){
            ScopeLogger.logError("Error while receiving AI model data");
        }

        return result;
    }

    /**
     * Saves the AI configuration settings to a properties file.
     *
     * <p>This method takes a map containing configuration values (API key, API URL, model, and model description),
     * sets them as properties, and writes them to a file specified by {@code configAIPath}.</p>
     *
     * @param cfg a map containing the configuration values with the keys: "API_KEY", "API_URL", "MODEL", and "MODEL_DESCRIPTION"
     * @return {@code true} if the configuration was successfully saved; {@code false} if an error occurred while saving
     */
    public static boolean saveConfigAI(Map<String, String> cfg){
        boolean result = false;
        Properties prop = new Properties();

        prop.setProperty("API_KEY", cfg.getOrDefault("API_KEY", ""));
        prop.setProperty("API_URL", cfg.getOrDefault("API_URL", ""));
        prop.setProperty("model", cfg.getOrDefault("MODEL", ""));
        prop.setProperty("model_description", cfg.getOrDefault("MODEL_DESCRIPTION", ""));

        try (OutputStream output = new FileOutputStream(configAIPath)) {
            prop.store(output, "AI Model Configuration");
            result = true;
        } catch (IOException e) {
            ScopeLogger.logError("Error while saving AI model config");
        }

        return result;
    }

    /**
     * Saves the given chat history to a file in JSON format.
     * This method writes the list of chat messages to a file at the specified path.
     * If an error occurs during saving, it logs the error.
     *
     * @param messages A list of ChatMessage objects representing the chat history to be saved.
     */
    public static void saveChatHistory(List<ChatMessage> messages) {
        try (FileWriter writer = new FileWriter(chatHistoryPath)) {
            Gson gson = new GsonBuilder().registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter()).create();
            gson.toJson(messages, writer);
        } catch (IOException e) {
            ScopeLogger.logError("Error while saving chat history: ", e);
        }
    }

    /**
     * Loads the chat history from a file and returns it as a list of ChatMessage objects.
     * If the file is empty, doesn't exist, or an error occurs during loading or parsing,
     * it returns an empty list.
     *
     * @return A list of ChatMessage objects representing the loaded chat history.
     */
    public static List<ChatMessage> loadChatHistory() {
        List<ChatMessage> chatHistory = new ArrayList<>();
        File file = new File(chatHistoryPath);

        if (!file.exists() || file.length() == 0) {
            ScopeLogger.logInfo("Chat history file is empty or does not exist.");
            return chatHistory;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Gson gson = new GsonBuilder().registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter()).create();
            chatHistory = gson.fromJson(reader, new TypeToken<List<ChatMessage>>() {}.getType());

            if (chatHistory == null) {
                chatHistory = new ArrayList<>();
            }
        } catch (IOException | JsonSyntaxException e) {
            ScopeLogger.logError("Error parsing chat history JSON: ", e);
            chatHistory = new ArrayList<>();
        }

        return chatHistory;
    }

    /**
     * Clears the chat history by deleting the chat history file.
     * This method deletes the file specified by `chatHistoryPath`.
     */
    public static void clearChatHistory(){
        deleteFile(chatHistoryPath, "chatHistory.json");
    }
}
