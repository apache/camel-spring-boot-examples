package org.apache.camel.example.springboot.numbers.mainrouter.service;

import jakarta.validation.constraints.NotNull;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.stream.IntStream;

import static org.apache.camel.example.springboot.numbers.common.model.MessageTypes.PROCESS_NUMBER_COMMAND;

@Service
public class NumberGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(NumberGeneratorService.class);

    private final ProducerTemplate producerTemplate;

    private final NumberStatisticsService numberStatisticsService;

    public NumberGeneratorService(@NotNull final ProducerTemplate producerTemplate,
                                  final NumberStatisticsService numberStatisticsService) {
        this.producerTemplate = producerTemplate;
        this.numberStatisticsService = numberStatisticsService;
        producerTemplate.start();
    }

    /**
     * With this approach, it completed 1000000 messages in 88 seconds.
     *
     * @param limit number of messages to send
     */
    void parallelStreamApproach(int limit) {
            IntStream.rangeClosed(1, limit)
                    .parallel()
                    .mapToObj(Integer::toString)
                    .forEach(n -> producerTemplate.sendBodyAndHeaders("direct:command", n, Map.of(
                            "command", PROCESS_NUMBER_COMMAND,
                            "number", n)));
    }

    /**
     * With this approach, it completed 1000000 messages in 327 seconds.
     *
     * @param limit number of messages to send
     */
    void fluxPublishOnApproach(int limit) {
        Flux.range(1, limit)
                .publishOn(Schedulers.boundedElastic())
                .map(Object::toString)
                .doOnNext(n -> producerTemplate.sendBodyAndHeaders("direct:command", n, Map.of(
                        "command", PROCESS_NUMBER_COMMAND,
                        "number", n)))
                .subscribe();
    }

    /**
     * With this approach, it completed 1000000 messages in 81 seconds.
     *
     * @param limit number of messages to send
     */
    void fluxParallelElasticApproach(int limit) {
        Flux.range(1, limit)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .map(Object::toString)
                .doOnNext(n -> producerTemplate.sendBodyAndHeaders("direct:command", n, Map.of(
                        "command", PROCESS_NUMBER_COMMAND,
                        "number", n)))
                .subscribe();
    }

    /**
     * With this approach, it completed 1000000 messages in 45 seconds.
     *
     * @param limit number of messages to send
     */
    void fluxFlatMapApproach(int limit) {
        Flux.range(1, limit)
                .flatMap(n -> Mono.just(n)
                        .map(Object::toString)
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(strN -> producerTemplate.sendBodyAndHeaders("direct:command", strN,
                                Map.of("command", PROCESS_NUMBER_COMMAND, "number", strN))))
                .subscribe();
    }

    /**
     * When a command has been received to generate numbers, this will continuously generate
     * numbers and send them in a command to have recipients process the numbers.  It will
     * only stop when a limit (if any) is reached, or if a subsequent command instructs
     * number message generation to stop
     *
     * @param limit the count of numbers to produce (zero means Integer.MAX_VALUE)
     */
    public void generateNumbers(int limit, ParallelApproachType parallelApproach) {
        try {
            LOG.info("Generating numbers from 1 to {}", limit);
            numberStatisticsService.resetStats();
            long begin = System.currentTimeMillis();
            switch (parallelApproach) {
                case FLUX_FLAT_MAP: {
                    fluxFlatMapApproach(limit);
                    break;
                }
                case FLUX_PARALLEL: {
                    fluxParallelElasticApproach(limit);
                    break;
                }
                case PARALLEL_STREAM: {
                    parallelStreamApproach(limit);
                    break;
                }
                case FLUX_PUBLISH_ON_BOUNDED_ELASTIC: {
                    fluxPublishOnApproach(limit);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Invalid parallelism approach specified: " + parallelApproach);
            }
            LOG.info("Generated numbers in {}s", (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            LOG.warn("########## Exception when trying to send number messages", e);
        }
    }

    public enum ParallelApproachType {
        FLUX_FLAT_MAP,
        FLUX_PARALLEL,
        PARALLEL_STREAM,
        FLUX_PUBLISH_ON_BOUNDED_ELASTIC
    }
}
