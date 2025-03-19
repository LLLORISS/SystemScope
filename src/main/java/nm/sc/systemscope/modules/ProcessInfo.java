package nm.sc.systemscope.modules;

/**
 * Represents information about a process.
 * Stores the process name and its process identifier (PID).
 */
public class ProcessInfo {
    private String processName;
    private int pid;

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
     * Gets the process name.
     *
     * @return The name of the process.
     */
    public String getProcessName() {
        return processName;
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
     * Sets the process identifier (PID).
     *
     * @param pid The new process identifier (PID).
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     * Sets the process name.
     *
     * @param processName The new name of the process.
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * Returns a string representation of the process, including its PID and name.
     *
     * @return A string representation of the process in the format "(PID: <pid>) <processName>".
     */
    @Override
    public String toString(){
        return "(PID: " + pid + ") "+ processName;
    }
}
