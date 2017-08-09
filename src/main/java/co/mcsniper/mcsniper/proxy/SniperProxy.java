package co.mcsniper.mcsniper.proxy;

public class SniperProxy {

    protected String ip;
    protected int port;

    protected String username;
    protected String password;

    protected boolean socks;

    public SniperProxy(String ip, int port, String username, String password) {
        this(ip, port, username, password, false);
    }

    public SniperProxy(String ip, int port, String username, String password, boolean socks) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.socks = socks;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isSocks() {
        return this.isSocks();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.ip).append(":").append(this.port);

        if (this.username != null && this.password != null) {
            stringBuilder.append(" [A]");
        }

        return stringBuilder.toString();
    }

}
