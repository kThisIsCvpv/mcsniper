package com.lyphiard.simplerequest;

import java.util.HashMap;

public class SimpleHttpResponse {

    private String response;
    private int responseCode;
    private HashMap<String, String> cookies;

    public SimpleHttpResponse(String response, int responseCode, HashMap<String, String> cookies) {
        this.response = response;
        this.responseCode = responseCode;
        this.cookies = cookies;
    }

    public String getResponse() {
        return this.response;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public HashMap<String, String> getCookies() {
        return this.cookies;
    }

}