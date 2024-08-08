package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;

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
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/getActor/")) {
                handleGet(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/checkRelatedActors/")) {
                handleRelatedActors(r);
            }
            else{
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String name, actorId;

        if (deserialized.has("name") && deserialized.has("actorId")) {
            name = deserialized.getString("name");
            actorId = deserialized.getString("actorId");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            // refuses to create the node idk why but completes with no issue
            // use merge query instead?
            String query = "CREATE (a: actor {name: $name, actorID: $actorId});";
            StatementResult result = session.run(query, parameters("name", name, "actorId", actorId));
            while (result.hasNext()) System.out.println(result.next());
            r.sendResponseHeaders(200, -1);
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId, response;

        if (deserialized.has("actorId")) actorId = deserialized.getString("actorId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            String query = "MATCH (a: actor {actorId: $actorId})-[:ACTED_IN*1]->(m) RETURN DISTINCT m.movieId;";
            StatementResult result = session.run(query, parameters("actorId", actorId));

            // TODO: extract result, format response
            response = "test 2";

            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (NoSuchRecordException e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(404, -1);
        }
        catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handleRelatedActors(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String actorId1, actorId2, response;

        if (deserialized.has("actorId1") && deserialized.has("actorId2")) {
            actorId1 = deserialized.getString("actorId1");
            actorId2 = deserialized.getString("actorId2");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            String query = "MATCH (a: actor {actorId: $actorId1})-[:ACTED_IN]->(m)<-[:ACTED_IN]-(a: actor {actorId: $actorId2}) RETURN DISTINCT m;";
            StatementResult result = session.run(query, parameters("actorId1", actorId1, "actorId2", actorId2));

            // TODO: extract result, format response
            response = "";

            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (NoSuchRecordException e) {
            r.sendResponseHeaders(404, -1);
        }
        catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }
}
