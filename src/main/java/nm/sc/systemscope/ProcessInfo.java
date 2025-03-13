package nm.sc.systemscope;

public class ProcessInfo {
    private String processName;
    private int pid;

    public ProcessInfo(String processName, int pid){
        this.processName = processName;
        this.pid = pid;
    }

    public String getProcessName() {
        return processName;
    }

    public int getPid(){
        return this.pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public String toString(){
        return "(PID: " + pid + ") "+ processName;
    }
}
