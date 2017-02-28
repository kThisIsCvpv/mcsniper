package co.mcsniper.mcsniper.util;

import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.lyphiard.simplerequest.RequestType;
import com.lyphiard.simplerequest.SimpleHttpRequest;
import com.lyphiard.simplerequest.SimpleHttpResponse;

import co.mcsniper.mcsniper.proxy.ProxyHandler;

public class SecurityPasser implements Runnable {

    private String cookies;
    private String password;
    private ProxyHandler handler;

    public SecurityPasser(String cookies, String password, ProxyHandler handler) {
        this.cookies = cookies;
        this.password = password;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            final HashMap<String, String> cookie = new HashMap<String, String>();
            cookie.put("PLAY_SESSION", this.cookies);

            String rawSession = new String(this.cookies);
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

            final String authToken = arguments.get("___AT");
            
            SimpleHttpResponse response = new SimpleHttpRequest("https://account.mojang.com/me")
                    .setCookies(cookie)
                    .setType(RequestType.POST)
                    .throwHttpErrors(false)
                    .execute();
            
            if (response.getResponse().contains("Confirm your identity")) {
                String[] questionIds = new String[3];

                for (int i = 0; i < 3; i++) {
                    questionIds[i] = "0";
                }

                Document doc = Jsoup.parse(response.getResponse());

                for (Element input : doc.getElementsByTag("input")) {
                    if (input.hasAttr("name") && input.attr("name").startsWith("questionId")) {
                        int index = Integer.parseInt(input.attr("name").replace("questionId", ""));
                        questionIds[index] = input.attr("value");
                    }
                }

                final String[] finalQuestions = questionIds.clone();
                final List<Proxy> proxies = this.handler.getAllProxies();
                
                for (final Proxy proxy : proxies) {
                    new Thread(new Runnable(){
                        public void run(){
                            for(int i = 0; i < 2; i++) {
                                try {
                                    SimpleHttpResponse challenge  = new SimpleHttpRequest("https://account.mojang.com/me/completeChallenge")
                                            .setProxy(proxy)
                                            .addField("authenticityToken", authToken)
                                            .addField("answer0", password)
                                            .addField("answer1", password)
                                            .addField("answer2", password)
                                            .addField("questionId0", finalQuestions[0])
                                            .addField("questionId1", finalQuestions[1])
                                            .addField("questionId2", finalQuestions[2])
                                            .setCookies(cookie)
                                            .setType(RequestType.POST)
                                            .throwHttpErrors(false).execute();
                                    
                                    if (challenge.getResponse().equals("Security challenge passed.")) 
                                        break;
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    
                    Thread.sleep(10L);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
