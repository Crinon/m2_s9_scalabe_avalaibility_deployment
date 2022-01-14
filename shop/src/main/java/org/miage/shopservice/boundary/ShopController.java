package org.miage.shopservice.boundary;

import org.miage.shopservice.entity.ShopBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
public class ShopController {

	RestTemplate template;

	@Value("${app.url.servicebanque}")
	private String baseurl;

	public ShopController (RestTemplate rt) {
		this.template = rt;
	}

	@GetMapping("/shop/do-operation/shopid/{shopid}/customerid/{customerid}/amount/{price}")
	public ResponseEntity<?> paymentByCustomerid(@PathVariable("shopid") String shopid,
										 @PathVariable("customerid") String customerid,
										 @PathVariable Double price,
										 @RequestHeader("Authorization") String bearerToken
	) {
		// URL avec les paramètres de l'opération
		String url = this.baseurl+"/operations/shopservice/shopid/"
				+ shopid + "/customerid/"
				+ customerid + "/amount/"
				+ price;
		// Transfert du token
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", bearerToken);


		try {
			// Requête pour créer l'opération
			return template.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers), ShopBean.class);
		} catch (final HttpStatusCodeException e) {
			// Si le user a fait un requête avec une mauvaise identification
			if(e.getStatusCode() == HttpStatus.UNAUTHORIZED){
				String errorMessage = "{\"message\":\"Request not processed, reason is : incorrect token provided\"}";
				return ResponseEntity.badRequest().body(errorMessage);
			}
			// En cas d'erreur différent de 401 UNAUTHORIZE
			return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}
	}


	@GetMapping("/shop/do-operation/shopid/{shopid}/customercardnumber/{customercardnumber}/amount/{price}")
	public ResponseEntity<?> paymentByCustomerCardNumber(@PathVariable("shopid") String shopid,
										 @PathVariable("customercardnumber") String customercardnumber,
										 @PathVariable Double price,
										 @RequestHeader("Authorization") String bearerToken
	) {

		// URL avec les paramètres de l'opération
		String url = this.baseurl+"/operations/shopservice" +
				"/shopid/" + shopid +
				"/customercardnumber/" + customercardnumber +
				"/amount/" + price;
		// Transfert du token
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Authorization", bearerToken);

		try {
			// Requête pour créer l'opération
			return template.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers), ShopBean.class);
		} catch (final HttpStatusCodeException e) {
			// Si le user a fait un requête avec une mauvaise identification
			if(e.getStatusCode() == HttpStatus.UNAUTHORIZED){
				String errorMessage = "{\"message\":\"Request not processed, reason is : incorrect token provided\"}";
				return ResponseEntity.badRequest().body(errorMessage);
			}
			// En cas d'erreur différent de 401 UNAUTHORIZE
			return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}
	}

}