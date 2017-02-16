package co.mcsniper.mcsniper.sniper.util;

public class LogUtils {

    public static String formatResponse(String response) {
        if (response.toLowerCase().contains("the request could not be satisfied")) {
            response = "Request not Satisfied";
        } else if (response.contains("This exception has been logged with id")) {
            response= "Application Error";
        } else if (response.toLowerCase().contains("<!doctype") || response.toLowerCase().contains("<html")) {
            response = response.replace("\r", "").replace("\n", "");
        } else if (response.toLowerCase().contains("501 not implemented")) {
            response = "HTTP 501";
        } else if (response.toLowerCase().contains("404 not found")) {
            response = "HTTP 404";
        } else if (response.toLowerCase().contains("403 access denied")) {
            response = "HTTP 403";
        } else if (response.toLowerCase().contains("502 bad gateway")) {
            response = "HTTP 502";
        } else if (response.toLowerCase().contains("500 internal server error")) {
            response = "HTTP 500";
        } else if (response.replace(" ", "").equals("")) {
            response = "Empty";
        }

        return response;
    }

}
