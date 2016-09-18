package co.mcsniper.mcsniper.sniper;

import java.net.Proxy;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.lyphiard.simplerequest.RequestType;
import com.lyphiard.simplerequest.SimpleHttpRequest;

public class NameChanger implements Runnable {

    private NameSniper main;

    private int server;
    private int instance;

    private String url;
    private Proxy proxy;

    private String name;
    private String session;
    private String password;
    private String authToken;

    private String[][] log;

    public NameChanger(NameSniper main, int server, int instance, String url, Proxy proxy, String name, String session, String password, String[][] log) {
        this.main = main;
        this.server = server;
        this.instance = instance;
        this.url = url;
        this.proxy = proxy;
        this.name = name;
        this.session = session;
        this.password = password;
        this.log = log;

        String rawSession = this.session;
        if (rawSession.startsWith("\"") && rawSession.endsWith("\"") && rawSession.length() > 2) {
            rawSession = rawSession.substring(1, rawSession.length() - 1);
        }
        
        rawSession = rawSession.substring(rawSession.indexOf('-') + 1);

        Map<String, String> arguments = new HashMap<String, String>();
        String[] components = rawSession.split("&");

        for (String component : components) {
            if (!component.contains("=")) {
                continue;
            }

            String key = component.substring(0, component.indexOf("="));
            String value = component.substring(component.indexOf("=") + 1, component.length());
            arguments.put(key, value);
        }        
        
        this.authToken = arguments.get("___AT");

        if (this.authToken == null) {
            throw new NullPointerException("Unable to properly identify Authenticity Token in cookie.");
        }
    }

    public void run() {
        try {
            long startTime = System.currentTimeMillis();

            String response = new SimpleHttpRequest(this.url)
                    .setProxy(this.proxy)
                    .addField("authenticityToken", this.authToken)
                    .addField("newName", this.name)
                    .addField("password", this.password)
                    .setType(RequestType.POST)
                    .setTimeout(30000)
                    .addHeader("Accept-Language", "en-US,en;q=0.8")
                    .setCookie("PLAY_SESSION", this.session)
                    .execute()
                    .getResponse();

            long endTime = System.currentTimeMillis();

            DecimalFormat decimalFormat = new DecimalFormat("+###,###;-####,###");
            this.log[this.server][this.instance] = "[" + endTime + "] [" + (endTime - startTime) + "ms] [" + decimalFormat.format(endTime - this.main.getDate()) + "ms] " + response;

            if (response.contains("Name changed")) {
                this.main.setSuccessful();
            }
        } catch (Exception ex) {
            this.log[this.server][this.instance] = ex.getClass().getSimpleName();
        }
    }

}