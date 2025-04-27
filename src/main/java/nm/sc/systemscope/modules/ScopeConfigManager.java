package nm.sc.systemscope.modules;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages the configuration for the System Scope application.
 * Loads, saves, and updates configuration values from a properties file.
 */
public class ScopeConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private static String API_KEY = "", API_URL = "", MODEL_DESCRIPTION = "", MODEL = "";
    private static final Properties props = new Properties();
    private static Theme theme = Theme.DARK;
    private static boolean saveBenchLogs, generateAIReport, showCPUTemp, showCPUUsage, showGPUTemp, showGPUUsage, lastSave = true;
    private static int mainDelay = 2;

    static {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            ScopeLogger.logInfo("Config not found, default config will be set");
            setDefaults();
            save();
        } else {
            loadConfig();
        }
    }

    /**
     * Sets default values for the configuration and stores them in the properties.
     */
    private static void setDefaults() {
        theme = Theme.DARK;
        saveBenchLogs = true;
        generateAIReport = false;
        showCPUTemp = true;
        showGPUTemp = true;
        showCPUUsage = true;
        showGPUUsage = true;
        API_KEY = "";
        API_URL = "";
        MODEL_DESCRIPTION = "";
        MODEL = "";
        mainDelay = 2;

        props.setProperty("theme", theme.toString());
        props.setProperty("saveBenchLogs", String.valueOf(saveBenchLogs));
        props.setProperty("generateAIReport", String.valueOf(generateAIReport));
        props.setProperty("show_cpu_temp", String.valueOf(showCPUTemp));
        props.setProperty("show_cpu_usage", String.valueOf(showCPUUsage));
        props.setProperty("show_gpu_temp", String.valueOf(showGPUTemp));
        props.setProperty("show_gpu_usage", String.valueOf(showGPUUsage));
        props.setProperty("API_KEY", API_KEY);
        props.setProperty("API_URL", API_URL);
        props.setProperty("model_description", MODEL_DESCRIPTION);
        props.setProperty("model", MODEL);
        props.setProperty("main_delay", String.valueOf(mainDelay));
    }

    /**
     * Loads application configuration from a properties file.
     * <p>
     * Reads the configuration values from the {@code CONFIG_FILE} and initializes
     * various application settings, including UI theme, logging options, AI report generation,
     * display options for CPU and GPU metrics, API credentials, and update intervals.
     * </p>
     *
     * <p>If the configuration file cannot be read (e.g., file missing or corrupted),
     * default values are used, and an error message is printed to the console.</p>
     *
     * <p>Properties loaded:</p>
     * <ul>
     *     <li>{@code theme} - UI theme ("dark" by default)</li>
     *     <li>{@code saveBenchLogs} - Whether to save benchmark logs</li>
     *     <li>{@code generateAIReport} - Whether to generate an AI report</li>
     *     <li>{@code show_cpu_temp} - Whether to show CPU temperature</li>
     *     <li>{@code show_cpu_usage} - Whether to show CPU usage</li>
     *     <li>{@code show_gpu_temp} - Whether to show GPU temperature</li>
     *     <li>{@code show_gpu_usage} - Whether to show GPU usage</li>
     *     <li>{@code API_KEY} - API authentication key</li>
     *     <li>{@code API_URL} - API endpoint URL</li>
     *     <li>{@code model_description} - AI model description</li>
     *     <li>{@code model} - AI model name</li>
     *     <li>{@code main_delay} - Delay between updates (in seconds, default 2)</li>
     * </ul>
     *
     * @see java.util.Properties
     */
    public static void loadConfig() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            props.load(reader);

            theme = Theme.fromString(props.getProperty("theme", "dark"));
            saveBenchLogs = Boolean.parseBoolean(props.getProperty("saveBenchLogs", "false"));
            generateAIReport = Boolean.parseBoolean(props.getProperty("generateAIReport", "false"));
            showCPUTemp = Boolean.parseBoolean(props.getProperty("show_cpu_temp", "false"));
            showCPUUsage = Boolean.parseBoolean(props.getProperty("show_cpu_usage", "false"));
            showGPUTemp = Boolean.parseBoolean(props.getProperty("show_gpu_temp", "false"));
            showGPUUsage = Boolean.parseBoolean(props.getProperty("show_gpu_usage", "false"));
            API_KEY = props.getProperty("API_KEY", "");
            API_URL = props.getProperty("API_URL", "");
            MODEL_DESCRIPTION = props.getProperty("model_description", "");
            MODEL = props.getProperty("model", "");
            mainDelay = Integer.parseInt(props.getProperty("main_delay", "2"));
        } catch (IOException e) {
            System.out.println("Не вдалося завантажити конфіг. Використовуються стандартні значення.");
        }
    }

    /**
     * Saves the current configuration to the properties file.
     */
    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            props.store(writer, "System Scope Configuration");
        } catch (IOException e) {
            ScopeLogger.logError("Error while saving config: " + e);
            lastSave = false;
        }
    }

    /**
     * Returns the current theme.
     *
     * @return the theme
     */
    public static Theme getTheme() {
        return theme;
    }

    /**
     * Sets the theme and updates the config file.
     *
     * @param newTheme the new theme to set
     */
    public static void setTheme(Theme newTheme) {
        theme = newTheme;
        props.setProperty("theme", newTheme.toString());
        save();
    }

    /**
     * Checks if benchmark logs are enabled for saving.
     *
     * @return true if saving benchmark logs is enabled, false otherwise
     */
    public static boolean isSaveBenchLogs() {
        return saveBenchLogs;
    }

    /**
     * Toggles the saveBenchLogs flag and updates the config file.
     */
    public static void swapSaveBenchLogs() {
        saveBenchLogs = !isSaveBenchLogs();
        props.setProperty("saveBenchLogs", String.valueOf(saveBenchLogs));
        save();
    }

    /**
     * Checks if AI report generation is enabled.
     *
     * @return true if AI report generation is enabled, false otherwise
     */
    public static boolean isGenerateAIReport() { return generateAIReport; }

    /**
     * Checks if CPU temperature display is enabled.
     *
     * @return {@code true} if CPU temperature should be displayed, {@code false} otherwise.
     */
    public static boolean isShowCPUTemp() { return showCPUTemp;}

    /**
     * Sets the CPU temperature display option and saves the updated configuration.
     *
     * @param key {@code true} to enable CPU temperature display, {@code false} to disable.
     */
    public static void setShowCPUTemp(boolean key) {
        showCPUTemp = key;
        props.setProperty("show_cpu_temp", String.valueOf(showCPUTemp));
        save();
    }

    /**
     * Checks if CPU usage display is enabled.
     *
     * @return {@code true} if CPU usage should be displayed, {@code false} otherwise.
     */
    public static boolean isShowCPUUsage() { return showCPUUsage; }

    /**
     * Sets the CPU usage display option and saves the updated configuration.
     *
     * @param key {@code true} to enable CPU usage display, {@code false} to disable.
     */
    public static void setShowCPUUsage(boolean key) {
        showCPUUsage = key;
        props.setProperty("show_cpu_usage", String.valueOf(showCPUUsage));
        save();
    }

    /**
     * Checks if GPU temperature display is enabled.
     *
     * @return {@code true} if GPU temperature should be displayed, {@code false} otherwise.
     */
    public static boolean isShowGPUTemp() { return showGPUTemp; }

    /**
     * Sets the GPU temperature display option and saves the updated configuration.
     *
     * @param key {@code true} to enable GPU temperature display, {@code false} to disable.
     */
    public static void setShowGPUTemp(boolean key) {
        showGPUTemp = key;
        props.setProperty("show_gpu_temp", String.valueOf(showGPUTemp));
        save();
    }

    /**
     * Checks if GPU usage display is enabled.
     *
     * @return {@code true} if GPU usage should be displayed, {@code false} otherwise.
     */
    public static boolean isShowGPUUsage() { return showGPUUsage; }


    /**
     * Sets the GPU usage display option and saves the updated configuration.
     *
     * @param key {@code true} to enable GPU usage display, {@code false} to disable.
     */
    public static void setShowGPUUsage(boolean key) {
        showGPUUsage = key;
        props.setProperty("show_gpu_usage", String.valueOf(showGPUUsage));
        save();
    }

    /**
     * Checks if any benchmark metric display is enabled (CPU or GPU temperature/usage).
     *
     * @return {@code true} if any of the benchmark options are enabled, {@code false} otherwise.
     */
    public static boolean isShowBenchmark(){
        return showCPUTemp || showCPUUsage || showGPUTemp || showGPUUsage;
    }

    /**
     * Toggles the generateAIReport flag and updates the config file.
     */
    public static void swapGenerateAIReport() {
        generateAIReport = !isSaveBenchLogs();
        props.setProperty("generateAIReport", String.valueOf(saveBenchLogs));
        save();
    }

    /**
     * Returns the configured API key.
     *
     * @return the API key
     */
    public static String getAPI_KEY() {
        return API_KEY;
    }

    /**
     * Sets the API key and updates the config file.
     *
     * @param key the API key to set
     */
    public static void setAPI_KEY(String key) {
        API_KEY = key;
        props.setProperty("API_KEY", key);
        save();
    }

    /**
     * Returns the configured API URL.
     *
     * @return the API URL
     */
    public static String getAPI_URL() {
        return API_URL;
    }

    /**
     * Sets the API URL and updates the config file.
     *
     * @param url the API URL to set
     */
    public static void setAPI_URL(String url) {
        API_URL = url;
        props.setProperty("API_URL", url);
        save();
    }

    /**
     * Returns the model description.
     *
     * @return the model description
     */
    public static String getMODEL_DESCRIPTION() {
        return MODEL_DESCRIPTION;
    }

    /**
     * Sets the model description and updates the config file.
     *
     * @param desc the model description to set
     */
    public static void setMODEL_DESCRIPTION(String desc) {
        MODEL_DESCRIPTION = desc;
        props.setProperty("model_description", desc);
        save();
    }

    /**
     * Returns the model identifier or name.
     *
     * @return the model name
     */
    public static String getMODEL() {
        return MODEL;
    }

    /**
     * Sets the model identifier or name and updates the config file.
     *
     * @param model the model name to set
     */
    public static void setMODEL(String model) {
        MODEL = model;
        props.setProperty("model", model);
        save();
    }

    /**
     * Gets the current main delay value.
     *
     * @return The current main delay in seconds.
     */
    public static int getMainDelay(){ return mainDelay; }

    /**
     * Sets the main delay value and saves the updated configuration.
     *
     * @param delay The new main delay value in seconds.
     */
    public static void setMainDelay(int delay){
        mainDelay = delay;
        props.setProperty("main_delay", String.valueOf(delay));
        save();
    }

    /**
     * Returns the status of the last save operation.
     *
     * @return true if the last save was successful, false otherwise
     */
    public static boolean getSaveStatus(){
        return lastSave;
    }
}
