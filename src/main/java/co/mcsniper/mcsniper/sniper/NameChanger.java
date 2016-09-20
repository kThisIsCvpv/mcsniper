package co.mcsniper.mcsniper.sniper;

import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

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

    private String[][][] log;

    public NameChanger(NameSniper main, int server, int instance, String url, Proxy proxy, String name, String session, String password, String[][][] log) {
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
        WebClient client = null;
        
        try {
            WebRequest request = new WebRequest(new URL(this.url), HttpMethod.POST);
            
            request.setAdditionalHeader("Accept", "*/*");
            request.setAdditionalHeader("Accept-Encoding", "gzip, deflate, br");
            request.setAdditionalHeader("Accept-Language", "en-US,en;q=0.8");
            request.setAdditionalHeader("Connection", "keep-alive");
            request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            request.setAdditionalHeader("DNT", "1");
            request.setAdditionalHeader("Host", "account.mojang.com");
            request.setAdditionalHeader("Origin", "https://account.mojang.com");
            request.setAdditionalHeader("Referer", this.url);
            request.setAdditionalHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
            request.setAdditionalHeader("X-Requested-With", "XMLHttpRequest");
            
            request.setRequestParameters(new ArrayList<NameValuePair>());
            request.getRequestParameters().add(new NameValuePair("authenticityToken", this.authToken));
            request.getRequestParameters().add(new NameValuePair("newName", this.name));
            request.getRequestParameters().add(new NameValuePair("password", this.password));

            String fullAddress = this.proxy.toString().split("/")[1];
            String proxyAddress = fullAddress.split(":")[0];
            int proxyPort = Integer.parseInt(fullAddress.split(":")[1]);
            
            client = new WebClient(BrowserVersion.CHROME, proxyAddress, proxyPort);
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setCssEnabled(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setThrowExceptionOnScriptError(false);
            
            String response = client.getPage(request).getWebResponse().getContentAsString();
                        
//            String response = new SimpleHttpRequest(this.url)
//                    .setProxy(this.proxy)
//                    .addField("authenticityToken", this.authToken)
//                    .addField("newName", this.name)
//                    .addField("password", this.password)
//                    .setType(RequestType.POST)
//                    .setTimeout(30000)
//                    .addHeader("Accept-Language", "en-US,en;q=0.8")
//                    .setCookie("PLAY_SESSION", this.session)
//                    .execute()
//                    .getResponse();

            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();

            this.log[this.server][this.instance][0] = response;
            this.log[this.server][this.instance][1] = (endTime - this.main.getDate()) + "";

            if (response.contains("Name changed")) {
                this.main.setSuccessful();
            }
        } catch (Exception ex) {
            this.log[this.server][this.instance][0] = ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : "");

            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();
            this.log[this.server][this.instance][1] = (endTime - this.main.getDate()) + "";
        } finally {
            if(client != null) {
                try {
                    client.close();
                } catch (Exception ex) {
                    
                }
            }
        }
    }

}