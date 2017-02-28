package co.mcsniper.mcsniper.sniper;

import java.net.Proxy;

public class Response {

    private String response;
    private int statusCode;
    private long offset;
    private long webOffset;
    private Proxy proxy;

    public Response(String response, int statusCode, long offset, long webOffset, Proxy proxy) {
        this.response = formatResponse(response);
        this.statusCode = statusCode;
        this.offset = offset;
        this.webOffset = webOffset;
        this.proxy = proxy;
    }

    public String getResponse() {
        return this.response;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public long getOffset() {
        return this.offset;
    }

    public long getWebOffset() {
        return this.webOffset;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public static String formatResponse(String response) {
        if (response.toLowerCase().contains("the request could not be satisfied")) {
            response = "Request not Satisfied";
        } else if (response.contains("This exception has been logged with id")) {
            response= "Application Error";
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
        } else if (response.toLowerCase().contains("<!doctype") || response.toLowerCase().contains("<html")) {
            response = response.replace("\r", "").replace("\n", "");
        }

        return response;
    }

}
