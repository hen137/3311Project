package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        Actor actor = new Actor();
        Movie movie = new Movie();
        Relationship relationship = new Relationship();
        Bacon bacon = new Bacon();
        DoS dos = new DoS();

        server.createContext("/api/v1/addActor/", actor);
        server.createContext("/api/v1/getActor/", actor);
        server.createContext("/api/v1/allMovies", actor);

        server.createContext("/api/addMovie/", movie);
        server.createContext("/api/getMovie/", movie);
        server.createContext("/api/allActors/", movie);

        server.createContext("/api/addRelationship/", relationship);
        server.createContext("/api/hasRelationship/", relationship);

        server.createContext("/api/computeBaconNumber/", bacon);
        server.createContext("/api/computeBaconPath/", bacon);

        server.createContext("/api/computeDoSNumber/", dos);
        server.createContext("/api/computeDoSPath/", dos);

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
