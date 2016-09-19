package co.mcsniper.mcsniper.sniper;

import java.net.Proxy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

import co.mcsniper.mcsniper.MCSniper;
import co.mcsniper.mcsniper.sniper.util.Util;

@SuppressWarnings("deprecation")
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
    private String[][][] responses;

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
        logBuilder.append("Proxy Offset: " + (new DecimalFormat("+###,###;-###,###")).format(this.proxyOffset) + "ms\n\n");

        JSONObject responses = new JSONObject();
        List<String> validResponses = new ArrayList<String>();

        for (int server = 0; server < this.proxyAmount; server++) {
            logBuilder.append("Session #" + (server + 1) + ": " + proxySet[server].toString() + "\n");

            for (int instance = 0; instance < this.proxyInstances; instance++) {
                String response = this.responses[server][instance][0];
                long responseTime = Long.parseLong(this.responses[server][instance][1] == null ? "0" : this.responses[server][instance][1]);
                response = response == null ? "null" : StringEscapeUtils.unescapeJava(response.replaceAll("\n", " "));

                if (response.contains("<!DOCTYPE html>") || response.contains("<html")) {
                    response = "HTML Response";
                }

                logBuilder.append("\tInstance " + (instance + 1) + " ( " + (new DecimalFormat("+###,###;-###,###")).format(responseTime) + "ms ): " + response + "\n");

                if (!response.equals("null")) {
                    validResponses.add(responseTime + " " + response);
                }

                String parsedResponse = response.contains("Exception: ") ? response.substring(0, response.indexOf("Exception: ") + 9) : response;
                if (responses.has(parsedResponse)) {
                    responses.increment(parsedResponse);
                } else {
                    responses.put(parsedResponse, 1);
                }
            }

            logBuilder.append("\n");
        }

        Collections.sort(validResponses, new Comparator<String>() {
            public int compare(String o1, String o2) {
                try {
                    return Integer.compare(Integer.parseInt(o1.split(" ")[0]), Integer.parseInt(o2.split(" ")[0]));
                } catch (NumberFormatException ex) {
                    return o1.compareTo(o2);
                }
            }
        });

        logBuilder.append("#####################################\n\n");
        for (int x = 0; x < validResponses.size(); x++) {
            String orderedResponse = validResponses.get(x);
            String[] args = orderedResponse.split(" ");
            int responseTime = Util.isInteger(args[0]) ? Integer.parseInt(args[0]) : 0;

            String response = "";
            for (int i = 1; i < args.length; i++) {
                response += (i == 1 ? "" : " ") + args[i];
            }

            logBuilder.append("[ " + (new DecimalFormat("+###,###;-###,###")).format(responseTime) + "ms ] " + response);
            if (x != (validResponses.size() - 1)) {
                logBuilder.append("\n");
            }
        }

        JSONObject config = new JSONObject();
        config.put("proxies", this.proxyAmount);
        config.put("instances", this.proxyInstances);
        config.put("offset", this.proxyOffset);

        this.handler.getMySQL().pushLog(this.handler.getServerName(), this.snipeID, this.name, parseDate, this.successful ? 1 : 0, logBuilder.toString(), responses, config);
        done = true;
    }

    public void start() {
        this.proxySet = new Proxy[this.proxyAmount];
        this.responses = new String[this.proxyAmount][this.proxyInstances][2];

        List<Proxy> allocatedProxies = this.handler.getProxyHandler().getProxies(this.proxyAmount);
        for (int i = 0; i < this.proxySet.length; i++) {
            this.proxySet[i] = allocatedProxies.get(i);
        }

        this.drone = new Thread(this);
        this.drone.start();
    }

    public MCSniper getHandler() {
        return this.handler;
    }

    public String getName() {
        return this.name;
    }

    public String getUUID() {
        return this.uuid;
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