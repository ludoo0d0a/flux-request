package com.example.fluxrequest;

import com.example.fluxrequest.model.User;
import com.example.fluxrequest.model.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@SpringBootTest
@Slf4j
class FluxRequestApplicationTests {

    public static final String url = "http://localhost:8080/users";
    public static final String GENERATE = "generate";
    public static final String PAGESIZE = "size";
    public static final String PAGENUMBER = "page";

    public static final int pagesize = 10;
    public static final int generate = 87;
    public static final int pageStart = 0;
    private static final int PARALLEL_TASKS = 2;

    @Test
    void query_parallel() {
        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .build();

        // First request to get total pages from Link header
        ResponseEntity<UserResponse> firstPageResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam(GENERATE, generate)
                        .queryParam(PAGESIZE, pagesize)
                        .queryParam(PAGENUMBER, pageStart)
                        .build())
                .retrieve()
                .toEntity(UserResponse.class)
                .block();

        int total = Integer.parseInt(firstPageResponse.getHeaders().get("x-count").get(0));

        int totalPages = (int) Math.floor(total / pagesize);
        log.info("{} items on {} pages", total, totalPages);

        List<User> results = Flux.range(pageStart+1, totalPages)
                .flatMap(page ->
                        {
                            log.info("Requesting page: {}", page);
                            return webClient.get()
                                    .uri(uriBuilder -> uriBuilder
                                            .queryParam(GENERATE, generate)
                                            .queryParam(PAGESIZE, pagesize)
                                            .queryParam(PAGENUMBER, page)
                                            .build())
                                    .retrieve()
                                    .bodyToMono(UserResponse.class)
                                    //.delayElement(Duration.ofMillis(500))
                                    ;
                        }, PARALLEL_TASKS // Concurrent requests limited to 2
                )
                .parallel(PARALLEL_TASKS) // no effect ?
                .runOn(Schedulers.boundedElastic())
                .sequential()
                .map(UserResponse::getContent)
                .flatMap(Flux::fromIterable)
                .collectList()
                .block();

        List<User> allResults = Stream.concat(
                Objects.requireNonNull(firstPageResponse.getBody()).getContent().stream(),
                results.stream()
            ).toList();

        log.info("Total results collected: {}", allResults.size());


    }
    @Test
    void query_simple() {
        WebClient webClient = WebClient.create();

        Flux<String> issuesFlux = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(String.class);

        StepVerifier.create(issuesFlux)
                .expectNextCount(30) // GitHub API typically returns 30 items per page
                .verifyComplete();
    }



}
