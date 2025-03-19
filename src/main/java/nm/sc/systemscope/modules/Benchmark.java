package nm.sc.systemscope.modules;

/**
 * A class that contains information about the current benchmark
 */
public class Benchmark {
    private static String processName;
    private static String absolutePath;
    private static boolean benchmarkStarted;

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
     * Get the status of the benchmark work.
     *
     * @return Status of benchmark.
     */
    public static Boolean getBenchmarkStarted(){
        return benchmarkStarted;
    }

    /**
     * Set new process name.
     *
     * @param process Name of process.
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
     * Set status of benchmark.
     *
     * @param flag Status of benchmark.
     */
    public static void setBenchmarkStarted(boolean flag){
        benchmarkStarted = flag;
    }

    /**
     * Clear all benchmark data.
     *
     */
    public static void clearInfo(){
        processName = "";
        benchmarkStarted = false;
        absolutePath = "";
    }
}
