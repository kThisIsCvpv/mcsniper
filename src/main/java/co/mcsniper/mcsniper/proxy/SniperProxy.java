package co.mcsniper.mcsniper.proxy;

public class SniperProxy {

    protected String ip;
    protected int port;

    protected String username;
    protected String password;

    public SniperProxy(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (this.username != null && this.password != null) {
            stringBuilder.append(this.username)
                    .append(":")
                    .append(this.password)
                    .append("@");
        }

        stringBuilder.append(this.ip)
                .append(":")
                .append(this.port);

        return stringBuilder.toString();
    }

}
