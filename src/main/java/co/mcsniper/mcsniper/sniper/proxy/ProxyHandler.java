package co.mcsniper.mcsniper.sniper.proxy;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ProxyHandler {

    private ProxyValidator proxyValidator = new ProxyValidator();

    private List<Proxy> availableProxies = new ArrayList<>();
    private int currentProxy = 0;

    public ProxyHandler(File inputFile) throws IOException {
        Scanner fileScanner = new Scanner(inputFile);

        while (fileScanner.hasNextLine()) {
            String proxy = fileScanner.nextLine().trim();
            if (this.proxyValidator.validateProxy(proxy)) {
                this.availableProxies.add(new Proxy(Type.HTTP, new InetSocketAddress(proxy.split(":")[0], Integer.parseInt(proxy.split(":")[1]))));
            }
        }

        fileScanner.close();
    }

    public List<Proxy> getProxies(int amount) {
        List<Proxy> returnList = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            returnList.add(getNextProxy());
        }

        return returnList;
    }

    private Proxy getNextProxy() {
        Proxy returnProxy = this.availableProxies.get(this.currentProxy);

        this.currentProxy++;
        if (this.currentProxy >= this.availableProxies.size()) {
            this.currentProxy = 0;
        }

        return returnProxy;
    }

    public void shuffle() {
        Collections.shuffle(this.availableProxies);
    }

}