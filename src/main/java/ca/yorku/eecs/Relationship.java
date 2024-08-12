package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import java.io.IOException;
import java.io.OutputStream;

import static org.neo4j.driver.v1.Values.parameters;

public class Relationship implements HttpHandler {
    public Relationship() {
    }

    @Override
    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equalsIgnoreCase("PUT")) {
                handlePut(r);
            } else if (r.getRequestMethod().equalsIgnoreCase("GET")) {
                handleGet(r);
            } else {
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }

    private void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId, movieId;
        if (deserialized.has("actorId") && deserialized.has("movieId")) {
            actorId = deserialized.getString("actorId");
            movieId = deserialized.getString("movieId");
        } else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String query;
                StatementResult result;

                query = "MATCH (a:actor {id: $actorId}) RETURN a";
                result = tx.run(query, parameters("actorId", actorId));
                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                query = "MATCH (m:movie {id: $movieId}) RETURN m";
                result = tx.run(query, parameters("movieId", movieId));
                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                query = "MATCH (a:actor {id: $actorId}), (m:movie {id: $movieId}) MERGE (a)-[:ACTED_IN]->(m)";
                tx.run(query, parameters("actorId", actorId, "movieId", movieId));

//                System.out.println("Relationship created: Actor " + actorId + " -> Movie " + movieId);

                r.sendResponseHeaders(200, -1);
            } catch (Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
                r.sendResponseHeaders(500, -1);
            }
        }
    }

    private void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        boolean relationshipExists;
        String actorId, movieId;
        if (deserialized.has("actorId") && deserialized.has("movieId")) {
            actorId = deserialized.getString("actorId");
            movieId = deserialized.getString("movieId");
        } else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        JSONObject response = new JSONObject();
        response.put("actorId", actorId);
        response.put("movieId", movieId);

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String query;
                StatementResult result;

                query = "MATCH (a:actor {id: $actorId}) RETURN a";
                result = tx.run(query, parameters("actorId", actorId));
                if (!result.hasNext()) {
                    System.out.println("Actor not found: " + actorId);
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                query = "MATCH (m:movie {id: $movieId}) RETURN m";
                result = tx.run(query, parameters("movieId", movieId));
                if (!result.hasNext()) {
                    System.out.println("Movie not found: " + movieId);
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                query = "MATCH (a:actor {id: $actorId})-[:ACTED_IN]->(m:movie {id: $movieId}) RETURN a, m";
                result = tx.run(query, parameters("actorId", actorId, "movieId", movieId));

                relationshipExists = result.hasNext();

//                System.out.printf("Relationship %s found: Actor %s -> Movie %s\n", (relationshipExists)? "" : "not", actorId, movieId);

                response.put("hasRelationship", relationshipExists);

                String responseText = response.toString();
                r.sendResponseHeaders(200, responseText.length());
                OutputStream os = r.getResponseBody();
                os.write(responseText.getBytes());
                os.close();
            } catch (Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
                r.sendResponseHeaders(500, -1);
            }
        }
    }

}