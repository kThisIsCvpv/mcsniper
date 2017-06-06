package co.mcsniper.mcsniper.util;

import co.mcsniper.mcsniper.MCSniper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;

public class Hardware {

    public static void updateHardware(MCSniper sniper) {
        Cpu cpu = getCpu();
        double[] memory = getMemory();
        double[] load = getLoad();

        try {
            PreparedStatement ps = sniper.getMySQL().createConnection().prepareStatement("INSERT INTO hardware " +
                    "(server, cpu_model, cpu_threads, total_memory, free_memory, load_1m, load_5m, load_15m) " +
                    "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE cpu_model = ?, cpu_threads = ?, total_memory = ?," +
                    "free_memory = ?, load_1m = ?, load_5m = ?, load_15m = ?");
            ps.setString(1, sniper.getServerName());
            ps.setString(2, cpu.getModel());
            ps.setInt(3, cpu.getThreadCount());
            ps.setDouble(4, memory[0]);
            ps.setDouble(5, memory[1]);
            ps.setDouble(6, load[0]);
            ps.setDouble(7, load[1]);
            ps.setDouble(8, load[2]);
            ps.setString(9, cpu.getModel());
            ps.setInt(10, cpu.getThreadCount());
            ps.setDouble(11, memory[0]);
            ps.setDouble(12, memory[1]);
            ps.setDouble(13, load[0]);
            ps.setDouble(14, load[1]);
            ps.setDouble(15, load[2]);
            ps.execute();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static Cpu getCpu() {
        String[] cpuInfo = readFile("/proc/cpuinfo").split("\n");

        int threadCount = 0;
        String model = "Unknown";

        for (String line : cpuInfo) {
            line = line.trim().replaceAll("\\s+", " ");

            if (line.startsWith("processor :")) {
                threadCount++;
            }

            if (line.startsWith("model name :")) {
                model = line.replace("model name : ", "");
            }
        }

        return new Cpu(model, threadCount);
    }

    protected static double[] getMemory() {
        String[] memInfo = readFile("/proc/meminfo").split("\n");

        double totalMemory = 0;
        double freeMemory = 0;

        for (String line : memInfo) {
            line = line.trim().replaceAll("\\s+", " ");

            if (line.startsWith("MemTotal: ")) {
                try {
                    totalMemory = Double.parseDouble(line.replace("MemTotal: ", "").replace(" kB", "")) / 1024 / 1024;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (line.startsWith("MemFree: ")) {
                try {
                    freeMemory = Double.parseDouble(line.replace("MemFree: ", "").replace(" kB", "")) / 1024 / 1024;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return new double[]{
                Math.round(totalMemory * 100.0) / 100.0,
                Math.round(freeMemory * 100.0) / 100.0
        };
    }

    protected static double[] getLoad() {
        String[] loadAvg = readFile("/proc/loadavg").trim().replaceAll("\\s+", " ").split(" ");

        return new double[]{
                Math.round(Double.parseDouble(loadAvg[0]) * 100.0) / 100.0,
                Math.round(Double.parseDouble(loadAvg[1]) * 100.0) / 100.0,
                Math.round(Double.parseDouble(loadAvg[2]) * 100.0) / 100.0,
        };
    }

    protected static String readFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            StringBuilder file = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                file.append(line).append("\n");
            }

            return file.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private static class Cpu {

        private int threadCount;
        private String model;

        public Cpu(String model, int threadCount) {
            this.threadCount = threadCount;
            this.model = model;
        }

        public String getModel() {
            return this.model;
        }

        public int getThreadCount() {
            return this.threadCount;
        }

    }

}
