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

public class Movie implements HttpHandler {
    public Movie() {

    }

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/getMovie")) {
                handleGet(r);
            }
            else if (r.getRequestMethod().equals("GET") && r.getHttpContext().getPath().equals("/api/v1/getRelatedMovies")) {
                handleRelatedMovies(r);
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

        String movieId, name, query;
        StatementResult result;

        if (deserialized.has("name") && deserialized.has("movieId")) {
            name = deserialized.getString("name");
            movieId = deserialized.getString("movieId");
        }
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            query = "MERGE (m: movie {name: $name, id: $movieId});";
            result = session.run(query, parameters("name", name, "movieId", movieId));

            r.sendResponseHeaders(200, -1);
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            r.sendResponseHeaders(500, -1);
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String movieId, name, query, response;
        StringBuilder actors;
        StatementResult result;

        if (deserialized.has("movieId")) movieId = deserialized.getString("movieId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (m: movie {id: $movieId}) RETURN m.name;";
                result = tx.run(query, parameters("movieId", movieId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                name = result.next().get("movie.name").asString();

                query = "MATCH (a: actor)-[:ACTED_IN*1]->(m: movie {id: $movieId}) RETURN DISTINCT a.name;";
                result = tx.run(query, parameters("movieId", movieId));

                actors = new StringBuilder("[");
                if (!result.hasNext()) actors.append("]");
                else while (result.hasNext()) actors.append(result.next().get("a.name")).append((result.hasNext())? "," : "]");

                response = String.format("{\"movieId\": \"%s\", \"name\": \"%s\", \"actors\": %s}", movieId, name, actors);

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

    public void handleRelatedMovies(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String movieId, query, response;
        StringBuilder movies;
        StatementResult result;

        if (deserialized.has("movieId")) movieId = deserialized.getString("movieId");
        else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        try (Session session = Utils.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                query = "MATCH (m: movie {id: $movieId}) RETURN m.name;";
                result = tx.run(query, parameters("movieId", movieId));

                if (!result.hasNext()) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }

                query = "MATCH (m1: movie {id: $movieId})-[:FRANCHISE]->(m2: movie) RETURN DISTINCT m2.id;";
                result = tx.run(query, parameters("movieId", movieId));

                movies = new StringBuilder("[");

                while (result.hasNext()) movies.append(result.next().get("m.id")).append((result.hasNext())? "," : "");

                query = "MATCH (m1: movie)-[:FRANCHISE]->(m2: movie {id: $movieId}) RETURN DISTINCT m1.id;";
                result = tx.run(query, parameters("movieId", movieId));

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
