package co.mcsniper.mcsniper.sniper.name;

import java.util.*;

import co.mcsniper.mcsniper.proxy.SniperProxy;
import co.mcsniper.mcsniper.sniper.AbstractSniper;
import co.mcsniper.mcsniper.MCSniper;

public class NameSniper extends AbstractSniper {

    private String uuid;
    private String sessionCookie;
    private String password;

    public NameSniper(MCSniper handler, int snipeId, long snipeDate, String name, String uuid, String sessionCookie, String password, int proxyCount, int proxyInstances, int proxyOffset, int functionOffset) {
        super(handler, name, snipeId, proxyCount, proxyInstances, proxyOffset, functionOffset, snipeDate, true);

        this.uuid = uuid;
        this.sessionCookie = sessionCookie;
        this.password = password;
    }

    @Override
    protected TimerTask createNameChanger(AbstractSniper sniper, SniperProxy proxy, String name, long proxyOffset) {
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