package co.mcsniper.mcsniper.sniper.giftcode;

import co.mcsniper.mcsniper.MCSniper;
import co.mcsniper.mcsniper.sniper.util.Util;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

import java.net.Proxy;
import java.text.DecimalFormat;
import java.util.*;

public class GCSniper implements Runnable {

    private MCSniper handler;

    private int snipeID;
    private long snipeDate;
    private String name;

    private String sessionCookie;
    private String giftcode;

    private int proxyAmount;
    private int proxyInstances;
    private long proxyOffset;

    private Thread drone = null;
    private List<Thread> threads = new ArrayList<>();

    private Proxy[] proxySet;
    private String[][][] responses;

    private boolean successful = false;
    private boolean done = false;

    public GCSniper(MCSniper handler, int snipeID, long snipeDate, String name, String sessionCookie, String giftcode, int proxyAmount, int proxyInstances, long proxyOffset) {
        this.handler = handler;

        this.name = name;
        this.snipeID = snipeID;
        this.snipeDate = snipeDate;

        this.sessionCookie = sessionCookie;
        this.giftcode = giftcode;

        this.proxyAmount = proxyAmount;
        this.proxyInstances = proxyInstances;
        this.proxyOffset = proxyOffset;
    }

    public void run() {
        long clickTime = this.snipeDate + this.proxyOffset;
        long pushDelay = this.snipeDate + (30L * 1000L);

        int count = 0;
        long systemTimeOffset = System.currentTimeMillis() - this.getHandler().getWorldTime().currentTimeMillis();

        for (int server = 0; server < this.proxyAmount; server++) {
            for (int instance = 0; instance < this.proxyInstances; instance++) {
                Date date = new Date(clickTime + ((count % 2 == 0 ? 1 : -1) * (2 * (long) Math.ceil(count / 2D))) + systemTimeOffset);
                (new Timer()).schedule(new GCChanger(
                        this,
                        server,
                        instance,
                        this.proxySet[server],
                        this.name,
                        this.sessionCookie,
                        this.giftcode,
                        this.responses
                ), date);

                count++;
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

        logBuilder.append("Giftcode Snipe: true\n");
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

                if (response.toLowerCase().contains("<!doctype") || response.toLowerCase().contains("<html")) {
                    response = "HTML Response";
                } else if (response.toLowerCase().contains("501 not implemented")) {
                    response = "HTTP 501";
                } else if (response.toLowerCase().contains("404 not found")) {
                    response = "HTTP 404";
                } else if (response.contains("<p>Redirecting <img src=\"/images/ajax-loader.gif\"></p>")) {
                    response = "Redirecting";
                } else if (response.contains("<") && response.contains(">")) {
                    response = "HTML Response (2)";
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
