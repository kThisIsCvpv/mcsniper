package co.mcsniper.mysql;

public class ServerInfo {

    private String serverName;

    private int snipeProxies;
    private int snipeInstances;
    private int snipeOffset;
    private int functionOffset;

    public ServerInfo(String serverName, int snipeProxies, int snipeInstances, int snipeOffset, int functionOffset) {
        this.serverName = serverName;
        this.snipeProxies = snipeProxies;
        this.snipeInstances = snipeInstances;
        this.snipeOffset = snipeOffset;
        this.functionOffset = functionOffset;
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

    public int getFunctionOffset() {
        return this.functionOffset;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ServerInfo) && ((ServerInfo) obj).getServerName().equals(this.serverName);
    }

}