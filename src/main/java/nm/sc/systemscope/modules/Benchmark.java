package nm.sc.systemscope.modules;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import nm.sc.systemscope.controllers.SystemScopeController;

import java.io.IOException;

/**
 * A class that contains information about the current benchmark
 */
public class Benchmark {
    private static String processName;
    private static String absolutePath;
    private static boolean benchmarkStarted;
    private static BenchWindow benchWindow = null;
    private static SystemScopeController controller;

    /**
     * Private constructor of the class
     */
    private Benchmark() {
        throw new UnsupportedOperationException("Benchmark is a static utility class and cannot be instantiated.");
    }

    /**
     * Static class constructor
     */
    static {
        processName = "";
        absolutePath = "";
        benchmarkStarted = false;
    }

    /**
     * Get the name of the process.
     *
     * @return Name Process.
     */
    public static String getProcessName(){
        return processName;
    }

    /**
     * Get the absolute path of the file.
     *
     * @return Absolute path of the file.
     */
    public static String getAbsolutePath(){
        return absolutePath;
    }

    /**
     * A method that runs the benchmark.
     * It initializes the benchmark, starts the process, and monitors its execution.
     *
     * @param instance The instance of SystemScopeController.
     */
    public static void startBenchmark(SystemScopeController instance){
        controller = instance;
        if(absolutePath != null && !absolutePath.isEmpty() && !benchmarkStarted){
            new Thread(() -> {
                benchmarkStarted = true;
                controller.swapBenchButton();
                if (launchFile(System.getProperty("os.name").toLowerCase())){{
                    benchWindow = new BenchWindow();
                    benchWindow.initialize();


                    waitForFileToClose();

                    Platform.runLater(() -> {
                        if (benchWindow != null) {
                            benchWindow.close();
                            benchWindow = null;
                        }
                    });

                    benchmarkStarted = false;
                    controller.swapBenchButton();

                }}
            }).start();

        }
    }

    /**
     * Stops the currently running benchmark.
     * Terminates the process and updates UI elements.
     */
    public static void stopBenchmark(){
        benchmarkStarted = false;

        if (processName != null && !processName.isEmpty()) {

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "pgrep -f \"" + processName + "\"");

            try {
                Process process = processBuilder.start();
                process.waitFor();

                String processID = new String(process.getInputStream().readAllBytes()).trim();

                if (!processID.isEmpty()) {
                    ProcessBuilder killProcessBuilder = new ProcessBuilder("bash", "-c", "kill -9 " + processID);
                    killProcessBuilder.start();
                } else {
                    System.out.println("Процес не знайдений.");
                }
                clearInfo();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }

        Platform.runLater(() -> {
            if (benchWindow != null) {
                benchWindow.close();
                benchWindow = null;
            }

            controller.swapBenchButton();
        });
    }

    /**
     * Method that runs the selected file for the benchmark.
     *
     * @param os Name of the operating system.
     * @return True if the process is started successfully, false otherwise.
     */
    private static boolean launchFile(String os){
        try{
            ProcessBuilder processBuilder;

            String selectedFile = Benchmark.getAbsolutePath();

            if(os.contains("win")){
                processBuilder = new ProcessBuilder(selectedFile);
            }
            else{
                processBuilder = new ProcessBuilder("bash", "-c", "chmod +x \"" + selectedFile + "\" && \"" + selectedFile + "\"");
            }

            Benchmark.setBenchmarkStarted(true);

            processBuilder.inheritIO();
            Process process = processBuilder.start();

            return process.isAlive();
        }
        catch(Exception e){
            System.err.println("Помилка запуску гри: " + e.getMessage());
            Platform.runLater(() -> {
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Перевірте правильність вибраного файлу.");
                alert.showAndWait();
            });
            return false;
        }
    }

    /**
     * A method that monitors the current status of the process.
     * The method ends when the benchmark file is closed.
     */
    private static void waitForFileToClose(){
        try{
            String selectedFile = Benchmark.getAbsolutePath();
            String processName = selectedFile.substring(selectedFile.lastIndexOf("/") + 1).replace(".sh", "");
            Benchmark.setProcessName(processName);
            boolean isRunning = true;

            while(isRunning){
                Thread.sleep(3000);
                isRunning = isProcessRunning(processName, System.getProperty("os.name").toLowerCase());
            }

            System.out.println("Гра завершена, закриваємо бенчмарк...");
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * A method that checks if the process is running in the operating system.
     *
     * @param processName Name of the process.
     * @param os          Name of the operating system.
     * @return True if the process is running, false otherwise.
     */
    private static boolean isProcessRunning(String processName, String os){
        try{
            Process process;
            if(os.contains("win")){
                process = new ProcessBuilder("tasklist").start();
            }
            else{
                process = new ProcessBuilder("bash", "-c", "pgrep -c -f \"" + processName + "\"").start();
            }

            String output = new String(process.getInputStream().readAllBytes());
            return Integer.parseInt(output.trim()) > 0;
        }
        catch(Exception e){
            return false;
        }
    }

    /**
     * Get the status of the benchmark work.
     *
     * @return True if the benchmark is running, false otherwise.
     */
    public static Boolean getBenchmarkStarted(){
        return benchmarkStarted;
    }

    /**
     * Set a new process name.
     *
     * @param process Name of the process.
     */
    public static void setProcessName(String process){
        processName = process;
    }

    /**
     * Set Absolute path of the file.
     *
     * @param path Absolute path of the file.
     */
    public static void setAbsolutePath(String path){
        absolutePath = path;
    }

    /**
     * Set the status of the benchmark.
     *
     * @param flag True if the benchmark is running, false otherwise.
     */
    public static void setBenchmarkStarted(boolean flag){
        benchmarkStarted = flag;
    }

    /**
     * Get the benchmark window instance.
     *
     * @return The BenchWindow instance.
     */
    public static BenchWindow getBenchWindow(){ return benchWindow;}

    /**
     * Clear all benchmark data.
     * This resets the process name, absolute path, and benchmark status.
     */
    public static void clearInfo(){
        processName = "";
        benchmarkStarted = false;
        absolutePath = "";
    }
}
