package org.apache.camel.example.springboot.numbers.mainrouter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.COMMAND_RESET_STATS;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.ENDPOINT_DIRECT_COMMAND;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_COMMAND;

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

    public String resetStats() {
        start = System.currentTimeMillis();
        countsMap.clear();
        currentMs.set(0L);
        producerTemplate.sendBodyAndHeader(ENDPOINT_DIRECT_COMMAND, COMMAND_RESET_STATS, HEADER_COMMAND, COMMAND_RESET_STATS);
        return "OK - Statistics have been reset.";
    }

    public void updateStats(final String body) {
        try {
            CommandMessage message = objectMapper.readValue(body, CommandMessage.class);
            long now = System.currentTimeMillis();
            long newTime = now - start;
            long elapsed = currentMs.updateAndGet(cv -> Math.max(cv, newTime));
            countsMap.put("elapsed seconds", elapsed / 1000);
            message.params().forEach((key, val) -> countsMap.merge(key, Long.parseLong(val), Math::max));
        } catch (JsonProcessingException e) {
            LOG.warn("Error when trying to update number statistics", e);
        }
    }

    public Map<String, Long> getCounts() {
        LOG.info("Getting number statistics");
        return Map.copyOf(countsMap);
    }
}
