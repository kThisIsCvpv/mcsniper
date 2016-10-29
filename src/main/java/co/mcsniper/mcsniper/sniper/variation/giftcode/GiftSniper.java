package co.mcsniper.mcsniper.sniper.variation.giftcode;

import java.net.Proxy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

import co.mcsniper.mcsniper.AbstractSniper;
import co.mcsniper.mcsniper.MCSniper;
import co.mcsniper.mcsniper.sniper.util.Util;

@SuppressWarnings("deprecation")
public class GiftSniper extends AbstractSniper implements Runnable {

    private static final DecimalFormat timeFormat = new DecimalFormat("+###,###;-###,###");

    private MCSniper handler;
    private int snipeID;
    
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

    public GiftSniper(MCSniper handler, int snipeID, long snipeDate, String name, String sessionCookie, String giftcode, int proxyAmount, int proxyInstances, long proxyOffset) {
        super(name, snipeDate);
        this.handler = handler;

        this.snipeID = snipeID;

        this.sessionCookie = sessionCookie;
        this.giftcode = giftcode;

        this.proxyAmount = proxyAmount;
        this.proxyInstances = proxyInstances;
        this.proxyOffset = proxyOffset;
    }
    
    public void run() {
        long clickTime = this.getDate() + this.proxyOffset;
        long pushDelay = this.getDate() + (30L * 1000L);

        int count = 0;
        long systemTimeOffset = System.currentTimeMillis() - this.getHandler().getWorldTime().currentTimeMillis();

        for (int server = 0; server < this.proxyAmount; server++) {
            for (int instance = 0; instance < this.proxyInstances; instance++) {
                Date date = new Date(clickTime + ((count % 2 == 0 ? 1 : -1) * (2 * (long) Math.ceil(count / 2D))) + systemTimeOffset);
                (new Timer()).schedule(new GiftChanger(
                        this,
                        server,
                        instance,
                        this.proxySet[server],
                        this.getName(),
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
        String parseDate = MCSniper.DATE_FORMAT.format(this.getDate());

        logBuilder.append("Final Result: ").append(this.successful ? "Success\n" : "Fail\n");
        logBuilder.append("Server Name: ").append(this.handler.getServerName()).append("\n");
        logBuilder.append("Server Host: ").append(this.handler.getServerIP()).append("\n\n");
        logBuilder.append("Name: ").append(this.getDate()).append("\n");
        logBuilder.append("Gift Code Snipe: Yes\n");
        logBuilder.append("Local Timestamp: ").append(parseDate).append("\n");
        logBuilder.append("UNIX Timestamp: ").append(this.getDate()).append("\n\n");
        logBuilder.append("Proxy Count: ").append(this.proxyAmount).append("\n");
        logBuilder.append("Proxy Instances: ").append(this.proxyInstances).append("\n");
        logBuilder.append("Proxy Offset: ").append((new DecimalFormat("+###,###;-###,###")).format(this.proxyOffset)).append("ms\n\n");

        JSONObject responses = new JSONObject();
        List<String> validResponses = new ArrayList<String>();

        for (int server = 0; server < this.proxyAmount; server++) {
            logBuilder.append("Session #").append(server + 1).append(": ").append(proxySet[server].toString()).append("\n");

            for (int instance = 0; instance < this.proxyInstances; instance++) {
                String response = this.responses[server][instance][0];
                long responseTime = Long.parseLong(this.responses[server][instance][1] == null ? "0" : this.responses[server][instance][1]);
                long webResponseTime = Long.parseLong(this.responses[server][instance][2] == null ? "0" : this.responses[server][instance][2]);
                response = response == null ? "null" : StringEscapeUtils.unescapeJava(response.replaceAll("\n", " "));

                if (response.toLowerCase().contains("<!doctype") || response.toLowerCase().contains("<html")) {
                    response = "HTML Response";
                } else if (response.toLowerCase().contains("501 not implemented")) {
                    response = "HTTP 501";
                } else if (response.toLowerCase().contains("404 not found")) {
                    response = "HTTP 404";
                } else if (response.contains("<") && response.contains(">")) {
                    response = "HTML Response (2)";
                }

                logBuilder.append("\tInstance ")
                        .append(instance + 1)
                        .append(" ( ")
                        .append(timeFormat.format(responseTime))
                        .append("ms ): ")
                        .append(response)
                        .append("\n");

                if (!response.equals("null")) {
                    validResponses.add(responseTime + " " + webResponseTime + " " + response);
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
            long responseTime = Util.isLong(args[0]) ? Long.parseLong(args[0]) : 0;
            long webResponseTime = Util.isLong(args[1]) ? Long.parseLong(args[1]) : 0;

            String response = "";
            for (int i = 2; i < args.length; i++) {
                response += (i == 2 ? "" : " ") + args[i];
            }

            logBuilder.append("[ ")
                    .append(timeFormat.format(responseTime))
                    .append("ms ] [")
                    .append(timeFormat.format(webResponseTime))
                    .append("ms ] ")
                    .append(response);
            if (x != (validResponses.size() - 1)) {
                logBuilder.append("\n");
            }
        }

        JSONObject config = new JSONObject();
        config.put("proxies", this.proxyAmount);
        config.put("instances", this.proxyInstances);
        config.put("offset", this.proxyOffset);

        this.handler.getMySQL().pushLog(this.handler.getServerName(), this.snipeID, this.getName(), parseDate, this.successful ? 1 : 0, logBuilder.toString(), responses, config);
        done = true;
    }

    public void start() {
        this.proxySet = new Proxy[this.proxyAmount];
        this.responses = new String[this.proxyAmount][this.proxyInstances][3];

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

    public void setSuccessful() {
        this.successful = true;
        this.handler.getMySQL().updateStatus(this.snipeID, 1);
    }

    public boolean isDone() {
        return this.done;
    }

}