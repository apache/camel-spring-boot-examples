package org.apache.camel.example.springboot.numbers.mainrouter.service;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubscribersService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribersService.class);

    private final ProducerTemplate producerTemplate;

    public SubscribersService(final ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
        producerTemplate.start();
    }

    public String listSubscriptions(final String channel) {
        LOG.info("Getting subscriptions list for channel '{}'", channel);
        return producerTemplate.requestBodyAndHeader(
                "direct:list", "", "subscribeChannel", channel, String.class);
    }
}
