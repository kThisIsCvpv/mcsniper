package co.mcsniper.mcsniper.sniper.mysql;

public class ServerInfo {

    private String serverName;

    private int snipeProxies;
    private int snipeInstances;
    private int snipeOffset;

    public ServerInfo(String serverName, int snipeProxies, int snipeInstances, int snipeOffset) {
        this.serverName = serverName;
        this.snipeProxies = snipeProxies;
        this.snipeInstances = snipeInstances;
        this.snipeOffset = snipeOffset;
    }

    public String getServerName() {
        return this.serverName;
    }

    public int getProxyAmount() {
        return this.snipeProxies;
    }

    public int getProxyInstances() {
        return this.snipeInstances;
    }

    public int getProxyOffset() {
        return this.snipeOffset;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ServerInfo) && ((ServerInfo) obj).getServerName().equals(this.serverName);
    }

}