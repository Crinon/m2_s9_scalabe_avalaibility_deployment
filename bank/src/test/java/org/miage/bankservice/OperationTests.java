package org.miage.bankservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;
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
        operationResource.deleteAll();
        cardResource.deleteAll();
        accountResource.deleteAll();
        RestAssured.port = port;
    }

    @Test
    public void pingOperations() {
        when().get("/operations").then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void getOneOperation() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(password)
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
				null,
                passwordEncoder.encode("noneedauth")
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        // Création de l'opération et vérification du code HTTP201 CREATED
        Response response = given()
                .auth().oauth2(token)
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
        given()
                .auth().oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void rejectGetOneOperationAnotherAccount() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
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
                null,
                passwordEncoder.encode("noneedauth")
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        // Credentials du compte à créer
        String passportNumberStranger = "64YU75640";
        // Création d'un compte et de sa carte
        Card newCardStranger = new Card();
        Account accountStranger = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumberStranger,
                "+33675896700",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(accountStranger);
        cardResource.save(newCardStranger);

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);
        // Authentification avec le compte pour tentative d'usurpation
        String tokenStranger = getUserToken(passportNumberStranger, password);

        // Création de l'opération et vérification du code HTTP201 CREATED
        Response response = given()
                .auth().oauth2(token)
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
        given()
                .auth().oauth2(tokenStranger)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void postRejectAnotherAccountid() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        String passportNumber2 = "64XX75640";
        String passportNumber3 = "64FF75640";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);
        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber2,
                "+33675896711",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);
        Card newCard3 = new Card();
        Account accountCustomer2 = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber3,
                "+33675896700",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(accountCustomer2);
        cardResource.save(newCard3);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer2.getId(),
                accountShop.getId()
        );

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        // Création de l'opération et vérification du code HTTP400 BAD REQUEST
        given()
                .auth().oauth2(token)
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
    public void getAllOperations() throws Exception {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(password)
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
				null,
                passwordEncoder.encode("noneedauth")
        );
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

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        // Envoi des inputs et vérification du code HTTP201 CREATED
       given()
                .auth().oauth2(token)
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        given()
                .auth().oauth2(token)
                .body(ToolBox.toJsonString(operationInput2))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        given()
                .auth().oauth2(token)
                .get("/operations")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .body("_embedded.operations.size()",equalTo(2));
    }

    @Test
    public void getNotFoundAccount() throws JSONException {
        // Credentials du compte à créer
        String passportNumber = "64EB75640";
        String password = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        // Authentification avec le compte créé
        String token = getUserToken(passportNumber, password);

        given().auth().oauth2(token).get("/operations/42").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    // Payer avec un taux différent de 1.0
    @Test
    public void createRatedOperation() throws Exception {
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        AccountInput accountInputCustomer = new AccountInput(
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+49675896767",
                null,
                null,
				null,
				null,
                customerPassword
        );
        Response responseCustomer = given()
                .body(ToolBox.toJsonString(accountInputCustomer))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String customerToken = getUserToken(customerPassportNumber, customerPassword);

        String location = responseCustomer.getHeader("Location");
        Response responseGet = given()
                .auth().oauth2(customerToken)
                .get(location)
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
                shopPassportNumber,
                "+49675896770",
                null,
				null,
				null,
				null,
                shopPassword
        );
        Response response = given()
                .body(ToolBox.toJsonString(accountInputShop))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        String location2 = response.getHeader("Location");
        Response responseGet2 = given()
                .auth().oauth2(shopToken)
                .get(location2)
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
                .auth().oauth2(shopToken)
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();
        List<Card> cards = cardResource.findAll();
        // On vérifie que le client a été débité avec le taux
        assertEquals("180.0",cards.get(0).getCash());
        // On vérifie que le shop a reçu son amount normal
        assertEquals("400.0",cards.get(1).getCash());
    }

    // Refus de paiement si gpslocked et différents pays
    @Test
    public void rejectWithBlockedCard() throws Exception {
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        AccountInput accountInputCustomer = new AccountInput(
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+49675896767",
                null,
                null,
                null,
                null,
                customerPassword
        );
        Response responseCustomer = given()
                .body(ToolBox.toJsonString(accountInputCustomer))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String customerToken = getUserToken(customerPassportNumber, customerPassword);

        String location = responseCustomer.getHeader("Location");

        Response responseGet = given()
                .auth().oauth2(customerToken)
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
                .auth().oauth2(customerToken)
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
                shopPassportNumber,
                "+49675896770",
                null,
                null,
                null,
                null,
                shopPassword
        );

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

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        String location2 = response.getHeader("Location");

        Response responseGet2 = given()
                .auth().oauth2(shopToken)
                .get(location2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // Récupération de l'id du compte
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
                .auth().oauth2(shopToken)
                .body(ToolBox.toJsonString(operationInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/operations")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    // Refus de paiement si gpslocked et différents pays
    @Test
    public void rejectWithRegionlockedCard() throws Exception {
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        AccountInput accountInputCustomer = new AccountInput(
                "Gérard",
                "Bouchard",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+49675896767",
                null,
				null,
				null,
				null,
                customerPassword
        );
        Response responseCustomer = given()
                .body(ToolBox.toJsonString(accountInputCustomer))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Authentification avec le compte créé
        String customerToken = getUserToken(customerPassportNumber, customerPassword);

        String location = responseCustomer.getHeader("Location");

        Response responseGet = given()
                .auth().oauth2(customerToken)
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

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(customerToken)
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
                shopPassportNumber,
                "+49675896770",
                null,
				null,
				null,
				null,
                shopPassword
        );

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

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        String location2 = response.getHeader("Location");

        Response responseGet2 = given()
                .auth().oauth2(shopToken)
                .get(location2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // Récupération de l'id du compte
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
                .auth().oauth2(shopToken)
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
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword)
        );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        // Authentification avec le compte créé
        String customerToken = getUserToken(customerPassportNumber, customerPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                "inexistantaccount"
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(customerToken)
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
        // Credentials du compte à créer
        String shopPassportNumber = "64EB75640";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
				null,
				null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                "inexistantaccount",
                accountShop.getId()
        );

        // Envoi des inputs et vérification du code HTTP201 CREATED
        given()
                .auth().oauth2(shopToken)
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
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword)
        );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
				null,
				null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                11000.00,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        given()
                .auth().oauth2(shopToken)
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
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";

        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword)
        );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        String urlShopService = "operations/shopservice/shopid/"+accountShop.getId()+"/customerid/"+accountCustomer.getId()+"/amount/"+200;


        given()
                .auth().oauth2(shopToken)
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
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Credentials du compte à créer
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword)
                );
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
				null,
				null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        String urlShopService = "operations/shopservice/shopid/"+accountShop.getId()+"/customercardnumber/"+newCard1.getNumber()+"/amount/"+200;

        given()
                .auth().oauth2(shopToken)
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
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                customerPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(customerPassportNumber, customerPassword);

        String urlShopService = "operations/shopservice/shopid/"+accountShop.getId()+"/customercardnumber/fakecardnumber/amount/"+200;

        given()
                .auth().oauth2(shopToken)
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
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(customerPassword));
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
                null,
                null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .auth().oauth2(shopToken)
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
                .auth().oauth2(shopToken)
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
        given()
                .auth().oauth2(shopToken)
                .get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerAccountCustomerOperation() throws Exception{
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
				null,
				null,
                passwordEncoder.encode(customerPassword));
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        // Authentification avec le compte créé
        String customerToken = getUserToken(customerPassportNumber, customerPassword);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
				null,
				null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .auth().oauth2(shopToken)
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
                .auth().oauth2(shopToken)
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
        given()
                .auth().oauth2(customerToken)
                .get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerAccountShopOperation() throws Exception{
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(customerPassword));
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
                null,
                null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .auth().oauth2(shopToken)
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
                .auth().oauth2(shopToken)
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
        given()
                .auth().oauth2(shopToken)
                .get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

    @Test
    public void assemblerCollectionOperation() throws Exception{
        // Credentials du compte à créer
        String customerPassportNumber = "64EB75640";
        String customerPassword = "a";
        // Credentials du compte à créer
        String shopPassportNumber = "11JJ75000";
        String shopPassword = "a";
        // Création d'un compte et de sa carte
        Card newCard1 = new Card();
        Account accountCustomer = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                customerPassportNumber,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard1.getIdcard(),
                null,
                null,
                passwordEncoder.encode(customerPassword));
        accountResource.save(accountCustomer);
        cardResource.save(newCard1);

        Card newCard2 = new Card();
        Account accountShop = new Account(
                UUID.randomUUID().toString(),
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                shopPassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                newCard2.getIdcard(),
                null,
                null,
                passwordEncoder.encode(shopPassword)
        );
        accountResource.save(accountShop);
        cardResource.save(newCard2);

        // Authentification avec le compte créé
        String shopToken = getUserToken(shopPassportNumber, shopPassword);

        OperationInput operationInput = new OperationInput(
                "My word",
                12.01,
                Operation.Category.DAILYLIFE,
                accountCustomer.getId(),
                accountShop.getId()
        );

        Response response = given()
                .auth().oauth2(shopToken)
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
                .auth().oauth2(shopToken)
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
        given()
                .auth().oauth2(shopToken)
                .get(url.getPath())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
    }

}
