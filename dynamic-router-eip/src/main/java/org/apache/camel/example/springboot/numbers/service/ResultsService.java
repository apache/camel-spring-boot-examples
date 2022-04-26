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
package org.apache.camel.example.springboot.numbers.service;

import org.apache.camel.example.springboot.numbers.config.ExampleConfig;
import org.apache.camel.util.StopWatch;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Holds processing results so that they can be displayed, etc.
 */
@Service
public class ResultsService {

    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

    /**
     * A map of the "bin" or "category" to the list of values in that bin.
     */
    private final ConcurrentHashMap<String, Collection<Integer>> results;

    /**
     * The {@link CountDownLatch} to wait for all messages to be received.
     */
    private final CountDownLatch countDownLatch;

    /**
     * The number of sent messages.
     */
    private final int numberSent;

    /**
     * Create the service and initialize the results map.
     */
    public ResultsService(final CountDownLatch countDownLatch,
            final ExampleConfig config) {
        this.results = new ConcurrentHashMap<>();
        this.countDownLatch = countDownLatch;
        this.numberSent = config.getSendMessageCount();
    }

    /**
     * Add a result value for the given key/bin.
     *
     * @param key   the bin or category
     * @param value the value to add to the bin
     */
    public void addResult(final String key, int value) {
        results.computeIfAbsent(key, v -> Collections.synchronizedCollection(new ArrayList<>())).add(value);
        countDownLatch.countDown();
    }

    /**
     * Get the results map.
     *
     * @return the results map
     */
    public Map<String, Collection<Integer>> getResults() {
        return results;
    }

    /**
     * Get a message that contains the statistics of the messaging.
     *
     * @param watch a {@link StopWatch} that was started at the beginning of messaging
     * @return a message that contains the statistics of the messaging
     */
    public String getStatistics(final StopWatch watch) {
        final long taken = watch.taken();
        final int totalCount = getResults().values()
                .stream()
                .mapToInt(Collection::size)
                .sum();
        final int numberLength = numberFormat.format(totalCount)
                .length();
        StringBuilder statistics = new StringBuilder("Finished in ")
                .append(taken).append("ms")
                .append("\nDynamic Router Spring Boot Numbers Example Results:\n");
        getResults().entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().size() - o1.getValue().size())
                .map(e -> String.format("%7s: %" + numberLength + "s [%3d%% routed, %3d%% sent]",
                        e.getKey(), numberFormat.format(e.getValue().size()),
                        e.getValue().size() * 100 / totalCount,
                        e.getValue().size() * 100 / numberSent))
                .forEach(s -> statistics.append("\n\t").append(s));
        statistics.append("\n\n\t")
                .append(String.format("%7s: %" + numberLength + "s [%3d%% routed, %3d%% sent]",
                        "total", numberFormat.format(totalCount),
                        totalCount * 100 / totalCount,
                        totalCount * 100 / numberSent));
        return statistics.toString();
    }
}
