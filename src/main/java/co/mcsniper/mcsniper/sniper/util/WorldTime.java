package co.mcsniper.mcsniper.sniper.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WorldTime {

    public static void main(String[] args) throws Exception {
        WorldTime wt = new WorldTime();
        while(true) {
            long time = System.currentTimeMillis();
            long worldTime = wt.currentTimeMillis();
            
            System.out.println(time + " | " + worldTime + " | " + (worldTime - time));
            Thread.sleep(100L);
        }
    }
    
    private String[] timeDelay = new String[20];
    private Thread timedCalculator;

    public WorldTime() {
        calculateDelay(true);

        timedCalculator = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    calculateDelay(false);
                }
            }
        });

        timedCalculator.start();
    }

    public long getDelay() {
        long currentDelay = 0;
        for (int i = 0; i < timeDelay.length; i++) {
            if (timeDelay[i] != null) {
                currentDelay += Long.parseLong(timeDelay[i]);
            } else {
                return currentDelay / (i + 1);
            }
        }
        return currentDelay / timeDelay.length;
    }

    public void calculateDelay(boolean debug) {
        for (int i = 0; i < timeDelay.length; i++) {
            boolean failed = true;
            while (failed) {
                try {
                    if (!debug) {
                        Thread.sleep(2500L);
                    }

                    URL url = new URL("https://timeserver.mcsniper.co/");

                    URLConnection connection = url.openConnection();
                    long requestTime = System.currentTimeMillis();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String parsed = "";

                    String reader;
                    while ((reader = br.readLine()) != null) {
                        parsed += reader;
                    }

                    String readLine = (parsed.trim().replaceAll("\\.", "") + "0000").substring(0, 13);
                    long serverTime = Long.parseLong(readLine) - ((System.currentTimeMillis() - requestTime) / 2L);
                    timeDelay[i] = Long.toString(serverTime - System.currentTimeMillis());

                    br.close();
                    failed = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis() + this.getDelay();
    }

}