package com.github.productreactiveapi;

import com.github.productreactiveapi.handler.ProductHandler;
import com.github.productreactiveapi.model.Product;
import com.github.productreactiveapi.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ReactiveapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveapiApplication.class, args);
	}

	@Bean
	CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository repository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null, "Big Latte", 2.99),
					new Product(null, "Big Decaf", 2.49),
					new Product(null, "Green Tea", 1.99))
					.flatMap(repository::save);

			productFlux
					.thenMany(repository.findAll())
					.subscribe(System.out::println);
		};
	}

	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route()
				.path("/products",
						builder -> builder
								.nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
										nestedBuilder -> nestedBuilder
												.GET("/events", handler::getProductEvents)
												.GET("/{id}", handler::getProduct)
												.GET(handler::getAllProducts)
												.PUT("/{id}", handler::updateProduct)
												.POST(handler::saveProduct)
								)
								.DELETE("/{id}", handler::deleteProduct)
								.DELETE(handler::deleteAllProducts)
				).build();
	}
}
