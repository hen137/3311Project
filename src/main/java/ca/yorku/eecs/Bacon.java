package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Path;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import static org.neo4j.driver.v1.Values.parameters;

public class Bacon implements HttpHandler {
    private final String actorIdKevinBacon = "nm0000102";

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

    public void handleGetNumber(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        int baconNumber;
        String actorId, query, response;
        StatementResult result;

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

                if (actorId.equals(actorIdKevinBacon)) baconNumber = 0;
                else {
                    query = "MATCH p = shortestpath((a1: actor {actorId: $actorId})-[:ACTED_IN*]-(a2: actor {actorId: $actorIdKevinBacon})) RETURN p";
                    result = tx.run(query, parameters("actorId", actorId, "actorIdKevinBacon", actorIdKevinBacon));

                    if (!result.hasNext()) {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }
                }

                baconNumber = result.next().get("p").asPath().length() / 2;

                response = String.format("{\"baconNumber\": %d}", baconNumber);

                r.sendResponseHeaders(200, response.length());
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
                r.sendResponseHeaders(500, -1);
            }
        }
    }

    public void handleGetPath(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId, query, response;
        StringBuilder baconPath;
        StatementResult result;

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

                query = "MATCH p = shortestpath((a1: actor {actorId: $actorId})-[:ACTED_IN*]-(a2: actor {actorId: $actorIdKevinBacon})) RETURN p";
                result = tx.run(query, parameters("actorId", actorId, "actorIdKevinBacon", actorIdKevinBacon));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                baconPath = new StringBuilder("[");
                boolean actor_movie = true;
                for (Path.Segment step: result.next().get("p").asPath()) {
                    if (!actor_movie) {
                        query = "MATCH (a) WHERE ID(a) = $ID RETURN a.actorId";
                        result = tx.run(query, parameters("ID", step.end().id()));

                        baconPath.append((Objects.equals(result.next().get("a.actorId").toString(), actorIdKevinBacon))? "," : ", \"" + actorIdKevinBacon + "\"]");

                        actor_movie = true;
                        continue;
                    }

                    query = "MATCH (a) WHERE ID(a) = $ID RETURN a.actorId";
                    result = tx.run(query, parameters("ID", step.start().id()));

                    baconPath.append(result.next().get("a.actorId")).append(",");

                    query = "MATCH (m) WHERE ID(m) = $ID RETURN m.movieId";
                    result = tx.run(query, parameters("ID", step.end().id()));

                    baconPath.append(result.next().get("m.movieId"));

                    actor_movie = false;
                }

                response = String.format("{\"baconPath\": %s}", baconPath);

                r.sendResponseHeaders(200, response.length());
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }catch (Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
                r.sendResponseHeaders(500, -1);
            }
        }
    }
}
