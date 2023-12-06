package org.apache.camel.example.springboot.numbers.mainrouter.web;

import org.apache.camel.example.springboot.numbers.mainrouter.service.NumberGeneratorService;
import org.apache.camel.example.springboot.numbers.mainrouter.service.NumberGeneratorService.ParallelApproachType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class NumberGeneratorController {

    final NumberGeneratorService numberGeneratorService;

    public NumberGeneratorController(NumberGeneratorService numberGeneratorService) {
        this.numberGeneratorService = numberGeneratorService;
    }

    @PutMapping(path = "/generate")
    public Mono<Void> generate(
            @RequestParam("limit") String limit,
            @RequestParam("parallelApproach") ParallelApproachType parallelApproach) {
        numberGeneratorService.generateNumbers(Integer.parseInt(limit), parallelApproach);
        return Mono.empty();
    }
}
