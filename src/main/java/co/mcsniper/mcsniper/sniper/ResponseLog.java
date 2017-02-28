package co.mcsniper.mcsniper.sniper;

import co.mcsniper.mcsniper.MCSniper;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResponseLog {

    private Queue<Response> responses;
    private AtomicBoolean success;
    private MCSniper handler;
    private AbstractSniper sniper;

    public ResponseLog(MCSniper handler, AbstractSniper sniper) {
        this.responses = new ConcurrentLinkedQueue<Response>();
        this.success = new AtomicBoolean();
        this.handler = handler;
        this.sniper = sniper;
    }

    public void addResponse(Response response) {
        this.responses.add(response);
    }

    public void setSuccess(boolean success) {
        this.success.set(success);
    }

    public boolean isSuccess() {
        return this.success.get();
    }

    public void pushLog() {
        StringBuilder sb = new StringBuilder();

        sb.append("Server Name: ").append(this.handler.getServerName()).append("\n");
        sb.append("Server Host: ").append(this.handler.getServerIP()).append("\n\n");

        sb.append("Snipe Name: ").append(this.sniper.getName()).append("\n");
        sb.append("Snipe Date: ").append(this.sniper.getDate()).append("\n");
        sb.append("Snipe Result: ").append(this.isSuccess() ? "Success" : "Fail").append("\n");
        sb.append("Proxy Count: ").append(this.sniper.getProxyCount()).append("\n");
        sb.append("Proxy Instances: ").append(this.sniper.getProxyInstances()).append("\n");
        sb.append("Proxy Offset: ").append(this.sniper.getProxyOffset()).append("\n");

        List<Response> validResponses = new ArrayList<Response>(this.responses.size());

        while (this.responses.size() > 0) {
            validResponses.add(this.responses.poll());
        }

        Collections.sort(validResponses, new Comparator<Response>() {
            public int compare(Response o1, Response o2) {
                return Long.compare(o1.getOffset(), o2.getOffset());
            }
        });

        sb.append("\n########## Response Log ##########\n\n");

        DecimalFormat timeFormat = new DecimalFormat("+###,###;-###,###");

        JSONObject responses = new JSONObject();
        JSONObject configuration = new JSONObject();

        configuration.put("proxies", this.sniper.getProxyCount())
                .put("instances", this.sniper.getProxyInstances())
                .put("offset", this.sniper.getProxyOffset());

        for (Response response : validResponses) {
            sb.append(StringUtils.rightPad(response.getProxy().toString(), 30, " "));
            sb.append("[ ").append(timeFormat.format(response.getOffset())).append("ms ] [ ");
            sb.append(timeFormat.format(response.getWebOffset())).append("ms ] ");
            sb.append("( HTTP ").append(response.getStatusCode()).append(") ");
            sb.append(response.getResponse()).append("\n");

            String parsedResponse = response.getResponse().contains("Exception: ") ? response.getResponse().substring(0, response.getResponse().indexOf("Exception: ") + 9) : response.getResponse();
            if (responses.has(parsedResponse)) {
                responses.increment(parsedResponse);
            } else {
                responses.put(parsedResponse, 1);
            }
        }

        this.handler.getMySQL().pushLog(
                this.handler.getServerName(),
                this.sniper.getSnipeId(),
                this.sniper.getName(),
                MCSniper.DATE_FORMAT.format(this.sniper.getDate()),
                this.success.get() ? 1 : 0,
                sb.toString(),
                responses,
                configuration
        );
    }

}
