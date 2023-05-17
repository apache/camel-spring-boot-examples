package com.example.demo;

import org.apache.camel.Exchange;

public class MergeAggregationStrategy implements org.apache.camel.AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null)
        {
            return newExchange;
        }
        String oldPayload = oldExchange.getIn().getBody(String.class);
        String newPayload = newExchange.getIn().getBody(String.class);
        String result = oldPayload + ", " + newPayload;
        oldExchange.getIn().setBody(result);
        return oldExchange;
    }

    @Override
    public void onCompletion(Exchange exchange) {
        String payload = exchange.getIn().getBody(String.class);
        String result = "["+payload+"]";
        exchange.getIn().setBody(result);
    }
}
