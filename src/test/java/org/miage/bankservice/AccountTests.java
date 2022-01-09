package org.miage.bankservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
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
import static org.junit.Assert.assertThrows;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountTests {

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
	public void pingAccounts() {
		when().get("/accounts").then().statusCode(HttpStatus.SC_OK);
	}

	@Test
	public void getNotFoundAccount() {
		when().get("/accounts/42").then().statusCode(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void createGetOneAccount() {
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
				null
			);
		accountResource.save(account);
		Response response = when().get("/accounts/"+account.getId())
								.then()
								.statusCode(HttpStatus.SC_OK)
								.extract()
								.response();
		String jsonAsString = response.asString();
		assertThat(jsonAsString,containsString("Tom"));
	}

	@Test
	public void createGetAllAccounts() {
		Card newCard1 = new Card();
		Account account1 = new Account(
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
		accountResource.save(account1);
		Card newCard2 = new Card();
		Account account2 = new Account(
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
		accountResource.save(account2);
		when().get("/accounts/")
								.then()
								.statusCode(HttpStatus.SC_OK)
								.and()
								.assertThat()
				.body("_embedded.accounts.size()",equalTo(2));
	}

	@Test
	public void apiPostAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675896767",
				null,
				null,
				null,
				null
		);

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

		// Interrogation location vérification du code HTTP200 OK
		String location = response.getHeader("Location");
		when().get(location).then().statusCode(HttpStatus.SC_OK);
	}

//	Un passeport ne peut être utilisé 2 fois
	@Test
	public void passportIsUniqueAccounts() throws Exception {
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

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

		AccountInput accountInput2 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675890000",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response response2 = given()
				.body(ToolBox.toJsonString(accountInput2))
				.contentType(ContentType.JSON)
				.when()
				.post("/accounts")
				.then()
				.statusCode(HttpStatus.SC_BAD_REQUEST)
				.extract()
				.response();
	}

//	Un téléphone ne peut être utilisé 2 fois
	@Test
	public void phonenumberIsUniqueAccounts() throws Exception {
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

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

		AccountInput accountInput2 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"77EB77777",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response response2 = given()
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
	public void apiPostBadNameTooLongAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"loremispumloremispumloremispumloremispum",
				"MacCalloway",
				Account.Country.FRANCE,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP401 BAD REQUEST
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
	public void apiPostBadNameTooShortAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"i",
				"MacCalloway",
				Account.Country.FRANCE,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP401 BAD REQUEST
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
	public void apiPostBadNameAlphanumericAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Nicolas123",
				"MacCalloway",
				Account.Country.FRANCE,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP401 BAD REQUEST
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
	public void apiPostBadNameBlankAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"",
				"MacCalloway",
				Account.Country.FRANCE,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP401 BAD REQUEST
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

//	 Utilisation d'un téléphone incorrect
	@Test
	public void apiPostBadPhoneAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.FRANCE,
				"64EB75640",
				"+496758zedzd96767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP401 BAD REQUEST
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

	//	 Utilisation d'un pays null
	@Test
	public void apiPostCountryNullAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				null,
				"64EB75640",
				"+496758zedzd96767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP401 BAD REQUEST
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

//	 Utiliser un pays non supporté (déclenche un IllegalArgumentException pour l'Enum Country)
	@Test()
	public void apiPostBadCountryAccount() {
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
					null
			);
		});
	}

//	L'API permet de modifier uniquement le téléphone, le prénom et le nom
	@Test
	public void apiPatchAccount() {
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
				null
			);
		accountResource.save(account);



		String JSON_DATA = ""
						+ "    {"
						+ "      \"name\": \"Jane\","
						+ "      \"surname\" : \"Doe\","
						+ "      \"phoneGlobal\" : \"+33678958812\""
						+ "}";

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response responsePatch = given()
				.body(JSON_DATA)
				.contentType(ContentType.JSON)
				.when()
				.patch("/accounts/"+account.getId())
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();

		Response responseGet = when().get("/accounts/"+account.getId())
								.then()
								.statusCode(HttpStatus.SC_OK)
								.extract()
								.response();
		String jsonAsString = responseGet.asString();
		assertThat(jsonAsString, CoreMatchers.allOf(
				containsString("Jane"),
				containsString("Doe"),
				containsString("33678958812")));
	}

