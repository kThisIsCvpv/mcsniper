package co.mcsniper.mcsniper.sniper.name;

import java.net.Proxy;
import java.util.*;

import co.mcsniper.mcsniper.sniper.AbstractSniper;
import co.mcsniper.mcsniper.MCSniper;

public class NameSniper extends AbstractSniper {

    private String uuid;
    private String sessionCookie;
    private String password;

    public NameSniper(MCSniper handler, int snipeId, long snipeDate, String name, String uuid, String sessionCookie, String password, int proxyCount, int proxyInstances, long proxyOffset) {
        super(handler, name, snipeId, proxyCount, proxyInstances, proxyOffset, snipeDate, true);

        this.uuid = uuid;
        this.sessionCookie = sessionCookie;
        this.password = password;
    }

    @Override
    protected TimerTask createNameChanger(AbstractSniper sniper, Proxy proxy, String name, long proxyOffset) {
        return new NameChanger(
                this,
                this.uuid,
                proxy,
                name,
                this.sessionCookie,
                this.password,
                proxyOffset
        );
    }

}