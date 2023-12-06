package org.apache.camel.example.springboot.numbers.mainrouter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.camel.example.springboot.numbers.common.model.MessageTypes.RESET_STATS_COMMAND;

@Service
public class NumberStatisticsService {

    private static final Logger LOG = LoggerFactory.getLogger(NumberStatisticsService.class);

    private final Map<String, Long> countsMap;

    private final AtomicLong currentMs = new AtomicLong(0L);

    private long start = 0L;

    private final ProducerTemplate producerTemplate;

    private final ObjectMapper objectMapper;

    public NumberStatisticsService(final ProducerTemplate producerTemplate,
                                   final ObjectMapper objectMapper) {
        this.producerTemplate = producerTemplate;
        this.countsMap = new ConcurrentSkipListMap<>();
        this.objectMapper = objectMapper;
    }

    public void resetStats() {
        LOG.info("Resetting statistics");
        start = System.currentTimeMillis();
        countsMap.clear();
        currentMs.set(0L);
        producerTemplate.sendBodyAndHeader("direct:command", RESET_STATS_COMMAND, "command", RESET_STATS_COMMAND);
    }

    public void updateStats(final String body) throws Exception {
        CommandMessage message = objectMapper.readValue(body, CommandMessage.class);
        long now = System.currentTimeMillis();
        long newTime = now - start;
        long elapsed = currentMs.updateAndGet(cv -> Math.max(cv, newTime));
        countsMap.put("elapsed seconds", elapsed / 1000);
        message.params().forEach((key, val) -> countsMap.merge(key, Long.parseLong(val), Math::max));
    }

    public Map<String, Long> getCounts() {
        LOG.info("Getting number statistics");
        return Map.copyOf(countsMap);
    }
}
