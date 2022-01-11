package org.miage.bankservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.net.URL;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CardTests {

    @LocalServerPort
    int port;

    @Autowired
    AccountResource accountResource;
    @Autowired
    CardResource cardResource;

    @BeforeEach
    public void setupContext() {
        accountResource.deleteAll();
        cardResource.deleteAll();
        RestAssured.port = port;
    }


    @Test
    public void pingCards() {
        when().get("/cards").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getNotFoundCard() {
        when().get("/cards/42").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void codeGetOneCard() {
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                "64EB75640",
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null, "a");
        accountResource.save(account);
        cardResource.save(newCard);
        Response response = when().get("/cards/" + newCard.getIdcard())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("300.00"));
    }

    @Test
    public void apiGetOneCard() throws Exception {
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");
        when().get("/cards/" + cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

    }

    @Test
    public void getAllCards() {
        cardResource.save(new Card());
        cardResource.save(new Card());
        when().get("/cards/")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .body("_embedded.cards.size()", equalTo(2));
    }

    @Test
    public void getOneCardByNumber() {
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                "64EB75640",
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null, "a");
        accountResource.save(account);
        cardResource.save(newCard);
        Response response = when().get("/cards/cardid/" + newCard.getNumber())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("300.00"));
    }

    @Test
    public void patchBlockedCard() throws Exception {
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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

        String location = response.getHeader("Location");
        Response responseGet = when().get(location)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responsePatch = given()
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
        Response responseAccount = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        String location = responseAccount.getHeader("Location");
        Response response = when().get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = response.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        Response responseGet = when().get("/cards/" + cardid)
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
        when().get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerCollectionAccount() throws Exception {
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
        Response responseAccount = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        String location = responseAccount.getHeader("Location");
        Response response = when().get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        // Récupération de l'id de la carte créée
        String jsonAsString = response.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String cardid = json.getString("fkidcard");

        Response responseGet = when().get("/cards/" + cardid)
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
        when().get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }


}
