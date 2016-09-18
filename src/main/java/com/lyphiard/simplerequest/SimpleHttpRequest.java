package com.lyphiard.simplerequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleHttpRequest {

    private String url;
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36";
    private RequestType requestType = RequestType.GET;
    private Proxy proxy = Proxy.NO_PROXY;
    private HashMap<String, String> cookies = new HashMap<String, String>();
    private HashMap<String, String> fields = new HashMap<String, String>();
    private HashMap<String, String> headers = new HashMap<String, String>();
    private int timeout = 3000;
    private String data = "";
    private boolean throwHttpErrors = true;

    private int redirectCount = 0;

    public SimpleHttpRequest(String url) {
        this.url = url;
    }

    private SimpleHttpRequest(String url, int redirectCount) {
        this.url = url;
        this.redirectCount = redirectCount;
    }

    public SimpleHttpRequest setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public SimpleHttpRequest setType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public SimpleHttpRequest setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public SimpleHttpRequest setCookies(HashMap<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }
    
    public SimpleHttpRequest setCookie(String key, String value) {
    	this.cookies.put(key, value);
    	return this;
    }

    public SimpleHttpRequest setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public SimpleHttpRequest setData(String data) {
        this.data = data;
        return this;
    }

    public SimpleHttpRequest addField(String key, String value) {
        this.fields.put(key, value);
        return this;
    }

    public SimpleHttpRequest addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public SimpleHttpRequest throwHttpErrors(boolean throwHttpErrors) {
        this.throwHttpErrors = throwHttpErrors;
        return this;
    }

    public SimpleHttpResponse execute() throws Exception {
        if (!this.headers.containsKey("Content-Type")) {
            this.headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        }

        URL url = new URL(this.getUrl());
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection(this.proxy);

        urlConn.setRequestMethod(this.requestType.name());
        urlConn.setInstanceFollowRedirects(false);
        urlConn.setConnectTimeout(this.timeout);
        urlConn.setReadTimeout(this.timeout);

        for (Map.Entry<String, String> header : this.headers.entrySet()) {
            urlConn.addRequestProperty(header.getKey(), header.getValue());
        }

        urlConn.addRequestProperty("User-Agent", this.userAgent);

        if (!this.cookies.isEmpty() && this.getCookies() != null) {
            urlConn.addRequestProperty("Cookie", this.getCookies());
        }

        urlConn.setDoOutput(true);
        urlConn.connect();

        if (this.requestType == RequestType.POST) {
            if (this.data.equals("")) {
                for (Map.Entry<String, String> entry : this.fields.entrySet()) {
                    this.data = this.data + URLEncoder.encode(entry.getKey(), "utf-8") + "=" + URLEncoder.encode(entry.getValue(), "utf-8") + "&";
                }

                this.data = this.data.length() != 0 && this.data.charAt(this.data.length() - 1) == '&' ? this.data.substring(0, this.data.length() - 1) : this.data;
            }

            DataOutputStream dataOutputStream = new DataOutputStream(urlConn.getOutputStream());
            dataOutputStream.write(this.data.getBytes());
            dataOutputStream.flush();
            dataOutputStream.close();
        }

        int responseCode = urlConn.getResponseCode();

        Map<String, List<String>> headerFields = urlConn.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");

        if (cookiesHeader != null) {
            for (String cookieHeader : cookiesHeader) {
                HttpCookie httpCookie = HttpCookie.parse(cookieHeader).get(0);
                if (this.cookies.containsKey(httpCookie.getName())) {
                    this.cookies.remove(httpCookie.getName());
                }

                this.cookies.put(httpCookie.getName(), httpCookie.getValue());
            }
        }

        List<String> redirect = headerFields.get("Location");
        if (redirect != null) {
            String redirectTo = redirect.get(0).replace("Location:", "").trim();
            if (this.redirectCount >= 15) {
                throw new Exception("Too many redirects!");
            }

            return new SimpleHttpRequest(redirectTo, this.redirectCount + 1)
                    .setType(RequestType.GET)
                    .setCookies(this.cookies)
                    .execute();
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getResponseCode() == 200 ? urlConn.getInputStream() : (this.throwHttpErrors ? urlConn.getInputStream() : urlConn.getErrorStream())));
        String line;
        StringBuffer response = new StringBuffer();

        while ((line = bufferedReader.readLine()) != null) {
            response.append(line);
        }

        urlConn.getInputStream().close();
        bufferedReader.close();
        urlConn.disconnect();

        return new SimpleHttpResponse(
                response.toString(),
                responseCode,
                this.cookies
        );
    }

    private String getUrl() throws Exception {
        if (this.requestType == RequestType.GET && !this.fields.isEmpty()) {
            this.url = this.url + "?";
            for (Map.Entry<String, String> entry : this.fields.entrySet()) {
                this.url = this.url + URLEncoder.encode(entry.getKey(), "utf-8") + "=" + URLEncoder.encode(entry.getValue(), "utf-8") + "&";
            }

            this.url = this.url.charAt(this.url.length() - 1) == '&' ? this.url.substring(0, this.url.length() - 1) : this.url;
        }
        return this.url;
    }

    private String getCookies() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> cookie : this.cookies.entrySet()) {
            stringBuilder.append(cookie.getKey()).append('=').append(cookie.getValue()).append(";");
        }

        String cookies = stringBuilder.toString();
        cookies = cookies.length() != 0 && cookies.charAt(cookies.length() - 1) == ';' ? cookies.substring(0, cookies.length() - 1) : cookies;
        return cookies;
    }

}