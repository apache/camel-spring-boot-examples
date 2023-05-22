/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
