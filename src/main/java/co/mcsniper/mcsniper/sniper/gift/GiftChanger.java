package co.mcsniper.mcsniper.sniper.gift;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import co.mcsniper.mcsniper.proxy.SniperProxy;
import co.mcsniper.mcsniper.sniper.Response;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.lyphiard.simplerequest.RequestType;
import com.lyphiard.simplerequest.SimpleHttpRequest;
import com.lyphiard.simplerequest.SimpleHttpResponse;

public class GiftChanger extends TimerTask {

    private GiftSniper main;

    private SniperProxy proxy;

    private String name;
    private String session;
    private String code;
    private String authToken;
    private long proxyOffset;

    public GiftChanger(GiftSniper main, SniperProxy proxy, String name, String session, String code, long proxyOffset) {
        this.main = main;
        this.proxy = proxy;
        this.name = name;
        this.session = session;
        this.code = code;
        this.proxyOffset = proxyOffset;

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
        if (this.proxy.isSocks()) {
            this.runSOCKS();
        } else {
            this.runHTTP();
        }
    }

    public void runSOCKS() {
        try {            
            Proxy socks = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(this.proxy.getIp(), this.proxy.getPort()));
            SimpleHttpResponse response = new SimpleHttpRequest("https://account.mojang.com/redeem/createProfile")
                    .setProxy(socks)
                    .addField("authenticityToken", this.authToken)
                    .addField("profileName", this.name)
                    .addField("termsId", "231")
                    .addField("acceptTerms", "on")
                    .addField("code", this.code)
                    .setType(RequestType.POST)
                    .setTimeout(30000)
                    .addHeader("Accept-Language", "en-US,en;q=0.8")
                    .setCookie("PLAY_SESSION", this.session)
                    .throwHttpErrors(false)
                    .execute();
            
            long webEndTime = -1;
            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();

            this.main.getLog().addResponse(new Response(response.getResponse(), response.getResponseCode(), endTime - this.main.getDate(), webEndTime == -1 ? 0 : webEndTime - this.main.getDate(), this.proxy, this.proxyOffset));

            if (response.getResponse().contains("Profile created.")) {
                this.main.setSuccessful();
            }
        } catch (Exception ex) {
            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();
            this.main.getLog().addResponse(new Response(ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : ""), 0, endTime - this.main.getDate(), 99999, this.proxy, this.proxyOffset));
        }
    }

    public void runHTTP() {
        WebClient client = null;

        try {
            WebRequest request = new WebRequest(new URL("https://account.mojang.com/redeem/createProfile"), HttpMethod.POST);

            request.setAdditionalHeader("Accept", "*/*");
            request.setAdditionalHeader("Accept-Encoding", "gzip, deflate, br");
            request.setAdditionalHeader("Accept-Language", "en-US,en;q=0.8");
            request.setAdditionalHeader("Connection", "keep-alive");
            request.setAdditionalHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            request.setAdditionalHeader("DNT", "1");
            request.setAdditionalHeader("Host", "account.mojang.com");
            request.setAdditionalHeader("Origin", "https://account.mojang.com");
            request.setAdditionalHeader("Referer", "https://account.mojang.com");
            request.setAdditionalHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
            request.setAdditionalHeader("X-Requested-With", "XMLHttpRequest");

            request.setRequestParameters(new ArrayList<NameValuePair>());
            request.getRequestParameters().add(new NameValuePair("authenticityToken", this.authToken));
            request.getRequestParameters().add(new NameValuePair("profileName", this.name));
            request.getRequestParameters().add(new NameValuePair("termsId", "231"));
            request.getRequestParameters().add(new NameValuePair("acceptTerms", "on"));
            request.getRequestParameters().add(new NameValuePair("code", this.code));

            client = new WebClient(BrowserVersion.CHROME, this.proxy.getIp(), this.proxy.getPort());

            if (this.proxy.getUsername() != null && this.proxy.getPassword() != null) {
                DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) client.getCredentialsProvider();
                credentialsProvider.addCredentials(this.proxy.getUsername(), this.proxy.getPassword());
            }

            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setCssEnabled(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setThrowExceptionOnScriptError(false);
            client.getOptions().setTimeout(45000);
            client.getCookieManager().addCookie(new Cookie("account.mojang.com", "PLAY_SESSION", this.session));

            WebResponse webResponse = client.getPage(request).getWebResponse();

            String date = webResponse.getResponseHeaderValue("Date");
            long webEndTime = -1;

            if (date != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss z", Locale.US);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                try {
                    webEndTime = simpleDateFormat.parse(date).getTime();
                } catch (ParseException e) {
                    webEndTime = -1;
                }
            }

            String response = webResponse.getContentAsString();

            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();

            this.main.getLog().addResponse(new Response(response, webResponse.getStatusCode(), endTime - this.main.getDate(), webEndTime == -1 ? 0 : webEndTime - this.main.getDate(), this.proxy, this.proxyOffset));

            if (response.contains("Profile created.")) {
                this.main.setSuccessful();
            }
        } catch (Exception ex) {
            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();

            this.main.getLog().addResponse(new Response(ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : ""), 0, endTime - this.main.getDate(), 99999, this.proxy, this.proxyOffset));
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ex) {

                }
            }
        }
    }

}