package co.mcsniper.mcsniper.sniper.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.lyphiard.simplerequest.SimpleHttpRequest;

public class Util {

    public static String getIP() {
        try {
            return new SimpleHttpRequest("https://api.ipify.org").execute().getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    public static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String ln;
        while ((ln = br.readLine()) != null) {
            sb = sb.append(ln);
        }

        br.close();
        return sb.toString();
    }

}