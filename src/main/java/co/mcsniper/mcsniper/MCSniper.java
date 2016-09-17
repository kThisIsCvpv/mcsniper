package co.mcsniper.mcsniper;

public class MCSniper {

	private boolean isMinecraft;

	public MCSniper(boolean isMinecraft) {
		this.isMinecraft = isMinecraft;
	}

	public boolean isMinecraftServer() {
		return this.isMinecraft;
	}
}
