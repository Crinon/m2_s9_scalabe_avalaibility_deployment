package org.miage.bankservice;

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
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OperationTests {
    @LocalServerPort
    int port;

    @Autowired
    AccountResource accountResource;
    @Autowired
    CardResource cardResource;
    @Autowired
    OperationResource operationResource;
    @Autowired
    OperationRepresentation operationRepresentation;

    @BeforeEach
    public void setupContext() {
        operationResource.deleteAll();
        cardResource.deleteAll();
        accountResource.deleteAll();
        RestAssured.port = port;
        String JSON_DATA = ""
                + "    {"
                + "      \"username\" : \"ADMIN\","
                + "      \"password\" : \"ADMIN\""
                + "}";
        given().body(JSON_DATA).contentType(ContentType.JSON).when().post("/login");

    }

    @Test
    public void pingAccounts() {
        when().get("/operations").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getNotFoundAccount() {
        when().get("/operations/42").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void apiGetOneOperation() throws Exception {
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Interrogation location vérification du code HTTP200 OK
        String location = response.getHeader("Location");
        when().get(location).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getAllOperations() throws Exception {
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        OperationInput operationInput2 = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
       given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        given()
                .body(ToolBox.toJsonString(operationInput2))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        when().get("/operations/")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .body("_embedded.operations.size()",equalTo(2));
    }


    @Test
    public void getAllOperationsFromAccount() throws Exception {
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        Card newCard3 = new Card();
        Account accountCustomer2 = new Account(
                UUID.randomUUID().toString(),
                "Francis",
                "Huster",
                Account.Country.GERMANY,
                "77UU77777",
                "+4960000767",
                ToolBox.generateIBAN(),
                newCard3.getIdcard(),
				null,
				null,"a"        );
        accountResource.save(accountCustomer2);
        cardResource.save(newCard3);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        OperationInput operationInput2 = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        OperationInput operationInput3 = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer2.getId(),
                accountShop.getId()
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        given()
                .body(ToolBox.toJsonString(operationInput2))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        given()
                .body(ToolBox.toJsonString(operationInput3))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();


        Response fouille =given()
                .when()
                .get("/operations")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        when().get("/operations/account/"+accountCustomer.getId())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .body("_embedded.operations.size()",equalTo(2));
        when().get("/operations/account/"+accountCustomer2.getId())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .body("_embedded.operations.size()",equalTo(1));
        when().get("/operations/account/"+accountShop.getId())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .body("_embedded.operations.size()",equalTo(3));
    }

    // Payer avec un taux différent de 1.0
    @Test
    public void createRatedOperation() throws Exception {
        AccountInput accountInputCustomer = new AccountInput(
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                "64FR75640",
                "+49675896767",
                null,
                null,
				null,
				null,"a"        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responseCustomer = given()
                .body(ToolBox.toJsonString(accountInputCustomer))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        String location = responseCustomer.getHeader("Location");
        Response responseGet = when().get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        //		Récupération de l'id du compte
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        String idCustomer = json.getString("id");



        AccountInput accountInputShop = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.UK,
                "64GB75640",
                "+49675896770",
                null,
				null,
				null,
				null,"a"        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response response = given()
                .body(ToolBox.toJsonString(accountInputShop))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        String location2 = response.getHeader("Location");
        Response responseGet2 = when().get(location2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        //		Récupération de l'id du compte
        String jsonAsString2 = responseGet2.asString();
        JSONObject json2 = new JSONObject(jsonAsString2);
        String idShop = json2.getString("id");

        OperationInput operationInput = new OperationInput(
                "My word",
                100.00,
                Operation.Category.DAILYLIFE,
                idCustomer,
                idShop
        );

        given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        List<Card> cards = cardResource.findAll();
        System.out.println(cards.get(0).getIdcard());
        // On vérifie que le client a été débité avec le taux
        assertEquals("180.0",cards.get(0).getCash());
        // On vérifie que le shop a reçu son amount normal
        assertEquals("400.0",cards.get(1).getCash());
    }


    // Refus de paiement si gpslocked et différents pays
    @Test
    public void patchRegionlockCard() throws Exception {
        AccountInput accountInputCustomer = new AccountInput(
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                "64FR75640",
                "+49675896767",
                null,
				null,
				null,
				null,"a"        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response responseCustomer = given()
                .body(ToolBox.toJsonString(accountInputCustomer))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        String location = responseCustomer.getHeader("Location");
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
                .patch("/cards/"+cardid)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();


        //		Récupération de l'id du compte
        String jsonAsString2 = responseGet.asString();
        JSONObject json2 = new JSONObject(jsonAsString2);
        String idCustomer = json2.getString("id");



        AccountInput accountInputShop = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.UK,
                "64GB75640",
                "+49675896770",
                null,
				null,
				null,
				null,"a"        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response response = given()
                .body(ToolBox.toJsonString(accountInputShop))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        String location2 = response.getHeader("Location");
        Response responseGet2 = when().get(location2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        //		Récupération de l'id du compte
        String jsonAsString3 = responseGet2.asString();
        JSONObject json3 = new JSONObject(jsonAsString3);
        String idShop = json3.getString("id");

        OperationInput operationInput = new OperationInput(
                "My word",
                100.00,
                Operation.Category.DAILYLIFE,
                idCustomer,
                idShop
        );

        given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();

    }

    @Test
    public void badShopid() throws Exception {
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
				null,"a"        );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                "blabla"
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void badCustomerid() throws Exception {
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                "blabla",
                accountShop.getId()
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    // Refus de paiement si sliding limit dépassé
    @Test
    public void exceededSlidinglimit() throws Exception {
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                11000.00,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
            }

    // Shop : payer avec l'id du client de la carte directement
    @Test
    public void serviceShopclientid() throws Exception {
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        String urlShopService = "operations/shopservice/shopid/"+accountShop.getId()+"/customerid/"+accountCustomer.getId()+"/amount/"+200;


        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(urlShopService)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
    }

    @Test
    // Shop : payer avec le numéro de la carte directement avec un taux de 1.0
    public void serviceShopclientCardnumber() throws Exception {
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        String urlShopService = "operations/shopservice/shopid/"+accountShop.getId()+"/customercardnumber/"+newCard1.getNumber()+"/amount/"+200;

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(urlShopService)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
    }


    @Test
    // Shop : payer avec le numéro de la carte directement avec un taux de 1.0
    public void serviceShopBadInputClientCardnumber() throws Exception {
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        String urlShopService = "operations/shopservice/shopid/"+accountShop.getId()+"/customercardnumber/fakecardnumber/amount/"+200;

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(urlShopService)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void assemblerSelfOperation() throws Exception{
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        String location = response.getHeader("Location");

        Response responseOperation = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

//		Récupération du lien assembleur de la carte
        String jsonAsStringCard = responseOperation.asString();
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
    public void assemblerAccountCustomerOperation() throws Exception{
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        String location = response.getHeader("Location");

        Response responseOperation = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

//		Récupération du lien assembleur de la carte
        String jsonAsStringCard = responseOperation.asString();
        JSONObject jsonCard = new JSONObject(jsonAsStringCard);
        JSONObject links = jsonCard.getJSONObject("_links");
        String selfUrl = links.getJSONObject("account_customer").getString("href");
        URL url = new URL(selfUrl);
        when().get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerAccountShopOperation() throws Exception{
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        String location = response.getHeader("Location");

        Response responseOperation = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

//		Récupération du lien assembleur de la carte
        String jsonAsStringCard = responseOperation.asString();
        JSONObject jsonCard = new JSONObject(jsonAsStringCard);
        JSONObject links = jsonCard.getJSONObject("_links");
        String selfUrl = links.getJSONObject("account_shop").getString("href");
        URL url = new URL(selfUrl);
        when().get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerCollectionOperation() throws Exception{
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
				null,"a"        );
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
				null,"a"        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        String location = response.getHeader("Location");

        Response responseOperation = given()
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

//		Récupération du lien assembleur de la carte
        String jsonAsStringCard = responseOperation.asString();
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
