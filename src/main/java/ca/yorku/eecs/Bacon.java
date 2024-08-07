package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Bacon implements HttpHandler {
    public Bacon() {

    }

    public void handle(HttpExchange r) {

    }

    public void handleGetNumber(HttpExchange r) {

    }

    public void handleGetPath(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);


    }
}
