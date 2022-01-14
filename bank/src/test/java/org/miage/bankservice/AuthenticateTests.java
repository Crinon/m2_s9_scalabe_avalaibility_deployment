package org.miage.bankservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.bankservice.boundary.AccountRepresentation;
import org.miage.bankservice.boundary.AccountResource;
import org.miage.bankservice.boundary.CardResource;
import org.miage.bankservice.entity.Account;
import org.miage.bankservice.entity.AccountInput;
import org.miage.bankservice.entity.Card;
import org.miage.bankservice.miscellaneous.ToolBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.net.URL;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthenticateTests {
    @LocalServerPort
    int port;

    @Autowired
    AccountResource accountResource;
    @Autowired
    CardResource cardResource;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AccountRepresentation accountRepresentation;

    public String getUserToken(String passport, String password) throws JSONException {
        String JSON_DATA = ""
                + "    {"
                + "      \"passportNumber\": \"" + passport + "\","
                + "      \"password\" : \"" + password + "\""
                + "}";
        Response response = given()
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Set token
        String jsonAsString = response.asString();
        JSONObject json = new JSONObject(jsonAsString);
        return json.getString("token");
    }

    @BeforeEach
    public void setupContext() {
        accountResource.deleteAll();
        cardResource.deleteAll();
        RestAssured.port = port;
    }
    @Test
    public void ping() {
        when().get("/authenticate").then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void getToken() throws JSONException {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(account);
        // Payload pour authentification
        String JSON_DATA = ""
                + "    {"
                + "      \"passportNumber\": \"" + passportNumber + "\","
                + "      \"password\" : \"" + password + "\""
                + "}";
        // Envoie des données de login
        Response response = given()
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Vérification de la présence d'un token
        String jsonAsString = response.asString();
        JSONObject json = new JSONObject(jsonAsString);
        Assert.assertTrue(json.has("token"));
    }

    @Test
    public void loginNoAccount() {
        // Payload pour authentification
        String JSON_DATA = ""
                + "    {"
                + "      \"passportNumber\": \"no\","
                + "      \"password\" : \"no\""
                + "}";
        // Envoie des données de login
        given()
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .extract()
                .response();

    }

    @Test
    public void loginBadPassword() {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(account);
        // Payload pour authentification
        String JSON_DATA = ""
                + "    {"
                + "      \"passportNumber\": \"" + passportNumber + "\","
                + "      \"password\" : \"incorrect_password\""
                + "}";
        // Envoie des données de login
        Response response = given()
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .post("/authenticate")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .extract()
                .response();
    }

}
