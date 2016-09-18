package co.mcsniper.mcsniper.sniper;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import co.mcsniper.mcsniper.MCSniper;

public class NameSniper implements Runnable {

    private MCSniper handler;

    private int snipeID;
    private long snipeDate;
    private String name;

    private String uuid;
    private String url;
    private String sessionCookie;
    private String password;

    private int proxyAmount;
    private int proxyInstances;
    private long proxyOffset;

    private Thread drone = null;
    private List<Thread> threads = new ArrayList<>();

    private Proxy[] proxySet;
    private String[][] responses;

    private boolean successful = false;
    private boolean done = false;

    public NameSniper(MCSniper handler, int snipeID, long snipeDate, String name, String uuid, String sessionCookie, String password, int proxyAmount, int proxyInstances, long proxyOffset) {
        this.handler = handler;

        this.name = name;
        this.snipeID = snipeID;
        this.snipeDate = snipeDate;

        this.uuid = uuid;
        this.url = "https://account.mojang.com/me/renameProfile/" + this.uuid;
        this.sessionCookie = sessionCookie;
        this.password = password;

        this.proxyAmount = proxyAmount;
        this.proxyInstances = proxyInstances;
        this.proxyOffset = proxyOffset;
    }

    public void run() {
        long clickTime = this.snipeDate + this.proxyOffset;
        long pushDelay = this.snipeDate + (30L * 1000L);

        while (this.handler.getWorldTime().currentTimeMillis() <= clickTime)
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        for (int server = 0; server < this.proxyAmount; server++) {
            for (int instance = 0; instance < this.proxyInstances; instance++) {
                Thread t = new Thread(new NameChanger(this, server, instance, this.url, this.proxySet[server], this.name, this.sessionCookie, this.password, this.responses));
                this.threads.add(t);
                t.start();
            }
        }

        while (this.handler.getWorldTime().currentTimeMillis() <= pushDelay)
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        for (Thread t : this.threads) {
            try {
                t.interrupt();
                t.suspend();
            } catch (Exception ignored) {
            }
        }

        StringBuilder logBuilder = new StringBuilder();
        String parseDate = MCSniper.DATE_FORMAT.format(this.snipeDate);
        
        logBuilder.append("Final Result: " + (this.successful ? "Successful" : "Failure") + "\n");
        logBuilder.append("Server Name: " + this.handler.getServerName() + "\n");
        logBuilder.append("Server Host: " + this.handler.getServerIP() + "\n\n");
        logBuilder.append("Name: " + this.name + "\n");
        logBuilder.append("Local Timestamp: " + parseDate + "\n");
        logBuilder.append("UNIX Timestamp: " + this.snipeDate + "\n\n");
        logBuilder.append("Proxy Count: " + this.proxyAmount + "\n");
        logBuilder.append("Proxy Instances: " + this.proxyInstances + "\n");
        logBuilder.append("Proxy Offset: " + (this.proxyOffset > 0 ? "+" : "-") + this.proxyOffset + "\n\n");

        for (int server = 0; server < this.proxyAmount; server++) {
            logBuilder.append("Session #" + (server + 1) + ": " + proxySet[server].toString() + "\n");

            for (int instance = 0; instance < this.proxyInstances; instance++) {
                String response = this.responses[server][instance];
                response = response == null ? "null" : response.replaceAll("\n", " ");
                logBuilder.append("\tInstance " + (instance + 1) + ": " + response);
            }

            logBuilder.append("\n");
        }

        this.handler.getMySQL().pushLog(this.handler.getServerName() + " / " + this.handler.getServerIP(), this.snipeID, this.name, parseDate, this.successful ? 1 : 0, logBuilder.toString());
        done = true;
    }

    public void start() {
        this.proxySet = new Proxy[this.proxyAmount];
        this.responses = new String[this.proxyAmount][this.proxyInstances];

        List<Proxy> allocatedProxies = this.handler.getProxyHandler().getProxies(this.proxyAmount);
        for (int i = 0; i < this.proxySet.length; i++) {
            this.proxySet[i] = allocatedProxies.get(i);
        }

        this.drone = new Thread(this);
        this.drone.start();
    }

    public long getDate() {
        return this.snipeDate;
    }

    public void setSuccessful() {
        this.successful = true;
        this.handler.getMySQL().updateStatus(this.snipeID, 1);
    }

    public boolean isDone() {
        return this.done;
    }

}