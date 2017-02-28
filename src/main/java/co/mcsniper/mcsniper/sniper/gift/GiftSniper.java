package co.mcsniper.mcsniper.sniper.gift;

import java.net.Proxy;
import java.util.*;

import co.mcsniper.mcsniper.sniper.AbstractSniper;
import co.mcsniper.mcsniper.MCSniper;

public class GiftSniper extends AbstractSniper {
    
    private String sessionCookie;
    private String giftcode;

    public GiftSniper(MCSniper handler, int snipeId, long snipeDate, String name, String sessionCookie, String giftcode, int proxyCount, int proxyInstances, long proxyOffset) {
        super(handler, name, snipeId, proxyCount, proxyInstances, proxyOffset, snipeDate);

        this.sessionCookie = sessionCookie;
        this.giftcode = giftcode;
    }

    @Override
    protected TimerTask createNameChanger(AbstractSniper sniper, Proxy proxy, String name) {
        return new GiftChanger(
                this,
                proxy,
                name,
                this.sessionCookie,
                this.giftcode
        );
    }

}