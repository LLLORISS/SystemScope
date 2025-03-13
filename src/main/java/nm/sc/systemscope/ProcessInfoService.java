package nm.sc.systemscope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessInfoService {
    public static List<ProcessInfo> getRunningProcesses() throws IOException {
        List<ProcessInfo> processList = new ArrayList<>();

        String os = System.getProperty("os.name").toLowerCase();
        Process process;

        if (os.contains("win")) {
            process = Runtime.getRuntime().exec("tasklist /fo csv");
        } else {
            process = Runtime.getRuntime().exec("ps -e");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        if (!os.contains("win")) {
            reader.readLine();
        }

        while ((line = reader.readLine()) != null) {
            String[] processDetails = line.trim().split("\\s+");

            if (processDetails.length >= 4) {
                String pidString = processDetails[0];
                String processName = processDetails[3];

                int pid = -1;
                try {
                    pid = Integer.parseInt(pidString);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid PID: " + pidString);
                }

                if (pid != -1) {
                    processList.add(new ProcessInfo(processName, pid));
                }
            }
        }

        return processList;
    }


    public static void killProcess(int pid) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        Process process;

        if(os.contains("win")){
            process = Runtime.getRuntime().exec("taskkill /PID " + pid);
        }
        else{
            process = Runtime.getRuntime().exec("kill -9 " + pid);
        }

        process.waitFor();
    }

    public static List<ProcessInfo> searchProcess(String searchInput) throws IOException {
        List<ProcessInfo> filtered = new ArrayList<>();

        boolean isNumeric = false;
        int searchPid = -1;
        try {
            searchPid = Integer.parseInt(searchInput);
            isNumeric = true;
        } catch (NumberFormatException e) {
            isNumeric = false;
        }

        for (ProcessInfo process : getRunningProcesses()) {
            boolean matchesName = process.getProcessName().toLowerCase().contains(searchInput.toLowerCase());

            boolean matchesPid = false;
            if (isNumeric) {
                String pidString = String.valueOf(process.getPid());
                if (pidString.contains(searchInput)) {
                    matchesPid = true;
                }
            }

            if (matchesName || matchesPid) {
                filtered.add(process);
            }
        }

        return filtered;
    }
}
