package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import java.io.IOException;

import static org.neo4j.driver.v1.Values.parameters;

public class Bacon implements HttpHandler {
    public Bacon() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/computeBaconNumber/")) {
                handleGetNumber(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/computeBaconPath/")) {
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

    public void handleGetPath(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId, query, response;
        StringBuilder baconPath;
        StatementResult result;

        String actorIdKevinBacon = "nm0000102";

        if (deserialized.has("actorId")) actorId = deserialized.getString("actorId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (a: actor {actorId: $actorId}) RETURN a;";
                result = tx.run(query, parameters("actorId", actorId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                query = "";
                result = tx.run(query, parameters("actorId", actorId));

                baconPath = new StringBuilder("[");


            }catch (Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
                r.sendResponseHeaders(500, -1);
            }
        }
        catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }
}
