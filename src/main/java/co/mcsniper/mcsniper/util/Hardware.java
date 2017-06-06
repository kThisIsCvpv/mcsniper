package co.mcsniper.mcsniper.util;

import co.mcsniper.mcsniper.MCSniper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;

public class Hardware {

    public static void updateHardware(MCSniper sniper) {
        Cpu cpu = getCpu();
        double totalMemory = getTotalMemory();

        try {
            PreparedStatement ps = sniper.getMySQL().createConnection().prepareStatement("INSERT INTO hardware (server, cpu_model, cpu_threads, total_memory) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE cpu_model = ?, cpu_threads = ?, total_memory = ?");
            ps.setString(1, sniper.getServerName());
            ps.setString(2, cpu.getModel());
            ps.setInt(3, cpu.getThreadCount());
            ps.setDouble(4, totalMemory);
            ps.setString(5, cpu.getModel());
            ps.setInt(6, cpu.getThreadCount());
            ps.setDouble(7, totalMemory);
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

    protected static double getTotalMemory() {
        String[] memInfo = readFile("/proc/meminfo").split("\n");

        double totalMemory = 0;

        for (String line : memInfo) {
            line = line.trim().replaceAll("\\s+", " ");

            if (line.startsWith("MemTotal: ")) {
                try {
                    totalMemory = Double.parseDouble(line.replace("MemTotal: ", "").replace(" kB", "")) / 1024 / 1024;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return Math.round(totalMemory * 100.0) / 100.0;
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
