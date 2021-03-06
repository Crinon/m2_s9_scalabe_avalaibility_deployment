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
		SpringApplication.run(MyownrevolutApplication.class, args);
	}

	// OpenAPI descriptions will be available at the path /v3/api-docs by default
	// http://localhost:8082/v3/api-docs/
	// http://localhost:8082/swagger-ui/index.html
	// puis  /v3/api-docs
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
