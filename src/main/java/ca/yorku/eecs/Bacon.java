package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Bacon implements HttpHandler {
    public Bacon() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET")) {
                String path = r.getRequestURI().getPath();
                if (path.endsWith("/computeBaconNumber/")) {
                    handleGetNumber(r);
                } else if (path.endsWith("/computeBaconPath/")) {
                    handleGetPath(r);
                } else {
                    r.sendResponseHeaders(404, -1);
                }
            } else {
                r.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
        }

    }

    public void handleGetNumber(HttpExchange r) {
         String queryStr = String.format("MATCH (a:Actor {id: '%s'}), (b:Actor {id: 'nm0000102'}), " +
                    "p=shortestPath((a)-[:ACTED_IN*]-(b)) " +
                    "RETURN length(p) AS baconNumber", actorId);
            StatementResult result = session.run(queryStr);

            if (result.hasNext()) {
                int baconNumber = result.next().get("baconNumber").asInt();

                JSONObject response = new JSONObject();
                response.put("actorId", actorId);
                response.put("baconNumber", baconNumber);

                String responseText = response.toString();
                r.sendResponseHeaders(200, responseText.length()); 
                OutputStream os = r.getResponseBody();
                os.write(responseText.getBytes());
                os.close();
            } else {
                r.sendResponseHeaders(404, -1); 
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1); 
        }

    }

    public void handleGetPath(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);


    }
}
