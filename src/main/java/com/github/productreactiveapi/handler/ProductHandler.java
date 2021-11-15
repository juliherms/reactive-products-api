package com.github.productreactiveapi.handler;

import com.github.productreactiveapi.model.Product;
import com.github.productreactiveapi.model.ProductEvent;
import com.github.productreactiveapi.repository.ProductRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * This class responsible to implements business logic for Product
 */
@Component
public class ProductHandler {

    private ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * This method responsible to list all products
     * @param request
     * @return
     */
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {

        Flux<Product> products = repository.findAll();

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(products, Product.class);
    }

    /**
     * This method responsible to get product by id
     * @param request
     * @return
     */
    public Mono<ServerResponse> getProduct(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> productMono = this.repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(product)))
                .switchIfEmpty(notFound);
    }

    /**
     * Method responsible to save product
     * @param request
     * @return
     */
    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono.flatMap(product ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(repository.save(product), Product.class));
    }

    /**
     * Method responsible to updateProduct
     * @param request
     * @return
     */
    public Mono<ServerResponse> updateProduct(ServerRequest request) {

        String id = request.pathVariable("id");
        Mono<Product> existingProductMono = this.repository.findById(id);

        Mono<Product> productMono = request.bodyToMono(Product.class);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono.zipWith(existingProductMono,
                (product, existingProduct) ->
                        new Product(existingProduct.getId(), product.getName(), product.getPrice())
        ).flatMap(product ->
                ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .body(repository.save(product), Product.class)
        ).switchIfEmpty(notFound);
    }

    /**
     * Method responsible to delete product
     * @param request
     * @return
     */
    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> productMono = this.repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(existingProduct ->
                        ServerResponse.ok()
                                .build(repository.delete(existingProduct))
                )
                .switchIfEmpty(notFound);
    }

    /**
     * method responsible to delete all products
     * @param request
     * @return
     */
    public Mono<ServerResponse> deleteAllProducts(ServerRequest request) {
        return ServerResponse.ok()
                .build(repository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest request) {
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1)).map(val ->
                new ProductEvent(val, "Product Event")
        );

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvent.class);
    }



}
