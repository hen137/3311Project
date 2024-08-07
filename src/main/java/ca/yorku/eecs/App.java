package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
    private static final Actor ACTOR = new Actor();
    private static final Movie MOVIE = new Movie();
    private static final Relationship RELATIONSHIP = new Relationship();
    private static final Bacon BACON = new Bacon();
    private static final DoS DOS = new DoS();

    static int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        server.createContext("/api/v1/addActor/", ACTOR);
        server.createContext("/api/v1/getActor/", ACTOR);
        server.createContext("/api/v1/checkRelatedActors/", ACTOR);

        server.createContext("/api/v1/addMovie/", MOVIE);
        server.createContext("/api/v1/getMovie/", MOVIE);
        server.createContext("/api/v1/getRelatedMovies/", MOVIE);

        server.createContext("/api/v1/addRelationship/", RELATIONSHIP);
        server.createContext("/api/v1/hasRelationship/", RELATIONSHIP);

        server.createContext("/api/v1/computeBaconNumber/", BACON);
        server.createContext("/api/v1/computeBaconPath/", BACON);

        server.createContext("/api/v1/computeDoSNumber/", DOS);
        server.createContext("/api/v1/computeDoSPath/", DOS);

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
