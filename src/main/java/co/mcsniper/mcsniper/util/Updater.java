package co.mcsniper.mcsniper.util;

import com.lyphiard.simplerequest.SimpleHttpRequest;
import com.lyphiard.simplerequest.SimpleHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {

    private static boolean DEVELOPMENT_MODE = true;

    public static boolean checkForUpdates(String currentVersion) {

        if (DEVELOPMENT_MODE) {
            return false;
        }

        try {
            SimpleHttpResponse response = new SimpleHttpRequest("https://api.github.com/repos/Lyphiard/MCSniper/releases")
                    .addHeader("Authorization", "token 2ec64c7c3f5da0fd5e8efda5f4b4035bef206dd7")
                    .addHeader("Accept", "application/json")
                    .execute();

            JSONArray releases = new JSONArray(response.getResponse());

            if (releases.length() < 1) {
                return false;
            }

            JSONObject release = releases.getJSONObject(0);
            if (!release.getString("tag_name").equals(currentVersion)) {
                return update(release, currentVersion);
            }
        } catch (Exception e) {
            System.out.println("Could not check for updates: " + e.getMessage());
        }
        
        return false;
    }

    private static boolean update(JSONObject release, String currentVersion) {
        System.out.println("Detected new version: " + release.getString("tag_name") + " (Current Version: " + currentVersion + ")");

        int assetId = 0;

        JSONArray assets = release.getJSONArray("assets");
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);

            if (asset.getString("name").equals("MCSniper.jar")) {
                assetId = asset.getInt("id");
                break;
            }
        }

        if (assetId == 0) {
            System.out.println("Could not find asset MCSniper.jar");
            return false;
        }

        try {
            System.out.println("Downloading new version (Asset ID " + assetId + ")");
            File toUpdate = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

            System.out.println("Starting file download...");
            System.out.println("Saving updated version to " + toUpdate.getAbsolutePath() + "...");

            URL url = new URL("https://api.github.com/repos/Lyphiard/MCSniper/releases/assets/" + assetId);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.addRequestProperty("Accept", "application/octet-stream");
            httpConn.addRequestProperty("Authorization", "token 2ec64c7c3f5da0fd5e8efda5f4b4035bef206dd7");
            httpConn.getResponseCode();

            httpConn = (HttpURLConnection) (new URL(httpConn.getURL().toString())).openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(toUpdate);

                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                System.out.println("File download complete. Restarting...");

                Runtime.getRuntime().exec("killall -9 java");
                System.exit(0);
                return true;
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }

            httpConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

}