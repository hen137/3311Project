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
            if (r.getRequestMethod().equalsIgnoreCase("PUT") && r.getHttpContext().getPath().equals("/api/v1/addRelationship/")) {
                handlePut(r);
            } else if (r.getRequestMethod().equalsIgnoreCase("GET") && r.getHttpContext().getPath().equals("/api/v1/hasRelationship/")) {
                handleGet(r);
            } else {
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            StatementResult result;
            String query;

            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (a:Actor {id: $actorId}) RETURN a";
                result = tx.run(query, parameters("actorId", actorId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1); 
                    return;
                }
            }

            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (m:Movie {id: $movieId}) RETURN m";
                result = tx.run(query, parameters("movieId", movieId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1); 
                    return;
                }
            }

            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (a:Actor {id: $actorId}), (m:Movie {id: $movieId}) " +
                        "MERGE (a)-[:ACTED_IN]->(m)";
                tx.run(query, parameters("actorId", actorId, "movieId", movieId));
                tx.success();
                System.out.println("Relationship created: Actor " + actorId + " -> Movie " + movieId);
                r.sendResponseHeaders(200, -1); 
            } catch (Exception e) {
                e.printStackTrace();
                r.sendResponseHeaders(500, -1); 
            }
        }
    }

    private void handleGet(HttpExchange r) throws IOException, JSONException {
        String query = r.getRequestURI().getQuery();
        String actorId = null;
        String movieId = null;

        String[] params = query.split("&");
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair[0].equals("actorId")) {
                actorId = pair[1];
            } else if (pair[0].equals("movieId")) {
                movieId = pair[1];
            }
        }

        if (actorId == null || movieId == null) {
            r.sendResponseHeaders(400, -1); 
            return;
        }

        try (Session session = Utils.driver.session()) {
            StatementResult result;
            String query1;

            try (Transaction tx = session.beginTransaction()) {
                query1 = "MATCH (a:Actor {id: $actorId}) RETURN a";
                result = tx.run(query1, parameters("actorId", actorId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }
            }

            try (Transaction tx = session.beginTransaction()) {
                query1 = "MATCH (m:Movie {id: $movieId}) RETURN m";
                result = tx.run(query1, parameters("movieId", movieId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }
            }

            try (Transaction tx = session.beginTransaction()) {
                String queryStr = "MATCH (a:Actor {id: $actorId})-[:ACTED_IN]->(m:Movie {id: $movieId}) RETURN a, m";
                result = tx.run(queryStr, parameters("actorId", actorId, "movieId", movieId));

                if (result.hasNext()) {
                    JSONObject response = new JSONObject();
                    response.put("actorId", actorId);
                    response.put("movieId", movieId);
                    response.put("hasRelationship", true);

                    String responseText = response.toString();
                    r.sendResponseHeaders(200, responseText.length()); 
                    OutputStream os = r.getResponseBody();
                    os.write(responseText.getBytes());
                    os.close();
                    System.out.println("Relationship found: Actor " + actorId + " -> Movie " + movieId);
                } else {
                    r.sendResponseHeaders(404, -1); 
                    System.out.println("No relationship found: Actor " + actorId + " -> Movie " + movieId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                r.sendResponseHeaders(500, -1); 
            }
        }
    }
}
