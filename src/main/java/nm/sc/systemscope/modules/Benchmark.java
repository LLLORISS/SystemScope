package nm.sc.systemscope.modules;

public class Benchmark {
    private static String processName;
    private static String absolutePath;
    private static Boolean benchmarkStarted;

    static {
        processName = "";
        absolutePath = "";
        benchmarkStarted = false;
    }

    public static String getProcessName(){
        return processName;
    }

    public static String getAbsolutePath(){
        return absolutePath;
    }

    public static Boolean getBenchmarkStarted(){
        return benchmarkStarted;
    }

    public static void setProcessName(String process){
        processName = process;
    }

    public static void setAbsolutePath(String path){
        absolutePath = path;
    }

    public static void setBenchmarkStarted(Boolean flag){
        benchmarkStarted = flag;
    }

    public static void cleanInfo(){
        processName = "";
        benchmarkStarted = false;
        absolutePath = "";
    }
}
