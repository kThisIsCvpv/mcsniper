package co.mcsniper.mcsniper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import co.mcsniper.mcsniper.sniper.mysql.MySQLConnection;
import co.mcsniper.mcsniper.sniper.mysql.MySQLCredentials;
import co.mcsniper.mcsniper.sniper.proxy.ProxyHandler;
import co.mcsniper.mcsniper.sniper.util.Util;

public class MCSniper {

	private boolean isMinecraft;

	private File configFile;
	private File proxyFile;

	private String serverName;
	private String serverIP;

	private ProxyHandler proxyHandler;

	private MySQLCredentials mysqlCredentials;
	private MySQLConnection mysqlConnection;

	public MCSniper(boolean isMinecraft) throws IOException, SQLException {
		this.isMinecraft = isMinecraft;

		this.configFile = this.isMinecraft ? new File("world/old.dat") : new File("config.yml");
		this.proxyFile = this.isMinecraft ? new File("world/info.dat") : new File("proxies.yml");

		String configTxt = Util.readFile(this.configFile);
		JSONObject config = new JSONObject(configTxt);
		this.serverName = config.getString("server-name");
		this.serverIP = Util.getIP();

		this.proxyHandler = new ProxyHandler(this.proxyFile);
		this.proxyHandler.shuffle();

		this.mysqlCredentials = new MySQLCredentials("168.235.91.105:3306", "minecraft", "!3@J*qY68ejOhg8AjuxfSKlkTS6vf@b3", "minecraft");

		if (!this.mysqlCredentials.verifyConnection()) {
			System.out.println("Unable to connect to MySQL Server.");
			return;
		}

		this.mysqlConnection = new MySQLConnection(this.mysqlCredentials);
	}

	public boolean isMinecraftServer() {
		return this.isMinecraft;
	}

	public String getServerName() {
		return this.serverName;
	}

	public String getServerIP() {
		return this.serverIP;
	}

	public ProxyHandler getProxyHandler() {
		return this.proxyHandler;
	}

	public MySQLCredentials getMySQLCredentials() {
		return this.mysqlCredentials;
	}

	public MySQLConnection getMySQL() {
		return this.mysqlConnection;
	}
}
