package co.mcsniper;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import co.mcsniper.mcsniper.MCSniper;

/**
 * Minecraft Spigot/Bukkit Plugin Execution Main Class
 * @author Charles
 */
@SuppressWarnings("deprecation")
public class MainServer extends JavaPlugin {

	private Thread instance;

	public void onEnable() {
		this.instance = new Thread(new Runnable() {
			public void run() {
				try {
					new MCSniper(true);
				} catch (IOException | SQLException e) {
					e.printStackTrace();
				}
			}
		});

		this.instance.start();
	}

	public void onDisable() {
		try {
			this.instance.interrupt();
			this.instance.suspend();
		} catch (Exception ignorred) { }
	}
}
