package co.mcsniper.mcsniper.proxy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import co.mcsniper.mcsniper.MCSniper;

public class ProxyHandler {

	private List<SniperProxy> availableProxies = new ArrayList<>();
	private int currentProxy = 0;

	public ProxyHandler(MCSniper sniper) throws IOException {
		try {
			Connection connection = sniper.getMySQL().createConnection();

			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM proxy_list WHERE node = ?;");
			preparedStatement.setString(1, sniper.getServerName());

			ResultSet resultSet = preparedStatement.executeQuery();
			String url = null;

			while (resultSet.next()) {
				url = resultSet.getString("url");
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();

			if (url != null) {
				this.availableProxies = listProxies(url);
				System.out.println("Successfully loaded " + this.availableProxies.size() + " different entries!");
			} else {
				throw new Exception("Unable to find proxy list for server!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<SniperProxy> listProxies(String url) {
		WebClient client = new WebClient();
		List<SniperProxy> proxies = new ArrayList<SniperProxy>();

		try {
			Page page = client.getPage(url);
			String[] dataList = page.getWebResponse().getContentAsString().split("\n");

			for (String data : dataList) {
				data = data.trim();
				if (data.contains(":")) {
					String username = null;
					String password = null;

					if (data.contains("@")) {
						String[] proxyParts = data.split("@");
						String[] authData = proxyParts[0].split(":");
						username = authData[0];
						password = authData[1];

						data = proxyParts[1];
					}

					String[] proxyData = data.split(":");

					proxies.add(new SniperProxy(proxyData[0], Integer.parseInt(proxyData[1]), username, password));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			client.close();
		}

		return proxies;
	}

	public List<SniperProxy> getAllProxies() {
		return this.availableProxies;
	}

	public List<SniperProxy> getProxies(int amount) {
		List<SniperProxy> returnList = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			returnList.add(getNextProxy());
		}

		return returnList;
	}

	private SniperProxy getNextProxy() {
		if (this.availableProxies.size() == 0) {
			return null;
		}

		SniperProxy returnProxy = this.availableProxies.get(this.currentProxy);

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