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

public class Actor implements HttpHandler {
    public Actor() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/getActor")) {
                handleGet(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/checkRelatedActors")) {
                handleRelatedActors(r);
            }
            else{
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId, name, query;
        StatementResult result;

        if (deserialized.has("name") && deserialized.has("actorId")) {
            name = deserialized.getString("name");
            actorId = deserialized.getString("actorId");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            query = "MERGE (a: actor {name: $name, id: $actorId});";
            result = session.run(query, parameters("name", name, "actorId", actorId));

            r.sendResponseHeaders(200, -1);
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId, name, query, response;
        StringBuilder movies;
        StatementResult result;

        if (deserialized.has("actorId")) actorId = deserialized.getString("actorId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (a: actor {id: $actorId}) RETURN a.name;";
                result = tx.run(query, parameters("actorId", actorId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                name = result.next().get("a.name").asString();

                query = "MATCH (a: actor {id: $actorId})-[:ACTED_IN*1]->(m: movie) RETURN DISTINCT m.id;";
                result = tx.run(query, parameters("actorId", actorId));

                movies = new StringBuilder("[");
                if (!result.hasNext()) movies.append("]");
                else while (result.hasNext()) movies.append(result.next().get("m.id")).append((result.hasNext())? "," : "]");

                response = String.format("{\"actorId\": \"%s\", \"name\": \"%s\", \"movies\": %s}", actorId, name, movies);

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

    public void handleRelatedActors(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId1, actorId2, query, response;
        StringBuilder movies;
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
                    query = "MATCH (a: actor {id: $actorId}) RETURN a.name;";
                    result = tx.run(query, parameters("actorId", actorId));

                    if (!result.hasNext()) {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }
                }

                query = "MATCH (a1: actor {id: $actorId1})-[:ACTED_IN]->(m: movie)<-[:ACTED_IN]-(a2: actor {id: $actorId2}) RETURN DISTINCT m.id;";
                result = tx.run(query, parameters("actorId1", actorId1, "actorId2", actorId2));

                movies = new StringBuilder("[");

                if (!result.hasNext()) movies.append("]");
                else while (result.hasNext()) movies.append(result.next().get("m.id")).append((result.hasNext())? "," : "]");

                response = String.format("{\"movies\": %s}", movies);

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
