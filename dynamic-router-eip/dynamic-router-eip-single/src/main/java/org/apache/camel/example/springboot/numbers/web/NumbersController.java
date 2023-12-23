package org.apache.camel.example.springboot.numbers.web;

import org.apache.camel.example.springboot.numbers.service.NumbersService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class NumbersController {

    private final NumbersService numbersService;

    public NumbersController(NumbersService numbersService) {
        this.numbersService = numbersService;
    }

    @PutMapping(path = "/generate")
    public Mono<String> generate(@RequestParam("limit") int limit) throws InterruptedException {
        return Mono.just(numbersService.start(limit));
    }
}
