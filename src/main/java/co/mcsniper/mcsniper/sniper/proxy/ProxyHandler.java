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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import co.mcsniper.mcsniper.MCSniper;

public class ProxyHandler {

	private List<Proxy> availableProxies = new ArrayList<>();
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

		// try {
		// Connection connection = sniper.getMySQL().createConnection();
		//
		// PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM proxies WHERE node = ? AND enabled = 1");
		// preparedStatement.setString(1, sniper.getServerName());
		// preparedStatement.setFetchSize(100);
		// ResultSet resultSet = preparedStatement.executeQuery();
		//
		// while (resultSet.next()) {
		// this.availableProxies.add(new Proxy(Proxy.Type.valueOf(resultSet.getString("type")), new InetSocketAddress(resultSet.getString("ip"), resultSet.getInt("port"))));
		// }
		//
		// resultSet.close();
		// preparedStatement.close();
		// connection.close();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	private List<Proxy> listProxies(String url) {
		WebClient client = new WebClient();
		List<Proxy> proxies = new ArrayList<Proxy>();

		try {
			Page page = client.getPage(url);
			String[] dataList = page.getWebResponse().getContentAsString().split("\n");

			for (String data : dataList) {
				String parsed = data.trim();
				if (parsed.contains(":")) {
					String ip = parsed.split(":")[0];
					int port = Integer.parseInt(parsed.split(":")[1]);
					proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			client.close();
		}

		return proxies;
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