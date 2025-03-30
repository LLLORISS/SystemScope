package nm.sc.systemscope.modules;

/**
 * Represents information about a process.
 * Stores the process name and its process identifier (PID).
 */
public class ProcessInfo {
    private final String processName;
    private final int pid;

    /**
     * Constructs a ProcessInfo object with the specified process name and PID.
     *
     * @param processName The name of the process.
     * @param pid The process identifier (PID).
     */
    public ProcessInfo(String processName, int pid){
        this.processName = processName;
        this.pid = pid;
    }

    /**
     * Gets the process identifier (PID).
     *
     * @return The process identifier (PID).
     */
    public int getPid(){
        return this.pid;
    }

    /**
     * Returns a string representation of the process, including its PID and name.
     *
     * @return A string representation of the process in the format "(PID: <pid>) <processName>".
     */
    @Override public String toString(){
        return "(PID: " + pid + ") "+ processName;
    }
}
