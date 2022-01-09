package org.miage.bankservice;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.bankservice.boundary.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.bankservice.boundary.AccountResource;
import org.miage.bankservice.boundary.CardResource;
import org.miage.bankservice.boundary.OperationRepresentation;
import org.miage.bankservice.boundary.OperationResource;
import org.miage.bankservice.entity.*;
import org.miage.bankservice.miscellaneous.ToolBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    public class TransfertTests {
        @LocalServerPort
        int port;

        @Autowired
        AccountResource accountResource;
        @Autowired
        CardResource cardResource;
        @Autowired
        OperationResource transfertResource;
        @Autowired
        TransfertRepresentation transfertRepresentation;


        @BeforeEach
        public void setupContext() {
            transfertResource.deleteAll();
            cardResource.deleteAll();
            accountResource.deleteAll();
            RestAssured.port = port;
        }

        @Test
        public void pingAccounts() {
            when().get("/transferts").then().statusCode(HttpStatus.SC_OK);
        }

        @Test
        public void getNotFoundAccount() {
            when().get("/transferts/11").then().statusCode(HttpStatus.SC_NOT_FOUND);
        }

        @Test
        public void postOneTransfert() throws Exception {
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            when().get(location).then().statusCode(HttpStatus.SC_OK);
        }


        @Test
        public void getAllTransfert() throws Exception {
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();
            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            when().get(location).then().statusCode(HttpStatus.SC_OK);

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response2 = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location2 = response2.getHeader("Location");
            when().get(location2).then().statusCode(HttpStatus.SC_OK);

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response3 = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location3 = response3.getHeader("Location");
            when().get(location3).then().statusCode(HttpStatus.SC_OK);

            when().get("/transferts/")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .and()
                    .assertThat()
                    .body("_embedded.transferts.size()",equalTo(3));
        }


        // Get all transferts from one account
        @Test
        public void getTransfertsFromAccount() throws Exception {
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            Card newCard3 = new Card();
            Account accountCustomer2 = new Account(
                    UUID.randomUUID().toString(),
                    "Bernard",
                    "Benoit",
                    Account.Country.GERMANY,
                    "77TT77777",
                    "+49675806767",
                    ToolBox.generateIBAN(),
                    newCard3.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer2);
            cardResource.save(newCard3);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            TransfertInput transfertInput2 = new TransfertInput(
                    12.00,
                    accountCustomer2.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();
            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            when().get(location).then().statusCode(HttpStatus.SC_OK);

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response2 = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location2 = response2.getHeader("Location");
            when().get(location2).then().statusCode(HttpStatus.SC_OK);

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response3 = given()
                    .body(ToolBox.toJsonString(transfertInput2))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location3 = response3.getHeader("Location");
            when().get(location3).then().statusCode(HttpStatus.SC_OK);

            when().get("/transferts/account/"+accountCustomer.getId())
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .and()
                    .assertThat()
                    .body("_embedded.transferts.size()",equalTo(2));
        }


        // ID from faux
        @Test
        public void postBadFromInputTransfert() throws Exception {
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    "fakeshopid"
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .extract()
                    .response();
        }


        // Bad input to
        @Test
        public void postBadToInputTransfert() throws Exception {
            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    "facustomerid",
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .extract()
                    .response();
        }


        // Bad input amount
        @Test
        public void postBadInputAmountTransfert() throws Exception {
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            String JSON_DATA = ""
                    + "    {"
                    + "      \"amount\" : true,"
                    + "      \"idaccountFrom\" : "+accountShop.getId()+","
                    + "      \"idaccountTo\" : "+accountCustomer.getId()
                    + "}";

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(JSON_DATA)
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .extract()
                    .response();
        }

        // Transfert avec taux != 1.00
        @Test
        public void postRatedTransfert() throws Exception {
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.UK,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    100.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            when().get(location).then().statusCode(HttpStatus.SC_OK);

            List<Card> cards = cardResource.findAll();
            System.out.println(cards.get(0).getIdcard());
            // On vérifie que le client a été débité avec le taux
            assertEquals("200.0",cards.get(0).getCash());
            // On vérifie que le shop a reçu son amount normal
            assertEquals("420.0",cards.get(1).getCash());
        }


        // Assembler
        @Test
        public void assemblerSelfTransfert() throws Exception{
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            Response responseTransfert = when().get(location).then().statusCode(HttpStatus.SC_OK).extract().response();

//		Récupération du lien assembleur de la carte
            String jsonAsStringCard = responseTransfert.asString();
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
        public void assemblerAccountSenderTransfert() throws Exception{
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            Response responseTransfert = when().get(location).then().statusCode(HttpStatus.SC_OK).extract().response();

//		Récupération du lien assembleur de la carte
            String jsonAsStringCard = responseTransfert.asString();
            JSONObject jsonCard = new JSONObject(jsonAsStringCard);
            JSONObject links = jsonCard.getJSONObject("_links");
            String selfUrl = links.getJSONObject("account_sender").getString("href");
            URL url = new URL(selfUrl);
            when().get(url.getPath())
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .response();
        }

        @Test
        public void assemblerAccountReceiverTransfert() throws Exception{
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            Response responseTransfert = when().get(location).then().statusCode(HttpStatus.SC_OK).extract().response();

//		Récupération du lien assembleur de la carte
            String jsonAsStringCard = responseTransfert.asString();
            JSONObject jsonCard = new JSONObject(jsonAsStringCard);
            JSONObject links = jsonCard.getJSONObject("_links");
            String selfUrl = links.getJSONObject("account_receiver").getString("href");
            URL url = new URL(selfUrl);
            when().get(url.getPath())
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract()
                    .response();
        }

        @Test
        public void assemblerCollectionTransfert() throws Exception{
            Card newCard1 = new Card();
            Account accountCustomer = new Account(
                    UUID.randomUUID().toString(),
                    "Tom",
                    "Sawyer",
                    Account.Country.FRANCE,
                    "64EB75640",
                    "+33675896767",
                    ToolBox.generateIBAN(),
                    newCard1.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountCustomer);
            cardResource.save(newCard1);

            Card newCard2 = new Card();
            Account accountShop = new Account(
                    UUID.randomUUID().toString(),
                    "Peter",
                    "MacCalloway",
                    Account.Country.GERMANY,
                    "77XX77777",
                    "+49675896767",
                    ToolBox.generateIBAN(),
                    newCard2.getIdcard(),
                    null,
                    null
            );
            accountResource.save(accountShop);
            cardResource.save(newCard2);

            TransfertInput transfertInput = new TransfertInput(
                    12.00,
                    accountCustomer.getId(),
                    accountShop.getId()
            );

            // Envoi des inputs et vérification du code HTTP201 CREATED
            Response response = given()
                    .body(ToolBox.toJsonString(transfertInput))
                    .contentType(ContentType.JSON)
                    .when()
                    .post("/transferts")
                    .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .extract()
                    .response();

            // Interrogation location vérification du code HTTP200 OK
            String location = response.getHeader("Location");
            Response responseTransfert = when().get(location).then().statusCode(HttpStatus.SC_OK).extract().response();

//		Récupération du lien assembleur de la carte
            String jsonAsStringCard = responseTransfert.asString();
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
