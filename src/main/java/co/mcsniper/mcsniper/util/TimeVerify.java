package co.mcsniper.mcsniper.util;

import com.lyphiard.simplerequest.SimpleHttpRequest;

import java.util.TimerTask;

public class TimeVerify extends TimerTask {

    @Override
    public void run() {
        try {
            new SimpleHttpRequest("https://mcsniper.co/util/timeverify")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
