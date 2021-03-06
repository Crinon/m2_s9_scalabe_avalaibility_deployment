package org.miage.bankservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
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
class AccountTests {

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
    public void pingNotAuth() {
        when().get("/accounts").then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "44KK22222", roles = "USER")
    public void pingWithMockAuth() {
        AccountInput accountInput = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.FRANCE,
                "44KK22222",
                "+49675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                "a"
        );
        accountRepresentation.saveAccount(accountInput);
        ResponseEntity<?> responseEntityGet = accountRepresentation.getConnectedAccount(); // path "/accounts"
        Assertions.assertEquals(HttpStatus.SC_OK, responseEntityGet.getStatusCodeValue());
    }

    @Test
    public void patchNotFoundAccount() throws JSONException {
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";

        // Cr??ation du compte
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

        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);
        // V??rification de la r??ponse de la route
        String JSON_DATA = ""
                + "    {"
                + "      \"phoneGlobal\" : \"+33678442566\""
                + "}";

        // Patch avec un faux id et v??rification du code HTTP404 NOT FOUND
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/accounts/42")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .extract()
                .response();
    }

    @Test
    public void getAccountByConnectedId() throws Exception {
        // Valeurs a retrouver dans le JSON du get /accounts
        String surnameToFind = "Peter";
        String nameToFind = "MacCalloway";
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";
        // Cr??ation du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                surnameToFind,
                nameToFind,
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
        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);
        // V??rification de la r??ponse de la route
        Response responseGet = given()
                .auth().oauth2(token)
                .get("/accounts/"+account.getId())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = responseGet.asString();
        assertThat(jsonAsString, containsString(nameToFind));
        assertThat(jsonAsString, containsString(surnameToFind));
    }

    @Test
    public void getAccountByAnotherId() throws Exception {
        // Credentials du compte ?? cr??er
        String passport = "64EB75640";
        String password = "a";

        // Cr??ation du compte propri??taire
        Account myaccount = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passport,
                "+33675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(myaccount);
        // Cr??ation du compte ??tranger ?? patcher
        Account notMyAccount = new Account(
                UUID.randomUUID().toString(),
                "Richard",
                "Lebeau",
                Account.Country.FRANCE,
                "55GG66666",
                "+33670006767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                passwordEncoder.encode("dontcare")
        );
        accountResource.save(notMyAccount);

        // Authentification avec le compte cr????
        String token = getUserToken(passport, password);

        // V??rification de la r??ponse BAD_REQUEST de la route avec accountid
        given()
                .auth().oauth2(token)
                .get("/accounts/"+notMyAccount.getId())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void getConnectedAccount() throws Exception {
        // Valeurs a retrouver dans le JSON du get /accounts
        String surnameToFind = "Peter";
        String nameToFind = "MacCalloway";
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";
        // Cr??ation du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                surnameToFind,
                nameToFind,
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
        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);
        // V??rification de la r??ponse de la route
        Response responseGet = given()
                .auth().oauth2(token)
                .get("/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();
        String jsonAsString = responseGet.asString();
        assertThat(jsonAsString, containsString(nameToFind));
        assertThat(jsonAsString, containsString(surnameToFind));
    }

    // V??rification de la conformit?? du lien "location" pr??sent dans un header de SC_CREATED
    @Test
    public void getLocationAccount() throws Exception {
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";

        // Compte ?? cr??er
        Card newCard = new Card();
        AccountInput accountInput = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                newCard,
                null,
                null,
                password
        );
        // Cr??ation du compte
        Response response = given()
                .body(ToolBox.toJsonString(accountInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // R??cup??ration du location du header retourn??
        String location = response.getHeader("Location");

        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);

        // V??rification du location
        given()
                .auth()
                .oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    //	Un passeport ne peut ??tre utilis?? 2 fois
    @Test
    public void passportIsUnique() throws Exception {
        // Num??ro de passeport que l'on essaie d'utiliser deux fois
        String samePassportNumber = "64EB75640";

        // Premier compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                samePassportNumber,
                "+49675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                "a"
        );
        // Le premier compte doit ??tre SC_CREATED car le passeport n'est pas encore utilis??
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Deuxi??me compte
        AccountInput accountInput2 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                samePassportNumber,
                "+49675890000",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                "a"
        );
        // Le deuxi??me compte doit ??tre SC_BAD_REQUEST
        // car le passeport ne peut pas ??tre utilis?? une deuxi??me fois
        given()
                .body(ToolBox.toJsonString(accountInput2))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    //	Un t??l??phone ne peut ??tre utilis?? 2 fois
    @Test
    public void phonenumberIsUnique() throws Exception {
        // Num??ro de t??l??phone que l'on essaie d'utiliser deux fois
        String samePhoneNumber = "+49675896767";

        // Premier compte
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "64EB75640",
                samePhoneNumber,
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                "a"
        );
        // Le premier compte doit ??tre SC_CREATED car le t??l??phone n'est pas encore utilis??
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // Deuxi??me compte
        AccountInput accountInput2 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                "77EB77777",
                samePhoneNumber,
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                "a"
        );

        // Le deuxi??me compte doit ??tre SC_BAD_REQUEST
        // car le passeport ne peut pas ??tre utilis?? une deuxi??me fois
        given()
                .body(ToolBox.toJsonString(accountInput2))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostBadNameTooLong() throws Exception {
        // Compte ?? cr??er avec un nom trop long
        AccountInput accountInput1 = new AccountInput(
                "loremispumloremispumloremispumloremispum",
                "MacCalloway",
                Account.Country.FRANCE,
                "64EB75640",
                "+49675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null, "a"
        );

        // V??rification du code HTTP401 BAD REQUEST
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostBadNameTooShortAccount() throws Exception {
        // Compte ?? cr??er avec un nom trop court
        AccountInput accountInput1 = new AccountInput(
                "i",
                "MacCalloway",
                Account.Country.FRANCE,
                "64EB75640",
                "+49675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                "a"
        );

        // V??rification du code HTTP401 BAD REQUEST
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostBadNameAlphanumericAccount() throws Exception {
        // Compte ?? cr??er avec un nom contenant des chiffres
        AccountInput accountInput1 = new AccountInput(
                "Nicolas123",
                "MacCalloway",
                Account.Country.FRANCE,
                "64EB75640",
                "+49675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null, "a"
        );

        // V??rification du code HTTP401 BAD REQUEST
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostBadNameBlankAccount() throws Exception {
        // Compte ?? cr??er avec un nom vide
        AccountInput accountInput1 = new AccountInput(
                "",
                "MacCalloway",
                Account.Country.FRANCE,
                "64EB75640",
                "+49675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null, "a"
        );

        // EV??rification du code HTTP401 BAD REQUEST
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostBadPhoneAccount() throws Exception {
        // Compte ?? cr??er avec un num??ro de t??l??phone incorrect
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.FRANCE,
                "64EB75640",
                "+496758zedzd96767",
                ToolBox.generateIBAN(),
                null,
                null,
                null, "a"
        );

        // Envoi des inputs et v??rification du code HTTP401 BAD REQUEST
        Response response = given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostCountryNullAccount() throws Exception {
        // Compte ?? cr??er sans pays
        AccountInput accountInput1 = new AccountInput(
                "Peter",
                "MacCalloway",
                null,
                "64EB75640",
                "+496758zedzd96767",
                ToolBox.generateIBAN(),
                null,
                null,
                null, "a"
        );

        // V??rification du code HTTP401 BAD REQUEST
        given()
                .body(ToolBox.toJsonString(accountInput1))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void apiPostBadCountryAccount() {
        //	 Utiliser un pays non support?? (d??clenche un IllegalArgumentException pour l'Enum Country)
        assertThrows(IllegalArgumentException.class, () -> {
            AccountInput accountInput1 = new AccountInput(
                    "Peter",
                    "MacCalloway",
                    Account.Country.valueOf("UNKNOWN"),
                    "64EB75640",
                    "+496758zedzd96767",
                    ToolBox.generateIBAN(),
                    null,
                    null,
                    null,
                    "a"
            );
        });
    }

    //	L'API permet de modifier uniquement le t??l??phone, le pr??nom et le nom
    @Test
    public void apiPatchableFields() throws JSONException {
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";
        // Valeurs de base
        String nameBefore = "Tom";
        String surnameBefore = "Sawyer";
        String phoneBefore = "+33675896767";
        // Valeurs du patch
        String nameAfter = "Jane";
        String surnameAfter = "Doe";
        String phoneAfter = "+33678958812";

        // Cr??ation du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                nameBefore,
                surnameBefore,
                Account.Country.FRANCE,
                passportNumber,
                phoneBefore,
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(account);

        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);
        // V??rification de la r??ponse de la route
        String JSON_DATA = ""
                + "    {"
                + "      \"name\": \"" + nameAfter + "\","
                + "      \"surname\" : \"" + surnameAfter + "\","
                + "      \"phoneGlobal\" : \"" + phoneAfter + "\""
                + "}";

        // Patch et v??rification du code HTTP201 OK
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/accounts/" + account.getId())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // R??cup??ration du account patch??
        Response responseGet = given()
                .auth().oauth2(token)
                .get("/accounts/" + account.getId())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // V??rification des donn??es patch??es
        String jsonAsString = responseGet.asString();
        assertThat(jsonAsString, CoreMatchers.allOf(
                containsString(nameAfter),
                containsString(surnameAfter),
                containsString(phoneAfter)));
    }

    //	L'API ne permet pas de modifier autre chose que le t??l??phone, le pr??nom et le nom
    @Test
    public void patchProtectedFieldPassportAccount() throws JSONException {
        // Credentials du compte ?? cr??er
        String passportBefore = "64EB75640";
        String password = "a";
        // Valeur ?? modifier
        String passportAfter = "44TT75000";

        // Cr??ation du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passportBefore,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)

        );
        accountResource.save(account);

        // Authentification avec le compte cr????
        String token = getUserToken(passportBefore, password);

        // Donn??es du patch sur un champ prot??g??
        String JSON_DATA = ""
                + "    {"
                + "      \"passportNumber\" : \"" + passportAfter + "\""
                + "}";

        // V??rification du code HTTP400 BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/accounts/" + account.getId())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    //	L'API ne permet pas de modifier autre chose que le t??l??phone, le pr??nom et le nom
    @Test
    public void patchProtectedFieldIbanAccount() throws JSONException {
        // Credentials du compte ?? cr??er
        String passport = "64EB75640";
        String password = "a";
        // Valeur ?? modifier
        String IBAN = "44TT75000";

        // Cr??ation du compte
        Card newCard = new Card();
        Account account = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passport,
                "+33675896767",
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(account);

        // Authentification avec le compte cr????
        String token = getUserToken(passport, password);

        // Donn??es du patch sur un champ prot??g??
        String JSON_DATA = ""
                + "    {"
                + "      \"IBAN\" : \"" + IBAN + "\""
                + "}";

        // V??rification du code HTTP400 BAD_REQUEST
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/accounts/" + account.getId())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    //	L'API ne permet pas de modifier autre chose que le t??l??phone, le pr??nom et le nom
    @Test
    public void patchAnotherAccount() throws JSONException {
        // Credentials du compte ?? cr??er
        String passport = "64EB75640";
        String password = "a";
        // Valeur ?? modifier
        String newName = "Bernard";

        // Cr??ation du compte propri??taire
        Account myaccount = new Account(
                UUID.randomUUID().toString(),
                "Tom",
                "Sawyer",
                Account.Country.FRANCE,
                passport,
                "+33675896767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                passwordEncoder.encode(password)
        );
        accountResource.save(myaccount);
        // Cr??ation du compte ??tranger ?? patcher
        Account notMyAccount = new Account(
                UUID.randomUUID().toString(),
                "Richard",
                "Lebeau",
                Account.Country.FRANCE,
                "55GG66666",
                "+33670006767",
                ToolBox.generateIBAN(),
                null,
                null,
                null,
                passwordEncoder.encode("dontcare")
        );
        accountResource.save(notMyAccount);


        // Authentification avec le compte cr????
        String token = getUserToken(passport, password);

        // Donn??es du patch
        String JSON_DATA = ""
                + "    {"
                + "      \"name\" : \"" + newName + "\""
                + "}";

        // Tentative de patcher un compte diff??rent de celui connect??
        given()
                .auth().oauth2(token)
                .body(JSON_DATA)
                .contentType(ContentType.JSON)
                .when()
                .patch("/accounts/" + notMyAccount.getId())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
    }

    @Test
    public void assemblerSelfAccount() throws Exception {
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";

        // Compte ?? cr??er
        Card newCard = new Card();
        AccountInput accountInput = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                newCard,
                null,
                null,
                password
        );
        // Cr??ation du compte
        Response response = given()
                .body(ToolBox.toJsonString(accountInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // R??cup??ration du location du header retourn??
        String location = response.getHeader("Location");

        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);

        // V??rification du location
        Response responseGet = given()
                .auth()
                .oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // R??cup??ration du lien assembleur self
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        JSONObject links = json.getJSONObject("_links");
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
    @WithMockUser(username = "44KK22222", roles = "USER")
    public void assemblerCardInAccount() throws Exception {
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";

        // Compte ?? cr??er
        Card newCard = new Card();
        AccountInput accountInput = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                newCard,
                null,
                null,
                password
        );
        // Cr??ation du compte
        Response response = given()
                .body(ToolBox.toJsonString(accountInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // R??cup??ration du location du header retourn??
        String location = response.getHeader("Location");

        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);

        // V??rification du location
        Response responseGet = given()
                .auth()
                .oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // R??cup??ration du lien assembleur pointant sur la carte du compte
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        JSONObject links = json.getJSONObject("_links");
        String selfUrl = links.getJSONObject("card").getString("href");
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
        // Credentials du compte ?? cr??er
        String passportNumber = "64EB75640";
        String password = "a";

        // Compte ?? cr??er
        Card newCard = new Card();
        AccountInput accountInput = new AccountInput(
                "Peter",
                "MacCalloway",
                Account.Country.GERMANY,
                passportNumber,
                "+49675896767",
                null,
                newCard,
                null,
                null,
                password
        );
        // Cr??ation du compte
        Response response = given()
                .body(ToolBox.toJsonString(accountInput))
                .contentType(ContentType.JSON)
                .when()
                .post("/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        // R??cup??ration du location du header retourn??
        String location = response.getHeader("Location");

        // Authentification avec le compte cr????
        String token = getUserToken(passportNumber, password);

        // V??rification du location
        Response responseGet = given()
                .auth()
                .oauth2(token)
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        // R??cup??ration du lien assembleur collection
        String jsonAsString = responseGet.asString();
        JSONObject json = new JSONObject(jsonAsString);
        JSONObject links = json.getJSONObject("_links");
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
