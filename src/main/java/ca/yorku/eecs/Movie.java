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

public class Movie implements HttpHandler {
    public Movie() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/getMovie/")) {
                handleGet(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/getRelatedMovies/")) {
                handleRelatedMovies(r);
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

        String name, movieId;

        if (deserialized.has("name") && deserialized.has("movieId")) {
            name = deserialized.getString("name");
            movieId = deserialized.getString("movieId");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            // refuses to create the node idk why but completes with no issue
            // use merge query instead?
            String query = "CREATE (a: actor {name: $name, movieId: movieId});";
            StatementResult result = session.run(query, parameters("name", name, "movieId", movieId));
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

        String movieId, response;

        if (deserialized.has("movieId")) movieId = deserialized.getString("movieId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            String query = "MATCH (a)-[:ACTED_IN*1]->(m: movie {movieId: $movieId}) RETURN DISTINCT a.actorId;";
            StatementResult result = session.run(query, parameters("movieId", movieId));

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

    public void handleRelatedMovies(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String movieId, response;

        if (deserialized.has("movieId")) movieId = deserialized.getString("movieId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            // TODO: create query
            String query = "";
            StatementResult result = session.run(query, parameters("movieId", movieId));

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
