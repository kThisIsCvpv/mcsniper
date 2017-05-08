package co.mcsniper.mcsniper.sniper.gift;

import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import co.mcsniper.mcsniper.sniper.Response;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class GiftChanger extends TimerTask {

    private GiftSniper main;

    private Proxy proxy;

    private String name;
    private String session;
    private String code;
    private String authToken;
    private long proxyOffset;

    public GiftChanger(GiftSniper main, Proxy proxy, String name, String session, String code, long proxyOffset) {
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

            String fullAddress = this.proxy.toString().split("/")[1];
            String proxyAddress = fullAddress.split(":")[0];
            int proxyPort = Integer.parseInt(fullAddress.split(":")[1]);

            client = new WebClient(BrowserVersion.CHROME, proxyAddress, proxyPort);
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

            this.main.getLog().addResponse(new Response(
                    response,
                    webResponse.getStatusCode(),
                    endTime - this.main.getDate(),
                    webEndTime == -1 ? 0 : webEndTime - this.main.getDate(),
                    this.proxy,
                    this.proxyOffset
            ));

            if (response.contains("Profile created.")) {
                this.main.setSuccessful();
            }
        } catch (Exception ex) {
            long endTime = this.main.getHandler().getWorldTime().currentTimeMillis();

            this.main.getLog().addResponse(new Response(
                    ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : ""),
                    0,
                    endTime - this.main.getDate(),
                    0,
                    this.proxy,
                    this.proxyOffset
            ));
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