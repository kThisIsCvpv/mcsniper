package co.mcsniper.mcsniper;

public abstract class AbstractSniper {

    private String name;
    private long date;

    public AbstractSniper(String name, long date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return this.name;
    }

    public long getDate() {
        return this.date;
    }

    public abstract void start();

    public abstract boolean isDone();
}
