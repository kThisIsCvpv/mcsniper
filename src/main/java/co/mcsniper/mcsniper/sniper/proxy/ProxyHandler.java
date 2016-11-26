package co.mcsniper.mcsniper.sniper.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.mcsniper.mcsniper.MCSniper;

public class ProxyHandler {

    private List<Proxy> availableProxies = new ArrayList<>();
    private int currentProxy = 0;

    public ProxyHandler(MCSniper sniper) throws IOException {

        try {
            Connection connection = sniper.getMySQL().createConnection();

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM proxies WHERE node = ? AND enabled = 1");
            preparedStatement.setString(1, sniper.getServerName());
            preparedStatement.setFetchSize(100);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                this.availableProxies.add(new Proxy(Proxy.Type.valueOf(resultSet.getString("type")), new InetSocketAddress(resultSet.getString("ip"), resultSet.getInt("port"))));
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Proxy> getAllProxies() {
        return this.availableProxies;
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