package org.miage.intervenantservice;

import static io.restassured.RestAssured.when;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.miage.bankservice.boundary.AccountResource;
import org.miage.bankservice.entity.Account;
import org.miage.bankservice.entity.AccountInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.apache.http.HttpStatus;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static io.restassured.RestAssured.given;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountServiceApplicationTests {

	@LocalServerPort
	int port;

    AccountResource ir;

	@BeforeEach
	public void setupContext() {
		ir.deleteAll();
		RestAssured.port = port;
	}

	@Test
	public void pingApi() {
		when().get("/accounts").then().statusCode(HttpStatus.SC_OK);
	}

//	@Test
//	public void getOne() {
//		Account i1 = new Account(UUID.randomUUID().toString(),"Tom","Sawyer",Account.Country.FRANCE,"22AA55555","+33675896767", "FR7630001007941234567890185");
//		ir.save(i1);
//		Response response = when().get("/accounts/"+i1.getId())
//								.then()
//								.statusCode(HttpStatus.SC_OK)
//								.extract()
//								.response();
//		String jsonAsString = response.asString();
//		assertThat(jsonAsString,containsString("Tom"));
//	}








//	@Test
//	public void getAll() {
//		Account i1 = new Account(UUID.randomUUID().toString(),"Tom","Sawyer",Account.Country.FRANCE,"22AA55555","+33675896767", "FR7630001007941234567890185");
//		ir.save(i1);
//		Account i2 = new Account(UUID.randomUUID().toString(),"Leblanc","Roberto",Account.Country.FRANCE,"11AA55555","+33675896764", "FR7641199110541234567890180");
//		ir.save(i2);
//		when().get("/accounts/")
//								.then()
//								.statusCode(HttpStatus.SC_OK)
//								.and()
//								.assertThat()
//								.body("size()",equalTo(2));
//	}

	@Test
	public void getNotFound() {
		when().get("/accounts/42").then().statusCode(HttpStatus.SC_NOT_FOUND);
	}

//	@Test
//	public void postApi() throws Exception{
//		AccountInput i1 = new AccountInput("Tom","Sawyer",Account.Country.FRANCE,"22AA55555","+33675896767");
//		Response response = given()
//							.body(this.toJsonString(i1))
//							.contentType(ContentType.JSON)
//							.when()
//							.post("/accounts")
//							.then()
//							.statusCode(HttpStatus.SC_CREATED)
//							.extract()
//							.response();
//		String location = response.getHeader("Location");
//		when().get(location).then().statusCode(HttpStatus.SC_OK);
//	}

//	@Test
//    public void deleteApi() throws Exception {
//        Account i1 = new Account(UUID.randomUUID().toString(), "Tom","Sawyer",Account.Country.FRANCE,"22AA55555","+33675896767", "FR7630001007941234567890185");
//        ir.save(i1);
//        when().delete("/accounts/" + i1.getId()).then().statusCode(HttpStatus.SC_NO_CONTENT);
//        when().get("/accounts/" + i1.getId()).then().statusCode(HttpStatus.SC_NOT_FOUND);
//    }

	private String toJsonString(Object o) throws Exception {
		ObjectMapper map = new ObjectMapper();
		return map.writeValueAsString(o);
	}








}
