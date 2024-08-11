package ca.yorku.eecs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;


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

    public void testaddActorPass() {
        String payload = "{ \"name\": \"Neve Campbell\", \"actorId\": \"nc200102\" }";
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
                .put("/api/v1/addActor/");
        assertEquals(400, response.getStatusCode());
    }


    public void testaddMovieFail() {
        String payload = "{ \"movieID\": \"ps24311\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addMovie/");
        assertEquals(400, response.getStatusCode());
    }

    public void testaddMoviePass() {
        String payload = "{ \"title\": \"Wild Things\", \"movieId\": \"wt12345\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addMovie/");
        assertEquals(200, response.getStatusCode());
    }

    public void testgetActorPass()  {
        String payload = "{ \"actorId\": \"th708951\",}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/getActor/");
        assertEquals(200, response.getStatusCode());
    }

    public void testgetActorFail() {
        String payload = "{ \"actorId\": \"2\",}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/getActor/");
        assertEquals(404, response.getStatusCode());
    }

    public void testgetMoviePass() {
        String payload = "{ \"movieId\": \"sm234571\",}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/getMovie/");
        assertEquals(200, response.getStatusCode());
    }

    public void testgetMovieFail() {
        String payload = "{ \"movieId\": \"sm234991\",}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/getMovie/");
        assertEquals(404, response.getStatusCode());
    }

    public void testaddRelationshipPass() {
        String payload = "{ \"actorId\": \"nc200102\", \"movieId\": \"wt12345\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addRelationship/");
        assertEquals(200, response.getStatusCode());
    }

    public void testaddRelationshipFail() {
        String payload = "{ \"actorId\": \"ld10011\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v1/addRelationship/");
        assertEquals(400, response.getStatusCode());
    }

    public void testhasRelationshipPass() {

        String payload = "{ \"actorId\": \"th708951\", \"movieId\": \"sm234571\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/hasRelationship/");
        assertEquals(200, response.getStatusCode());
    }

    public void testhasRelationshipFail() {

        String payload = "{ \"actorId\": \"abcd\", \"movieId\": \"sm234571\" }";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/hasRelationship/");
        assertEquals(404, response.getStatusCode());
    }

    public void testcomputeBaconNumberPass() {
        String payload = "{ \"actorId\": \"nc200102\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/computeBaconNumber/");
        assertEquals(200, response.getStatusCode());
    }

    public void testcomputeBaconNumberFail() {
        String payload = "{ \"actorId\": \"12345\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/computeBaconNumber/");
        assertEquals(404, response.getStatusCode());
    }

    public void testcomputeBaconPathPass() {
        String payload = "{ \"actorId\": \"nc200102\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/computeBaconPath/");
        assertEquals(200, response.getStatusCode());
    }

    public void testcomputeBaconPathFail() {
        String payload = "{ \"actorId\": \"a0002\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/computeBaconPath/");
        assertEquals(404, response.getStatusCode());
    }

    public void testcheckRelatedActorsPass() {
        String payload = "{ \"actorId1\": \"nm0000102\", \"actorId2\": \"nc200102\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/checkRelatedActors/");
        assertEquals(200, response.getStatusCode());
    }

    public void testcheckRelatedActorsFail() {
        String payload = "{ \"actorId1\": \"abcd\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/checkRelatedActors/");
        assertEquals(400, response.getStatusCode());
    }

    public void testgetRelatedMoviesPass() {
        String payload = "{ \"movieId\": \"m0004\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/getRelatedMovies/");
        assertEquals(200, response.getStatusCode());
    }

    public void testgetRelatedMoviesFail() {
        String payload = "{ \"actorID\": \"m0002\"}";
        Response response = given()
                .contentType("application/json")
                .body(payload)
                .when()
                .get("/api/v1/getRelatedMovies/");
        assertEquals(400, response.getStatusCode());


    }


    //Dont Work right now




}
