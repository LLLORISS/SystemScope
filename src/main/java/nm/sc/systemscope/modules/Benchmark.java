package nm.sc.systemscope.modules;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nm.sc.systemscope.SystemScopeMain;
import nm.sc.systemscope.controllers.BenchSelectorController;
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
    private static long startTime;
    private static long endTime;
    private static BenchSelectorController benchController;

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
    public static void startBenchmark(SystemScopeController instance) {
        controller = instance;

        if (!benchmarkStarted && benchWindow == null) {
            Platform.runLater(() -> {
                try {
                    showBenchSelector();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Starts the benchmark process. Initializes the benchmark and prompts the user to select a benchmark file.
     *
     */
    public static void startBenchmark() {
        if (!benchmarkStarted && benchWindow == null) {
            Platform.runLater(() -> {
                try {
                    showBenchSelector();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Show the benchmark selector and set the absolute path.
     */
    private static void showBenchSelector() throws IOException {
        FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource("BenchSelector-view.fxml"));
        Parent root = loader.load();
        benchController = loader.getController();

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        benchController.setStage(stage);
        benchController.setScene(scene);
        stage.setTitle("Вибір гри");
        stage.showAndWait();

        if (benchController.getStartClicked()) {
            absolutePath = benchController.getSelectedFile();
            if (absolutePath != null && !absolutePath.isEmpty()) {
                if(controller != null) {
                    controller.swapBenchButton();
                }
                startBenchmarkInBackground();
            } else {
                stopBenchmark();
            }
        }
    }

    /**
     * Starts the benchmark in a background task.
     */
    private static void startBenchmarkInBackground() {
        Task<Void> benchmarkTask = new Task<Void>() {
            @Override protected Void call() throws Exception {

                startTime = System.currentTimeMillis();

                benchmarkStarted = true;
                if (launchFile(System.getProperty("os.name").toLowerCase())) {
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
                    Platform.runLater(() -> {
                        if(controller != null) {
                            controller.swapBenchButton();
                        }
                    });
                }
                return null;
            }
        };

        Thread benchmarkThread = new Thread(benchmarkTask);
        benchmarkThread.setDaemon(true);
        benchmarkThread.start();
    }

    /**
     * Stops the currently running benchmark.
     */
    public static void stopBenchmark() {
        benchmarkStarted = false;

        if (processName != null && !processName.isEmpty()) {
            Task<Void> stopTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    endTime = System.currentTimeMillis();

                    stopRunningProcess();
                    return null;
                }
            };

            Thread stopThread = new Thread(stopTask);
            stopThread.setDaemon(true);
            stopThread.start();
        }

        Platform.runLater(() -> {
            if (benchWindow != null) {
                benchWindow.close();
                benchWindow = null;
            }

            if (controller != null) {
                controller.swapBenchButton();
            }
        });
    }

    /**
     * Stops the running process using process name.
     */
    private static void stopRunningProcess() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "pgrep -f \"" + processName + "\"");

        try {
            Process process = processBuilder.start();
            process.waitFor();

            String processID = new String(process.getInputStream().readAllBytes()).trim();
            System.out.println(processID);
            if (!processID.isEmpty()) {
                ProcessBuilder killProcessBuilder = new ProcessBuilder("bash", "-c", "kill -9 " + processID);
                killProcessBuilder.start();
            } else {
                System.out.println("Процес не знайдений.");
            }

            endTime = System.currentTimeMillis();

            double duration = (endTime - startTime) / 1000.0;

            DataStorage.createLogFile(processName, benchController.getBenchmarkName(), BenchWindow.getTemperaturesCPU(),
                    BenchWindow.getTemperaturesGPU(), BenchWindow.getUsagesCPU(), BenchWindow.getUsagesGPU(),
                    BenchWindow.getAverageTempCPU(), BenchWindow.getAverageTempGPU(), BenchWindow.getAverageUsageCPU(), BenchWindow.getAverageUsageGPU(), duration);

            clearInfo();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch the selected benchmark file.
     */
    private static boolean launchFile(String os) {
        try {
            ProcessBuilder processBuilder;
            String selectedFile = Benchmark.getAbsolutePath();

            if (os.contains("win")) {
                processBuilder = new ProcessBuilder(selectedFile);
            } else {
                processBuilder = new ProcessBuilder("bash", "-c", "chmod +x \"" + selectedFile + "\" && \"" + selectedFile + "\"");
            }

            Benchmark.setBenchmarkStarted(true);

            processBuilder.inheritIO();
            Process process = processBuilder.start();

            return process.isAlive();
        } catch (Exception e) {
            System.err.println("Помилка запуску гри: " + e.getMessage());
            Platform.runLater(() -> {
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Перевірте правильність вибраного файлу.");
                alert.showAndWait();
            });
            return false;
        }
    }

    /**
     * Wait for the benchmark file to close and end the process.
     */
    private static void waitForFileToClose() {
        try {
            String selectedFile = Benchmark.getAbsolutePath();
            String processName = selectedFile.substring(selectedFile.lastIndexOf("/") + 1).replace(".sh", "");
            Benchmark.setProcessName(processName);
            boolean isRunning = true;

            while (isRunning) {
                Thread.sleep(3000);
                isRunning = isProcessRunning(processName, System.getProperty("os.name").toLowerCase());
            }

            System.out.println("Гра завершена, закриваємо бенчмарк...");
        } catch (InterruptedException e) {
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
    private static boolean isProcessRunning(String processName, String os) {
        try {
            Process process;
            if (os.contains("win")) {
                process = new ProcessBuilder("tasklist").start();
            } else {
                process = new ProcessBuilder("bash", "-c", "pgrep -c -f \"" + processName + "\"").start();
            }

            String output = new String(process.getInputStream().readAllBytes());
            return Integer.parseInt(output.trim()) > 0;
        } catch (Exception e) {
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
