package com.example.fluxrequest.controller;

import com.example.fluxrequest.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class UserController {


    static List<User> staticAllUsers;

    @PostConstruct
    void init(){
        staticAllUsers = generateList(300);
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<Page<User>>> getUsers(
            @RequestParam(defaultValue = "100") int generate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws InterruptedException {

        List<User> allUsers = staticAllUsers.subList(0, generate);

        int start = page * size;
        int end = Math.min(start + size, allUsers.size());
        List<User> pageContent = allUsers.subList(start, end);

        Page<User> userPage = new PageImpl<>(
                pageContent,
                PageRequest.of(page, size),
                allUsers.size());

        Thread.sleep(2000);
        return Mono.just(ResponseEntity.ok()
                .header("x-count", String.valueOf(allUsers.size()))
                .body(userPage));

//        return Mono.just(new PageImpl<>(
//                pageContent,
//                PageRequest.of(page, size),
//                allUsers.size()));
    }

    private static List<User> generateList(int count) {
        final AtomicInteger idGenerator = new AtomicInteger(1);

        List<User> allUsers = IntStream.range(0, count)
                .mapToObj(i -> User.builder()
                        .id(idGenerator.getAndIncrement())
                        .username("user" + UUID.randomUUID().toString().substring(0, 8))
                        .firstName("FirstName" + UUID.randomUUID().toString().substring(0, 5))
                        .build())
                .collect(Collectors.toList());
        return allUsers;
    }
}
