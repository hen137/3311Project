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

    //@org.junit.TestOpen right now
    public void testaddActorPass() {
        String payload = "{ \"name\": \"Leonardo DiCaprio\", \"actorId\": \"ld10011\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addActor/");
        assertEquals(200, response.getStatusCode());
    }


    public void testaddActorFail() {
        String payload = "{ \"name\": \"Denzel Washington\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addActor");
        assertEquals(400, response.getStatusCode());
    }


    public void testaddMovieFail() {
        String payload = "{ \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addMovie");
        assertEquals(400, response.getStatusCode());
    }

    public void testaddMoviePass() {
        String payload = "{ \"name\": \"Parasite\", \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addMovie");
        assertEquals(200, response.getStatusCode());
    }

    public void testaddRelationshipPass() {
        String payload = "{ \"actorID\": \"ld10011\", \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addRelationship");
        assertEquals(200, response.getStatusCode());
    }

    public void testaddRelationshipFail() {
        String payload = "{ \"actorID\": \"ld10011\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addRelationship");
        assertEquals(400, response.getStatusCode());
    }

    public void testgetActorPass() {
        Response response = given()
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/getActor");
        assertEquals(200, response.getStatusCode());
    }

    public void testgetActorFail() {
        Response response = given()
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/getActor");
        assertEquals(404, response.getStatusCode());
    }

    public void testgetMoviePass() {
        Response response = given()
                .queryParam("movieID", "ps24311")
                .when()
                .get("/api/v1/getMovie");
        assertEquals(200, response.getStatusCode());
    }

    public void testgetMovieFail() {
        Response response = given()
                .queryParam("movieID", "ss24312")
                .when()
                .get("/api/v1/getMovie");
        assertEquals(404, response.getStatusCode());
    }

    public void testhasRelationshipPass() {
        Response response = given()
                .queryParam("movieID", "ss24312")
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/hasRelationship");
        assertEquals(200, response.getStatusCode());
    }

    public void testhasRelationshipFail() {
        Response response = given()
                .queryParam("movieID", "ps24311")
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/hasRelationship");
        assertEquals(404, response.getStatusCode());
    }

    public void testcomputeBaconNumberPass() {
        Response response = given()
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/computeBaconNumber");
        assertEquals(200, response.getStatusCode());
    }

    public void testcomputeBaconNumberFail() {
        Response response = given()
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/computeBaconNumber");
        assertEquals(404, response.getStatusCode());
    }

    public void testcomputeBaconPathPass() {
        Response response = given()
                .queryParam("actorID", "ld10011")
                .when()
                .get("/api/v1/computeBaconPath");
        assertEquals(200, response.getStatusCode());
    }

    public void testcomputeBaconPathFail() {
        Response response = given()
                .queryParam("actorID", "cd10011")
                .when()
                .get("/api/v1/computeBaconPath");
        assertEquals(404, response.getStatusCode());
    }

    //Actor ID with no path to Kevin Bacon
    public void testcomputeBaconPathNoPath() {
        Response response = given()
                .queryParam("actorId", "nm9999999") // Actor ID with no path to Kevin Bacon
                .when()
                .get("/api/v1/computeBaconPath");

        // Assert the response code is 404 NOT FOUND
        assertEquals(404, response.getStatusCode());
    }

    //Multiple Bacon Paths with same bacon number
    public void testcomputeBaconPathMultiplePaths() {
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
    public void testcomputeBaconPathKevinBacon() {
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
