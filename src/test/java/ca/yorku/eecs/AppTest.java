package ca.yorku.eecs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import io.restassured.response.Response;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    @org.junit.Test
    public void addActorPass() {
        String payload = "{ \"name\": \"Leonardo DiCaprio\", \"actorID\": \"ld10011\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addActor");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void addActorFail() {
        String payload = "{ \"name\": \"Denzel Washington\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addActor");
        assertEquals(400, response.getStatusCode());
    }

    @org.junit.Test
    public void addMovieFail() {
        String payload = "{ \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addMovie");
        assertEquals(400, response.getStatusCode());
    }

    @org.junit.Test
    public void addMoviePass() {
        String payload = "{ \"name\": \"Parasite\", \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addMovie");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void addRelationshipPass() {
        String payload = "{ \"actorID\": \"ld10011\", \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addRelationship");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void addRelationshipFail() {
        String payload = "{ \"actorID\": \"ld10011\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addRelationship");
        assertEquals(400, response.getStatusCode());
    }

    @org.junit.Test
    public void getActorPass() {
        Response response = given()
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/getActor");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void getActorFail() {
        Response response = given()
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/getActor");
        assertEquals(404, response.getStatusCode());
    }

    @org.junit.Test
    public void getMoviePass() {
        Response response = given()
                .queryParam("movieID", "ps24311")
                .when()
                .get("/api/v1/getMovie");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void getMovieFail() {
        Response response = given()
                .queryParam("movieID", "ss24312")
                .when()
                .get("/api/v1/getMovie");
        assertEquals(404, response.getStatusCode());
    }

    @org.junit.Test
    public void hasRelationshipPass() {
        Response response = given()
                .queryParam("movieID", "ss24312")
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/hasRelationship");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void hasRelationshipFail() {
        Response response = given()
                .queryParam("movieID", "ps24311")
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/hasRelationship");
        assertEquals(404, response.getStatusCode());
    }

    @org.junit.Test
    public void computeBaconNumberPass() {
        Response response = given()
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/computeBaconNumber");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void computeBaconNumberFail() {
        Response response = given()
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/computeBaconNumber");
        assertEquals(404, response.getStatusCode());
    }

    @org.junit.Test
    public void computeBaconPathPass() {
        Response response = given()
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/computeBaconPath");
        assertEquals(200, response.getStatusCode());
    }

    @org.junit.Test
    public void computeBaconPathFail() {
        Response response = given()
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/computeBaconPath");
        assertEquals(404, response.getStatusCode());
    }

    //Actor ID with no path to Kevin Bacon
    @org.junit.Test
    public void computeBaconPathNoPath() {
        Response response = given()
                .queryParam("actorId", "nm9999999") // Actor ID with no path to Kevin Bacon
                .when()
                .get("/api/v1/computeBaconPath");

        // Assert the response code is 404 NOT FOUND
        assertEquals(404, response.getStatusCode());
    }

    //Multiple Bacon Paths with same bacon number
    @org.junit.Test
    public void computeBaconPathMultiplePaths() {
        Response response = given()
                .queryParam("actorId", "nm0000102") // Actor ID with multiple paths to Kevin Bacon
                .when()
                .get("/api/v1/computeBaconPath");

        // Assert the response code is 200 OK
        assertEquals(200, response.getStatusCode());

        // Assert the response body is not null
        // Check that the response contains Kevin Bacon's ID
        String baconId = "nm0000102"; // Kevin Bacon's actorId
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains(baconId));
    }

    //Bacon path for Kevin Bacon
    @org.junit.Test
    public void computeBaconPathKevinBacon() {
        Response response = given()
                .queryParam("actorId", "nm0000102") // Kevin Bacon's actorId
                .when()
                .get("/api/v1/computeBaconPath");

        // Assert the response code is 200 OK
        assertEquals(200, response.getStatusCode());

        // Assert the response body is not null
        // Check that the response contains Kevin Bacon's ID
        String baconId = "nm0000102"; // Kevin Bacon's actorId
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains(baconId));
    }

}
