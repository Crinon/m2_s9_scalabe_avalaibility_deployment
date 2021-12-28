package org.miage.bankservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;

@SpringBootApplication
public class MyownrevolutApplication {

	public static void main(String[] args) {
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		System.out.println("ufhriufhre");
		SpringApplication.run(MyownrevolutApplication.class, args);
	}

	@Bean
	public OpenAPI bankAPI() {
		return new OpenAPI().info(new Info()
			.title("Bank API")
			.version("1.0")
			.description("Documentation API Bank 1.0"));
	}

	// Affichage des ids
	@Bean
	public RepositoryRestConfigurer repositoryRestConfigurer(EntityManager entityManager) {
		return RepositoryRestConfigurer.withConfig(config -> {
			config.exposeIdsFor(entityManager.getMetamodel().getEntities()
					.stream().map(Type::getJavaType).toArray(Class[]::new));
		});
	}
}
