package org.miage.bankservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URL;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CardTests {

    @LocalServerPort
    int port;

    @Autowired
    AccountResource accountResource;
    @Autowired
    AccountRepresentation accountRepresentation;
    @Autowired
    CardResource cardResource;
    @Autowired
    PasswordEncoder passwordEncoder;


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
    public void pingNotAuth() {
        when().get("cards").then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void getMyCard() throws JSONException {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
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
        cardResource.save(newCard);

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        Response response = given()
                .auth().oauth2(token)
                .get("/cards")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("300.00"));
    }

    @Test
    public void getCardByNumber() throws JSONException {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
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
        cardResource.save(newCard);

        Response response = given()
                .get("/cards/cardid/"+newCard.getNumber())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString(newCard.getIdcard()));
    }


    @Test
    public void getByIdMyCard() throws JSONException {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
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
        cardResource.save(newCard);

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        Response response = given()
                .auth().oauth2(token)
                .get("/cards/" + newCard.getIdcard())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("300.00"));
    }

    @Test
    public void getByIdNotMyCard() throws JSONException {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
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
        cardResource.save(newCard);

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        Response response = given()
                .auth().oauth2(token)
                .get("/cards/777")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchNotFoundCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"blocked\" : true"
                + "}";

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/777")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .extract()
                .response();
    }

    @Test
    public void patchNotMyCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        // Création du compte étranger et de la carte étrangère à patcher
        Card notMyCard = new Card();
        Account notMyAccount = new Account(
                UUID.randomUUID().toString(),
                "Richard",
                "Lebeau",
                Account.Country.FRANCE,
                "55GG66666",
                "+33670006767",
                ToolBox.generateIBAN(),
                notMyCard.getIdcard(),
                null,
                null,
                passwordEncoder.encode("dontcare")
        );
        accountResource.save(notMyAccount);
        cardResource.save(notMyCard);

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"blocked\" : true"
                + "}";

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/"+notMyCard.getIdcard())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchBlockedCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"blocked\" : true"
                + "}";

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputBlockedCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"blocked\" : 123"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchRegionlockCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"regionLocked\" : true"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputRegionlockCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"regionLocked\" : 123"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchContactlessCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"contactless\" : false"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputContactlessCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"contactless\" : 123"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchSlidinglimitCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                password);
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"slidinglimit\" : 1234"
                + "}";

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputSlidinglimitTolowCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"slidinglimit\" : 49"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputSlidinglimitToobigCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"slidinglimit\" : 10000"
                + "}";

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputNullSlidinglimitCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"slidinglimit\" : null"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void patchBadInputSlidinglimitNotIntCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"slidinglimit\" : true"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    // PATCH protected field (money +1)
    @Test
    public void patchProtectedFieldMoneyCard() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = response.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        String JSON_DATA = ""
                + "    {"
                + "      \"cash\" : 1234"
                + "}";

        // Envoi des inputs et vérification du code HTTP400 SC_BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }


    @Test
    public void assemblerSelfAccount() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");
        Response responseAccount = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = responseAccount.getHeader("Location");
        Response response = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = response.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        Response responseGet = given()
                .auth().oauth2(token)
                .get("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

//		Récupération du lien assembleur de la carte
        String jsonAsStringCard = responseGet.asString();
        JSONObject jsonCard = new JSONObject(jsonAsStringCard);
        JSONObject links = jsonCard.getJSONObject("_links");
        String selfUrl = links.getJSONObject("self").getString("href");
        URL url = new URL(selfUrl);
        given()
                .auth().oauth2(token)
                .get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerCollectionAccount() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";

        // Création du compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                "+49675896767",
                null,
                null,
                null,
                null, "a");
        Response responseAccount = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        String location = responseAccount.getHeader("Location");
        Response response = given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = response.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        Response responseGet = given()
                .auth().oauth2(token)
                .get("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

//		Récupération du lien assembleur de la collection des cartes
        String jsonAsStringCard = responseGet.asString();
        JSONObject jsonCard = new JSONObject(jsonAsStringCard);
        JSONObject links = jsonCard.getJSONObject("_links");
        String selfUrl = links.getJSONObject("collection").getString("href");
        URL url = new URL(selfUrl);
        given()
                .auth().oauth2(token)
                .get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }


}
