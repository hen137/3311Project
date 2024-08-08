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
        assertEquals("Pass added successfully", response.jsonPath().getString("message"));
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
        assertEquals("Fail to add actor", response.jsonPath().getString("message"));
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
        assertEquals("Fail to add movie", response.jsonPath().getString("message"));
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
        assertEquals("Pass added successfully", response.jsonPath().getString("message"));
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
        assertEquals("Pass added successfully", response.jsonPath().getString("message"));
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
        assertEquals("Fail to add relationship", response.jsonPath().getString("message"));
    }

    @org.junit.Test
    public void getActorPass() {
        Response response = given()
                .contentType("application/json")
                .when()
                .get("/api/v1/getActor/ld10011");
        assertEquals(200, response.getStatusCode());
        assertEquals("Leonardo DiCaprio", response.jsonPath().getString("name"));
    }

    @org.junit.Test
    public void getActorFail() {
        Response response = given()
                .contentType("application/json")
                .when()
                .get("/api/v1/getActor/ld10012");
        assertEquals(400, response.getStatusCode());
        assertEquals("Fail to get actor", response.jsonPath().getString("message"));
    }

    @org.junit.Test
    public void getMoviePass() {
        Response response = given()
                .contentType("application/json")
                .when()
                .get("/api/v1/getMovie/ps24311");
        assertEquals(200, response.getStatusCode());
        assertEquals("Parasite", response.jsonPath().getString("name"));
    }

    @org.junit.Test
    public void getMovieFail() {
        Response response = given()
                .contentType("application/json")
                .when()
                .get("/api/v1/getMovie/ps24312");
        assertEquals(400, response.getStatusCode());
        assertEquals("Fail to get movie", response.jsonPath().getString("message"));
    }
    
}
