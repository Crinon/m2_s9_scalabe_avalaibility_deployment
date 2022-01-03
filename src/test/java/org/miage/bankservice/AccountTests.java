package org.miage.bankservice;

import static io.restassured.RestAssured.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.bankservice.boundary.AccountResource;
import org.miage.bankservice.entity.Account;
import org.miage.bankservice.entity.AccountInput;
import org.miage.bankservice.entity.Card;
import org.miage.bankservice.entity.Transfert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.apache.http.HttpStatus;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.hamcrest.MatcherAssert.assertThat;
import static io.restassured.RestAssured.given;
import io.restassured.RestAssured;

import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountTests {

	@LocalServerPort
	int port;

	@Autowired
    AccountResource accountResource;

	private String toJsonString(Object o) throws Exception {
		ObjectMapper map = new ObjectMapper();
		return map.writeValueAsString(o);
	}

	@BeforeEach
	public void setupContext() {
		accountResource.deleteAll();
		RestAssured.port = port;
	}


	@Test
	public void apiPingAccounts() {
		when().get("/accounts").then().statusCode(HttpStatus.SC_OK);
	}

	@Test
	public void codeGetNotFoundAccount() {
		when().get("/accounts/42").then().statusCode(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void codeGetOneAccount() {
		Card newCard = new Card();
		Account account = new Account(
				UUID.randomUUID().toString(),
				"Tom",
				"Sawyer",
				Account.Country.FRANCE,
				"22AA55555",
				"+33675896767",
				"FR7630001007941234567890185",
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
	public void codeGetAllAccounts() {
		Card newCard1 = new Card();
		Account account1 = new Account(
				UUID.randomUUID().toString(),
				"Tom",
				"Sawyer",
				Account.Country.FRANCE,
				"22AA55555",
				"+33675896767",
				"FR7630001007941234567890185",
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
				"22AA55555",
				"+49675896767",
				"GE7630001007941234567890185",
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
								.body("size()",equalTo(2));
	}



	@Test
	public void apiPostAccount() throws Exception{
		AccountInput accountInput1 = new AccountInput(
				"Peter",
				"MacCalloway",
				Account.Country.GERMANY,
				"22AA55555",
				"+49675896767",
				"DE89 3704 0044 0532 0130 00",
				null,
				null,
				null
		);

		// Envoi des inputs et vérification du code HTTP201 CREATED
		Response response = given()
							.body(this.toJsonString(accountInput1))
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

//	@Test
//    public void deleteApi() throws Exception {
//        Account i1 = new Account(UUID.randomUUID().toString(), "Tom","Sawyer",Account.Country.FRANCE,"22AA55555","+33675896767", "FR7630001007941234567890185");
//        ir.save(i1);
//        when().delete("/accounts/" + i1.getId()).then().statusCode(HttpStatus.SC_NO_CONTENT);
//        when().get("/accounts/" + i1.getId()).then().statusCode(HttpStatus.SC_NOT_FOUND);
//    }










}
