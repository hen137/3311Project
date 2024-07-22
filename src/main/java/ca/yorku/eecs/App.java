package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    public static final Actor ACTOR = new Actor();
    public static final Movie MOVIE = new Movie();
    public static final Relationship RELATIONSHIP = new Relationship();
    public static final Bacon BACON = new Bacon();
    public static final DoS DOS = new DoS();

    static int PORT = 8080;

    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        server.createContext("/api/v1/addActor/", ACTOR);
        server.createContext("/api/v1/getActor/", ACTOR);
        server.createContext("/api/v1/allMovies", ACTOR);

        server.createContext("/api/addMovie/", MOVIE);
        server.createContext("/api/getMovie/", MOVIE);
        server.createContext("/api/allActors/", MOVIE);

        server.createContext("/api/addRelationship/", RELATIONSHIP);
        server.createContext("/api/hasRelationship/", RELATIONSHIP);

        server.createContext("/api/computeBaconNumber/", BACON);
        server.createContext("/api/computeBaconPath/", BACON);

        server.createContext("/api/computeDoSNumber/", DOS);
        server.createContext("/api/computeDoSPath/", DOS);

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
