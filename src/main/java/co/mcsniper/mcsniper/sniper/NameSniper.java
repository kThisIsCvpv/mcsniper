package co.mcsniper.mcsniper.sniper;

import co.mcsniper.mcsniper.MCSniper;

public class NameSniper {

	private MCSniper handler;

	private int snipeID;
	private long snipeDate;

	private String uuid;
	private String sessionCookie;
	private String password;

	private int proxyAmount;
	private int proxyInstances;
	private long proxyOffset;

	public NameSniper(MCSniper handler, int snipeID, long snipeDate, String uuid, String sessionCookie, String password, int proxyAmount, int proxyInstances, long proxyOffset) {
		this.handler = handler;

		this.snipeID = snipeID;
		this.snipeDate = snipeDate;

		this.uuid = uuid;
		this.sessionCookie = sessionCookie;
		this.password = password;

		this.proxyAmount = proxyAmount;
		this.proxyInstances = proxyInstances;
		this.proxyOffset = proxyOffset;
	}

	public MCSniper getHandler() {
		return this.handler;
	}

	public int getID() {
		return this.snipeID;
	}

	public long getDate() {
		return this.snipeDate;
	}

	public String getUUID() {
		return this.uuid;
	}

	public String getSession() {
		return this.sessionCookie;
	}

	public String getPassword() {
		return this.password;
	}

	public int getProxyAmount() {
		return this.proxyAmount;
	}

	public int getProxyInstance() {
		return this.proxyInstances;
	}

	public long getProxyOffset() {
		return this.proxyOffset;
	}
}
