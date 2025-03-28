package nm.sc.systemscope.modules;

/**
 * Represents a benchmark log file with its absolute path and file name.
 * This class is used to encapsulate the details of a benchmark log file
 * such as its location and name for easier management and retrieval.
 */
public class ScopeBenchLog {
    private String absolutePath;
    private String fileName;

    /**
     * Default constructor that initializes the fields to null.
     */
    public ScopeBenchLog(){
        this.absolutePath = null;
        this.fileName = null;
    }

    /**
     * Constructor that initializes the benchmark log with the specified absolute path and file name.
     *
     * @param absolutePath the absolute path of the benchmark log file.
     * @param fileName the name of the benchmark log file.
     */
    public ScopeBenchLog(String absolutePath, String fileName){
        this.absolutePath = absolutePath;
        this.fileName = fileName;
    }

    /**
     * Gets the absolute path of the benchmark log file.
     *
     * @return the absolute path of the log file.
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Gets the name of the benchmark log file.
     *
     * @return the name of the log file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the absolute path of the benchmark log file.
     *
     * @param absolutePath the new absolute path of the log file.
     */
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    /**
     * Sets the name of the benchmark log file.
     *
     * @param fileName the new name of the log file.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns a string representation of the benchmark log file, which is the file name.
     *
     * @return the file name of the benchmark log file.
     */
    @Override public String toString(){
        return fileName;
    }
}
