package co.mcsniper.mcsniper.util;

import java.util.ArrayList;
import java.util.List;

import co.mcsniper.mcsniper.proxy.ProxyHandler;

public class SecurityManager {

    private ProxyHandler proxyHandler;
    private List<String> verifiedCookies = new ArrayList<String>();

    public SecurityManager(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    public void verify(String cookie, String answers) {
        if (this.verifiedCookies.contains(cookie))
            return;
        
        this.verifiedCookies.add(cookie);
        new Thread(new SecurityPasser(cookie, answers, proxyHandler)).start();
    }
}
