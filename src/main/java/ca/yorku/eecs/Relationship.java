package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Relationship implements HttpHandler {
    public Relationship() {

    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePost(r);
            } else if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            } else {
                r.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    public void handlePost(HttpExchange r) {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        if (deserialized.has("actorId") && deserialized.has("movieId")) {
            String actorId = deserialized.getString("actorId");
            String movieId = deserialized.getString("movieId");
            
            try (Session session = Utils.driver.session()) {
                String query = String.format("MATCH (a:Actor {id: '%s'}), (m:Movie {id: '%s'}) CREATE (a)-[:ACTED_IN]->(m)", actorId, movieId);
                session.run(query);
                r.sendResponseHeaders(200, -1); 
            } catch (Exception e) {
                r.sendResponseHeaders(500, -1); 
            }
        } else {
            r.sendResponseHeaders(400, -1); 
        }
    }

    public void handleGet(HttpExchange r) {
        String query = r.getRequestURI().getQuery();
        String[] params = query.split("&");
        String actorId = null;
        String movieId = null;

        for (String param : params) {
            String[] pair = param.split("=");
            if (pair[0].equals("actorId")) {
                actorId = pair[1];
            } else if (pair[0].equals("movieId")) {
                movieId = pair[1];
            }
        }

        if (actorId == null || movieId == null) {
            r.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        try (Session session = Utils.driver.session()) {
            String queryStr = String.format("MATCH (a:Actor {id: '%s'})-[:ACTED_IN]->(m:Movie {id: '%s'}) RETURN a, m", actorId, movieId);
            StatementResult result = session.run(queryStr);

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
            } else {
                r.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1); 
        }
    }
    }
}
