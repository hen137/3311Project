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

public class DoS implements HttpHandler {
    public DoS() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/computeDoSNumber")) {
                handleGetNumber(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/computeDoSPath")) {
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

        int DoSNumber;
        String actorId1, actorId2, query, response;
        StatementResult result;

        if (deserialized.has("actorId1") && deserialized.has("actorId2")) {
            actorId1 = deserialized.getString("actorId1");
            actorId2 = deserialized.getString("actorId2");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                for (String actorId: new String[]{actorId1, actorId2}) {
                    query = "MATCH (a: actor {actorId: $actorId}) RETURN a.name;";
                    result = tx.run(query, parameters("actorId", actorId));

                    if (!result.hasNext()) {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }
                }

                if (actorId1.equals(actorId2)) DoSNumber = 0;
                else {
                    query = "MATCH p = shortestpath((a1: actor {actorId: $actorId1})-[:ACTED_IN*]-(a2: actor {actorId: $actorId2})) RETURN p";
                    result = tx.run(query, parameters("actorId1", actorId1, "actorId2", actorId2));

                    if (!result.hasNext()) {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }

                    DoSNumber = result.next().get("p").asPath().length() / 2;
                }

                response = String.format("{\"DoSNumber\": %d}", DoSNumber);

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

        String actorId1, actorId2, query, response;
        StringBuilder DosPath;
        StatementResult result;

        if (deserialized.has("actorId1") && deserialized.has("actorId2")) {
            actorId1 = deserialized.getString("actorId1");
            actorId2 = deserialized.getString("actorId2");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                for (String actorId: new String[]{actorId1, actorId2}) {
                    query = "MATCH (a: actor {actorId: $actorId}) RETURN a.name;";
                    result = tx.run(query, parameters("actorId", actorId));

                    if (!result.hasNext()) {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }
                }

                query = "MATCH p = shortestpath((a1: actor {actorId: $actorId1})-[:ACTED_IN*]-(a2: actor {actorId: $actorId2})) RETURN p";
                result = tx.run(query, parameters("actorId1", actorId1, "actorId2", actorId2));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                boolean actor_movie = true;
                DosPath = new StringBuilder("[");

                for (Path.Segment step: result.next().get("p").asPath()) {
                    if (!actor_movie) {
                        query = "MATCH (a) WHERE ID(a) = $ID RETURN a.actorId";
                        result = tx.run(query, parameters("ID", step.end().id()));

                        DosPath.append((Objects.equals(result.next().get("a.actorId").toString(), actorId2))? "," : ", \"" + actorId2 + "\"]");

                        actor_movie = true;
                        continue;
                    }

                    query = "MATCH (a) WHERE ID(a) = $ID RETURN a.actorId";
                    result = tx.run(query, parameters("ID", step.start().id()));

                    DosPath.append(result.next().get("a.actorId")).append(",");

                    query = "MATCH (m) WHERE ID(m) = $ID RETURN m.movieId";
                    result = tx.run(query, parameters("ID", step.end().id()));

                    DosPath.append(result.next().get("m.movieId"));

                    actor_movie = false;
                }

                response = String.format("{\"DosPath\": %s}", DosPath);

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
}
