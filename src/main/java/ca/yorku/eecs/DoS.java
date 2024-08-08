package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class DoS implements HttpHandler {
    public DoS() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/computeDoSNumber/")) {
                handleGetNumber(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/computeDoSPath/")) {
                handleGetPath(r);
            }
            else{
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handleGetNumber(HttpExchange r) {

    }

    public void handleGetPath(HttpExchange r) {

    }
}
