package nm.sc.systemscope.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.scene.chart.XYChart;
import nm.sc.systemscope.adapters.ChatMessageAdapter;
import nm.sc.systemscope.adapters.ScopeChatAdapter;
import nm.sc.systemscope.adapters.XYChartDataAdapter;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String chatHistoryPath = dataFolderPath + "chat_logs/";

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

            boolean showCPUT = ScopeConfigManager.isShowCPUTemp();
            boolean showCPUU = ScopeConfigManager.isShowCPUUsage();
            boolean showGPUT = ScopeConfigManager.isShowGPUTemp();
            boolean showGPUU = ScopeConfigManager.isShowGPUUsage();

            if(showCPUT) {
                writer.write("CPU Temperature (°C): ");
                for (Integer temp : tCPU) {
                    writer.write(temp + " ");
                }
                writer.write("\n");
            }

            if(showGPUT) {
                writer.write("GPU Temperature (°C): ");
                for (Integer temp : tGPU) {
                    writer.write(temp + " ");
                }
                writer.write("\n");
            }

            if(showCPUU) {
                writer.write("CPU Usage (%): ");
                for (Integer usage : uCPU) {
                    writer.write(usage + " ");
                }
                writer.write("\n");
            }

            if(showGPUU) {
                writer.write("GPU Usage (%): ");
                for (Integer usage : uGPU) {
                    writer.write(usage + " ");
                }
                writer.write("\n");
            }

            if(showCPUT) {
                writer.write("Average CPU Temperature: " + atCPU + "\n");
            }
            if(showGPUT) {
                writer.write("Average GPU Temperature: " + atGPU + "\n");
            }
            if(showCPUU) {
                writer.write("Average CPU Usage: " + auCPU + "\n");
            }
            if(showGPUU) {
                writer.write("Average GPU Usage: " + auGPU + "\n");
            }

            writer.write("--------------------------------------------------\n\n");

            System.out.println(ScopeConfigManager.isGenerateAIReport());
            if(ScopeConfigManager.isGenerateAIReport()) {
                writer.write("-----------------ScopeHelper report---------------\n");

                StringBuilder aiMessage = new StringBuilder("Analyze Data on ukrainian and english:\n");

                if (ScopeConfigManager.isShowCPUTemp()) {
                    aiMessage.append("Average CPU Temp: ").append(atCPU).append("\n");
                }
                if (ScopeConfigManager.isShowCPUUsage()) {
                    aiMessage.append("Average CPU Usage: ").append(auCPU).append("\n");
                }
                if (ScopeConfigManager.isShowGPUTemp()) {
                    aiMessage.append("Average GPU Temp: ").append(atGPU).append("\n");
                }
                if (ScopeConfigManager.isShowGPUUsage()) {
                    aiMessage.append("Average GPU Usage: ").append(auGPU).append("\n");
                }

                aiMessage.append("Game name: ").append(gameName);

                String report = ScopeAIHelper.request(aiMessage.toString());


                writer.write(report + "\n");

                writer.write("--------------------------------------------------\n");
            }

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
     * Saves the provided chat history into a JSON file.
     * <p>
     * This method serializes the given {@link ScopeChat} object and stores it in a JSON file
     * within a folder specified by the {@code chatHistoryPath}. The file is named using
     * the chat's name and ID, ensuring each chat history is saved in a unique file.
     * </p>
     * <p>
     * If the folder does not exist, it will be created. If any errors occur during
     * the process, they are logged using {@link ScopeLogger}.
     * </p>
     *
     * @param chat The {@link ScopeChat} object to be saved, which contains the message history.
     */
    public static void saveChatHistory(ScopeChat chat) {
        File folder = new File(chatHistoryPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filename = chat.getChatName() + chat.getChatID() + ".json";
        File file = new File(folder, filename);

        try (FileWriter writer = new FileWriter(file, false)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ScopeChat.class, new ScopeChatAdapter())
                    .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
                    .create();
            gson.toJson(chat, writer);
        } catch (IOException e) {
            ScopeLogger.logError("Помилка при збереженні історії чату: " + filename, e);
        }
    }

    /**
     * Loads all chat histories from the chat history folder.
     * <p>
     * This method reads all JSON files from the folder specified by the {@code chatHistoryPath}
     * and deserializes them into a list of {@link ScopeChat} objects. It attempts to extract
     * the chat ID and name from the file name and assigns them to each {@link ScopeChat} object.
     * If the file name does not contain a valid UUID, an error is logged.
     * </p>
     *
     * @return A list of {@link ScopeChat} objects representing the chat histories.
     */
    public static List<ScopeChat> loadChatHistory() {
        List<ScopeChat> chatList = new ArrayList<>();

        File folder = new File(chatHistoryPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return chatList;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return chatList;
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ScopeChat.class, new ScopeChatAdapter())
                .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
                .create();

        Pattern uuidPattern = Pattern.compile("[0-9a-fA-F-]{36}");

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                ScopeChat chat = gson.fromJson(reader, ScopeChat.class);
                if (chat != null) {
                    String fileName = file.getName();
                    Matcher matcher = uuidPattern.matcher(fileName);

                    if (matcher.find()) {
                        String chatId = matcher.group();
                        String chatName = fileName.substring(0, matcher.start()).trim();

                        chat.setChatName(chatName);
                        chat.setChatID(chatId);

                        chatList.add(chat);
                    } else {
                        ScopeLogger.logError("Не вдалося знайти UUID в імені файлу: " + fileName, Optional.empty());
                    }
                }
            } catch (IOException e) {
                ScopeLogger.logError("Помилка при завантаженні чату з файлу: " + file.getName(), e);
            }
        }

        return chatList;
    }

    /**
     * Clears the message history of the provided chat and saves the updated chat to a file.
     * <p>
     * This method removes all messages from the provided {@link ScopeChat} object's history
     * and immediately saves the chat with the cleared history to its respective file.
     * </p>
     *
     * @param chat The {@link ScopeChat} object whose message history is to be cleared.
     */
    public static void clearChatHistory(ScopeChat chat){
        if (chat != null) {
            chat.getMessageHistory().clear();

            saveChatHistory(chat);

        }
    }
}
