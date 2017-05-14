package co.mcsniper.mcsniper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import co.mcsniper.mcsniper.sniper.AbstractSniper;
import org.json.JSONArray;
import org.json.JSONObject;

import co.mcsniper.mysql.MySQLConnection;
import co.mcsniper.mysql.MySQLCredentials;
import co.mcsniper.mysql.ServerInfo;
import co.mcsniper.mcsniper.proxy.ProxyHandler;
import co.mcsniper.mcsniper.util.RestartManager;
import co.mcsniper.mcsniper.util.SecurityManager;
import co.mcsniper.mcsniper.util.Updater;
import co.mcsniper.mcsniper.util.Util;
import co.mcsniper.mcsniper.util.WorldTime;
import co.mcsniper.mcsniper.sniper.gift.GiftSniper;
import co.mcsniper.mcsniper.sniper.name.NameSniper;

public class MCSniper {

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEEEEEEE d, y - hh:mm:ss a z");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
    }

    private boolean isMinecraft;

    private File configFile;

    private String serverName;
    private String serverIP;
    private String version;

    private WorldTime worldTime;
    private ProxyHandler proxyHandler;
    private RestartManager restartManager;
    private SecurityManager securityManager;

    private MySQLCredentials mysqlCredentials;
    private MySQLConnection mysqlConnection;

    private HashMap<Integer, AbstractSniper> ongoingSnipes = new HashMap<Integer, AbstractSniper>();

    public MCSniper(boolean isMinecraft) throws IOException, SQLException, ClassNotFoundException {
        String classPath = MCSniper.class.getResource(MCSniper.class.getSimpleName() + ".class").toString();

        if (!classPath.startsWith("jar")) {
            System.out.println("Invalid class path: " + classPath);
            return;
        }

        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();

        this.version = "v" + attr.getValue("Build-Version");
        System.out.println("Running build " + this.version);

        Class.forName("com.mysql.jdbc.Driver");
        this.isMinecraft = isMinecraft;

        this.configFile = this.isMinecraft ? new File("world/old.dat") : new File("config.yml");

        String configTxt = Util.readFile(this.configFile);
        JSONObject config = new JSONObject(configTxt);
        this.serverName = config.getString("server-name");
        this.serverIP = Util.getIP();

        this.worldTime = new WorldTime();

        this.mysqlCredentials = new MySQLCredentials("45.35.41.219:3306", "minecraft", "!3@J*qY68ejOhg8AjuxfSKlkTS6vf@b3", "minecraft");

        if (!this.mysqlCredentials.verifyConnection()) {
            System.out.println("Unable to connect to MySQL Server.");
            return;
        } else {
            System.out.println("Connected to MySQL.");
        }

        this.mysqlConnection = new MySQLConnection(this.mysqlCredentials);

        this.proxyHandler = new ProxyHandler(this);
        this.proxyHandler.shuffle();
        this.securityManager = new SecurityManager(this.proxyHandler);

        long serverOffset = this.worldTime.currentTimeMillis() - System.currentTimeMillis();

        this.restartManager = new RestartManager(this);
        this.restartManager.updateStatus();

        System.out.println("#######################################");
        System.out.println("Server Name: " + this.serverName);
        System.out.println("Server IP: " + this.serverIP);
        System.out.println("Build Version: " + this.version);
        System.out.println("Server Offset: " + (serverOffset >= 0 ? "+" : "") + serverOffset + " ms");
        System.out.println("#######################################");

        while (true) {
            Map<Integer, AbstractSniper> updatedSnipes = new HashMap<>();
            Connection con = null;

            try {
                con = this.getMySQL().createConnection();

                PreparedStatement statement = con.prepareStatement("SELECT * FROM `sniper`;");
                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    int snipeID = result.getInt("id");

                    String nameToSnipe = result.getString("new_name");
                    long unixDate = result.getLong("unix_date");

                    String uuid = result.getString("mojang_uuid");
                    String password = result.getString("mojang_pass");
                    String session = result.getString("session");

                    String serverDetails = result.getString("servers");
                    boolean isGiftCode = result.getInt("is_giftcode") == 1;

                    JSONArray array = new JSONArray(serverDetails);
                    HashMap<String, ServerInfo> servers = new HashMap<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = new JSONObject(array.get(i).toString());
                        servers.put(obj.getString("name"), new ServerInfo(obj.getString("name"), obj.getInt("snipe_proxies"), obj.getInt("snipe_instances"), obj.getInt("snipe_offset"), obj.getInt("function_offset")));
                    }

                    if (servers.containsKey(this.serverName)) {
                        ServerInfo si = servers.get(this.serverName);
                        if (isGiftCode) {
                            updatedSnipes.put(snipeID, new GiftSniper(this, snipeID, unixDate, nameToSnipe, session, uuid, si.getProxyAmount(), si.getProxyInstances(), si.getProxyOffset()));
                        } else {
                            updatedSnipes.put(snipeID, new NameSniper(this, snipeID, unixDate, nameToSnipe, uuid, session, password, si.getProxyAmount(), si.getProxyInstances(), si.getProxyOffset()));
                        }
                    }

                }

                result.close();
                statement.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            for (int snipeid : updatedSnipes.keySet()) {
                if (this.ongoingSnipes.containsKey(snipeid)) {
                    continue;
                }

                AbstractSniper ns = updatedSnipes.get(snipeid);

                /*if (ns instanceof NameSniper) { // Security Questions Bypass Algorithm
                    NameSniper nt = (NameSniper) ns;
                    this.securityManager.verify(nt.getSession(), nt.getPassword());
                } // End*/

                long secUntil = (ns.getDate() - this.worldTime.currentTimeMillis()) / 1000L;

                if (secUntil >= (60) && secUntil <= (3 * 60)) {
                    this.ongoingSnipes.put(snipeid, ns);
                    ns.start();
                }
            }

            List<Integer> snipes = new ArrayList<>(this.ongoingSnipes.keySet());
            for (int snipeid : snipes) {
                AbstractSniper ns = this.ongoingSnipes.get(snipeid);

                if (ns.isDone()) {
                    this.ongoingSnipes.remove(snipeid);
                    if (this.ongoingSnipes.isEmpty()) {
                        Runtime.getRuntime().exec("killall -9 java");
                        System.exit(0);
                    }
                }
            }

            System.gc();

            if (this.ongoingSnipes.isEmpty()) {
                Updater.checkForUpdates(this.version);
                this.restartManager.checkForRestart();
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException ignored) {

            }
        }
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

    public WorldTime getWorldTime() {
        return this.worldTime;
    }

    public MySQLConnection getMySQL() {
        return this.mysqlConnection;
    }

    public String getVersion() {
        return this.version;
    }

}
