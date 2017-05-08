package co.mcsniper.mcsniper.sniper;

import co.mcsniper.mcsniper.MCSniper;
import co.mcsniper.mcsniper.util.TimeVerify;

import java.net.Proxy;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractSniper implements Runnable {

    /**
     * The offset for each sequential request
     */
    private static double K_OFFSET = 1.00;

    private String name;
    private int snipeId;
    private int proxyCount;
    private int proxyInstances;
    private long proxyOffset;
    private long date;
    private boolean done;
    private boolean useFunction;

    private MCSniper handler;
    private Proxy[] proxies;
    private ResponseLog log;
    private Thread drone;

    public AbstractSniper(MCSniper handler, String name, int snipeId, int proxyCount, int proxyInstances, long proxyOffset, long date, boolean useFunction) {
        this.handler = handler;
        this.name = name;
        this.snipeId = snipeId;
        this.proxyCount = proxyCount;
        this.proxyInstances = proxyInstances;
        this.proxyOffset = proxyOffset;
        this.date = date;
        this.done = false;
        this.useFunction = useFunction;
    }

    public String getName() {
        return this.name;
    }

    public int getSnipeId() {
        return this.snipeId;
    }

    public int getProxyCount() {
        return this.proxyCount;
    }

    public int getProxyInstances() {
        return this.proxyInstances;
    }

    public long getProxyOffset() {
        return this.proxyOffset;
    }

    public long getDate() {
        return this.date;
    }

    public Proxy[] getProxies() {
        return this.proxies;
    }

    public MCSniper getHandler() {
        return this.handler;
    }

    public ResponseLog getLog() {
        return this.log;
    }

    public void start() {
        this.proxies = new Proxy[this.proxyCount];
        this.log = new ResponseLog(handler, this);

        List<Proxy> allocatedProxies = this.handler.getProxyHandler().getProxies(this.proxyCount);
        for (int i = 0; i < this.proxies.length; i++) {
            this.proxies[i] = allocatedProxies.get(i);
        }

        this.drone = new Thread(this);
        this.drone.start();
    }

    public void run() {
        long clickTime = this.getDate();
        long pushDelay = this.getDate() + (60L * 1000L);

        int count = 0;
        long systemTimeOffset = System.currentTimeMillis() - this.getHandler().getWorldTime().currentTimeMillis();

        Date timeVerify = new Date(this.getDate() + systemTimeOffset - 60000);
        (new Timer()).schedule(new TimeVerify(), timeVerify);

        for (int server = 0; server < this.proxyCount; server++) {
            for (int instance = 0; instance < this.proxyInstances; instance++) {
                long snipingOffset;
                if (this.useFunction) {
                    snipingOffset = -(long) Math.sqrt((0.065 * count) + 55);
                } else {
                    snipingOffset = (count % 2 == 0 ? 1 : -1) * ((long) (K_OFFSET * Math.ceil(count / 2D))) + this.proxyOffset;
                }

                Date date = new Date(clickTime + snipingOffset + systemTimeOffset);
                (new Timer()).schedule(this.createNameChanger(this, this.getProxies()[server], this.getName(), snipingOffset), date);

                count++;
            }
        }

        while (this.handler.getWorldTime().currentTimeMillis() <= pushDelay) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.log.pushLog();
        this.done = true;
    }

    public boolean isDone() {
        return this.done;
    }

    public boolean isUseFunction() {
        return this.useFunction;
    }

    public void setSuccessful() {
        this.log.setSuccess(true);
        this.handler.getMySQL().updateStatus(this.getSnipeId(), 1);
    }

    protected abstract TimerTask createNameChanger(AbstractSniper sniper, Proxy proxy, String name, long proxyOffset);

}