//	L'API ne permet pas de modifier autre chose que le téléphone, le prénom et le nom
	@Test
	public void apiPatchProtectedFieldPassportAccount() {
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
				null
		);
		accountResource.save(account);

		String JSON_DATA = ""
				+ "    {"
				+ "      \"passportNumber\" : \"11YY44444\""
				+ "}";

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response responsePatch = given()
				.body(JSON_DATA)
				.contentType(ContentType.JSON)
				.when()
				.patch("/accounts/"+account.getId())
				.then()
				.statusCode(HttpStatus.SC_BAD_REQUEST)
				.extract()
				.response();
	}

	//	L'API ne permet pas de modifier autre chose que le téléphone, le prénom et le nom
	@Test
	public void apiPatchProtectedFieldIbanAccount() {
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
				null
		);
		accountResource.save(account);

		String JSON_DATA = ""
				+ "    {"
				+ "      \"IBAN\" : \"YQ45642788349968\""
				+ "}";

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response responsePatch = given()
				.body(JSON_DATA)
				.contentType(ContentType.JSON)
				.when()
				.patch("/accounts/"+account.getId())
				.then()
				.statusCode(HttpStatus.SC_BAD_REQUEST)
				.extract()
				.response();
	}

	@Test
	public void assemblerSelfAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response responsePost = given()
				.body(ToolBox.toJsonString(accountInput1))
				.contentType(ContentType.JSON)
				.when()
				.post("/accounts")
				.then()
				.statusCode(HttpStatus.SC_CREATED)
				.extract()
				.response();

		String location = responsePost.getHeader("Location");
		Response responseGet = when().get(location)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();

//		Récupération du lien assembleur de la carte du compte
		String jsonAsString = responseGet.asString();
		JSONObject json = new JSONObject(jsonAsString);
		JSONObject links = json.getJSONObject("_links");
		String selfUrl = links.getJSONObject("self").getString("href");
		URL url = new URL(selfUrl);
		when().get(url.getPath())
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();
	}

	@Test
	public void assemblerCardInAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response responsePost = given()
							.body(ToolBox.toJsonString(accountInput1))
							.contentType(ContentType.JSON)
							.when()
							.post("/accounts")
							.then()
							.statusCode(HttpStatus.SC_CREATED)
							.extract()
							.response();

		String location = responsePost.getHeader("Location");
		Response responseGet = when().get(location)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();

//		Récupération du lien assembleur de la carte du compte
		String jsonAsString = responseGet.asString();
		JSONObject json = new JSONObject(jsonAsString);
		JSONObject links = json.getJSONObject("_links");
		String cardurl = links.getJSONObject("card").getString("href");
		URL url = new URL(cardurl);
		when().get(url.getPath())
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();
	}

	@Test
	public void assemblerCollectionAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"64EB75640",
				"+49675896767",
				ToolBox.generateIBAN(),
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response responsePost = given()
				.body(ToolBox.toJsonString(accountInput1))
				.contentType(ContentType.JSON)
				.when()
				.post("/accounts")
				.then()
				.statusCode(HttpStatus.SC_CREATED)
				.extract()
				.response();

		String location = responsePost.getHeader("Location");
		Response responseGet = when().get(location)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();

//		Récupération du lien assembleur de la carte du compte
		String jsonAsString = responseGet.asString();
		JSONObject json = new JSONObject(jsonAsString);
		JSONObject links = json.getJSONObject("_links");
		String collectionUrl = links.getJSONObject("collection").getString("href");
		URL url = new URL(collectionUrl);
		when().get(url.getPath())
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.response();
	}

}
